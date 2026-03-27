package com.hiking.treasure.domain.vo.system;

import lombok.Data;

@Data
public class DashboardStatsVO {
    private long tenantCount;
    private long userCount;
    private long enabledUserCount;
    private long roleCount;
    private long permissionCount;
    private long departCount;
    private long announcementCount;
    private long quartzJobCount;
}
