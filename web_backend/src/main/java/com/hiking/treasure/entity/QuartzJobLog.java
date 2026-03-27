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
 * 定时任务执行日志
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("sys_quartz_job_log")
public class QuartzJobLog implements Serializable {

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
     * 任务ID
     */
    @TableField("job_id")
    private String jobId;

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
     * 调用目标
     */
    @TableField("invoke_target")
    private String invokeTarget;

    /**
     * Cron表达式
     */
    @TableField("cron_expression")
    private String cronExpression;

    /**
     * 执行状态:1成功 0失败
     */
    @TableField("status")
    private Integer status;

    /**
     * 异常信息
     */
    @TableField("exception_info")
    private String exceptionInfo;

    /**
     * 耗时(ms)
     */
    @TableField("cost_time")
    private Long costTime;

    @TableField("trigger_type")
    private String triggerType;

    /**
     * 执行时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
