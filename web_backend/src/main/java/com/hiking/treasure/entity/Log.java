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
 * 系统日志
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
@Getter
@Setter
@ToString
@TableName("sys_log")
@Accessors(chain = true)
public class Log implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 日志类型:1登录 2操作
     */
    @TableField("log_type")
    private Integer logType;

    /**
     * 日志内容
     */
    @TableField("log_content")
    private String logContent;

    /**
     * 操作类型
     */
    @TableField("operate_type")
    private Integer operateType;

    /**
     * 用户ID
     */
    @TableField("userid")
    private String userid;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * IP
     */
    @TableField("ip")
    private String ip;

    /**
     * 请求方法
     */
    @TableField("method")
    private String method;

    /**
     * 请求路径
     */
    @TableField("request_url")
    private String requestUrl;

    /**
     * 请求类型
     */
    @TableField("request_type")
    private String requestType;

    /**
     * 请求参数
     */
    @TableField("request_param")
    private String requestParam;

    /**
     * 耗时(毫秒)
     */
    @TableField("cost_time")
    private Long costTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
