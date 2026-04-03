package com.hiking.treasure.domain.vo.system;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DashboardAttentionProjectVO {
    private String projectId;
    private String projectCode;
    private String projectName;
    private String currentStageCode;
    private String currentStageName;
    private String riskLevel;
    private String blockingReason;
    private String budgetStatus;
    private Integer pendingApprovalCount;
    private Integer pendingDecisionCount;
    private LocalDateTime updateTime;
}
