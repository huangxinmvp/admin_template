package com.hiking.treasure.domain.dto.system;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SystemConfigBatchUpdateDTO {

    @Valid
    @NotEmpty
    private List<SystemConfigValueUpdateDTO> items;
}
