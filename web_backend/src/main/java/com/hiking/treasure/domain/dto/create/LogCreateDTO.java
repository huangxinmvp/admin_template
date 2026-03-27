package com.hiking.treasure.domain.dto.create;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "系统日志 - 新增DTO" )
public class LogCreateDTO {
    @Schema(description = "日志类型:1登录 2操作" )
    private Integer logType;
    @Schema(description = "日志内容" )
    private String logContent;
    @Schema(description = "操作类型" )
    private Integer operateType;
    @Schema(description = "用户ID" )
    private String userid;
    @Schema(description = "用户名" )
    private String username;
    @Schema(description = "IP" )
    private String ip;
    @Schema(description = "请求方法" )
    private String method;
    @Schema(description = "请求路径" )
    private String requestUrl;
    @Schema(description = "请求类型" )
    private String requestType;
    @Schema(description = "请求参数" )
    private String requestParam;
    @Schema(description = "耗时(毫秒)" )
    private Long costTime;
}
