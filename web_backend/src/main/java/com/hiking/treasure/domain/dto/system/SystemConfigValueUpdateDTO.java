package com.hiking.treasure.domain.dto.system;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SystemConfigValueUpdateDTO {

    @NotBlank
    private String configKey;

    private String configValue;
}
