package com.hiking.treasure.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hiking.treasure.domain.vo.system.UserSessionVO;
import com.hiking.treasure.entity.User;
import com.hiking.treasure.entity.UserSession;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

public interface UserSessionService extends IService<UserSession> {

    UserSession createSession(User user, HttpServletRequest request, long accessExpSeconds, long refreshExpSeconds);

    UserSession validateActiveSession(String sessionId, String userId);

    UserSession refreshSession(String sessionId, HttpServletRequest request, long accessExpSeconds, long refreshExpSeconds);

    void touchSession(String sessionId);

    boolean revokeSession(String sessionId, String operator, String reason);

    int revokeAllSessionsByUserId(String userId, String operator, String reason);

    List<UserSessionVO> listUserSessions(String userId);

    Map<String, Long> countActiveSessionsByUserIds(List<String> userIds);
}
