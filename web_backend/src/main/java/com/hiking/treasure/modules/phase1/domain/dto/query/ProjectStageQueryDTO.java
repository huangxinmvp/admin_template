package com.hiking.treasure.modules.phase1.domain.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 项目阶段 - 查询DTO")
public class ProjectStageQueryDTO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "阶段编码")
    private String stageCode;

    @Schema(description = "阶段名称")
    private String stageName;

    @Schema(description = "阶段状态")
    private String stageStatus;

    @Schema(description = "门禁状态")
    private String gateStatus;

    @Schema(description = "阶段负责人用户ID")
    private String ownerUserId;
}
