package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AICoOS 预算计划 - VO")
public class BudgetPlanVO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "预算计划名称")
    private String planName;

    @Schema(description = "币种")
    private String currencyCode;

    @Schema(description = "提议金额")
    private Long proposedAmount;

    @Schema(description = "批准金额")
    private Long approvedAmount;

    @Schema(description = "预留金额")
    private Long reservedAmount;

    @Schema(description = "已消耗金额")
    private Long consumedAmount;

    @Schema(description = "角色预算分配JSON")
    private String roleAllocationsJson;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "生效时间")
    private LocalDateTime effectiveAt;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
