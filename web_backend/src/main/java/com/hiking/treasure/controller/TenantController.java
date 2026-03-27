package com.hiking.treasure.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.domain.convert.TenantConvert;
import com.hiking.treasure.domain.dto.create.TenantCreateDTO;
import com.hiking.treasure.domain.dto.query.TenantQueryDTO;
import com.hiking.treasure.domain.dto.update.TenantUpdateDTO;
import com.hiking.treasure.domain.vo.TenantVO;
import com.hiking.treasure.entity.Tenant;
import com.hiking.treasure.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Tag(name = "租户管理")
@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;
    private final TenantConvert tenantConvert;

    @Operation(summary = "分页查询")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:tenant:view')")
    @GetMapping("/page")
    public Result<Page<TenantVO>> page(@Valid TenantQueryDTO dto,
                                       @RequestParam(defaultValue = "1") long pageNo,
                                       @RequestParam(defaultValue = "10") long pageSize) {
        LambdaQueryWrapper<Tenant> queryWrapper = new LambdaQueryWrapper<>();
        Tenant condition = tenantConvert.toEntity(dto);
        queryWrapper.setEntity(condition);

        Page<Tenant> page = new Page<>(pageNo, pageSize);
        IPage<Tenant> entityPage = tenantService.page(page, queryWrapper);
        List<TenantVO> voList = tenantConvert.toVOs(entityPage.getRecords());

        Page<TenantVO> voPage = new Page<>(pageNo, pageSize, entityPage.getTotal());
        voPage.setRecords(voList);
        return Result.ok(voPage);
    }

    @Operation(summary = "新增租户")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:tenant:create')")
    @PostMapping
    public Result<TenantVO> create(@Valid @RequestBody TenantCreateDTO dto) {
        Tenant tenant = tenantConvert.toEntity(dto);
        if (tenant.getStatus() == null) {
            tenant.setStatus(1);
        }
        tenantService.save(tenant);
        return Result.ok(tenantConvert.toVO(tenant));
    }

    @Operation(summary = "更新租户")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:tenant:update')")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable String id, @Valid @RequestBody TenantUpdateDTO dto) {
        Tenant tenant = tenantConvert.toEntity(dto);
        tenant.setId(id);
        return Result.ok(tenantService.updateById(tenant));
    }

    @Operation(summary = "删除租户")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:tenant:delete')")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(tenantService.deleteTenantById(id));
    }

    @Operation(summary = "批量删除租户")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:tenant:delete')")
    @DeleteMapping
    public Result<Boolean> deleteBatch(@RequestParam("ids") String ids) {
        return Result.ok(tenantService.deleteTenantsByIds(Arrays.asList(ids.split(","))));
    }

    @Operation(summary = "租户详情")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:tenant:view')")
    @GetMapping("/{id}")
    public Result<TenantVO> detail(@PathVariable String id) {
        return Result.ok(tenantConvert.toVO(tenantService.getById(id)));
    }
}
