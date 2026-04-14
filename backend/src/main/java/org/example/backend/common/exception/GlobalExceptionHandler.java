package org.example.backend.common.exception;

import io.jsonwebtoken.JwtException;
import io.minio.errors.MinioException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.example.backend.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理自定义业务异常
     * @param e 自定义业务异常
     * @param request HTTP请求对象
     * @return 统一失败响应结果
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        logger.warn("业务异常 - URI: [{}], 错误码: {}, 错误信息: {}", request.getRequestURI(), e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数绑定/校验失败异常
     * @param e 参数绑定异常
     * @param request HTTP请求对象
     * @return 统一失败响应结果
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        logger.warn("参数绑定失败 - URI: [{}], 错误: {}", request.getRequestURI(), message);
        return Result.fail(400, message);
    }

    /**
     * 处理单个参数约束校验异常
     * @param e 约束校验异常
     * @param request HTTP请求对象
     * @return 统一失败响应结果
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e,
                                                           HttpServletRequest request) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        logger.warn("约束校验失败 - URI: [{}], 错误: {}", request.getRequestURI(), message);
        return Result.fail(400, message);
    }

    /**
     * 处理缺少必要请求参数异常
     * @param e 缺少请求参数异常
     * @param request HTTP请求对象
     * @return 统一失败响应结果
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e,
                                                                      HttpServletRequest request) {
        logger.warn("缺少请求参数 - URI: [{}], 参数名: {}", request.getRequestURI(), e.getParameterName());
        return Result.fail(400, "缺少必要参数: " + e.getParameterName());
    }

    /**
     * 处理参数类型不匹配异常
     * @param e 参数类型不匹配异常
     * @param request HTTP请求对象
     * @return 统一失败响应结果
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e,
                                                                  HttpServletRequest request) {
        logger.warn("参数类型不匹配 - URI: [{}], 参数名: {}, 错误: {}", request.getRequestURI(), e.getName(), e.getMessage());
        return Result.fail(400, "参数 [" + e.getName() + "] 类型不匹配");
    }

    /**
     * 处理请求体解析失败异常
     * @param e 请求体不可读异常
     * @param request HTTP请求对象
     * @return 统一失败响应结果
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e,
                                                              HttpServletRequest request) {
        logger.warn("请求参数不可读(JSON格式错误) - URI: [{}], 错误: {}", request.getRequestURI(), e.getMessage());
        return Result.fail(400, "请求参数格式错误或JSON解析失败");
    }

    /**
     * 处理请求方法不支持异常
     * @param e 请求方法不支持异常
     * @param request HTTP请求对象
     * @return 统一失败响应结果
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e,
                                                                     HttpServletRequest request) {
        logger.warn("请求方法不支持 - URI: [{}], 方法: {}", request.getRequestURI(), e.getMethod());
        return Result.fail(405, "请求方法不支持: " + e.getMethod());
    }

    /**
     * 处理不支持的媒体类型异常
     * @param e 媒体类型不支持异常
     * @param request HTTP请求对象
     * @return 统一失败响应结果
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Result<Void> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        logger.warn("不支持的媒体类型 - URI: [{}], 类型: {}", request.getRequestURI(), e.getContentType());
        return Result.fail(415, "不支持的 Content-Type: " + e.getContentType());
    }

    /**
     * 处理响应写入失败异常
     * @param ex 响应写入异常
     * @param response HTTP响应对象
     */
    @ExceptionHandler(HttpMessageNotWritableException.class)
    public void handleDownloadException(HttpMessageNotWritableException ex, HttpServletResponse response) {
        logger.error("响应写入失败 (通常发生在文件下载) - 错误: {}", ex.getMessage());
        try {
            response.setStatus(500);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Download Error: 服务端响应写入失败");
        } catch (IOException e) {
            logger.error("处理下载异常时发生IO错误", e);
        }
    }

    /**
     * 处理权限不足访问异常
     * @param e 权限拒绝异常
     * @param request HTTP请求对象
     * @return 统一失败响应结果
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        logger.warn("权限不足 - URI:[{}], 错误: {}", request.getRequestURI(), e.getMessage());
        return Result.fail(403, "权限不足，禁止访问");
    }

    /**
     * 处理用户认证失败/未登录异常
     * @param e 认证异常
     * @param request HTTP请求对象
     * @return 统一失败响应结果
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result<Void> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        logger.warn("认证失败 - URI: [{}], 错误: {}", request.getRequestURI(), e.getMessage());
        return Result.fail(401, "未登录或认证失败");
    }

    /**
     * 处理JWT令牌无效/过期异常
     * @param e JWT解析异常
     * @param request HTTP请求对象
     * @return 统一失败响应结果
     */
    @ExceptionHandler(JwtException.class)
    public Result<Void> handleJwtException(JwtException e, HttpServletRequest request) {
        logger.warn("JWT令牌异常 - URI: [{}], 错误: {}", request.getRequestURI(), e.getMessage());
        return Result.fail(401, "Token 无效或已过期");
    }

    /**
     * 处理数据库唯一键冲突异常
     * @param e 唯一键重复异常
     * @param request HTTP请求对象
     * @return 统一失败响应结果
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKeyException(DuplicateKeyException e, HttpServletRequest request) {
        logger.warn("数据库唯一约束冲突 - URI: [{}], 错误: {}", request.getRequestURI(), e.getMessage());
        // 可以根据 e.getMessage() 提取具体重复字段，这里做通用返回
        return Result.fail(409, "数据违反唯一性约束，该记录已存在");
    }

    /**
     * 处理数据库数据完整性约束异常
     * @param e 数据完整性异常
     * @param request HTTP请求对象
     * @return 统一失败响应结果
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        logger.warn("数据完整性约束违反 - URI: [{}], 错误: {}", request.getRequestURI(), e.getMessage());
        return Result.fail(400, "提交的数据不合法，违反数据库约束");
    }

    /**
     * 处理文件上传大小超出限制异常
     * @param e 文件上传超限异常
     * @param request HTTP请求对象
     * @return 统一失败响应结果
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        logger.warn("文件上传超限 - URI: [{}]", request.getRequestURI());
        return Result.fail(413, "上传文件大小超过系统允许的限制");
    }

    /**
     * 处理MinIO文件服务操作异常
     * @param e MinIO服务异常
     * @param request HTTP请求对象
     * @return 统一失败响应结果
     */
    @ExceptionHandler(MinioException.class)
    public Result<Void> handleMinioException(MinioException e, HttpServletRequest request) {
        logger.error("MinIO 文件存储异常 - URI: [{}]", request.getRequestURI(), e);
        return Result.fail(500, "文件存储服务异常");
    }

    /**
     * 处理系统空指针异常
     * @param e 空指针异常
     * @param request HTTP请求对象
     * @return 统一失败响应结果
     */
    @ExceptionHandler(NullPointerException.class)
    public Result<Void> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        logger.error("空指针异常 - URI: [{}]", request.getRequestURI(), e);
        return Result.fail(500, "系统内部错误");
    }

    /**
     * 处理非法参数异常
     * @param e 非法参数异常
     * @param request HTTP请求对象
     * @return 统一失败响应结果
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        logger.warn("非法参数异常 - URI:[{}], 错误: {}", request.getRequestURI(), e.getMessage());
        return Result.fail(400, e.getMessage());
    }

    /**
     * 兜底处理所有未匹配的系统未知异常
     * @param e 通用异常
     * @param request HTTP请求对象
     * @return 统一失败响应结果
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        logger.error("系统异常 - URI: [{}]", request.getRequestURI(), e);
        return Result.fail(500, "系统繁忙，请稍后重试");
    }
}
