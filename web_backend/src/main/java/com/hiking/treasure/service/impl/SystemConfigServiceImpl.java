package com.hiking.treasure.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.domain.dto.system.SystemConfigValueUpdateDTO;
import com.hiking.treasure.domain.vo.system.BrandingConfigVO;
import com.hiking.treasure.domain.vo.system.SystemConfigGroupVO;
import com.hiking.treasure.domain.vo.system.SystemConfigItemVO;
import com.hiking.treasure.entity.SystemConfig;
import com.hiking.treasure.mapper.SystemConfigMapper;
import com.hiking.treasure.service.SystemConfigService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig>
        implements SystemConfigService {

    private static final String PASSWORD_VALUE_TYPE = "password";

    @Override
    public List<SystemConfigGroupVO> listGroupedConfigs() {
        Map<String, SystemConfigGroupVO> groupMap = new LinkedHashMap<>();
        List<SystemConfig> configs;
        try {
            configs = baseMapper.selectActivePlatformConfigs();
        } catch (Exception _error) {
            return List.of();
        }
        for (SystemConfig config : configs) {
            SystemConfigGroupVO group = groupMap.computeIfAbsent(config.getGroupCode(), key -> {
                SystemConfigGroupVO next = new SystemConfigGroupVO();
                next.setGroupCode(config.getGroupCode());
                next.setGroupName(config.getGroupName());
                next.setGroupSort(config.getGroupSort());
                return next;
            });

            String envOverrideValue = getEnvironmentOverride(config.getConfigKey());
            boolean environmentOverride = StringUtils.hasText(envOverrideValue);
            SystemConfigItemVO item = new SystemConfigItemVO();
            item.setConfigKey(config.getConfigKey());
            item.setConfigName(config.getConfigName());
            item.setConfigValue(environmentOverride ? envOverrideValue : config.getConfigValue());
            item.setDefaultValue(config.getDefaultValue());
            item.setValueType(config.getValueType());
            item.setConfigured(isConfigured(config, envOverrideValue));
            item.setRequiredFlag(config.getRequiredFlag());
            item.setPlaceholder(config.getPlaceholder());
            item.setDescription(config.getDescription());
            item.setOptionsJson(config.getOptionsJson());
            item.setSortNo(config.getSortNo());
            item.setEnvironmentOverride(environmentOverride);
            item.setValueSource(resolveValueSource(config, envOverrideValue));
            if (isPasswordConfig(config)) {
                item.setConfigValue("");
                item.setDefaultValue("");
                if (environmentOverride) {
                    item.setPlaceholder("当前由环境变量覆盖，留空不会影响运行中的有效值");
                } else if (!StringUtils.hasText(item.getPlaceholder())) {
                    item.setPlaceholder(item.getConfigured() ? "已配置，留空则保持原值" : "请输入敏感配置");
                }
            }
            group.getItems().add(item);
        }
        return groupMap.values().stream().toList();
    }

    @Override
    public boolean updateBatchValues(List<SystemConfigValueUpdateDTO> items) {
        if (items == null || items.isEmpty()) {
            return true;
        }

        boolean updated = true;
        for (SystemConfigValueUpdateDTO item : items) {
            SystemConfig config;
            try {
                config = baseMapper.selectActiveByKey(item.getConfigKey());
            } catch (Exception _error) {
                return false;
            }
            if (config == null) {
                continue;
            }
            if (isPasswordConfig(config) && !StringUtils.hasText(item.getConfigValue())) {
                continue;
            }
            config.setConfigValue(item.getConfigValue());
            updated = updateById(config) && updated;
        }
        return updated;
    }

    @Override
    public String getString(String configKey, String defaultValue) {
        String envOverrideValue = getEnvironmentOverride(configKey);
        if (StringUtils.hasText(envOverrideValue)) {
            return envOverrideValue;
        }
        SystemConfig config;
        try {
            config = baseMapper.selectActiveByKey(configKey);
        } catch (Exception _error) {
            return defaultValue;
        }
        if (config == null) {
            return defaultValue;
        }
        return StringUtils.hasText(config.getConfigValue())
                ? config.getConfigValue()
                : (StringUtils.hasText(config.getDefaultValue()) ? config.getDefaultValue() : defaultValue);
    }

    @Override
    public Integer getInt(String configKey, Integer defaultValue) {
        String value = getString(configKey, defaultValue == null ? null : String.valueOf(defaultValue));
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException _error) {
            return defaultValue;
        }
    }

    @Override
    public Boolean getBoolean(String configKey, Boolean defaultValue) {
        String value = getString(configKey, defaultValue == null ? null : String.valueOf(defaultValue));
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        return "1".equals(value) || "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
    }

    @Override
    public BrandingConfigVO getBrandingConfig() {
        BrandingConfigVO vo = new BrandingConfigVO();
        vo.setSiteName(getString("branding.siteName", "SaaS Admin Template"));
        vo.setSiteSubtitle(getString("branding.siteSubtitle", "开箱即用的 SaaS 后台管理模板"));
        vo.setLogoUrl(getString("branding.logoUrl", "/logo.svg"));
        vo.setPrimaryColor(getString("branding.primaryColor", "#1677ff"));
        vo.setCopyrightText(getString("branding.copyrightText", "SaaS Admin Template 版权所有"));
        return vo;
    }

    private boolean isPasswordConfig(SystemConfig config) {
        return PASSWORD_VALUE_TYPE.equalsIgnoreCase(config.getValueType());
    }

    private boolean isConfigured(SystemConfig config, String envOverrideValue) {
        return StringUtils.hasText(envOverrideValue)
                || StringUtils.hasText(config.getConfigValue())
                || StringUtils.hasText(config.getDefaultValue());
    }

    private String resolveValueSource(SystemConfig config, String envOverrideValue) {
        if (StringUtils.hasText(envOverrideValue)) {
            return "environment";
        }
        if (StringUtils.hasText(config.getConfigValue())) {
            return "database";
        }
        return StringUtils.hasText(config.getDefaultValue()) ? "default" : "unset";
    }

    private String getEnvironmentOverride(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            return null;
        }
        String envKey = "AICOOS_CONFIG_" + configKey
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replaceAll("[^A-Za-z0-9]+", "_")
                .toUpperCase();
        return System.getenv(envKey);
    }
}
