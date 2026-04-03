package com.hiking.treasure.modules.phase1.domain.dto.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AICoOS 审批记录 - 更新DTO")
public class ApprovalRecordUpdateDTO {

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "审批标题")
    private String title;

    @Schema(description = "决策事项ID")
    private String decisionItemId;

    @Schema(description = "审批类型")
    private String approvalType;

    @Schema(description = "来源对象类型")
    private String sourceObjectType;

    @Schema(description = "来源对象ID")
    private String sourceObjectId;

    @Schema(description = "发起人用户ID")
    private String requesterUserId;

    @Schema(description = "审批人用户ID")
    private String approverUserId;

    @Schema(description = "背景说明")
    private String description;

    @Schema(description = "风险摘要")
    private String riskSummary;

    @Schema(description = "预算影响摘要")
    private String budgetImpactSummary;

    @Schema(description = "推荐动作")
    private String recommendedAction;

    @Schema(description = "是否阻塞")
    private Integer blockerFlag;

    @Schema(description = "审批状态")
    private String approvalStatus;

    @Schema(description = "最近操作人用户ID")
    private String operatorUserId;

    @Schema(description = "提交时间")
    private LocalDateTime submittedAt;

    @Schema(description = "决策时间")
    private LocalDateTime decidedAt;

    @Schema(description = "决策说明")
    private String decisionNote;

    @Schema(description = "备注")
    private String remark;
}
