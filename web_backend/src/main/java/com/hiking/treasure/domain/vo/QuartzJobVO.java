package com.hiking.treasure.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "定时任务 - VO" )
public class QuartzJobVO {
    @Schema(description = "主键ID" )
    private String id;
    @Schema(description = "任务名称" )
    private String jobName;
    @Schema(description = "任务组" )
    private String jobGroup;
    @Schema(description = "调用目标(类#方法/Bean#方法/HTTP等)" )
    private String invokeTarget;
    @Schema(description = "Cron表达式" )
    private String cronExpression;
    @Schema(description = "misfire策略:SMART/IGNORE/MISFIRE" )
    private String misfirePolicy;
    @Schema(description = "是否并发:0否 1是" )
    private Integer concurrent;
    @Schema(description = "状态:1启用 0停用" )
    private Integer status;
    @Schema(description = "描述" )
    private String description;
    @Schema(description = "创建人" )
    private String createBy;
    @Schema(description = "创建时间" )
    private LocalDateTime createTime;
    @Schema(description = "更新人" )
    private String updateBy;
    @Schema(description = "更新时间" )
    private LocalDateTime updateTime;
}
