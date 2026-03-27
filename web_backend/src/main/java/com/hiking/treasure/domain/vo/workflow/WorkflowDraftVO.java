package com.hiking.treasure.domain.vo.workflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "流程设计草稿 VO")
public class WorkflowDraftVO {

    @Schema(description = "草稿ID")
    private String id;

    @Schema(description = "草稿名称")
    private String draftName;

    @Schema(description = "流程定义 Key")
    private String processDefinitionKey;

    @Schema(description = "流程名称")
    private String processDefinitionName;

    @Schema(description = "来源流程定义 ID")
    private String sourceDefinitionId;

    @Schema(description = "来源流程定义 Key")
    private String sourceDefinitionKey;

    @Schema(description = "流程分类")
    private String category;

    @Schema(description = "BPMN XML 内容")
    private String bpmnXml;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
