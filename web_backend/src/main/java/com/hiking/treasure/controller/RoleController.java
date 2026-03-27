package com.hiking.treasure.controller;

import java.util.Arrays;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.entity.Role;
import com.hiking.treasure.service.RoleService;
import com.hiking.treasure.domain.dto.RolePermissionAssignDTO;
import com.hiking.treasure.domain.dto.create.RoleCreateDTO;
import com.hiking.treasure.domain.dto.update.RoleUpdateDTO;
import com.hiking.treasure.domain.dto.query.RoleQueryDTO;
import com.hiking.treasure.domain.vo.RoleVO;
import com.hiking.treasure.domain.vo.system.OptionVO;
import com.hiking.treasure.domain.vo.system.RoleTreeVO;
import com.hiking.treasure.domain.convert.RoleConvert;
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
 * 角色表 - 控制器
 */
@Tag(name = "角色表", description = "角色表接口" )
@RestController
@RequestMapping("/api/role" )
public class RoleController extends BaseController<Role, RoleService>{

    @Resource
    private RoleService roleService;

    @Resource
    private RoleConvert roleConvert;


    @Operation(summary = "分页查询" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:role:view')")
    @GetMapping("/page" )
    public Result<Page<RoleVO>> page(@Valid RoleQueryDTO dto, @RequestParam(defaultValue = "1") long pageNo, @RequestParam(defaultValue = "10") long pageSize) {
    LambdaQueryWrapper<Role> qw = new LambdaQueryWrapper<>();
    Role cond = roleConvert.toEntity(dto);
    qw.setEntity(cond);

    Page<Role> page = new Page<>(pageNo, pageSize);
    IPage<Role> entityPage = roleService.page(page, qw);

    List<RoleVO> voList = roleConvert.toVOs(entityPage.getRecords());
    Page<RoleVO> voPage = new Page<>(pageNo, pageSize, entityPage.getTotal());
    voPage.setRecords(voList);

    return Result.ok(voPage);
}

    @Operation(summary = "新增" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:role:create')")
    @PostMapping
    public Result<RoleVO> create(@Valid @RequestBody RoleCreateDTO dto) {
        Role entity = roleConvert.toEntity(dto);
        roleService.save(entity);
        return Result.ok(roleConvert.toVO(entity));
    }

    @Operation(summary = "更新" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:role:update')")
    @PutMapping("/{id}" )
    public Result<Boolean> update(@PathVariable String id,
            @Valid @RequestBody RoleUpdateDTO dto) {
        Role entity = roleConvert.toEntity(dto);
        entity.setId(id);
        return Result.ok(roleService.updateById(entity));
    }

    @Operation(summary = "删除" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:role:delete')")
    @DeleteMapping("/{id}" )
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(roleService.deleteRoleById(id));
    }

    @Operation(summary = "批量删除" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:role:delete')")
    @DeleteMapping
    public Result<Boolean> deleteBatch(@RequestParam("ids") String ids) {
        return Result.ok(roleService.deleteRolesByIds(Arrays.asList(ids.split(","))));
    }

    @Operation(summary = "详情" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:role:view')")
    @GetMapping("/{id}" )
    public Result<RoleVO> detail(@PathVariable String id) {
        Role entity = roleService.getById(id);
        return Result.ok(roleConvert.toVO(entity));
    }

    @Operation(summary = "角色树")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:role:view')")
    @GetMapping("/tree")
    public Result<List<RoleTreeVO>> tree() {
        return Result.ok(roleService.listTree());
    }

    @Operation(summary = "角色选项")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:role:view')")
    @GetMapping("/options")
    public Result<List<OptionVO>> options() {
        return Result.ok(roleService.listOptions());
    }

    @Operation(summary = "查询角色关联权限ID")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:role:view')")
    @GetMapping("/{id}/permissionIds")
    public Result<List<String>> permissionIds(@PathVariable String id) {
        return Result.ok(roleService.getPermissionIds(id));
    }

    @Operation(summary = "保存角色关联权限ID")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:role:update')")
    @PutMapping("/{id}/permissionIds")
    public Result<Boolean> savePermissionIds(@PathVariable String id,
                                             @Valid @RequestBody RolePermissionAssignDTO dto) {
        return Result.ok(roleService.savePermissionIds(id, dto.getPermissionIds()));
    }

    @Operation(summary = "导出Excel" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:role:view')")
    @GetMapping("/exportXls" )
    public ModelAndView exportXls(HttpServletRequest request, RoleQueryDTO dto) {
        Role cond = roleConvert.toEntity(dto);
        String exportFields = null;
        Integer pageNumPerSheet = 500;
        return super.exportXlsSheet(request, cond, Role.class, "角色表", exportFields, pageNumPerSheet);
    }

    @Operation(summary = "导入Excel" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:role:create')")
    @PostMapping("/importExcel" )
    public Result<String> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, Role.class);
    }

    @Override
    protected RoleService service() {
        return this.roleService;
    }
}
