package com.hiking.treasure.entity;

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
@TableName("sys_system_config")
public class SystemConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("group_code")
    private String groupCode;

    @TableField("group_name")
    private String groupName;

    @TableField("group_sort")
    private Integer groupSort;

    @TableField("config_key")
    private String configKey;

    @TableField("config_name")
    private String configName;

    @TableField("config_value")
    private String configValue;

    @TableField("default_value")
    private String defaultValue;

    @TableField("value_type")
    private String valueType;

    @TableField("required_flag")
    private Integer requiredFlag;

    @TableField("placeholder")
    private String placeholder;

    @TableField("description")
    private String description;

    @TableField("options_json")
    private String optionsJson;

    @TableField("sort_no")
    private Integer sortNo;

    @TableField("built_in")
    private Integer builtIn;

    @TableField("status")
    private Integer status;

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
