package com.hiking.treasure.common.util;

import com.hiking.treasure.common.api.ErrorCode;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.config.PasswordPolicyConfig;
import com.hiking.treasure.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class PasswordPolicyValidator {

    private final PasswordPolicyConfig passwordPolicyConfig;
    private final SystemConfigService systemConfigService;

    private int minLength() {
        return systemConfigService.getInt(
                "security.password.minLength",
                passwordPolicyConfig.getMinLength()
        );
    }

    private int maxLength() {
        return systemConfigService.getInt(
                "security.password.maxLength",
                passwordPolicyConfig.getMaxLength()
        );
    }

    private boolean requireUppercase() {
        return Boolean.TRUE.equals(systemConfigService.getBoolean(
                "security.password.requireUppercase",
                passwordPolicyConfig.isRequireUppercase()
        ));
    }

    private boolean requireLowercase() {
        return Boolean.TRUE.equals(systemConfigService.getBoolean(
                "security.password.requireLowercase",
                passwordPolicyConfig.isRequireLowercase()
        ));
    }

    private boolean requireDigit() {
        return Boolean.TRUE.equals(systemConfigService.getBoolean(
                "security.password.requireDigit",
                passwordPolicyConfig.isRequireDigit()
        ));
    }

    private boolean requireSpecial() {
        return Boolean.TRUE.equals(systemConfigService.getBoolean(
                "security.password.requireSpecial",
                passwordPolicyConfig.isRequireSpecial()
        ));
    }

    public void validateOrThrow(String password) {
        if (!StringUtils.hasText(password)) {
            throw new BusinessException(ErrorCode.PASSWORD_TOO_WEAK, passwordRuleMessage());
        }
        if (password.length() < minLength() || password.length() > maxLength()) {
            throw new BusinessException(ErrorCode.PASSWORD_TOO_WEAK, passwordRuleMessage());
        }
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
        if ((requireUppercase() && !hasUpper)
                || (requireLowercase() && !hasLower)
                || (requireDigit() && !hasDigit)
                || (requireSpecial() && !hasSpecial)) {
            throw new BusinessException(ErrorCode.PASSWORD_TOO_WEAK, passwordRuleMessage());
        }
    }

    public String passwordRuleMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append("密码需为")
                .append(minLength())
                .append("-")
                .append(maxLength())
                .append("位");
        java.util.List<String> rules = new java.util.ArrayList<>();
        if (requireUppercase()) {
            rules.add("大写字母");
        }
        if (requireLowercase()) {
            rules.add("小写字母");
        }
        if (requireDigit()) {
            rules.add("数字");
        }
        if (requireSpecial()) {
            rules.add("特殊字符");
        }
        if (!rules.isEmpty()) {
            builder.append("，且同时包含").append(String.join("、", rules));
        }
        return builder.toString();
    }
}
