package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "AICoOS Agent 角色中心 - 列表项")
public class AgentRoleCenterListVO {

    @Schema(description = "角色ID")
    private String id;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色类型")
    private String roleCategory;

    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "是否默认")
    private Integer defaultFlag;

    @Schema(description = "预算系数")
    private BigDecimal budgetFactor;

    @Schema(description = "审批协作标记")
    private Integer approvalCollaborationFlag;

    @Schema(description = "启用阶段数")
    private Integer involvedStageCount;

    @Schema(description = "允许动作数")
    private Integer allowedActionCount;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
