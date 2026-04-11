package org.example.backend.common.exception;

import io.jsonwebtoken.JwtException;
import io.minio.errors.MinioException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.result.Result;
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

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /* ================= 业务自定义异常 ================= */

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 - URI: [{}], 错误码: {}, 错误信息: {}", request.getRequestURI(), e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /* ================= 参数校验与绑定异常 (Validation) ================= */

    /**
     * 处理参数校验异常 (@Valid @Validated @ModelAttribute)
     * 涵盖了 MethodArgumentNotValidException 和 BindException
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败 - URI: [{}], 错误: {}", request.getRequestURI(), message);
        return Result.fail(400, message);
    }

    /**
     * 处理单个参数校验异常（@RequestParam @PathVariable）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e,
                                                           HttpServletRequest request) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("约束校验失败 - URI: [{}], 错误: {}", request.getRequestURI(), message);
        return Result.fail(400, message);
    }

    /* ================= HTTP 请求与解析异常 (Spring Web) ================= */

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e,
                                                                      HttpServletRequest request) {
        log.warn("缺少请求参数 - URI: [{}], 参数名: {}", request.getRequestURI(), e.getParameterName());
        return Result.fail(400, "缺少必要参数: " + e.getParameterName());
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e,
                                                                  HttpServletRequest request) {
        log.warn("参数类型不匹配 - URI: [{}], 参数名: {}, 错误: {}", request.getRequestURI(), e.getName(), e.getMessage());
        return Result.fail(400, "参数 [" + e.getName() + "] 类型不匹配");
    }

    /**
     * 处理 JSON 格式错误或不可读
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e,
                                                              HttpServletRequest request) {
        log.warn("请求参数不可读(JSON格式错误) - URI: [{}], 错误: {}", request.getRequestURI(), e.getMessage());
        return Result.fail(400, "请求参数格式错误或JSON解析失败");
    }

    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e,
                                                                     HttpServletRequest request) {
        log.warn("请求方法不支持 - URI: [{}], 方法: {}", request.getRequestURI(), e.getMethod());
        return Result.fail(405, "请求方法不支持: " + e.getMethod());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Result<Void> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        log.warn("不支持的媒体类型 - URI: [{}], 类型: {}", request.getRequestURI(), e.getContentType());
        return Result.fail(415, "不支持的 Content-Type: " + e.getContentType());
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public void handleDownloadException(HttpMessageNotWritableException ex, HttpServletResponse response) {
        log.error("响应写入失败 (通常发生在文件下载) - 错误: {}", ex.getMessage());
        try {
            response.setStatus(500);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Download Error: 服务端响应写入失败");
        } catch (IOException e) {
            log.error("处理下载异常时发生IO错误", e);
        }
    }

    /* ================= 安全与鉴权异常 (Security + JJWT) ================= */

    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("权限不足 - URI:[{}], 错误: {}", request.getRequestURI(), e.getMessage());
        return Result.fail(403, "权限不足，禁止访问");
    }

    /**
     * 处理未认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result<Void> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.warn("认证失败 - URI: [{}], 错误: {}", request.getRequestURI(), e.getMessage());
        return Result.fail(401, "未登录或认证失败");
    }

    /**
     * 处理 JWT 解析异常
     */
    @ExceptionHandler(JwtException.class)
    public Result<Void> handleJwtException(JwtException e, HttpServletRequest request) {
        log.warn("JWT令牌异常 - URI: [{}], 错误: {}", request.getRequestURI(), e.getMessage());
        return Result.fail(401, "Token 无效或已过期");
    }

    /* ================= 数据库与持久层异常 (MySQL + MyBatis Plus) ================= */

    /**
     * 处理唯一键冲突异常 (如账号重复注册等)
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKeyException(DuplicateKeyException e, HttpServletRequest request) {
        log.warn("数据库唯一约束冲突 - URI: [{}], 错误: {}", request.getRequestURI(), e.getMessage());
        // 可以根据 e.getMessage() 提取具体重复字段，这里做通用返回
        return Result.fail(409, "数据违反唯一性约束，该记录已存在");
    }

    /**
     * 处理数据完整性异常 (如字段过长、非空限制等)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        log.warn("数据完整性约束违反 - URI: [{}], 错误: {}", request.getRequestURI(), e.getMessage());
        return Result.fail(400, "提交的数据不合法，违反数据库约束");
    }

    /* ================= 文件存储与第三方组件异常 (MinIO + Spring Web Multipart) ================= */

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.warn("文件上传超限 - URI: [{}]", request.getRequestURI());
        return Result.fail(413, "上传文件大小超过系统允许的限制");
    }

    /**
     * 处理 MinIO 操作异常
     */
    @ExceptionHandler(MinioException.class)
    public Result<Void> handleMinioException(MinioException e, HttpServletRequest request) {
        log.error("MinIO 文件存储异常 - URI: [{}]", request.getRequestURI(), e);
        return Result.fail(500, "文件存储服务异常");
    }

    /* ================= 系统级及未知异常 ================= */

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public Result<Void> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常 - URI: [{}]", request.getRequestURI(), e);
        return Result.fail(500, "系统内部错误");
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数异常 - URI:[{}], 错误: {}", request.getRequestURI(), e.getMessage());
        return Result.fail(400, e.getMessage());
    }

    /**
     * 处理其他所有未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 - URI: [{}]", request.getRequestURI(), e);
        return Result.fail(500, "系统繁忙，请稍后重试");
    }
}
