package com.hiking.treasure.domain.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "用户聚合详情")
public class UserDetailVO {
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "锁定到期时间")
    private LocalDateTime lockUntil;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "所属部门编码")
    private String sysOrgCode;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "角色ID列表")
    private List<String> roleIds;

    @Schema(description = "角色编码列表")
    private List<String> roleCodes;

    @Schema(description = "角色名称列表")
    private List<String> roleNames;

    @Schema(description = "部门ID列表")
    private List<String> departIds;

    @Schema(description = "部门名称列表")
    private List<String> departNames;
}
