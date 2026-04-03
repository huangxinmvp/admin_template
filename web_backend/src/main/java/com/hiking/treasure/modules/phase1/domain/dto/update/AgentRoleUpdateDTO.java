package com.hiking.treasure.modules.phase1.domain.dto.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "AICoOS Agent 角色 - 更新DTO")
public class AgentRoleUpdateDTO {

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色分类")
    private String roleCategory;

    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "职责摘要")
    private String capabilitySummary;

    @Schema(description = "是否默认")
    private Integer defaultFlag;

    @Schema(description = "预算系数")
    private BigDecimal budgetFactor;

    @Schema(description = "审批协作标记")
    private Integer approvalCollaborationFlag;

    @Schema(description = "是否需要审批")
    private Integer approvalRequired;

    @Schema(description = "最大并发数")
    private Integer maxConcurrency;

    @Schema(description = "备注")
    private String remark;
}
