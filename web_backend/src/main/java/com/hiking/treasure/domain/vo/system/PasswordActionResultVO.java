package com.hiking.treasure.domain.vo.system;

import lombok.Data;

@Data
public class PasswordActionResultVO {
    private boolean reloginRequired;
    private String message;

    public static PasswordActionResultVO of(boolean reloginRequired, String message) {
        PasswordActionResultVO result = new PasswordActionResultVO();
        result.setReloginRequired(reloginRequired);
        result.setMessage(message);
        return result;
    }
}
