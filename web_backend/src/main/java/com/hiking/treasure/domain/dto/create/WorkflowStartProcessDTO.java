package com.hiking.treasure.domain.dto.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "流程实例 - 发起DTO")
public class WorkflowStartProcessDTO {
    @NotBlank
    @Schema(description = "流程定义Key", example = "leaveApproval")
    private String definitionKey;

    @Schema(description = "业务Key", example = "LEAVE-20260320-0001")
    private String businessKey;

    @Schema(description = "流程标题/摘要", example = "张三请假申请")
    private String title;

    @Schema(description = "审批人用户名，示例流程 leaveApproval 需要传入", example = "admin")
    private String approver;

    @Schema(description = "流程变量")
    private Map<String, Object> variables;
}
