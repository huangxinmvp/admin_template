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
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("ai_agent_role")
public class AgentRole implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    @TableField("role_code")
    private String roleCode;

    @TableField("role_name")
    private String roleName;

    @TableField("role_category")
    private String roleCategory;

    @TableField("description")
    private String description;

    @TableField("status")
    private String status;

    @TableField("capability_summary")
    private String capabilitySummary;

    @TableField("default_flag")
    private Integer defaultFlag;

    @TableField("budget_factor")
    private BigDecimal budgetFactor;

    @TableField("approval_collaboration_flag")
    private Integer approvalCollaborationFlag;

    @TableField("approval_required")
    private Integer approvalRequired;

    @TableField("max_concurrency")
    private Integer maxConcurrency;

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
