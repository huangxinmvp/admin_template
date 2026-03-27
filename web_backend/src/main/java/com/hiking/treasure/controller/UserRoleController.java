package com.hiking.treasure.controller;

import java.util.Arrays;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.entity.UserRole;
import com.hiking.treasure.service.UserRoleService;
import com.hiking.treasure.domain.dto.create.UserRoleCreateDTO;
import com.hiking.treasure.domain.dto.update.UserRoleUpdateDTO;
import com.hiking.treasure.domain.dto.query.UserRoleQueryDTO;
import com.hiking.treasure.domain.vo.UserRoleVO;
import com.hiking.treasure.domain.convert.UserRoleConvert;
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
 * 用户-角色 关联 - 控制器
 */
@Tag(name = "用户-角色 关联", description = "用户-角色 关联接口" )
@RestController
@RequestMapping("/api/userRole" )
public class UserRoleController extends BaseController<UserRole, UserRoleService>{

    @Resource
    private UserRoleService userRoleService;

    @Resource
    private UserRoleConvert userRoleConvert;


    @Operation(summary = "分页查询" )
    @GetMapping("/page" )
    public Result<Page<UserRoleVO>> page(@Valid UserRoleQueryDTO dto, @RequestParam(defaultValue = "1") long pageNo, @RequestParam(defaultValue = "10") long pageSize) {
    LambdaQueryWrapper<UserRole> qw = new LambdaQueryWrapper<>();
    UserRole cond = userRoleConvert.toEntity(dto);
    qw.setEntity(cond);

    Page<UserRole> page = new Page<>(pageNo, pageSize);
    IPage<UserRole> entityPage = userRoleService.page(page, qw);

    List<UserRoleVO> voList = userRoleConvert.toVOs(entityPage.getRecords());
    Page<UserRoleVO> voPage = new Page<>(pageNo, pageSize, entityPage.getTotal());
    voPage.setRecords(voList);

    return Result.ok(voPage);
}

    @Operation(summary = "新增" )
    @PostMapping
    public Result<UserRoleVO> create(@Valid @RequestBody UserRoleCreateDTO dto) {
        UserRole entity = userRoleConvert.toEntity(dto);
        userRoleService.save(entity);
        return Result.ok(userRoleConvert.toVO(entity));
    }

    @Operation(summary = "更新" )
    @PutMapping("/{id}" )
    public Result<Boolean> update(@PathVariable String id,
            @Valid @RequestBody UserRoleUpdateDTO dto) {
        UserRole entity = userRoleConvert.toEntity(dto);
        entity.setId(id);
        return Result.ok(userRoleService.updateById(entity));
    }

    @Operation(summary = "删除" )
    @DeleteMapping("/{id}" )
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(userRoleService.removeById(id));
    }

    @Operation(summary = "批量删除" )
    @DeleteMapping
    public Result<Boolean> deleteBatch(@RequestParam("ids") String ids) {
        return Result.ok(userRoleService.removeByIds(Arrays.asList(ids.split(","))));
    }

    @Operation(summary = "详情" )
    @GetMapping("/{id}" )
    public Result<UserRoleVO> detail(@PathVariable String id) {
        UserRole entity = userRoleService.getById(id);
        return Result.ok(userRoleConvert.toVO(entity));
    }

    @Operation(summary = "导出Excel" )
    @GetMapping("/exportXls" )
    public ModelAndView exportXls(HttpServletRequest request, UserRoleQueryDTO dto) {
        UserRole cond = userRoleConvert.toEntity(dto);
        String exportFields = null;
        Integer pageNumPerSheet = 500;
        return super.exportXlsSheet(request, cond, UserRole.class, "用户-角色 关联", exportFields, pageNumPerSheet);
    }

    @Operation(summary = "导入Excel" )
    @PostMapping("/importExcel" )
    public Result<String> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, UserRole.class);
    }

    @Override
    protected UserRoleService service() {
        return this.userRoleService;
    }
}
