package com.hiking.treasure.domain.vo.system;

import lombok.Data;

import java.util.List;

@Data
public class CurrentUserProfileVO {
    private String userId;
    private String tenantId;
    private String tenantCode;
    private String tenantName;
    private String username;
    private String realname;
    private String avatar;
    private String email;
    private String phone;
    private List<String> roles;
    private List<String> depts;
    private List<String> permissions;
}
