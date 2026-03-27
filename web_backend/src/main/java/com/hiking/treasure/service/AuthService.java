package com.hiking.treasure.service;

import com.hiking.treasure.common.api.ErrorCode;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.common.security.SecurityUtils;
import com.hiking.treasure.common.util.JwtUtil;
import com.hiking.treasure.common.util.PasswordPolicyValidator;
import com.hiking.treasure.domain.dto.LoginDTO;
import com.hiking.treasure.domain.dto.RegisterDTO;
import com.hiking.treasure.domain.dto.UserChangePasswordDTO;
import com.hiking.treasure.domain.vo.AuthVO;
import com.hiking.treasure.domain.vo.system.PasswordActionResultVO;
import com.hiking.treasure.domain.vo.system.UserSessionVO;
import com.hiking.treasure.entity.Tenant;
import com.hiking.treasure.entity.User;
import com.hiking.treasure.entity.UserDepart;
import com.hiking.treasure.entity.UserRole;
import com.hiking.treasure.entity.UserSession;
import com.hiking.treasure.mapper.UserDepartMapper;
import com.hiking.treasure.mapper.UserRoleMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Date;
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final PermissionService permissionService;
    private final TenantService tenantService;
    private final UserRoleMapper userRoleMapper;
    private final UserDepartMapper userDepartMapper;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final UserSessionService userSessionService;

    @Transactional(rollbackFor = Exception.class)
    public AuthVO register(RegisterDTO dto, HttpServletRequest request) {
        // 1) 校验唯一
        if (userService.getByUsername(dto.getUsername()) != null) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        passwordPolicyValidator.validateOrThrow(dto.getPassword());
        // 2) 创建用户
        User u = new User();
        u.setTenantId(dto.getTenantId());
        u.setUsername(dto.getUsername());
        u.setPassword(encoder.encode(dto.getPassword()));
        u.setRealname(dto.getRealname());
        u.setStatus(1);
        tenantService.requireActiveTenant(dto.getTenantId());
        userService.save(u);

        // 3) 绑定角色
        if(!CollectionUtils.isEmpty(dto.getRoles())) {
            dto.getRoles().forEach(roleId -> {
                UserRole link = new UserRole();
                link.setId(null); // ASSIGN_UUID
                link.setUserId(u.getId());
                link.setRoleId(roleId);
                userRoleMapper.insert(link);
            });
        }
        if(!CollectionUtils.isEmpty(dto.getDepts())) {
            dto.getDepts().forEach(deptId -> {
                UserDepart link = new UserDepart();
                link.setId(null);
                link.setUserId(u.getId());
                link.setDepartId(deptId);
                userDepartMapper.insert(link);
            });
        }
        // 4) 签发 token
        u.setPwdUpdateTime(java.time.LocalDateTime.now());
        userService.updateById(u);
        UserSession session = userSessionService.createSession(u, request, jwt.getExpSeconds(), jwt.getRefreshExpSeconds());
        return buildAuthVO(u, session.getId());
    }

    public AuthVO login(LoginDTO dto, HttpServletRequest request) {
        User u = userService.getByUsername(dto.getUsername());
        validateUserAvailableForLogin(u);
        if (!encoder.matches(dto.getPassword(), u.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        tenantService.requireActiveTenant(u.getTenantId());
        u.setLastLoginTime(java.time.LocalDateTime.now());
        userService.updateById(u);
        UserSession session = userSessionService.createSession(u, request, jwt.getExpSeconds(), jwt.getRefreshExpSeconds());
        return buildAuthVO(u, session.getId());
    }

    public AuthVO refresh(String refreshToken, HttpServletRequest request) {
        Claims c = jwt.parse(refreshToken).getBody();
        if (!Boolean.TRUE.equals(c.get("rt"))) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        String uid = c.getSubject();
        String sessionId = c.get("sid", String.class);
        UserSession activeSession = userSessionService.validateActiveSession(sessionId, uid);
        if (activeSession == null) {
            throw new BusinessException(ErrorCode.SESSION_REVOKED);
        }
        User u = userService.getById(uid);
        validateUserAvailableForToken(u, c.getIssuedAt());
        tenantService.requireActiveTenant(u.getTenantId());
        u.setLastLoginTime(java.time.LocalDateTime.now());
        userService.updateById(u);
        userSessionService.refreshSession(sessionId, request, jwt.getExpSeconds(), jwt.getRefreshExpSeconds());
        return buildAuthVO(u, sessionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public PasswordActionResultVO changeOwnPassword(String userId, UserChangePasswordDTO dto) {
        User user = userService.getById(userId);
        validateUserAvailableForLogin(user);
        if (!encoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.CURRENT_PASSWORD_INVALID);
        }
        if (dto.getCurrentPassword().equals(dto.getNewPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_REUSED);
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }
        passwordPolicyValidator.validateOrThrow(dto.getNewPassword());
        user.setPassword(encoder.encode(dto.getNewPassword()));
        user.setPwdUpdateTime(java.time.LocalDateTime.now());
        if (!userService.updateById(user)) {
            throw new BusinessException(ErrorCode.PASSWORD_CHANGE_FAILED);
        }
        userSessionService.revokeAllSessionsByUserId(userId, user.getUsername(), "密码已修改");
        return PasswordActionResultVO.of(true, "密码已修改，请重新登录");
    }

    public AuthVO buildAuthVO(User u, String sessionId) {
        List<String> roles = userService.getRoleCodes(u.getId());
        List<String> depts = userService.getDepartIds(u.getId());
        List<String> permissions = permissionService.listPermissionCodesByUserId(u.getId());
        String at = jwt.createAccessToken(u.getId(), u.getUsername(), u.getTenantId(), sessionId, roles, permissions);
        String rt = jwt.createRefreshToken(u.getId(), sessionId);
        AuthVO vo = new AuthVO();
        vo.setAccessToken(at);
        vo.setRefreshToken(rt);
        vo.setExpiresIn(jwt.getExpSeconds());
        vo.setSessionId(sessionId);
        vo.setUserId(u.getId());
        vo.setTenantId(u.getTenantId());
        vo.setUsername(u.getUsername());
        vo.setRealname(u.getRealname());
        vo.setRoles(roles);
        vo.setDepts(depts);
        vo.setPermissions(permissions);
        Tenant tenant = tenantService.requireActiveTenant(u.getTenantId());
        if (tenant != null) {
            vo.setTenantCode(tenant.getTenantCode());
            vo.setTenantName(tenant.getTenantName());
        }
        return vo;
    }

    public AuthVO buildCurrentAuthVO(User u, String sessionId) {
        List<String> roles = userService.getRoleCodes(u.getId());
        List<String> depts = userService.getDepartIds(u.getId());
        List<String> permissions = permissionService.listPermissionCodesByUserId(u.getId());
        AuthVO vo = new AuthVO();
        vo.setSessionId(sessionId);
        vo.setExpiresIn(jwt.getExpSeconds());
        vo.setUserId(u.getId());
        vo.setTenantId(u.getTenantId());
        vo.setUsername(u.getUsername());
        vo.setRealname(u.getRealname());
        vo.setRoles(roles);
        vo.setDepts(depts);
        vo.setPermissions(permissions);
        Tenant tenant = tenantService.requireActiveTenant(u.getTenantId());
        if (tenant != null) {
            vo.setTenantCode(tenant.getTenantCode());
            vo.setTenantName(tenant.getTenantName());
        }
        return vo;
    }

    public boolean logoutCurrentSession() {
        return userSessionService.revokeSession(
                SecurityUtils.getRequiredSessionId(),
                SecurityUtils.getUsername(),
                "用户主动退出"
        );
    }

    public List<UserSessionVO> listCurrentUserSessions() {
        return userSessionService.listUserSessions(SecurityUtils.getRequiredUserId());
    }

    public boolean revokeSession(String sessionId) {
        UserSession session = userSessionService.getById(sessionId);
        if (session == null) {
            return false;
        }
        boolean isAdmin = SecurityUtils.hasRole("ADMIN")
                || SecurityUtils.hasAuthority("sys:security:view")
                || SecurityUtils.hasAuthority("sys:user:update");
        String currentUserId = SecurityUtils.getRequiredUserId();
        if (!currentUserId.equals(session.getUserId()) && !isAdmin) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return userSessionService.revokeSession(sessionId, SecurityUtils.getUsername(), "会话已被手动下线");
    }

    public int forceLogoutUserSessions(String userId) {
        return userSessionService.revokeAllSessionsByUserId(userId, SecurityUtils.getUsername(), "管理员强制下线");
    }

    public Map<String, Long> countActiveSessionsByUserIds(List<String> userIds) {
        return userSessionService.countActiveSessionsByUserIds(userIds);
    }

    private void validateUserAvailableForLogin(User user) {
        if (user == null || Integer.valueOf(0).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        if (user.getLockUntil() != null && user.getLockUntil().isAfter(java.time.LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.USER_LOCKED);
        }
    }

    private void validateUserAvailableForToken(User user, Date issuedAt) {
        if (user == null || Integer.valueOf(0).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        if (user.getLockUntil() != null && user.getLockUntil().isAfter(java.time.LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.USER_LOCKED);
        }
        if (user.getPwdUpdateTime() != null && (issuedAt == null || issuedAt.toInstant().isBefore(user.getPwdUpdateTime().atZone(java.time.ZoneId.systemDefault()).toInstant()))) {
            throw new BusinessException(ErrorCode.TOKEN_RELOGIN_REQUIRED);
        }
    }
}
