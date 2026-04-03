package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AICoOS 工作流模板中心 - 列表项")
public class WorkflowTemplateCenterListVO {

    @Schema(description = "模板ID")
    private String id;

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "项目类型")
    private String projectType;

    @Schema(description = "版本号")
    private Integer versionNo;

    @Schema(description = "总阶段数")
    private Integer stageCount;

    @Schema(description = "启用阶段数")
    private Integer enabledStageCount;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "是否默认")
    private Integer defaultFlag;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
