package com.hiking.treasure.common.exception;

import com.hiking.treasure.common.api.ErrorCode;
import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.common.web.RequestCorrelation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 统一输出 Result，避免在业务代码里到处 try/catch。
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException ex) {
        // <500 走 warn，>=500 走 error，按需
        if (ex.getCode() >= 500) {
            log.error("BusinessException requestId={} message={}", RequestCorrelation.currentRequestId(), ex.getMessage());
        } else {
            log.warn("BusinessException requestId={} message={}", RequestCorrelation.currentRequestId(), ex.getMessage());
        }
        Result<Object> result = Result.error(ex.getCode(), ex.getMessage());
        if (ex.getErrorKey() != null) {
            result.setErrorKey(ex.getErrorKey());
        }
        if (ex.getData() != null) {
            result.setResult(ex.getData());
        }
        return result;
    }


    /* ========== 400 Bad Request：参数与反序列化 ========== */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .filter(s -> s != null && !s.isBlank())
                .findFirst().orElse("参数校验失败");
        log.warn("400 MethodArgumentNotValid requestId={} message={}", RequestCorrelation.currentRequestId(), msg);
        return Result.error(ErrorCode.BAD_REQUEST, msg);
    }

    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException ex) {
        String msg = ex.getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .filter(s -> s != null && !s.isBlank())
                .findFirst().orElse("参数绑定失败");
        log.warn("400 BindException requestId={} message={}", RequestCorrelation.currentRequestId(), msg);
        return Result.error(ErrorCode.BAD_REQUEST, msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolation(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining("; "));
        if (msg.isBlank()) msg = "参数约束违规";
        log.warn("400 ConstraintViolation requestId={} message={}", RequestCorrelation.currentRequestId(), msg);
        return Result.error(ErrorCode.BAD_REQUEST, msg);
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public Result<?> handleBadRequest(Exception ex) {
        String msg;
        if (ex instanceof MissingServletRequestParameterException e) {
            msg = "缺少参数: " + e.getParameterName();
        } else if (ex instanceof MethodArgumentTypeMismatchException e) {
            msg = "参数类型错误: " + e.getName();
        } else {
            msg = "请求体解析失败";
        }
        log.warn("400 BadRequest requestId={} message={}", RequestCorrelation.currentRequestId(), msg);
        return Result.error(ErrorCode.BAD_REQUEST, msg);
    }

    /* ========== 401/403：鉴权/权限 ========== */

    @ExceptionHandler(AccessDeniedException.class)
    public Result<?> handleAccessDenied(AccessDeniedException ex) {
        log.warn("403 Forbidden requestId={} message={}", RequestCorrelation.currentRequestId(), ex.getMessage());
        return Result.error(ErrorCode.FORBIDDEN, "无权限访问");
    }

    /* ========== 404/405/415：路由与方法/媒体类型 ========== */

    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<?> handleNoHandlerFound(NoHandlerFoundException ex) {
        String msg = "接口不存在: " + ex.getRequestURL();
        log.warn("404 NotFound requestId={} message={}", RequestCorrelation.currentRequestId(), msg);
        return Result.error(ErrorCode.NOT_FOUND, msg);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String msg = "不支持的请求方法: " + ex.getMethod();
        log.warn("405 MethodNotAllowed requestId={} message={}", RequestCorrelation.currentRequestId(), msg);
        return Result.error(ErrorCode.BAD_REQUEST, msg);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Result<?> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        log.warn("415 UnsupportedMediaType requestId={} message={}", RequestCorrelation.currentRequestId(), ex.getMessage());
        return Result.error(ErrorCode.BAD_REQUEST, "不支持的媒体类型");
    }

    /* ========== 409/413：数据库冲突、上传过大 ========== */

    @ExceptionHandler({DuplicateKeyException.class, SQLIntegrityConstraintViolationException.class})
    public Result<?> handleDuplicateKey(Exception ex) {
        log.warn("409 Conflict requestId={} message={}", RequestCorrelation.currentRequestId(), ex.getMessage());
        return Result.error(ErrorCode.CONFLICT, "数据已存在或唯一约束冲突");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<?> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("409 DataIntegrityViolation requestId={} message={}", RequestCorrelation.currentRequestId(), ex.getMostSpecificCause().getMessage());
        return Result.error(ErrorCode.CONFLICT, "数据完整性校验失败");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<?> handleMaxUpload(MaxUploadSizeExceededException ex) {
        log.warn("413 PayloadTooLarge requestId={} message={}", RequestCorrelation.currentRequestId(), ex.getMessage());
        return Result.error(ErrorCode.BAD_REQUEST, "上传文件过大");
    }

    /* ========== 500：兜底异常 ========== */

    @ExceptionHandler(Throwable.class)
    public Result<?> handleThrowable(Throwable ex) {
        // 统一 500，避免敏感信息外泄
        log.error("500 InternalServerError requestId={}", RequestCorrelation.currentRequestId(), ex);
        return Result.error(ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}
