package com.hiking.treasure.modules.phase1.domain.dto.action;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 审批事项 - 动作请求DTO")
public class ApprovalActionRequestDTO {

    @Schema(description = "动作类型")
    private String actionType;

    @Schema(description = "动作说明")
    private String comment;
}
