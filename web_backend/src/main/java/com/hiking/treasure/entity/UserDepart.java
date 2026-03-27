package com.hiking.treasure.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * 用户-部门关系
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("sys_user_depart")
public class UserDepart implements Serializable {

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
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 部门ID
     */
    @TableField("depart_id")
    private String departId;

    /**
     * 关系类型:1主部门 2兼任
     */
    @TableField("rel_type")
    private Integer relType;

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
}
