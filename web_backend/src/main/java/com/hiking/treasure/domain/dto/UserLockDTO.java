package com.hiking.treasure.domain.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserLockDTO {
    @NotNull(message = "锁定到期时间不能为空")
    @Future(message = "锁定到期时间必须晚于当前时间")
    private LocalDateTime lockUntil;
}
