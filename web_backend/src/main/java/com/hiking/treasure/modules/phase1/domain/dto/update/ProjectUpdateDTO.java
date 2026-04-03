package com.hiking.treasure.modules.phase1.domain.dto.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 项目 - 更新DTO")
public class ProjectUpdateDTO {

    @Schema(description = "项目编码")
    private String projectCode;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "项目类型")
    private String projectType;

    @Schema(description = "需求摘要")
    private String intakeSummary;

    @Schema(description = "当前阶段编码")
    private String currentStageCode;

    @Schema(description = "项目状态")
    private String status;

    @Schema(description = "风险等级")
    private String riskLevel;

    @Schema(description = "负责人用户ID")
    private String ownerUserId;

    @Schema(description = "工作流模板ID")
    private String workflowTemplateId;

    @Schema(description = "备注")
    private String remark;
}
