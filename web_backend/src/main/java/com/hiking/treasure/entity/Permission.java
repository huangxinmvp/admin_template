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

/**
 * <p>
 * 权限表(菜单/按钮)
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("sys_permission")
public class Permission implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    /**
     * 父ID(顶级为空)
     */
    @TableField("parent_id")
    private String parentId;

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 路由/接口地址
     */
    @TableField("url")
    private String url;

    /**
     * 前端组件
     */
    @TableField("component")
    private String component;

    @TableField("scope_type")
    private String scopeType;

    @TableField("built_in")
    private Integer builtIn;

    /**
     * 权限标识(如: user:list)
     */
    @TableField("perms")
    private String perms;

    /**
     * 类型:0目录 1菜单 2按钮
     */
    @TableField("type")
    private Integer type;

    /**
     * 图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 排序
     */
    @TableField("sort_no")
    private Integer sortNo;

    /**
     * 是否隐藏:0否 1是
     */
    @TableField("hidden")
    private Integer hidden;

    /**
     * 始终显示目录
     */
    @TableField("always_show")
    private Integer alwaysShow;

    /**
     * 状态:1启用 0停用
     */
    @TableField("status")
    private Integer status;

    /**
     * 删除状态:0正常 1删除
     */
    @TableLogic
    @TableField(value = "del_flag", fill = FieldFill.INSERT)
    private Integer delFlag;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建人
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
