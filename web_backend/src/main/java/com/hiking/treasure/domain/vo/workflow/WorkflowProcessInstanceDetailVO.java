package com.hiking.treasure.domain.vo.workflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Schema(description = "流程实例详情 - VO")
public class WorkflowProcessInstanceDetailVO {
    @Schema(description = "流程实例基本信息")
    private WorkflowInstanceVO instance;

    @Schema(description = "当前活动任务列表")
    private List<WorkflowTaskVO> currentTasks;

    @Schema(description = "历史任务列表")
    private List<WorkflowTaskVO> historyTasks;

    @Schema(description = "流程变量")
    private Map<String, Object> variables;
}
