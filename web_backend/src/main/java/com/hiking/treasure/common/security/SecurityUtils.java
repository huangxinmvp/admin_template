package com.hiking.treasure.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static LoginUser getLoginUser() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    public static String getUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getUserId();
    }

    public static String getRequiredUserId() {
        String userId = getUserId();
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("当前未登录");
        }
        return userId;
    }

    public static String getTenantId() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getTenantId();
    }

    public static String getUsername() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getUsername();
    }

    public static String getSessionId() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getSessionId();
    }

    public static String getRequiredSessionId() {
        String sessionId = getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalStateException("当前会话无效");
        }
        return sessionId;
    }

    public static List<String> getRoles() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? List.of() : loginUser.getRoles();
    }

    public static boolean hasRole(String roleCode) {
        return getRoles().stream().anyMatch(role -> role != null && role.equalsIgnoreCase(roleCode));
    }

    public static boolean hasAuthority(String authority) {
        Authentication authentication = getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            if (authority.equals(grantedAuthority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
