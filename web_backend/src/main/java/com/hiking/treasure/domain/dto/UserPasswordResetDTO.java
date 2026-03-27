package com.hiking.treasure.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserPasswordResetDTO {
    @NotBlank(message = "新密码不能为空")
    private String password;
}
