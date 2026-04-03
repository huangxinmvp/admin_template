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
@TableName("ai_workflow_template")
public class WorkflowTemplate implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    @TableField("template_code")
    private String templateCode;

    @TableField("template_name")
    private String templateName;

    @TableField("project_type")
    private String projectType;

    @TableField("version_no")
    private Integer versionNo;

    @TableField("stage_count")
    private Integer stageCount;

    @TableField("status")
    private String status;

    @TableField("default_flag")
    private Integer defaultFlag;

    @TableField("description")
    private String description;

    @TableField("gate_checks_config")
    private String gateChecksConfig;

    @TableField("blocking_decision_config")
    private String blockingDecisionConfig;

    @TableField("blocking_approval_config")
    private String blockingApprovalConfig;

    @TableField("budget_threshold_config")
    private String budgetThresholdConfig;

    @TableField("high_risk_approval_config")
    private String highRiskApprovalConfig;

    @TableField("remark")
    private String remark;

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
