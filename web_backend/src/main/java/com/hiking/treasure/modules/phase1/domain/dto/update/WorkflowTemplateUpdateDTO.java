package com.hiking.treasure.modules.phase1.domain.dto.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 工作流模板 - 更新DTO")
public class WorkflowTemplateUpdateDTO {

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "项目类型")
    private String projectType;

    @Schema(description = "版本号")
    private Integer versionNo;

    @Schema(description = "阶段数量")
    private Integer stageCount;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "是否默认模板")
    private Integer defaultFlag;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "Gate 检查配置")
    private String gateChecksConfig;

    @Schema(description = "阻塞决策条件")
    private String blockingDecisionConfig;

    @Schema(description = "阻塞审批条件")
    private String blockingApprovalConfig;

    @Schema(description = "预算阈值条件")
    private String budgetThresholdConfig;

    @Schema(description = "高风险动作审批要求")
    private String highRiskApprovalConfig;

    @Schema(description = "备注")
    private String remark;
}
