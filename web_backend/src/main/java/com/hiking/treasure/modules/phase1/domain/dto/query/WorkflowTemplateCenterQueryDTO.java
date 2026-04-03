package com.hiking.treasure.modules.phase1.domain.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 工作流模板中心 - 查询 DTO")
public class WorkflowTemplateCenterQueryDTO {

    @Schema(description = "关键字")
    private String keyword;

    @Schema(description = "项目类型")
    private String projectType;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "默认模板标记")
    private Integer defaultFlag;
}
