package com.hiking.treasure.domain.vo.workflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "流程部署 - VO")
public class WorkflowDeploymentVO {
    @Schema(description = "部署ID")
    private String deploymentId;

    @Schema(description = "部署名称")
    private String name;

    @Schema(description = "部署分类")
    private String category;

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "部署时间")
    private LocalDateTime deploymentTime;

    @Schema(description = "资源列表")
    private List<String> resourceNames;

    @Schema(description = "本次部署包含的流程定义数量")
    private Long processDefinitionCount;
}
