package com.hiking.treasure.controller;

import java.util.Arrays;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.domain.dto.PermissionSortDTO;
import com.hiking.treasure.domain.dto.PermissionTreeSaveDTO;
import com.hiking.treasure.entity.Permission;
import com.hiking.treasure.service.PermissionService;
import com.hiking.treasure.domain.dto.create.PermissionCreateDTO;
import com.hiking.treasure.domain.dto.update.PermissionUpdateDTO;
import com.hiking.treasure.domain.dto.query.PermissionQueryDTO;
import com.hiking.treasure.domain.vo.PermissionVO;
import com.hiking.treasure.domain.vo.system.PermissionTreeSaveResultVO;
import com.hiking.treasure.domain.vo.system.PermissionTreeVO;
import com.hiking.treasure.domain.convert.PermissionConvert;
import com.hiking.treasure.common.web.BaseController;
import com.hiking.treasure.common.api.vo.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.servlet.ModelAndView;

/**
 * 权限表(菜单/按钮) - 控制器
 */
@Tag(name = "权限表(菜单/按钮)", description = "权限表(菜单/按钮)接口" )
@RestController
@RequestMapping("/api/permission" )
public class PermissionController extends BaseController<Permission, PermissionService>{

    @Resource
    private PermissionService permissionService;

    @Resource
    private PermissionConvert permissionConvert;


    @Operation(summary = "分页查询" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:permission:view')")
    @GetMapping("/page" )
    public Result<Page<PermissionVO>> page(@Valid PermissionQueryDTO dto, @RequestParam(defaultValue = "1") long pageNo, @RequestParam(defaultValue = "10") long pageSize) {
    LambdaQueryWrapper<Permission> qw = new LambdaQueryWrapper<>();
    Permission cond = permissionConvert.toEntity(dto);
    qw.setEntity(cond);

    Page<Permission> page = new Page<>(pageNo, pageSize);
    IPage<Permission> entityPage = permissionService.page(page, qw);

    List<PermissionVO> voList = permissionConvert.toVOs(entityPage.getRecords());
    Page<PermissionVO> voPage = new Page<>(pageNo, pageSize, entityPage.getTotal());
    voPage.setRecords(voList);

    return Result.ok(voPage);
}

    @Operation(summary = "新增" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:permission:create')")
    @PostMapping
    public Result<PermissionVO> create(@Valid @RequestBody PermissionCreateDTO dto) {
        Permission entity = permissionConvert.toEntity(dto);
        if (entity.getSortNo() == null) {
            entity.setSortNo(permissionService.getNextSortNo(entity.getParentId()));
        }
        permissionService.savePermission(entity);
        return Result.ok(permissionConvert.toVO(entity));
    }

    @Operation(summary = "更新" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:permission:update')")
    @PutMapping("/{id}" )
    public Result<Boolean> update(@PathVariable String id,
            @Valid @RequestBody PermissionUpdateDTO dto) {
        Permission entity = permissionConvert.toEntity(dto);
        entity.setId(id);
        return Result.ok(permissionService.updatePermission(entity));
    }

    @Operation(summary = "删除" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:permission:delete')")
    @DeleteMapping("/{id}" )
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(permissionService.deletePermissionById(id));
    }

    @Operation(summary = "批量删除" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:permission:delete')")
    @DeleteMapping
    public Result<Boolean> deleteBatch(@RequestParam("ids") String ids) {
        return Result.ok(permissionService.deletePermissionsByIds(Arrays.asList(ids.split(","))));
    }

    @Operation(summary = "详情" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:permission:view')")
    @GetMapping("/{id}" )
    public Result<PermissionVO> detail(@PathVariable String id) {
        Permission entity = permissionService.getById(id);
        return Result.ok(permissionConvert.toVO(entity));
    }

    @Operation(summary = "权限树")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:permission:view')")
    @GetMapping("/tree")
    public Result<List<PermissionTreeVO>> tree() {
        return Result.ok(permissionService.listTree());
    }

    @Operation(summary = "获取下一个排序号")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:permission:view')")
    @GetMapping("/nextSortNo")
    public Result<Integer> nextSortNo(@RequestParam(value = "parentId", required = false) String parentId) {
        return Result.ok(permissionService.getNextSortNo(parentId));
    }

    @Operation(summary = "保存菜单排序")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:permission:update')")
    @PostMapping("/sort")
    public Result<Boolean> sort(@RequestBody List<PermissionSortDTO> items) {
        return Result.ok(permissionService.saveSort(items));
    }

    @Operation(summary = "保存菜单整树")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:permission:update')")
    @PostMapping("/tree/save")
    public Result<PermissionTreeSaveResultVO> saveTree(@RequestBody List<PermissionTreeSaveDTO> tree) {
        return Result.ok(permissionService.saveTree(tree));
    }

    @Operation(summary = "导出Excel" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:permission:view')")
    @GetMapping("/exportXls" )
    public ModelAndView exportXls(HttpServletRequest request, PermissionQueryDTO dto) {
        Permission cond = permissionConvert.toEntity(dto);
        String exportFields = null;
        Integer pageNumPerSheet = 500;
        return super.exportXlsSheet(request, cond, Permission.class, "权限表(菜单/按钮)", exportFields, pageNumPerSheet);
    }

    @Operation(summary = "导入Excel" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:permission:create')")
    @PostMapping("/importExcel" )
    public Result<String> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, Permission.class);
    }

    @Override
    protected PermissionService service() {
        return this.permissionService;
    }
}
