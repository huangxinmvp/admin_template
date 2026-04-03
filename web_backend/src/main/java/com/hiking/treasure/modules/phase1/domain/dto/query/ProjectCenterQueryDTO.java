package com.hiking.treasure.modules.phase1.domain.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 项目中心 - 查询DTO")
public class ProjectCenterQueryDTO {

    @Schema(description = "关键字，匹配项目名称或项目编码")
    private String keyword;

    @Schema(description = "项目类型")
    private String projectType;

    @Schema(description = "当前阶段编码")
    private String currentStageCode;

    @Schema(description = "项目状态")
    private String status;

    @Schema(description = "风险等级")
    private String riskLevel;
}
