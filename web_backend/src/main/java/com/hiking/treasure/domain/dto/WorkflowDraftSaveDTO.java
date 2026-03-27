package com.hiking.treasure.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "流程设计草稿保存 DTO")
public class WorkflowDraftSaveDTO {

    @Schema(description = "草稿ID，存在则更新")
    private String id;

    @NotBlank(message = "草稿名称不能为空")
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

    @NotBlank(message = "BPMN XML 不能为空")
    @Schema(description = "BPMN XML 内容")
    private String bpmnXml;

    @Schema(description = "备注")
    private String remark;
}
