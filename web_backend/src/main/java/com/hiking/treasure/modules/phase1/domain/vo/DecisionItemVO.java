package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AICoOS 决策事项 - VO")
public class DecisionItemVO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "租户ID")
    private String tenantId;

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

    @Schema(description = "来源ID")
    private String sourceId;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "影响摘要")
    private String impactSummary;

    @Schema(description = "建议选项")
    private String suggestedOptions;

    @Schema(description = "推荐选项")
    private String recommendedOption;

    @Schema(description = "预算影响摘要")
    private String budgetImpactSummary;

    @Schema(description = "项目影响摘要")
    private String projectImpactSummary;

    @Schema(description = "是否阻塞")
    private Integer blockerFlag;

    @Schema(description = "优先级")
    private String priority;

    @Schema(description = "事项状态")
    private String status;

    @Schema(description = "发起人用户ID")
    private String requestedByUserId;

    @Schema(description = "处理人用户ID")
    private String assigneeUserId;

    @Schema(description = "到期时间")
    private LocalDateTime dueAt;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
