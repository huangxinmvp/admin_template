package com.hiking.treasure.modules.phase1.domain.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 工作流模板 - 查询DTO")
public class WorkflowTemplateQueryDTO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "是否默认模板")
    private Integer defaultFlag;
}
