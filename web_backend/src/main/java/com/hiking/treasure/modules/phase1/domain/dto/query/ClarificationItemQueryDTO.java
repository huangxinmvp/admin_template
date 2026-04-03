package com.hiking.treasure.modules.phase1.domain.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 澄清项 - 查询DTO")
public class ClarificationItemQueryDTO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "严重等级")
    private String severity;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "是否为生成项")
    private Integer generatedFlag;
}
