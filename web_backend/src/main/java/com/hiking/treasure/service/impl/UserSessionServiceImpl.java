package com.hiking.treasure.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.domain.vo.system.UserSessionVO;
import com.hiking.treasure.entity.User;
import com.hiking.treasure.entity.UserSession;
import com.hiking.treasure.mapper.UserSessionMapper;
import com.hiking.treasure.service.UserSessionService;
import com.hiking.treasure.common.util.RequestClientInfoResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserSessionServiceImpl extends ServiceImpl<UserSessionMapper, UserSession>
        implements UserSessionService {

    private static final int ACTIVE = 1;
    private static final int OFFLINE = 0;

    @Override
    public UserSession createSession(User user, HttpServletRequest request, long accessExpSeconds, long refreshExpSeconds) {
        LocalDateTime now = LocalDateTime.now();
        UserSession session = new UserSession();
        session.setTenantId(user.getTenantId());
        session.setUserId(user.getId());
        session.setClientIp(RequestClientInfoResolver.resolveClientIp(request));
        session.setUserAgent(RequestClientInfoResolver.resolveUserAgent(request));
        session.setDeviceName(RequestClientInfoResolver.resolveDeviceName(request));
        session.setLoginTime(now);
        session.setLastActiveTime(now);
        session.setAccessExpiresAt(now.plusSeconds(accessExpSeconds));
        session.setRefreshExpiresAt(now.plusSeconds(refreshExpSeconds));
        session.setStatus(ACTIVE);
        save(session);
        return session;
    }

    @Override
    public UserSession validateActiveSession(String sessionId, String userId) {
        if (sessionId == null || sessionId.isBlank()) {
            return null;
        }
        UserSession session = baseMapper.selectActiveById(sessionId);
        if (session == null) {
            return null;
        }
        if (userId != null && !userId.equals(session.getUserId())) {
            return null;
        }
        return session;
    }

    @Override
    public UserSession refreshSession(String sessionId, HttpServletRequest request, long accessExpSeconds, long refreshExpSeconds) {
        UserSession session = validateActiveSession(sessionId, null);
        if (session == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        session.setClientIp(RequestClientInfoResolver.resolveClientIp(request));
        session.setUserAgent(RequestClientInfoResolver.resolveUserAgent(request));
        session.setDeviceName(RequestClientInfoResolver.resolveDeviceName(request));
        session.setLastActiveTime(now);
        session.setAccessExpiresAt(now.plusSeconds(accessExpSeconds));
        session.setRefreshExpiresAt(now.plusSeconds(refreshExpSeconds));
        session.setStatus(ACTIVE);
        session.setLogoutReason(null);
        session.setLogoutBy(null);
        session.setLogoutTime(null);
        updateById(session);
        return session;
    }

    @Override
    public void touchSession(String sessionId) {
        UserSession session = validateActiveSession(sessionId, null);
        if (session == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (session.getLastActiveTime() != null && session.getLastActiveTime().isAfter(now.minusSeconds(60))) {
            return;
        }
        session.setLastActiveTime(now);
        updateById(session);
    }

    @Override
    public boolean revokeSession(String sessionId, String operator, String reason) {
        UserSession session = getById(sessionId);
        if (session == null || Integer.valueOf(1).equals(session.getDelFlag())) {
            return false;
        }
        session.setStatus(OFFLINE);
        session.setLogoutReason(reason);
        session.setLogoutBy(operator);
        session.setLogoutTime(LocalDateTime.now());
        return updateById(session);
    }

    @Override
    public int revokeAllSessionsByUserId(String userId, String operator, String reason) {
        List<UserSession> sessions = listUserSessionsEntity(userId);
        int affected = 0;
        for (UserSession session : sessions) {
            if (Integer.valueOf(ACTIVE).equals(session.getStatus())
                    && (session.getRefreshExpiresAt() == null || session.getRefreshExpiresAt().isAfter(LocalDateTime.now()))) {
                session.setStatus(OFFLINE);
                session.setLogoutReason(reason);
                session.setLogoutBy(operator);
                session.setLogoutTime(LocalDateTime.now());
                if (updateById(session)) {
                    affected++;
                }
            }
        }
        return affected;
    }

    @Override
    public List<UserSessionVO> listUserSessions(String userId) {
        return listUserSessionsEntity(userId).stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public Map<String, Long> countActiveSessionsByUserIds(List<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Map.of();
        }
        Map<String, Long> result = new LinkedHashMap<>();
        baseMapper.countActiveByUserIds(userIds).forEach(item ->
                result.put(item.getUserId(), item.getActiveCount() == null ? 0L : item.getActiveCount()));
        return result;
    }

    private List<UserSession> listUserSessionsEntity(String userId) {
        return baseMapper.selectByUserId(userId);
    }

    private UserSessionVO toVO(UserSession session) {
        UserSessionVO vo = new UserSessionVO();
        vo.setId(session.getId());
        vo.setTenantId(session.getTenantId());
        vo.setUserId(session.getUserId());
        vo.setClientIp(session.getClientIp());
        vo.setUserAgent(session.getUserAgent());
        vo.setDeviceName(session.getDeviceName());
        vo.setLoginTime(session.getLoginTime());
        vo.setLastActiveTime(session.getLastActiveTime());
        vo.setAccessExpiresAt(session.getAccessExpiresAt());
        vo.setRefreshExpiresAt(session.getRefreshExpiresAt());
        vo.setStatus(session.getStatus());
        vo.setLogoutReason(session.getLogoutReason());
        vo.setLogoutBy(session.getLogoutBy());
        vo.setLogoutTime(session.getLogoutTime());
        return vo;
    }
}
