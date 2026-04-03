package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS Agent 角色阶段参与配置 - VO")
public class AgentRoleStageParticipationVO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "角色ID")
    private String agentRoleId;

    @Schema(description = "阶段编码")
    private String stageCode;

    @Schema(description = "阶段名称")
    private String stageName;

    @Schema(description = "参与类型")
    private String participationType;

    @Schema(description = "备注")
    private String note;
}
