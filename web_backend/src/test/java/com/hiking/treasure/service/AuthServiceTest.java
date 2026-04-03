package com.hiking.treasure.service;

import com.hiking.treasure.common.api.ErrorCode;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.common.util.JwtUtil;
import com.hiking.treasure.common.util.PasswordPolicyValidator;
import com.hiking.treasure.domain.dto.LoginDTO;
import com.hiking.treasure.domain.dto.UserChangePasswordDTO;
import com.hiking.treasure.domain.vo.AuthVO;
import com.hiking.treasure.domain.vo.system.PasswordActionResultVO;
import com.hiking.treasure.entity.Tenant;
import com.hiking.treasure.entity.User;
import com.hiking.treasure.entity.UserSession;
import com.hiking.treasure.mapper.UserDepartMapper;
import com.hiking.treasure.mapper.UserRoleMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private PermissionService permissionService;
    @Mock
    private TenantService tenantService;
    @Mock
    private UserRoleMapper userRoleMapper;
    @Mock
    private UserDepartMapper userDepartMapper;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private PasswordPolicyValidator passwordPolicyValidator;
    @Mock
    private UserSessionService userSessionService;

    @InjectMocks
    private AuthService authService;

    @Test
    void buildAuthVoIncludesTenantRolesDeptsAndPermissions() {
        User user = new User();
        user.setId("u1");
        user.setTenantId("t1");
        user.setUsername("admin");
        user.setRealname("Admin");

        Tenant tenant = new Tenant();
        tenant.setTenantCode("acme");
        tenant.setTenantName("Acme");

        when(userService.getRoleCodes("u1")).thenReturn(List.of("ADMIN"));
        when(userService.getDepartIds("u1")).thenReturn(List.of("d1"));
        when(permissionService.listPermissionCodesByUserId("u1")).thenReturn(List.of("sys:user:view", "sys:tenant:view"));
        when(jwtUtil.createAccessToken(
                eq("u1"),
                eq("admin"),
                eq("t1"),
                eq("s1"),
                eq(List.of("ADMIN")),
                eq(List.of("sys:user:view", "sys:tenant:view"))
        ))
                .thenReturn("access-token");
        when(jwtUtil.createRefreshToken("u1", "s1")).thenReturn("refresh-token");
        when(tenantService.requireActiveTenant("t1")).thenReturn(tenant);

        AuthVO authVO = authService.buildAuthVO(user, "s1");

        assertEquals("u1", authVO.getUserId());
        assertEquals("t1", authVO.getTenantId());
        assertEquals("acme", authVO.getTenantCode());
        assertEquals("Acme", authVO.getTenantName());
        assertEquals("s1", authVO.getSessionId());
        assertIterableEquals(List.of("ADMIN"), authVO.getRoles());
        assertIterableEquals(List.of("d1"), authVO.getDepts());
        assertIterableEquals(List.of("sys:user:view", "sys:tenant:view"), authVO.getPermissions());
        assertEquals("access-token", authVO.getAccessToken());
        assertEquals("refresh-token", authVO.getRefreshToken());
    }

    @Test
    void loginRejectsWhenTenantDisabled() {
        User user = new User();
        user.setId("u1");
        user.setTenantId("t1");
        user.setUsername("admin");
        user.setPassword("encoded");
        user.setStatus(1);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("Admin@123456");
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(userService.getByUsername("admin")).thenReturn(user);
        when(encoder.matches("Admin@123456", "encoded")).thenReturn(true);
        when(tenantService.requireActiveTenant("t1")).thenThrow(new BusinessException(400, "租户不存在或已停用"));

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(dto, request));

        assertEquals("租户不存在或已停用", exception.getMessage());
        verify(userService, never()).updateById(user);
    }

    @Test
    void loginRejectsWhenUserLocked() {
        User user = new User();
        user.setId("u1");
        user.setTenantId("t1");
        user.setUsername("admin");
        user.setPassword("encoded");
        user.setStatus(1);
        user.setLockUntil(LocalDateTime.now().plusMinutes(10));

        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("Admin@123456");
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(userService.getByUsername("admin")).thenReturn(user);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(dto, request));

        assertEquals("账号已被锁定，请稍后再试", exception.getMessage());
        assertEquals(ErrorCode.USER_LOCKED.getKey(), exception.getErrorKey());
        verify(userService, never()).updateById(user);
    }

    @Test
    void refreshRejectsWhenTenantDisabled() {
        User user = new User();
        user.setId("u1");
        user.setTenantId("t1");
        user.setUsername("admin");
        user.setStatus(1);

        @SuppressWarnings("unchecked")
        Jws<Claims> jws = org.mockito.Mockito.mock(Jws.class);
        Claims claims = org.mockito.Mockito.mock(Claims.class);
        UserSession session = new UserSession().setId("s1").setUserId("u1");
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(jwtUtil.parse("refresh-token")).thenReturn(jws);
        when(jws.getBody()).thenReturn(claims);
        when(claims.get("rt")).thenReturn(Boolean.TRUE);
        when(claims.getSubject()).thenReturn("u1");
        when(claims.get("sid", String.class)).thenReturn("s1");
        when(userSessionService.validateActiveSession("s1", "u1")).thenReturn(session);
        when(userService.getById("u1")).thenReturn(user);
        when(tenantService.requireActiveTenant("t1")).thenThrow(new BusinessException(400, "租户不存在或已停用"));

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.refresh("refresh-token", request));

        assertEquals("租户不存在或已停用", exception.getMessage());
        verify(userService, never()).updateById(user);
    }

    @Test
    void refreshRejectsWhenPasswordUpdatedAfterTokenIssued() {
        User user = new User();
        user.setId("u1");
        user.setTenantId("t1");
        user.setUsername("admin");
        user.setStatus(1);
        user.setPwdUpdateTime(LocalDateTime.now().minusMinutes(1));

        @SuppressWarnings("unchecked")
        Jws<Claims> jws = org.mockito.Mockito.mock(Jws.class);
        Claims claims = org.mockito.Mockito.mock(Claims.class);
        Date issuedAt = Date.from(LocalDateTime.now().minusHours(1).atZone(ZoneId.systemDefault()).toInstant());
        UserSession session = new UserSession().setId("s1").setUserId("u1");
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(jwtUtil.parse("refresh-token")).thenReturn(jws);
        when(jws.getBody()).thenReturn(claims);
        when(claims.get("rt")).thenReturn(Boolean.TRUE);
        when(claims.getSubject()).thenReturn("u1");
        when(claims.get("sid", String.class)).thenReturn("s1");
        when(claims.getIssuedAt()).thenReturn(issuedAt);
        when(userSessionService.validateActiveSession("s1", "u1")).thenReturn(session);
        when(userService.getById("u1")).thenReturn(user);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.refresh("refresh-token", request));

        assertEquals("凭证已失效，请重新登录", exception.getMessage());
        verify(userService, never()).updateById(user);
    }

    @Test
    void changeOwnPasswordUpdatesPasswordAndPwdUpdateTime() {
        User user = new User();
        user.setId("u1");
        user.setStatus(1);
        user.setPassword("encoded-old");

        UserChangePasswordDTO dto = new UserChangePasswordDTO();
        dto.setCurrentPassword("old-pass");
        dto.setNewPassword("new-pass");
        dto.setConfirmPassword("new-pass");

        when(userService.getById("u1")).thenReturn(user);
        when(encoder.matches("old-pass", "encoded-old")).thenReturn(true);
        when(encoder.encode("new-pass")).thenReturn("encoded-new");
        when(userService.updateById(any(User.class))).thenReturn(true);

        PasswordActionResultVO changed = authService.changeOwnPassword("u1", dto);

        assertEquals(true, changed.isReloginRequired());
        assertEquals("密码已修改，请重新登录", changed.getMessage());
        assertEquals("encoded-new", user.getPassword());
        assertNotNull(user.getPwdUpdateTime());
        verify(userService).updateById(user);
    }

    @Test
    void changeOwnPasswordRejectsWhenCurrentPasswordInvalid() {
        User user = new User();
        user.setId("u1");
        user.setStatus(1);
        user.setPassword("encoded-old");

        UserChangePasswordDTO dto = new UserChangePasswordDTO();
        dto.setCurrentPassword("wrong-pass");
        dto.setNewPassword("new-pass");
        dto.setConfirmPassword("new-pass");

        when(userService.getById("u1")).thenReturn(user);
        when(encoder.matches("wrong-pass", "encoded-old")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.changeOwnPassword("u1", dto));

        assertEquals("当前密码错误", exception.getMessage());
        verify(userService, never()).updateById(any(User.class));
    }

    @Test
    void changeOwnPasswordRejectsWhenPasswordTooWeak() {
        User user = new User();
        user.setId("u1");
        user.setStatus(1);
        user.setPassword("encoded-old");

        UserChangePasswordDTO dto = new UserChangePasswordDTO();
        dto.setCurrentPassword("old-pass");
        dto.setNewPassword("weak");
        dto.setConfirmPassword("weak");

        when(userService.getById("u1")).thenReturn(user);
        when(encoder.matches("old-pass", "encoded-old")).thenReturn(true);
        org.mockito.Mockito.doThrow(new BusinessException(ErrorCode.PASSWORD_TOO_WEAK, "密码需为8-32位，且同时包含大写字母、小写字母、数字和特殊字符"))
                .when(passwordPolicyValidator).validateOrThrow("weak");

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.changeOwnPassword("u1", dto));

        assertEquals("密码需为8-32位，且同时包含大写字母、小写字母、数字和特殊字符", exception.getMessage());
        assertEquals(ErrorCode.PASSWORD_TOO_WEAK.getKey(), exception.getErrorKey());
    }
}
