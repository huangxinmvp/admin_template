package com.hiking.treasure.modules.phase1.domain.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS Agent 角色阶段参与配置 - 输入 DTO")
public class AgentRoleStageParticipationInputDTO {

    @Schema(description = "阶段编码")
    private String stageCode;

    @Schema(description = "阶段名称")
    private String stageName;

    @Schema(description = "参与类型")
    private String participationType;

    @Schema(description = "备注")
    private String note;
}
