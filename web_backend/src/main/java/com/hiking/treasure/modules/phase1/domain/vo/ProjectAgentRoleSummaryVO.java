package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "AICoOS 项目中心 - Agent 角色摘要")
public class ProjectAgentRoleSummaryVO {

    @Schema(description = "角色ID")
    private String id;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色类型")
    private String roleCategory;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "是否默认")
    private Integer defaultFlag;

    @Schema(description = "预算系数")
    private BigDecimal budgetFactor;

    @Schema(description = "审批协作标记")
    private Integer approvalCollaborationFlag;

    @Schema(description = "当前阶段参与类型")
    private String currentStageParticipationType;

    @Schema(description = "允许动作")
    private List<String> allowedActionCodes;
}
