package com.hiking.treasure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldFill;
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
 * 通告-用户关系
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("sys_announcement_send")
public class AnnouncementSend implements Serializable {

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
     * 通告ID
     */
    @TableField("annt_id")
    private String anntId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 是否已读:0未读 1已读
     */
    @TableField("read_flag")
    private Integer readFlag;

    /**
     * 阅读时间
     */
    @TableField("read_time")
    private LocalDateTime readTime;
}
