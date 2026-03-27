package com.hiking.treasure.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hiking.treasure.domain.dto.system.SystemConfigValueUpdateDTO;
import com.hiking.treasure.domain.vo.system.BrandingConfigVO;
import com.hiking.treasure.domain.vo.system.SystemConfigGroupVO;
import com.hiking.treasure.entity.SystemConfig;

import java.util.List;

public interface SystemConfigService extends IService<SystemConfig> {

    List<SystemConfigGroupVO> listGroupedConfigs();

    boolean updateBatchValues(List<SystemConfigValueUpdateDTO> items);

    String getString(String configKey, String defaultValue);

    Integer getInt(String configKey, Integer defaultValue);

    Boolean getBoolean(String configKey, Boolean defaultValue);

    BrandingConfigVO getBrandingConfig();
}
