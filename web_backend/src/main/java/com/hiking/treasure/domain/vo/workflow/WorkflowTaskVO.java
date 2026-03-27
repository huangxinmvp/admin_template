package com.hiking.treasure.domain.vo.workflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "流程任务 - VO")
public class WorkflowTaskVO {
    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "任务定义Key")
    private String taskDefinitionKey;

    @Schema(description = "任务名称")
    private String name;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "流程实例ID")
    private String processInstanceId;

    @Schema(description = "流程定义ID")
    private String processDefinitionId;

    @Schema(description = "流程定义Key")
    private String processDefinitionKey;

    @Schema(description = "流程定义名称")
    private String processDefinitionName;

    @Schema(description = "业务Key")
    private String businessKey;

    @Schema(description = "办理人")
    private String assignee;

    @Schema(description = "所有者")
    private String owner;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "截止时间")
    private LocalDateTime dueDate;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "是否已完成")
    private Boolean finished;

    @Schema(description = "是否挂起")
    private Boolean suspended;
}
