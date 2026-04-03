package com.hiking.treasure.modules.phase1.domain.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 预算中心 - 查询DTO")
public class BudgetCenterQueryDTO {

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "关键字，匹配项目名称或项目编码")
    private String keyword;

    @Schema(description = "项目类型")
    private String projectType;

    @Schema(description = "预算健康状态")
    private String budgetStatus;
}
