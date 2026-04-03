package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 工作流模板阶段配置 - VO")
public class WorkflowTemplateStageConfigVO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "模板ID")
    private String templateId;

    @Schema(description = "阶段编码")
    private String stageCode;

    @Schema(description = "阶段名称")
    private String stageName;

    @Schema(description = "阶段顺序")
    private Integer stageOrder;

    @Schema(description = "是否启用")
    private Integer enabledFlag;

    @Schema(description = "阶段描述")
    private String stageDescription;

    @Schema(description = "阶段备注")
    private String stageNote;
}
