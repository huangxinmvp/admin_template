package com.hiking.treasure.domain.vo.system;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DashboardRecentActivityVO {
    private String projectId;
    private String projectName;
    private String activityType;
    private String title;
    private String description;
    private String status;
    private LocalDateTime occurredAt;
}
