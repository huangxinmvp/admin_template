package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 项目中心 - 审批汇总")
public class ProjectApprovalSummaryVO {

    @Schema(description = "待处理审批数")
    private Integer pendingCount;

    @Schema(description = "阻塞审批数")
    private Integer blockerCount;

    @Schema(description = "已批准审批数")
    private Integer approvedCount;

    @Schema(description = "已拒绝审批数")
    private Integer rejectedCount;
}
