package com.hiking.treasure.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RegisterDTO {
    private String tenantId;
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String realname;
    private List<String> roles;
    private List<String> depts;
}
