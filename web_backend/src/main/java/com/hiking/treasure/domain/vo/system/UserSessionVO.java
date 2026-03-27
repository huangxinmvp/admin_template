package com.hiking.treasure.domain.vo.system;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserSessionVO {
    private String id;
    private String tenantId;
    private String userId;
    private String clientIp;
    private String userAgent;
    private String deviceName;
    private LocalDateTime loginTime;
    private LocalDateTime lastActiveTime;
    private LocalDateTime accessExpiresAt;
    private LocalDateTime refreshExpiresAt;
    private Integer status;
    private String logoutReason;
    private String logoutBy;
    private LocalDateTime logoutTime;
}
