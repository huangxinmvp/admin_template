package com.hiking.treasure.domain.dto.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "流程任务 - 办理DTO")
public class WorkflowTaskCompleteDTO {
    @Schema(description = "审批结果，示例流程 leaveApproval 会用到", example = "true")
    private Boolean approved;

    @Schema(description = "审批意见", example = "同意，请按时提交交接")
    private String comment;

    @Schema(description = "办理时追加的流程变量")
    private Map<String, Object> variables;
}
