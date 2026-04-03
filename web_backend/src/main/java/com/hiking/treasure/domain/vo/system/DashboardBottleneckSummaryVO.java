package com.hiking.treasure.domain.vo.system;

import lombok.Data;

@Data
public class DashboardBottleneckSummaryVO {
    private long blockerClarificationProjectCount;
    private long pendingDecisionProjectCount;
    private long blockingApprovalProjectCount;
    private long budgetWarningOrExceededProjectCount;
    private long missingCriticalRoleProjectCount;
    private long failedGateConditionProjectCount;
}
