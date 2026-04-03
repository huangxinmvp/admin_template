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
@ToString(exclude = {"requestPayloadJson", "responsePayloadJson"})
@Accessors(chain = true)
@TableName("ai_tool_integration_audit")
public class ToolIntegrationAudit implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    @TableField("project_id")
    private String projectId;

    @TableField("tool_type")
    private String toolType;

    @TableField("action_type")
    private String actionType;

    @TableField("source_object_type")
    private String sourceObjectType;

    @TableField("source_object_id")
    private String sourceObjectId;

    @TableField("binding_id")
    private String bindingId;

    @TableField("preview_flag")
    private Integer previewFlag;

    @TableField("confirmed_flag")
    private Integer confirmedFlag;

    @TableField("audit_status")
    private String auditStatus;

    @TableField("request_payload_json")
    private String requestPayloadJson;

    @TableField("response_payload_json")
    private String responsePayloadJson;

    @TableField("external_object_id")
    private String externalObjectId;

    @TableField("external_object_url")
    private String externalObjectUrl;

    @TableField("operator_user_id")
    private String operatorUserId;

    @TableField("error_message")
    private String errorMessage;

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
