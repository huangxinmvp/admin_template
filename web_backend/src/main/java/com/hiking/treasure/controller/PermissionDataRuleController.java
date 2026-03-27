package com.hiking.treasure.controller;

import java.util.Arrays;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.entity.PermissionDataRule;
import com.hiking.treasure.service.PermissionDataRuleService;
import com.hiking.treasure.domain.dto.create.PermissionDataRuleCreateDTO;
import com.hiking.treasure.domain.dto.update.PermissionDataRuleUpdateDTO;
import com.hiking.treasure.domain.dto.query.PermissionDataRuleQueryDTO;
import com.hiking.treasure.domain.vo.PermissionDataRuleVO;
import com.hiking.treasure.domain.convert.PermissionDataRuleConvert;
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
 * 数据权限规则 - 控制器
 */
@Tag(name = "数据权限规则", description = "数据权限规则接口" )
@RestController
@RequestMapping("/api/permissionDataRule" )
public class PermissionDataRuleController extends BaseController<PermissionDataRule, PermissionDataRuleService>{

    @Resource
    private PermissionDataRuleService permissionDataRuleService;

    @Resource
    private PermissionDataRuleConvert permissionDataRuleConvert;


    @Operation(summary = "分页查询" )
    @GetMapping("/page" )
    public Result<Page<PermissionDataRuleVO>> page(@Valid PermissionDataRuleQueryDTO dto, @RequestParam(defaultValue = "1") long pageNo, @RequestParam(defaultValue = "10") long pageSize) {
    LambdaQueryWrapper<PermissionDataRule> qw = new LambdaQueryWrapper<>();
    PermissionDataRule cond = permissionDataRuleConvert.toEntity(dto);
    qw.setEntity(cond);

    Page<PermissionDataRule> page = new Page<>(pageNo, pageSize);
    IPage<PermissionDataRule> entityPage = permissionDataRuleService.page(page, qw);

    List<PermissionDataRuleVO> voList = permissionDataRuleConvert.toVOs(entityPage.getRecords());
    Page<PermissionDataRuleVO> voPage = new Page<>(pageNo, pageSize, entityPage.getTotal());
    voPage.setRecords(voList);

    return Result.ok(voPage);
}

    @Operation(summary = "新增" )
    @PostMapping
    public Result<PermissionDataRuleVO> create(@Valid @RequestBody PermissionDataRuleCreateDTO dto) {
        PermissionDataRule entity = permissionDataRuleConvert.toEntity(dto);
        permissionDataRuleService.save(entity);
        return Result.ok(permissionDataRuleConvert.toVO(entity));
    }

    @Operation(summary = "更新" )
    @PutMapping("/{id}" )
    public Result<Boolean> update(@PathVariable String id,
            @Valid @RequestBody PermissionDataRuleUpdateDTO dto) {
        PermissionDataRule entity = permissionDataRuleConvert.toEntity(dto);
        entity.setId(id);
        return Result.ok(permissionDataRuleService.updateById(entity));
    }

    @Operation(summary = "删除" )
    @DeleteMapping("/{id}" )
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(permissionDataRuleService.removeById(id));
    }

    @Operation(summary = "批量删除" )
    @DeleteMapping
    public Result<Boolean> deleteBatch(@RequestParam("ids") String ids) {
        return Result.ok(permissionDataRuleService.removeByIds(Arrays.asList(ids.split(","))));
    }

    @Operation(summary = "详情" )
    @GetMapping("/{id}" )
    public Result<PermissionDataRuleVO> detail(@PathVariable String id) {
        PermissionDataRule entity = permissionDataRuleService.getById(id);
        return Result.ok(permissionDataRuleConvert.toVO(entity));
    }

    @Operation(summary = "导出Excel" )
    @GetMapping("/exportXls" )
    public ModelAndView exportXls(HttpServletRequest request, PermissionDataRuleQueryDTO dto) {
        PermissionDataRule cond = permissionDataRuleConvert.toEntity(dto);
        String exportFields = null;
        Integer pageNumPerSheet = 500;
        return super.exportXlsSheet(request, cond, PermissionDataRule.class, "数据权限规则", exportFields, pageNumPerSheet);
    }

    @Operation(summary = "导入Excel" )
    @PostMapping("/importExcel" )
    public Result<String> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PermissionDataRule.class);
    }

    @Override
    protected PermissionDataRuleService service() {
        return this.permissionDataRuleService;
    }
}
