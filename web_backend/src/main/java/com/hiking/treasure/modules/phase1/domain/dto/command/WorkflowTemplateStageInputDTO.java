package com.hiking.treasure.modules.phase1.domain.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 工作流模板阶段配置 - 输入 DTO")
public class WorkflowTemplateStageInputDTO {

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
