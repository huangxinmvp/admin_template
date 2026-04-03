package com.hiking.treasure.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.common.util.JwtUtil;
import com.hiking.treasure.entity.User;
import com.hiking.treasure.entity.UserSession;
import com.hiking.treasure.service.TenantService;
import com.hiking.treasure.service.UserSessionService;
import com.hiking.treasure.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private TenantService tenantService;
    @Mock
    private UserService userService;
    @Mock
    private UserSessionService userSessionService;
    @Mock
    private FilterChain filterChain;

    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        jwtAuthFilter = new JwtAuthFilter(jwtUtil, tenantService, userService, userSessionService, new ObjectMapper());
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsAccessTokenWhenTenantDisabled() throws Exception {
        @SuppressWarnings("unchecked")
        Jws<Claims> jws = mock(Jws.class);
        Claims claims = mock(Claims.class);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer access-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.parse("access-token")).thenReturn(jws);
        when(jws.getBody()).thenReturn(claims);
        when(claims.getSubject()).thenReturn("u1");
        when(claims.get("un")).thenReturn("admin");
        when(claims.get("tid")).thenReturn("t1");
        when(claims.get("sid", String.class)).thenReturn("s1");
        when(userSessionService.validateActiveSession("s1", "u1")).thenReturn(new UserSession().setId("s1"));
        when(tenantService.requireActiveTenant("t1")).thenThrow(new BusinessException(401, "租户不存在或已停用"));

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("租户不存在或已停用"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void rejectsAccessTokenWhenUserDisabled() throws Exception {
        @SuppressWarnings("unchecked")
        Jws<Claims> jws = mock(Jws.class);
        Claims claims = mock(Claims.class);
        User user = new User();
        user.setId("u1");
        user.setStatus(0);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer access-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.parse("access-token")).thenReturn(jws);
        when(jws.getBody()).thenReturn(claims);
        when(claims.getSubject()).thenReturn("u1");
        when(claims.get("un")).thenReturn("admin");
        when(claims.get("tid")).thenReturn("t1");
        when(claims.get("sid", String.class)).thenReturn("s1");
        when(userSessionService.validateActiveSession("s1", "u1")).thenReturn(new UserSession().setId("s1"));
        when(tenantService.requireActiveTenant("t1")).thenReturn(null);
        when(userService.getById("u1")).thenReturn(user);

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("用户不存在或已被禁用/锁定"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void rejectsAccessTokenWhenUserLocked() throws Exception {
        @SuppressWarnings("unchecked")
        Jws<Claims> jws = mock(Jws.class);
        Claims claims = mock(Claims.class);
        User user = new User();
        user.setId("u1");
        user.setStatus(1);
        user.setLockUntil(LocalDateTime.now().plusMinutes(10));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer access-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.parse("access-token")).thenReturn(jws);
        when(jws.getBody()).thenReturn(claims);
        when(claims.getSubject()).thenReturn("u1");
        when(claims.get("un")).thenReturn("admin");
        when(claims.get("tid")).thenReturn("t1");
        when(claims.get("sid", String.class)).thenReturn("s1");
        when(userSessionService.validateActiveSession("s1", "u1")).thenReturn(new UserSession().setId("s1"));
        when(tenantService.requireActiveTenant("t1")).thenReturn(null);
        when(userService.getById("u1")).thenReturn(user);

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("账号已被锁定，请稍后再试"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void rejectsAccessTokenWhenPasswordUpdatedAfterTokenIssued() throws Exception {
        @SuppressWarnings("unchecked")
        Jws<Claims> jws = mock(Jws.class);
        Claims claims = mock(Claims.class);
        User user = new User();
        user.setId("u1");
        user.setStatus(1);
        user.setPwdUpdateTime(LocalDateTime.now().minusMinutes(1));
        Date issuedAt = Date.from(LocalDateTime.now().minusHours(1).atZone(ZoneId.systemDefault()).toInstant());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer access-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.parse("access-token")).thenReturn(jws);
        when(jws.getBody()).thenReturn(claims);
        when(claims.getSubject()).thenReturn("u1");
        when(claims.get("un")).thenReturn("admin");
        when(claims.get("tid")).thenReturn("t1");
        when(claims.get("sid", String.class)).thenReturn("s1");
        when(claims.getIssuedAt()).thenReturn(issuedAt);
        when(userSessionService.validateActiveSession("s1", "u1")).thenReturn(new UserSession().setId("s1"));
        when(tenantService.requireActiveTenant("t1")).thenReturn(null);
        when(userService.getById("u1")).thenReturn(user);

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("凭证已失效，请重新登录"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void rejectsAccessTokenWhenTenantExpired() throws Exception {
        @SuppressWarnings("unchecked")
        Jws<Claims> jws = mock(Jws.class);
        Claims claims = mock(Claims.class);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer access-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.parse("access-token")).thenReturn(jws);
        when(jws.getBody()).thenReturn(claims);
        when(claims.getSubject()).thenReturn("u1");
        when(claims.get("un")).thenReturn("admin");
        when(claims.get("tid")).thenReturn("t1");
        when(claims.get("sid", String.class)).thenReturn("s1");
        when(userSessionService.validateActiveSession("s1", "u1")).thenReturn(new UserSession().setId("s1"));
        when(tenantService.requireActiveTenant("t1")).thenThrow(new BusinessException(400, "租户已过期"));

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("租户已过期"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, never()).doFilter(request, response);
    }
}
