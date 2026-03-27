package com.hiking.treasure.common.api;

import java.util.EnumSet;
import java.util.Arrays;

public enum ErrorCode {
    BAD_REQUEST(400, "BAD_REQUEST", "请求参数错误", Scope.DELETE_CONFLICT),
    UNAUTHORIZED(401, "UNAUTHORIZED", "未登录或令牌已失效", Scope.AUTH),
    FORBIDDEN(403, "FORBIDDEN", "无权限访问", Scope.AUTH),
    NOT_FOUND(404, "NOT_FOUND", "资源不存在"),
    CONFLICT(409, "CONFLICT", "资源冲突"),
    INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR", "服务器内部错误"),

    USERNAME_ALREADY_EXISTS(400, "USERNAME_ALREADY_EXISTS", "用户名已存在"),
    INVALID_CREDENTIALS(400, "INVALID_CREDENTIALS", "用户名或密码错误", Scope.AUTH),
    USER_DISABLED(401, "USER_DISABLED", "账号不存在或已被禁用/锁定", Scope.AUTH),
    USER_LOCKED(401, "USER_LOCKED", "账号已被锁定，请稍后再试", Scope.AUTH),
    INVALID_REFRESH_TOKEN(401, "INVALID_REFRESH_TOKEN", "非法刷新令牌", Scope.AUTH),
    TOKEN_RELOGIN_REQUIRED(401, "TOKEN_RELOGIN_REQUIRED", "凭证已失效，请重新登录", Scope.AUTH),
    SESSION_REVOKED(401, "SESSION_REVOKED", "当前会话已失效，请重新登录", Scope.AUTH),
    CURRENT_PASSWORD_INVALID(400, "CURRENT_PASSWORD_INVALID", "当前密码错误", Scope.PASSWORD),
    PASSWORD_REUSED(400, "PASSWORD_REUSED", "新密码不能与当前密码相同", Scope.PASSWORD),
    PASSWORD_CONFIRM_MISMATCH(400, "PASSWORD_CONFIRM_MISMATCH", "两次输入的新密码不一致", Scope.PASSWORD),
    PASSWORD_CHANGE_FAILED(500, "PASSWORD_CHANGE_FAILED", "密码修改失败", Scope.PASSWORD),
    PASSWORD_TOO_WEAK(400, "PASSWORD_TOO_WEAK", "密码强度不足", Scope.PASSWORD),
    TENANT_DISABLED(400, "TENANT_DISABLED", "租户不存在或已停用", Scope.AUTH),
    TENANT_EXPIRED(400, "TENANT_EXPIRED", "租户已过期", Scope.AUTH);

    private final int code;
    private final String key;
    private final String defaultMessage;
    private final EnumSet<Scope> scopes;

    ErrorCode(int code, String key, String defaultMessage, Scope... scopes) {
        this.code = code;
        this.key = key;
        this.defaultMessage = defaultMessage;
        this.scopes = scopes == null || scopes.length == 0 ? EnumSet.noneOf(Scope.class) : EnumSet.copyOf(Arrays.asList(scopes));
    }

    public int getCode() {
        return code;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public boolean inScope(Scope scope) {
        return scopes.contains(scope);
    }

    public enum Scope {
        AUTH,
        PASSWORD,
        DELETE_CONFLICT
    }
}
