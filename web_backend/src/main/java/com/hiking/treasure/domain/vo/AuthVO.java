package com.hiking.treasure.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class AuthVO {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;      // 秒
    private String sessionId;
    private String userId;
    private String tenantId;
    private String tenantCode;
    private String tenantName;
    private String username;
    private String realname;
    private List<String> roles;
    private List<String> depts;
    private List<String> permissions;
}
