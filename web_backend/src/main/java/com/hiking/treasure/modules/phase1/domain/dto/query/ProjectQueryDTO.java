package com.hiking.treasure.modules.phase1.domain.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 项目 - 查询DTO")
public class ProjectQueryDTO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "项目编码")
    private String projectCode;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "项目类型")
    private String projectType;

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
}
