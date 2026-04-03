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
@TableName("ai_approval_record")
public class ApprovalRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    @TableField("project_id")
    private String projectId;

    @TableField("title")
    private String title;

    @TableField("decision_item_id")
    private String decisionItemId;

    @TableField("approval_type")
    private String approvalType;

    @TableField("source_object_type")
    private String sourceObjectType;

    @TableField("source_object_id")
    private String sourceObjectId;

    @TableField("requester_user_id")
    private String requesterUserId;

    @TableField("approver_user_id")
    private String approverUserId;

    @TableField("description")
    private String description;

    @TableField("risk_summary")
    private String riskSummary;

    @TableField("budget_impact_summary")
    private String budgetImpactSummary;

    @TableField("recommended_action")
    private String recommendedAction;

    @TableField("blocker_flag")
    private Integer blockerFlag;

    @TableField("approval_status")
    private String approvalStatus;

    @TableField("operator_user_id")
    private String operatorUserId;

    @TableField("submitted_at")
    private LocalDateTime submittedAt;

    @TableField("decided_at")
    private LocalDateTime decidedAt;

    @TableField("decision_note")
    private String decisionNote;

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
