package com.hiking.treasure.domain.dto.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "定时任务 - 更新DTO" )
public class QuartzJobUpdateDTO {
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
}
