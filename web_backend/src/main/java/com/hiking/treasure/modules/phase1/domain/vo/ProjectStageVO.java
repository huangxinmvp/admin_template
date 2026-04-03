package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AICoOS 项目阶段 - VO")
public class ProjectStageVO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "租户ID")
    private String tenantId;

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

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
