package org.example.backend.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.backend.common.annotation.OperationLog;
import org.example.backend.common.util.SecurityUtil;
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
     * 环绕通知
     */
    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        Log log = new Log();
        log.setCreatedAt(LocalDateTime.now());

        // 填充基础和静态信息
        fillBaseInfo(log, operationLog);

        // 2. 解析基础的 SpEL 表达式 (处理前端传来的基础参数，如 targetId)
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();

        Object idVal = parseSpel(signature.getMethod(), args, operationLog.targetId());
        Object nameVal = parseSpel(signature.getMethod(), args, operationLog.targetName());

        Object result;
        try {
            // 3. 放行，执行真实的业务接口 (此时业务代码里可能会往 LogContextHolder 里塞值)
            result = joinPoint.proceed();
            log.setStatus(1);
            log.setErrorMsg("");
        } catch (Throwable e) {
            log.setStatus(0);
            log.setErrorMsg(e.getMessage() != null ? e.getMessage() : e.toString());
            throw e;
        } finally {
            // 4. 执行完毕后，从上下文中读取数据 (优先级最高)
            Map<String, Object> contextMap = LogContextHolder.getAll();
            if (contextMap.containsKey("targetId")) idVal = contextMap.get("targetId");
            if (contextMap.containsKey("targetName")) nameVal = contextMap.get("targetName");

            // 5. 组装最终的数据 (处理单体和批量)
            buildFinalTargetData(log, operationLog, idVal, nameVal, contextMap);

            // 6. 记录耗时，准备入库
            log.setCostTime(System.currentTimeMillis() - startTime);
            logService.saveLogAsync(log);

            // 7. 【切记】清理 ThreadLocal，防止内存泄漏或数据串线
            LogContextHolder.clear();
        }

        return result;
    }

    private void buildFinalTargetData(Log log, OperationLog operationLog, Object idVal, Object nameVal, Map<String, Object> contextMap) throws Exception {
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

    private void fillBaseInfo(Log log, OperationLog operationLog) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            log.setRequestUri(request.getRequestURI());
            log.setClientIp(getClientIp(request));
        }
        log.setUserId(SecurityUtil.getUserId());
        log.setUsername(SecurityUtil.getUsername());
        log.setRealName(SecurityUtil.getRealName());
        log.setModule(operationLog.module());
        log.setAction(operationLog.action());
        log.setTargetType(operationLog.targetType());
    }

    /**
     * 解析 SpEL 表达式
     */
    private Object parseSpel(Method method, Object[] args, String spel) {
        if (!StringUtils.hasText(spel)) return null;
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            String[] params = discoverer.getParameterNames(method);
            if (params != null) {
                for (int i = 0; i < params.length; i++) context.setVariable(params[i], args[i]);
            }
            Expression expression = parser.parseExpression(spel);
            return expression.getValue(context);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端真实IP
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
