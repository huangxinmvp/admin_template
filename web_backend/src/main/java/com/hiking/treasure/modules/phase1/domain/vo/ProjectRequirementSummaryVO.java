package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "AICoOS 项目中心 - 需求接收汇总")
public class ProjectRequirementSummaryVO {

    @Schema(description = "需求接收记录ID")
    private String requirementIntakeId;

    @Schema(description = "是否已有需求接收记录")
    private Boolean hasRequirementIntake;

    @Schema(description = "需求完整度")
    private Integer completenessScore;

    @Schema(description = "已填写核心字段数")
    private Integer completedCoreFieldCount;

    @Schema(description = "核心字段总数")
    private Integer totalCoreFieldCount;

    @Schema(description = "澄清项总数")
    private Integer clarificationCount;

    @Schema(description = "待处理澄清项数")
    private Integer openClarificationCount;

    @Schema(description = "阻塞澄清项数")
    private Integer blockerCount;

    @Schema(description = "缺失核心字段")
    private List<String> missingCoreFields;

    @Schema(description = "需求接收更新时间")
    private LocalDateTime lastIntakeUpdatedAt;
}
