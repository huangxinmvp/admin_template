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

            SystemConfigItemVO item = new SystemConfigItemVO();
            item.setConfigKey(config.getConfigKey());
            item.setConfigName(config.getConfigName());
            item.setConfigValue(config.getConfigValue());
            item.setDefaultValue(config.getDefaultValue());
            item.setValueType(config.getValueType());
            item.setRequiredFlag(config.getRequiredFlag());
            item.setPlaceholder(config.getPlaceholder());
            item.setDescription(config.getDescription());
            item.setOptionsJson(config.getOptionsJson());
            item.setSortNo(config.getSortNo());
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
            config.setConfigValue(item.getConfigValue());
            updated = updateById(config) && updated;
        }
        return updated;
    }

    @Override
    public String getString(String configKey, String defaultValue) {
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
}
