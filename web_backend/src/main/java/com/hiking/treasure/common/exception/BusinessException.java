package com.hiking.treasure.common.exception;

import com.hiking.treasure.common.api.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;
    private final String errorKey;
    private final Object data;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.errorKey = null;
        this.data = null;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.errorKey = null;
        this.data = null;
    }

    public BusinessException(int code, String message, Object data) {
        super(message);
        this.code = code;
        this.errorKey = null;
        this.data = data;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.code = errorCode.getCode();
        this.errorKey = errorCode.getKey();
        this.data = null;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.errorKey = errorCode.getKey();
        this.data = null;
    }

    public BusinessException(ErrorCode errorCode, String message, Object data) {
        super(message);
        this.code = errorCode.getCode();
        this.errorKey = errorCode.getKey();
        this.data = data;
    }
}
