package com.hiking.treasure.modules.phase1.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("ai_project_governance_state")
public class ProjectGovernanceState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    @TableField("project_id")
    private String projectId;

    @TableField("current_stage_code")
    private String currentStageCode;

    @TableField("current_stage_name")
    private String currentStageName;

    @TableField("current_stage_status")
    private String currentStageStatus;

    @TableField("current_gate_status")
    private String currentGateStatus;

    @TableField("governance_status")
    private String governanceStatus;

    @TableField("blocked_flag")
    private Integer blockedFlag;

    @TableField("at_risk_flag")
    private Integer atRiskFlag;

    @TableField("pending_decision_count")
    private Integer pendingDecisionCount;

    @TableField("blocker_decision_count")
    private Integer blockerDecisionCount;

    @TableField("pending_approval_count")
    private Integer pendingApprovalCount;

    @TableField("blocker_approval_count")
    private Integer blockerApprovalCount;

    @TableField("clarification_count")
    private Integer clarificationCount;

    @TableField("blocker_clarification_count")
    private Integer blockerClarificationCount;

    @TableField("failed_gate_condition_count")
    private Integer failedGateConditionCount;

    @TableField("blocking_gate_condition_count")
    private Integer blockingGateConditionCount;

    @TableField("missing_critical_role_count")
    private Integer missingCriticalRoleCount;

    @TableField("budget_status")
    private String budgetStatus;

    @TableField("blocker_reason_summary")
    private String blockerReasonSummary;

    @TableField("last_recomputed_at")
    private LocalDateTime lastRecomputedAt;

    @TableLogic
    @TableField(value = "del_flag", fill = FieldFill.INSERT)
    private Integer delFlag;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
