package com.hiking.treasure.domain.vo.workflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "流程定义 - VO")
public class WorkflowDefinitionVO {
    @Schema(description = "流程定义ID")
    private String id;

    @Schema(description = "流程定义Key")
    private String key;

    @Schema(description = "流程定义名称")
    private String name;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "部署ID")
    private String deploymentId;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "BPMN资源名")
    private String resourceName;

    @Schema(description = "流程图资源名")
    private String diagramResourceName;

    @Schema(description = "是否挂起")
    private Boolean suspended;
}
