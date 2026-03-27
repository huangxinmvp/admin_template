package com.hiking.treasure.domain.vo.system;

import lombok.Data;

@Data
public class SystemConfigItemVO {
    private String configKey;
    private String configName;
    private String configValue;
    private String defaultValue;
    private String valueType;
    private Integer requiredFlag;
    private String placeholder;
    private String description;
    private String optionsJson;
    private Integer sortNo;
}
