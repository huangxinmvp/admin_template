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
 * 系统通告
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("sys_announcement")
public class Announcement implements Serializable {

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
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 内容
     */
    @TableField("msg_content")
    private String msgContent;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 发布人
     */
    @TableField("sender")
    private String sender;

    /**
     * 优先级:1低 2中 3高
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 类别:1通知 2系统消息
     */
    @TableField("msg_category")
    private Integer msgCategory;

    /**
     * 类型:1通知公告 2系统消息
     */
    @TableField("msg_type")
    private Integer msgType;

    @TableField("receiver_scope")
    private String receiverScope;

    @TableField("target_tenant_id")
    private String targetTenantId;

    /**
     * 发布状态:0未发 1已发 2撤销
     */
    @TableField("send_status")
    private Integer sendStatus;

    /**
     * 发布时间
     */
    @TableField("send_time")
    private LocalDateTime sendTime;

    /**
     * 撤销时间
     */
    @TableField("cancel_time")
    private LocalDateTime cancelTime;

    /**
     * 删除状态
     */
    @TableLogic
    @TableField(value = "del_flag", fill = FieldFill.INSERT)
    private Integer delFlag;

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
