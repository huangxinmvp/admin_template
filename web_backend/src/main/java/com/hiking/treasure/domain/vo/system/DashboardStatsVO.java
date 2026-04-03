package com.hiking.treasure.domain.vo.system;

import lombok.Data;

import java.util.List;

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

    private DashboardOverviewVO overview;
    private List<DashboardStageDistributionVO> stageDistribution;
    private DashboardBottleneckSummaryVO bottleneckSummary;
    private DashboardBudgetHealthVO budgetHealth;
    private List<DashboardAttentionProjectVO> attentionProjects;
    private List<DashboardRecentActivityVO> recentGovernanceActivities;
}
