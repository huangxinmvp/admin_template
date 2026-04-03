package com.hiking.treasure.domain.vo.system;

import lombok.Data;

@Data
public class DashboardBudgetHealthVO {
    private long unplannedProjectCount;
    private long pendingProjectCount;
    private long healthyProjectCount;
    private long warningProjectCount;
    private long overrunProjectCount;
}
