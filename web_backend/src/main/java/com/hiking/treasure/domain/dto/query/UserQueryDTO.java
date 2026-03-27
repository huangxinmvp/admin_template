package com.hiking.treasure.domain.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户表 - 查询DTO" )
public class UserQueryDTO {
    @Schema(description = "主键ID" )
    private String id;
    @Schema(description = "租户ID" )
    private String tenantId;
    @Schema(description = "用户名(登录账户)" )
    private String username;
    @Schema(description = "真实姓名" )
    private String realname;
    @Schema(description = "头像" )
    private String avatar;
    @Schema(description = "邮箱" )
    private String email;
    @Schema(description = "手机号" )
    private String phone;
    @Schema(description = "状态: 1正常 0冻结" )
    private Integer status;
    @Schema(description = "删除状态: 0正常 1删除" )
    private Integer delFlag;
    @Schema(description = "备注" )
    private String remark;
    @Schema(description = "创建人" )
    private String createBy;
    @Schema(description = "创建时间" )
    private LocalDateTime createTime;
    @Schema(description = "更新人" )
    private String updateBy;
    @Schema(description = "更新时间" )
    private LocalDateTime updateTime;
    @Schema(description = "所属部门编码(数据权限)" )
    private String sysOrgCode;
}
