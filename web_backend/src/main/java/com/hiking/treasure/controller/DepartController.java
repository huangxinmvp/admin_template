package com.hiking.treasure.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.common.web.BaseController;
import com.hiking.treasure.domain.convert.DepartConvert;
import com.hiking.treasure.domain.dto.create.DepartCreateDTO;
import com.hiking.treasure.domain.dto.query.DepartQueryDTO;
import com.hiking.treasure.domain.dto.update.DepartUpdateDTO;
import com.hiking.treasure.domain.vo.DepartVO;
import com.hiking.treasure.domain.vo.system.DepartTreeVO;
import com.hiking.treasure.domain.vo.system.OptionVO;
import com.hiking.treasure.entity.Depart;
import com.hiking.treasure.service.DepartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

/**
 * 部门 - 控制器
 */
@Tag(name = "部门", description = "部门接口")
@RestController
@RequestMapping("/api/depart")
public class DepartController extends BaseController<Depart, DepartService> {

    @Resource
    private DepartService departService;

    @Resource
    private DepartConvert departConvert;

    @Operation(summary = "分页查询")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:depart:view')")
    @GetMapping("/page")
    public Result<Page<DepartVO>> page(@Valid DepartQueryDTO dto,
                                       @RequestParam(defaultValue = "1") long pageNo,
                                       @RequestParam(defaultValue = "10") long pageSize) {
        LambdaQueryWrapper<Depart> qw = new LambdaQueryWrapper<>();
        qw.setEntity(departConvert.toEntity(dto));

        Page<Depart> page = new Page<>(pageNo, pageSize);
        IPage<Depart> entityPage = departService.page(page, qw);

        List<DepartVO> voList = departConvert.toVOs(entityPage.getRecords());
        Page<DepartVO> voPage = new Page<>(pageNo, pageSize, entityPage.getTotal());
        voPage.setRecords(voList);
        return Result.ok(voPage);
    }

    @Operation(summary = "部门树")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:depart:view')")
    @GetMapping("/tree")
    public Result<List<DepartTreeVO>> tree() {
        return Result.ok(departService.listTree());
    }

    @Operation(summary = "部门选项")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:depart:view')")
    @GetMapping("/options")
    public Result<List<OptionVO>> options() {
        return Result.ok(departService.listOptions());
    }

    @Operation(summary = "新增")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:depart:create')")
    @PostMapping
    public Result<DepartVO> create(@Valid @RequestBody DepartCreateDTO dto) {
        Depart entity = departConvert.toEntity(dto);
        departService.save(entity);
        return Result.ok(departConvert.toVO(entity));
    }

    @Operation(summary = "更新")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:depart:update')")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable String id, @Valid @RequestBody DepartUpdateDTO dto) {
        Depart entity = departConvert.toEntity(dto);
        entity.setId(id);
        return Result.ok(departService.updateById(entity));
    }

    @Operation(summary = "删除")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:depart:delete')")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(departService.deleteDepartById(id));
    }

    @Operation(summary = "批量删除")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:depart:delete')")
    @DeleteMapping
    public Result<Boolean> deleteBatch(@RequestParam("ids") String ids) {
        return Result.ok(departService.deleteDepartsByIds(Arrays.asList(ids.split(","))));
    }

    @Operation(summary = "详情")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:depart:view')")
    @GetMapping("/{id}")
    public Result<DepartVO> detail(@PathVariable String id) {
        return Result.ok(departConvert.toVO(departService.getById(id)));
    }

    @Operation(summary = "导出Excel")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:depart:view')")
    @GetMapping("/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, DepartQueryDTO dto) {
        Depart cond = departConvert.toEntity(dto);
        return super.exportXlsSheet(request, cond, Depart.class, "部门", null, 500);
    }

    @Operation(summary = "导入Excel")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:depart:create')")
    @PostMapping("/importExcel")
    public Result<String> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, Depart.class);
    }

    @Override
    protected DepartService service() {
        return departService;
    }
}
