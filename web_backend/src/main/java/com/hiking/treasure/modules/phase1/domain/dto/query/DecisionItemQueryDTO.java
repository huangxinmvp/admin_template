package com.hiking.treasure.modules.phase1.domain.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 决策事项 - 查询DTO")
public class DecisionItemQueryDTO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "项目阶段ID")
    private String projectStageId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "事项类型")
    private String itemType;

    @Schema(description = "来源类型")
    private String sourceType;

    @Schema(description = "是否阻塞")
    private Integer blockerFlag;

    @Schema(description = "优先级")
    private String priority;

    @Schema(description = "事项状态")
    private String status;

    @Schema(description = "处理人用户ID")
    private String assigneeUserId;
}
