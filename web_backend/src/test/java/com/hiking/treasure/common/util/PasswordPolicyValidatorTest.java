package com.hiking.treasure.common.util;

import com.hiking.treasure.common.api.ErrorCode;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.config.PasswordPolicyConfig;
import com.hiking.treasure.service.SystemConfigService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PasswordPolicyValidatorTest {

    private final PasswordPolicyValidator validator = createValidator(defaultConfig());

    @Test
    void acceptsStrongPassword() {
        assertDoesNotThrow(() -> validator.validateOrThrow("Admin@123456"));
    }

    @Test
    void rejectsWeakPassword() {
        BusinessException exception = assertThrows(BusinessException.class, () -> validator.validateOrThrow("weak"));

        assertEquals(ErrorCode.PASSWORD_TOO_WEAK.getKey(), exception.getErrorKey());
        assertEquals("密码需为8-32位，且同时包含大写字母、小写字母、数字、特殊字符", exception.getMessage());
    }

    @Test
    void supportsCustomPasswordPolicyConfig() {
        PasswordPolicyConfig config = new PasswordPolicyConfig();
        config.setMinLength(6);
        config.setMaxLength(12);
        config.setRequireUppercase(false);
        config.setRequireLowercase(true);
        config.setRequireDigit(true);
        config.setRequireSpecial(false);

        PasswordPolicyValidator customValidator = createValidator(config);

        assertDoesNotThrow(() -> customValidator.validateOrThrow("abc123"));
        BusinessException exception = assertThrows(BusinessException.class, () -> customValidator.validateOrThrow("abcdef"));
        assertEquals("密码需为6-12位，且同时包含小写字母、数字", exception.getMessage());
    }

    private static PasswordPolicyConfig defaultConfig() {
        return new PasswordPolicyConfig();
    }

    private static PasswordPolicyValidator createValidator(PasswordPolicyConfig config) {
        SystemConfigService systemConfigService = mock(SystemConfigService.class);
        when(systemConfigService.getString(anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        when(systemConfigService.getInt(anyString(), anyInt()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        when(systemConfigService.getBoolean(anyString(), anyBoolean()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        return new PasswordPolicyValidator(config, systemConfigService);
    }
}
