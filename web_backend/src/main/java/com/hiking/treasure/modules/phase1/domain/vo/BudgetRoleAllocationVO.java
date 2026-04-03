package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 预算中心 - 角色预算分配")
public class BudgetRoleAllocationVO {

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "分配金额")
    private Long amount;

    @Schema(description = "占比")
    private Integer sharePercent;
}
