package com.hiking.treasure.controller;

import java.util.Arrays;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.entity.RolePermission;
import com.hiking.treasure.service.RolePermissionService;
import com.hiking.treasure.domain.dto.create.RolePermissionCreateDTO;
import com.hiking.treasure.domain.dto.update.RolePermissionUpdateDTO;
import com.hiking.treasure.domain.dto.query.RolePermissionQueryDTO;
import com.hiking.treasure.domain.vo.RolePermissionVO;
import com.hiking.treasure.domain.convert.RolePermissionConvert;
import com.hiking.treasure.common.web.BaseController;
import com.hiking.treasure.common.api.vo.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.servlet.ModelAndView;

/**
 * 角色-权限 关联 - 控制器
 */
@Tag(name = "角色-权限 关联", description = "角色-权限 关联接口" )
@RestController
@RequestMapping("/api/rolePermission" )
public class RolePermissionController extends BaseController<RolePermission, RolePermissionService>{

    @Resource
    private RolePermissionService rolePermissionService;

    @Resource
    private RolePermissionConvert rolePermissionConvert;


    @Operation(summary = "分页查询" )
    @GetMapping("/page" )
    public Result<Page<RolePermissionVO>> page(@Valid RolePermissionQueryDTO dto, @RequestParam(defaultValue = "1") long pageNo, @RequestParam(defaultValue = "10") long pageSize) {
    LambdaQueryWrapper<RolePermission> qw = new LambdaQueryWrapper<>();
    RolePermission cond = rolePermissionConvert.toEntity(dto);
    qw.setEntity(cond);

    Page<RolePermission> page = new Page<>(pageNo, pageSize);
    IPage<RolePermission> entityPage = rolePermissionService.page(page, qw);

    List<RolePermissionVO> voList = rolePermissionConvert.toVOs(entityPage.getRecords());
    Page<RolePermissionVO> voPage = new Page<>(pageNo, pageSize, entityPage.getTotal());
    voPage.setRecords(voList);

    return Result.ok(voPage);
}

    @Operation(summary = "新增" )
    @PostMapping
    public Result<RolePermissionVO> create(@Valid @RequestBody RolePermissionCreateDTO dto) {
        RolePermission entity = rolePermissionConvert.toEntity(dto);
        rolePermissionService.save(entity);
        return Result.ok(rolePermissionConvert.toVO(entity));
    }

    @Operation(summary = "更新" )
    @PutMapping("/{id}" )
    public Result<Boolean> update(@PathVariable String id,
            @Valid @RequestBody RolePermissionUpdateDTO dto) {
        RolePermission entity = rolePermissionConvert.toEntity(dto);
        entity.setId(id);
        return Result.ok(rolePermissionService.updateById(entity));
    }

    @Operation(summary = "删除" )
    @DeleteMapping("/{id}" )
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(rolePermissionService.removeById(id));
    }

    @Operation(summary = "批量删除" )
    @DeleteMapping
    public Result<Boolean> deleteBatch(@RequestParam("ids") String ids) {
        return Result.ok(rolePermissionService.removeByIds(Arrays.asList(ids.split(","))));
    }

    @Operation(summary = "详情" )
    @GetMapping("/{id}" )
    public Result<RolePermissionVO> detail(@PathVariable String id) {
        RolePermission entity = rolePermissionService.getById(id);
        return Result.ok(rolePermissionConvert.toVO(entity));
    }

    @Operation(summary = "导出Excel" )
    @GetMapping("/exportXls" )
    public ModelAndView exportXls(HttpServletRequest request, RolePermissionQueryDTO dto) {
        RolePermission cond = rolePermissionConvert.toEntity(dto);
        String exportFields = null;
        Integer pageNumPerSheet = 500;
        return super.exportXlsSheet(request, cond, RolePermission.class, "角色-权限 关联", exportFields, pageNumPerSheet);
    }

    @Operation(summary = "导入Excel" )
    @PostMapping("/importExcel" )
    public Result<String> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, RolePermission.class);
    }

    @Override
    protected RolePermissionService service() {
        return this.rolePermissionService;
    }
}
