package com.hiking.treasure.controller;

import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.common.security.SecurityUtils;
import com.hiking.treasure.domain.dto.LoginDTO;
import com.hiking.treasure.domain.dto.RegisterDTO;
import com.hiking.treasure.domain.dto.UserChangePasswordDTO;
import com.hiking.treasure.domain.vo.AuthVO;
import com.hiking.treasure.domain.vo.system.PasswordActionResultVO;
import com.hiking.treasure.domain.vo.system.UserSessionVO;
import com.hiking.treasure.entity.User;
import com.hiking.treasure.service.AuthService;
import com.hiking.treasure.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @Operation(summary = "注册")
    @PostMapping("/register")
    public Result<AuthVO> register(@Valid @RequestBody RegisterDTO dto, HttpServletRequest request) {
        return Result.ok(authService.register(dto, request));
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<AuthVO> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        return Result.ok(authService.login(dto, request));
    }

    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public Result<AuthVO> refresh(@RequestParam("token") String refreshToken, HttpServletRequest request) {
        return Result.ok(authService.refresh(refreshToken, request));
    }

    @Operation(summary = "当前用户信息")
    @GetMapping("/me")
    public Result<AuthVO> me() {
        String userId = SecurityUtils.getRequiredUserId();
        User user = userService.getById(userId);
        return Result.ok(authService.buildCurrentAuthVO(user, SecurityUtils.getSessionId()));
    }

    @Operation(summary = "当前用户修改密码")
    @PutMapping("/password")
    public Result<PasswordActionResultVO> changePassword(@Valid @RequestBody UserChangePasswordDTO dto) {
        return Result.ok(authService.changeOwnPassword(SecurityUtils.getRequiredUserId(), dto));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Boolean> logout() {
        return Result.ok(authService.logoutCurrentSession());
    }

    @Operation(summary = "当前用户会话列表")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/sessions")
    public Result<List<UserSessionVO>> sessions() {
        return Result.ok(authService.listCurrentUserSessions());
    }

    @Operation(summary = "当前用户或管理员下线指定会话")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Boolean> revokeSession(@PathVariable String sessionId) {
        return Result.ok(authService.revokeSession(sessionId));
    }

    @Operation(summary = "管理员强制下线用户全部会话")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:security:view') or hasAuthority('sys:user:update')")
    @PostMapping("/sessions/users/{userId}/force-logout")
    public Result<Integer> forceLogoutUserSessions(@PathVariable String userId) {
        return Result.ok(authService.forceLogoutUserSessions(userId));
    }

    @Operation(summary = "查询用户活跃会话数量")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:security:view') or hasAuthority('sys:user:view')")
    @GetMapping("/sessions/user-counts")
    public Result<Map<String, Long>> sessionCounts(@RequestParam("userIds") List<String> userIds) {
        return Result.ok(authService.countActiveSessionsByUserIds(userIds));
    }
}
