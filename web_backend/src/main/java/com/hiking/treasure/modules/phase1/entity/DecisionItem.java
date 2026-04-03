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
@TableName("ai_decision_item")
public class DecisionItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    @TableField("project_id")
    private String projectId;

    @TableField("project_stage_id")
    private String projectStageId;

    @TableField("title")
    private String title;

    @TableField("item_type")
    private String itemType;

    @TableField("source_type")
    private String sourceType;

    @TableField("source_id")
    private String sourceId;

    @TableField("description")
    private String description;

    @TableField("impact_summary")
    private String impactSummary;

    @TableField("suggested_options")
    private String suggestedOptions;

    @TableField("recommended_option")
    private String recommendedOption;

    @TableField("budget_impact_summary")
    private String budgetImpactSummary;

    @TableField("project_impact_summary")
    private String projectImpactSummary;

    @TableField("blocker_flag")
    private Integer blockerFlag;

    @TableField("priority")
    private String priority;

    @TableField("status")
    private String status;

    @TableField("requested_by_user_id")
    private String requestedByUserId;

    @TableField("assignee_user_id")
    private String assigneeUserId;

    @TableField("due_at")
    private LocalDateTime dueAt;

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
