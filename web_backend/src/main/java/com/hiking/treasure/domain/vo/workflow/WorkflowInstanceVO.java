package com.hiking.treasure.domain.vo.workflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "流程实例 - VO")
public class WorkflowInstanceVO {
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

    @Schema(description = "发起人ID")
    private String startUserId;

    @Schema(description = "发起时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "是否已结束")
    private Boolean finished;

    @Schema(description = "是否挂起")
    private Boolean suspended;

    @Schema(description = "当前活动任务ID")
    private String currentTaskId;

    @Schema(description = "当前活动任务名称")
    private String currentTaskName;
}
