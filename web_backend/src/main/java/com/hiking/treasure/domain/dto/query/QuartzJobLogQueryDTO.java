package com.hiking.treasure.domain.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "定时任务执行日志 - 查询DTO" )
public class QuartzJobLogQueryDTO {
    @Schema(description = "主键ID" )
    private String id;
    @Schema(description = "任务ID" )
    private String jobId;
    @Schema(description = "任务名称" )
    private String jobName;
    @Schema(description = "任务组" )
    private String jobGroup;
    @Schema(description = "调用目标" )
    private String invokeTarget;
    @Schema(description = "Cron表达式" )
    private String cronExpression;
    @Schema(description = "执行状态:1成功 0失败" )
    private Integer status;
    @Schema(description = "异常信息" )
    private String exceptionInfo;
    @Schema(description = "耗时(ms)" )
    private Long costTime;
    @Schema(description = "执行时间" )
    private LocalDateTime createTime;
}
