package com.hiking.treasure.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public final class RequestClientInfoResolver {

    private RequestClientInfoResolver() {
    }

    public static String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        for (String header : new String[]{"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"}) {
            String value = request.getHeader(header);
            if (StringUtils.hasText(value) && !"unknown".equalsIgnoreCase(value)) {
                int commaIndex = value.indexOf(',');
                return commaIndex >= 0 ? value.substring(0, commaIndex).trim() : value.trim();
            }
        }
        return request.getRemoteAddr();
    }

    public static String resolveUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.hasText(userAgent) ? userAgent : "unknown";
    }

    public static String resolveDeviceName(HttpServletRequest request) {
        String userAgent = resolveUserAgent(request).toLowerCase();
        String browser = "Unknown";
        if (userAgent.contains("edg")) {
            browser = "Edge";
        } else if (userAgent.contains("chrome")) {
            browser = "Chrome";
        } else if (userAgent.contains("safari")) {
            browser = "Safari";
        } else if (userAgent.contains("firefox")) {
            browser = "Firefox";
        }

        String device = "Desktop";
        if (userAgent.contains("iphone")) {
            device = "iPhone";
        } else if (userAgent.contains("ipad")) {
            device = "iPad";
        } else if (userAgent.contains("android")) {
            device = "Android";
        } else if (userAgent.contains("mac os x") || userAgent.contains("macintosh")) {
            device = "Mac";
        } else if (userAgent.contains("windows")) {
            device = "Windows";
        } else if (userAgent.contains("linux")) {
            device = "Linux";
        }

        return browser + " / " + device;
    }
}
