package com.hiking.treasure.modules.phase1.domain.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 预算计划 - 查询DTO")
public class BudgetPlanQueryDTO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "预算计划名称")
    private String planName;

    @Schema(description = "币种")
    private String currencyCode;

    @Schema(description = "状态")
    private String status;
}
