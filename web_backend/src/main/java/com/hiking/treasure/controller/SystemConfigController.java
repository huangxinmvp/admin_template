package com.hiking.treasure.controller;

import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.domain.dto.system.SystemConfigBatchUpdateDTO;
import com.hiking.treasure.domain.vo.system.BrandingConfigVO;
import com.hiking.treasure.domain.vo.system.SystemConfigGroupVO;
import com.hiking.treasure.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "系统配置")
@RestController
@RequestMapping("/api/systemConfig")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @Operation(summary = "公开品牌配置")
    @GetMapping("/branding")
    public Result<BrandingConfigVO> branding() {
        return Result.ok(systemConfigService.getBrandingConfig());
    }

    @Operation(summary = "查询系统配置分组")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:config:view')")
    @GetMapping("/groups")
    public Result<List<SystemConfigGroupVO>> groups() {
        return Result.ok(systemConfigService.listGroupedConfigs());
    }

    @Operation(summary = "批量更新系统配置")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:config:update')")
    @PutMapping("/batch")
    public Result<Boolean> updateBatch(@Valid @RequestBody SystemConfigBatchUpdateDTO dto) {
        return Result.ok(systemConfigService.updateBatchValues(dto.getItems()));
    }
}
