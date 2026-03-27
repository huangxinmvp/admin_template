package com.hiking.treasure.domain.dto.create;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户表 - 新增DTO" )
public class UserCreateDTO {
    @Schema(description = "租户ID" )
    private String tenantId;
    @Schema(description = "用户名(登录账户)" )
    private String username;
    @Schema(description = "真实姓名" )
    private String realname;
    @Schema(description = "密码(明文入参，服务端加密存储)" )
    private String password;
    @Schema(description = "密码盐(可选)" )
    private String salt;
    @Schema(description = "头像" )
    private String avatar;
    @Schema(description = "邮箱" )
    private String email;
    @Schema(description = "手机号" )
    private String phone;
    @Schema(description = "状态: 1正常 0冻结" )
    private Integer status;
    @Schema(description = "锁定到期时间" )
    private LocalDateTime lockUntil;
    @Schema(description = "备注" )
    private String remark;
    @Schema(description = "所属部门编码(数据权限)" )
    private String sysOrgCode;
    @Schema(description = "角色ID集合" )
    private List<String> roleIds;
    @Schema(description = "部门ID集合" )
    private List<String> departIds;
}
