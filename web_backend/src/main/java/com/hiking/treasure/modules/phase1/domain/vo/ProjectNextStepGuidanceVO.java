package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "AICoOS 项目中心 - 下一步建议")
public class ProjectNextStepGuidanceVO {

    @Schema(description = "建议下一步")
    private String recommendedNextStep;

    @Schema(description = "建议原因")
    private String recommendedReason;

    @Schema(description = "当前阻塞项")
    private List<String> currentBlockers;

    @Schema(description = "建议执行角色")
    private String recommendedActorRole;

    @Schema(description = "建议目标模块")
    private String recommendedTargetModule;

    @Schema(description = "建议优先级")
    private String recommendedPriority;

    @Schema(description = "优先动作")
    private List<ProjectNextStepActionVO> priorityActions;
}
