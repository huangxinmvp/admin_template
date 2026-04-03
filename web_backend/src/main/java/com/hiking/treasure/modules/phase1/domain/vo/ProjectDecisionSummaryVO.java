package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 项目中心 - 决策事项汇总")
public class ProjectDecisionSummaryVO {

    @Schema(description = "待处理事项数")
    private Integer pendingCount;

    @Schema(description = "开放事项数")
    private Integer openCount;

    @Schema(description = "阻塞决策事项数")
    private Integer blockerCount;

    @Schema(description = "已解决事项数")
    private Integer resolvedCount;
}
