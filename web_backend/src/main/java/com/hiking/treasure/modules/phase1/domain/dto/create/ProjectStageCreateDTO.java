package com.hiking.treasure.modules.phase1.domain.dto.create;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AICoOS 项目阶段 - 新增DTO")
public class ProjectStageCreateDTO {

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "阶段编码")
    private String stageCode;

    @Schema(description = "阶段名称")
    private String stageName;

    @Schema(description = "阶段顺序")
    private Integer stageOrder;

    @Schema(description = "阶段状态")
    private String stageStatus;

    @Schema(description = "门禁状态")
    private String gateStatus;

    @Schema(description = "阶段负责人用户ID")
    private String ownerUserId;

    @Schema(description = "开始时间")
    private LocalDateTime startedAt;

    @Schema(description = "结束时间")
    private LocalDateTime endedAt;

    @Schema(description = "备注")
    private String remark;
}
