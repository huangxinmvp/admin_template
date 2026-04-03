package com.hiking.treasure.common.api.vo;

import com.hiking.treasure.common.web.RequestCorrelation;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/** 通用返回体（等价 Jeecg Result，只保留常用工厂方法） */
public class Result<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 是否成功 */           private boolean success = true;
    /** 返回消息 */           private String message;
    /** 状态码 */             private int code = 200;
    /** 错误标识 */           private String errorKey;
    /** 数据 */               private T result;
    /** 时间戳 */             private long timestamp = Instant.now().toEpochMilli();
    /** 请求追踪ID */         private String requestId = RequestCorrelation.currentRequestId();

    public Result() {}

    public static <T> Result<T> ok() {
        Result<T> r = new Result<>(); r.setSuccess(true); r.setCode(200); r.setMessage("success"); return r;
    }
    public static <T> Result<T> ok(String msg) { Result<T> r = ok(); r.setMessage(msg); return r; }
    public static <T> Result<T> ok(T data) { Result<T> r = ok(); r.setResult(data); return r; }
    public static <T> Result<T> error(String msg) { Result<T> r = new Result<>(); r.setSuccess(false); r.setCode(500); r.setMessage(msg); return r; }
    public static <T> Result<T> error(int code, String msg) { Result<T> r = new Result<>(); r.setSuccess(false); r.setCode(code); r.setMessage(msg); return r; }
    public static <T> Result<T> error(com.hiking.treasure.common.api.ErrorCode errorCode) {
        Result<T> r = error(errorCode.getCode(), errorCode.getDefaultMessage());
        r.setErrorKey(errorCode.getKey());
        return r;
    }
    public static <T> Result<T> error(com.hiking.treasure.common.api.ErrorCode errorCode, String msg) {
        Result<T> r = error(errorCode.getCode(), msg);
        r.setErrorKey(errorCode.getKey());
        return r;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getErrorKey() { return errorKey; }
    public void setErrorKey(String errorKey) { this.errorKey = errorKey; }
    public T getResult() { return result; }
    public void setResult(T result) { this.result = result; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
}
