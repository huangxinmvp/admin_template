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
 * 定时任务
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("sys_quartz_job")
public class QuartzJob implements Serializable {

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
     * 任务名称
     */
    @TableField("job_name")
    private String jobName;

    /**
     * 任务组
     */
    @TableField("job_group")
    private String jobGroup;

    /**
     * 调用目标(类#方法/Bean#方法/HTTP等)
     */
    @TableField("invoke_target")
    private String invokeTarget;

    /**
     * Cron表达式
     */
    @TableField("cron_expression")
    private String cronExpression;

    /**
     * misfire策略:SMART/IGNORE/MISFIRE
     */
    @TableField("misfire_policy")
    private String misfirePolicy;

    /**
     * 是否并发:0否 1是
     */
    @TableField("concurrent")
    private Integer concurrent;

    /**
     * 状态:1启用 0停用
     */
    @TableField("status")
    private Integer status;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    @TableField("last_run_time")
    private LocalDateTime lastRunTime;

    @TableField("next_run_time")
    private LocalDateTime nextRunTime;

    @TableField("run_count")
    private Long runCount;

    @TableField("last_status")
    private Integer lastStatus;

    @TableField("last_message")
    private String lastMessage;

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
