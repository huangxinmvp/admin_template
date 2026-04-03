package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 项目中心 - 下一步建议动作")
public class ProjectNextStepActionVO {

    @Schema(description = "动作顺序")
    private Integer actionOrder;

    @Schema(description = "动作标签")
    private String actionLabel;

    @Schema(description = "目标模块")
    private String targetModule;

    @Schema(description = "推荐原因")
    private String recommendedReason;

    @Schema(description = "处理的阻塞或风险")
    private String addressedRisk;

    @Schema(description = "完成后的预期变化")
    private String expectedChange;
}
