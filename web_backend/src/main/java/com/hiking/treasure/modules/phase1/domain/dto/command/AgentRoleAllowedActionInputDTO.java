package com.hiking.treasure.modules.phase1.domain.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS Agent 角色允许动作配置 - 输入 DTO")
public class AgentRoleAllowedActionInputDTO {

    @Schema(description = "动作编码")
    private String actionCode;

    @Schema(description = "动作名称")
    private String actionName;

    @Schema(description = "是否允许")
    private Integer allowedFlag;

    @Schema(description = "是否需要审批")
    private Integer approvalRequiredFlag;

    @Schema(description = "备注")
    private String note;
}
