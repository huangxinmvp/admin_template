package com.hiking.treasure.modules.phase1.domain.dto.create;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 需求接收 - 新增DTO")
public class RequirementIntakeCreateDTO {

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "项目类型")
    private String projectType;

    @Schema(description = "业务目标")
    private String businessGoal;

    @Schema(description = "功能摘要")
    private String featureSummary;

    @Schema(description = "参考产品或链接")
    private String referenceProducts;

    @Schema(description = "时间预期")
    private String timelineExpectation;

    @Schema(description = "预算范围")
    private String budgetRange;

    @Schema(description = "技术约束")
    private String technicalConstraints;

    @Schema(description = "补充说明")
    private String notes;

    @Schema(description = "附件占位")
    private String attachmentPlaceholders;
}
