package com.hiking.treasure.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiking.treasure.common.api.ErrorCode;
import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.common.security.LoginUser;
import com.hiking.treasure.common.util.JwtUtil;
import com.hiking.treasure.entity.User;
import com.hiking.treasure.service.TenantService;
import com.hiking.treasure.service.UserSessionService;
import com.hiking.treasure.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TenantService tenantService;
    private final UserService userService;
    private final UserSessionService userSessionService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                Claims claims = jwtUtil.parse(token).getBody();
                String uid = claims.getSubject();
                String username = (String) claims.get("un");
                String tenantId = (String) claims.get("tid");
                String sessionId = claims.get("sid", String.class);
                if (sessionId == null || sessionId.isBlank() || userSessionService.validateActiveSession(sessionId, uid) == null) {
                    SecurityContextHolder.clearContext();
                    writeUnauthorized(res, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.SESSION_REVOKED.getKey(), "当前会话已失效，请重新登录");
                    return;
                }
                try {
                    tenantService.requireActiveTenant(tenantId);
                } catch (BusinessException ex) {
                    SecurityContextHolder.clearContext();
                    writeUnauthorized(res, ex.getCode(), ex.getErrorKey(), ex.getMessage());
                    return;
                }
                User currentUser = userService.getById(uid);
                if (currentUser == null || Integer.valueOf(0).equals(currentUser.getStatus())) {
                    SecurityContextHolder.clearContext();
                    writeUnauthorized(res, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.USER_DISABLED.getKey(), "用户不存在或已被禁用/锁定");
                    return;
                }
                if (currentUser.getLockUntil() != null && currentUser.getLockUntil().isAfter(java.time.LocalDateTime.now())) {
                    SecurityContextHolder.clearContext();
                    writeUnauthorized(res, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.USER_LOCKED.getKey(), "账号已被锁定，请稍后再试");
                    return;
                }
                if (currentUser.getPwdUpdateTime() != null && (claims.getIssuedAt() == null
                        || claims.getIssuedAt().toInstant().isBefore(currentUser.getPwdUpdateTime().atZone(java.time.ZoneId.systemDefault()).toInstant()))) {
                    SecurityContextHolder.clearContext();
                    writeUnauthorized(res, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.TOKEN_RELOGIN_REQUIRED.getKey(), "凭证已失效，请重新登录");
                    return;
                }
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) claims.get("roles");
                @SuppressWarnings("unchecked")
                List<String> permissions = (List<String>) claims.get("perms");
                // 构建权限
                List<SimpleGrantedAuthority> roleAuthorities = roles == null ? List.of()
                        : roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
                List<SimpleGrantedAuthority> permissionAuthorities = permissions == null ? List.of()
                        : permissions.stream().map(SimpleGrantedAuthority::new).toList();
                List<SimpleGrantedAuthority> authorities = java.util.stream.Stream.concat(roleAuthorities.stream(), permissionAuthorities.stream())
                        .distinct()
                        .toList();
                userSessionService.touchSession(sessionId);
                LoginUser loginUser = new LoginUser(uid, username, tenantId, sessionId, roles == null ? List.of() : roles);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(loginUser, null, authorities);
                authentication.setDetails(loginUser);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException e) {
                // token 无效，保持匿名
            }
        }
        chain.doFilter(req, res);
    }

    private void writeUnauthorized(HttpServletResponse response, int code, String errorKey, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        Result<Object> result = Result.error(code, message);
        result.setErrorKey(errorKey);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
