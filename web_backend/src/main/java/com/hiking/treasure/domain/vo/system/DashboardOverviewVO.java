package com.hiking.treasure.domain.vo.system;

import lombok.Data;

@Data
public class DashboardOverviewVO {
    private long totalProjectCount;
    private long activeProjectCount;
    private long pendingDecisionCount;
    private long pendingApprovalCount;
    private long blockedProjectCount;
    private long highRiskProjectCount;
    private long budgetWarningProjectCount;
    private long budgetExceededProjectCount;
}
