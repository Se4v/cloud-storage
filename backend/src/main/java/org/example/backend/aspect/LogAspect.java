package org.example.backend.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.backend.common.annotation.OperationLog;
import org.example.backend.common.util.SecurityUtils;
import org.example.backend.model.entity.Log;
import org.example.backend.service.LogService;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

@Aspect
@Component
public class LogAspect {
    private final LogService logService;
    private final ObjectMapper objectMapper;

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    public LogAspect(LogService logService, ObjectMapper objectMapper) {
        this.logService = logService;
        this.objectMapper = objectMapper;
    }

    /**
     * 操作日志环绕通知
     * @param joinPoint 切点对象
     * @param operationLog 操作日志注解
     * @return 目标方法执行结果
     * @throws Throwable 方法执行抛出的异常
     */
    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        // 记录接口执行开始时间
        long startTime = System.currentTimeMillis();
        Log log = new Log();
        log.setCreatedAt(LocalDateTime.now());

        // 填充日志基础信息和静态配置信息
        fillBaseInfo(log, operationLog);

        // 获取目标方法签名和入参，解析SpEL表达式
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();

        Object idVal = parseSpel(signature.getMethod(), args, operationLog.targetId());
        Object nameVal = parseSpel(signature.getMethod(), args, operationLog.targetName());

        Object result;
        try {
            // 执行目标业务方法
            result = joinPoint.proceed();
            // 执行成功，设置日志状态
            log.setStatus(1);
            log.setErrorMsg("");
        } catch (Throwable e) {
            // 执行失败，记录异常信息
            log.setStatus(0);
            log.setErrorMsg(e.getMessage() != null ? e.getMessage() : e.toString());
            throw e;
        } finally {
            // 从线程上下文获取业务层传递的扩展数据
            Map<String, Object> contextMap = LogContextHolder.getAll();
            if (contextMap.containsKey("targetId")) idVal = contextMap.get("targetId");
            if (contextMap.containsKey("targetName")) nameVal = contextMap.get("targetName");

            // 构建最终日志数据（处理批量操作、组装详情）
            buildFinalTargetData(log, operationLog, idVal, nameVal, contextMap);

            // 计算接口耗时并异步保存日志
            log.setCostTime(System.currentTimeMillis() - startTime);
            logService.saveLogAsync(log);

            // 清空线程上下文，防止内存泄漏
            LogContextHolder.clear();
        }

        return result;
    }

    /**
     * 构建日志最终目标数据
     * @param log 日志实体对象
     * @param operationLog 操作日志注解
     * @param idVal 解析后的目标ID
     * @param nameVal 解析后的目标名称
     * @param contextMap 线程上下文扩展数据
     * @throws Exception JSON序列化异常
     */
    private void buildFinalTargetData(Log log, OperationLog operationLog, Object idVal,
                                      Object nameVal, Map<String, Object> contextMap) throws Exception {
        Map<String, Object> finalDetailMap = new HashMap<>();
        // 如果 Context 中有业务层额外传的属性，也放进 detail JSON 里
        contextMap.forEach((k, v) -> {
            if (!"targetId".equals(k) && !"targetName".equals(k)) {
                finalDetailMap.put(k, v);
            }
        });

        // 补充注解上的静态 detail
        if (StringUtils.hasText(operationLog.detail()) && !finalDetailMap.containsKey("detail")) {
            finalDetailMap.put("remark", operationLog.detail());
        }

        // 判断是否为批量操作
        if (idVal instanceof Iterable || (idVal != null && idVal.getClass().isArray())) {
            log.setTargetId(0L);
            int size = (idVal instanceof Collection) ? ((Collection<?>) idVal).size() : ObjectUtils.toObjectArray(idVal).length;
            log.setTargetName("批量操作了 " + size + " 个对象");
            finalDetailMap.put("batch_ids", idVal);
        } else {
            log.setTargetId(idVal != null ? Long.parseLong(idVal.toString()) : 0L);
            log.setTargetName(nameVal != null ? nameVal.toString() : "");
        }

        // 如果 detail map 不为空，转成 JSON 存入
        if (!finalDetailMap.isEmpty()) {
            log.setDetail(objectMapper.writeValueAsString(finalDetailMap));
        }
    }

    /**
     * 填充日志基础信息
     * @param log 日志实体对象
     * @param operationLog 操作日志注解
     */
    private void fillBaseInfo(Log log, OperationLog operationLog) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            log.setRequestUri(request.getRequestURI());
            log.setClientIp(getClientIp(request));
        }
        log.setUserId(SecurityUtils.getUserId());
        log.setUsername(SecurityUtils.getUsername());
        log.setRealName(SecurityUtils.getRealName());
        log.setModule(operationLog.module());
        log.setAction(operationLog.action());
        log.setTargetType(operationLog.targetType());
    }

    /**
     * 解析 SpEL 表达式
     * @param method 目标方法对象
     * @param args 目标方法的入参
     * @param spel 待解析的 SpEL 表达式字符串
     * @return 表达式解析结果，解析失败或表达式为空时返回 null
     */
    private Object parseSpel(Method method, Object[] args, String spel) {
        if (!StringUtils.hasText(spel)) return null;
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            // 获取方法参数名
            String[] params = discoverer.getParameterNames(method);
            if (params != null) {
                // 将方法参数名和参数值存入SpEL上下文
                for (int i = 0; i < params.length; i++) context.setVariable(params[i], args[i]);
            }
            // 解析并执行SpEL表达式
            Expression expression = parser.parseExpression(spel);
            return expression.getValue(context);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端真实IP地址
     * @param request HTTP请求对象
     * @return 客户端真实IP字符串
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
        return ip;
    }
}
