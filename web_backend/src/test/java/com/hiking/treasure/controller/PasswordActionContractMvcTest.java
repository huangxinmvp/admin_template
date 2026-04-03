package com.hiking.treasure.controller;

import com.hiking.treasure.common.api.ErrorCode;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.common.exception.GlobalExceptionHandler;
import com.hiking.treasure.common.security.LoginUser;
import com.hiking.treasure.common.util.PasswordUtil;
import com.hiking.treasure.common.util.PasswordPolicyValidator;
import com.hiking.treasure.domain.dto.UserChangePasswordDTO;
import com.hiking.treasure.domain.vo.system.PasswordActionResultVO;
import com.hiking.treasure.service.AuthService;
import com.hiking.treasure.service.UserSessionService;
import com.hiking.treasure.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PasswordActionContractMvcTest {

    @Mock
    private AuthService authService;
    @Mock
    private UserService userService;
    @Mock
    private PasswordUtil passwordUtil;
    @Mock
    private PasswordPolicyValidator passwordPolicyValidator;
    @Mock
    private UserSessionService userSessionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthController authController = new AuthController(authService, userService);
        UserController userController = new UserController();
        ReflectionTestUtils.setField(userController, "userService", userService);
        ReflectionTestUtils.setField(userController, "passwordUtil", passwordUtil);
        ReflectionTestUtils.setField(userController, "passwordPolicyValidator", passwordPolicyValidator);
        ReflectionTestUtils.setField(userController, "userSessionService", userSessionService);

        mockMvc = MockMvcBuilders.standaloneSetup(authController, userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authChangePasswordReturnsReloginRequiredFlag() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new LoginUser("u1", "admin", "t1", "s1", java.util.List.of("ADMIN")),
                        null
                )
        );
        when(authService.changeOwnPassword(eq("u1"), any(UserChangePasswordDTO.class)))
                .thenReturn(PasswordActionResultVO.of(true, "密码已修改，请重新登录"));

        mockMvc.perform(put("/api/auth/password")
                        .contentType("application/json")
                        .content("""
                                {"currentPassword":"old","newPassword":"new","confirmPassword":"new"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.reloginRequired").value(true))
                .andExpect(jsonPath("$.result.message").value("密码已修改，请重新登录"));
    }

    @Test
    void adminResetPasswordReturnsReloginRequiredFlag() throws Exception {
        when(passwordUtil.encode("new-pass")).thenReturn("encoded");
        when(userService.resetPassword("u2", "encoded")).thenReturn(true);

        mockMvc.perform(put("/api/user/u2/password")
                        .contentType("application/json")
                        .content("""
                                {"password":"new-pass"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.reloginRequired").value(true))
                .andExpect(jsonPath("$.result.message").value("密码已重置，用户需重新登录"));
    }

    @Test
    void adminResetPasswordReturnsErrorKeyWhenPasswordWeak() throws Exception {
        org.mockito.Mockito.doThrow(new BusinessException(ErrorCode.PASSWORD_TOO_WEAK, "密码需为8-32位，且同时包含大写字母、小写字母、数字和特殊字符"))
                .when(passwordPolicyValidator).validateOrThrow("weak");

        mockMvc.perform(put("/api/user/u2/password")
                        .contentType("application/json")
                        .content("""
                                {"password":"weak"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorKey").value("PASSWORD_TOO_WEAK"))
                .andExpect(jsonPath("$.message").value("密码需为8-32位，且同时包含大写字母、小写字母、数字和特殊字符"));
    }
}
