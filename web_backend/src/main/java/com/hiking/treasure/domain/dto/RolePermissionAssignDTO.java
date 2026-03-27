package com.hiking.treasure.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RolePermissionAssignDTO {
    @NotNull
    private List<String> permissionIds;
}
