package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "AICoOS 工作流模板中心 - 详情")
public class WorkflowTemplateCenterDetailVO {

    @Schema(description = "模板ID")
    private String id;

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "项目类型")
    private String projectType;

    @Schema(description = "版本号")
    private Integer versionNo;

    @Schema(description = "总阶段数")
    private Integer stageCount;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "是否默认")
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

    @Schema(description = "阶段配置")
    private List<WorkflowTemplateStageConfigVO> stages;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
