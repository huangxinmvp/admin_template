package com.hiking.treasure.modules.phase1.domain.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 审批记录 - 查询DTO")
public class ApprovalRecordQueryDTO {

    @Schema(description = "主键ID")
    private String id;

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

    @Schema(description = "审批状态")
    private String approvalStatus;

    @Schema(description = "是否阻塞")
    private Integer blockerFlag;
}
