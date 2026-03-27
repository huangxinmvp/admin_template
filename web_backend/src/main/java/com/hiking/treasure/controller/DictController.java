package com.hiking.treasure.controller;

import java.util.Arrays;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.entity.Dict;
import com.hiking.treasure.service.DictService;
import com.hiking.treasure.domain.dto.create.DictCreateDTO;
import com.hiking.treasure.domain.dto.update.DictUpdateDTO;
import com.hiking.treasure.domain.dto.query.DictQueryDTO;
import com.hiking.treasure.domain.vo.DictVO;
import com.hiking.treasure.domain.convert.DictConvert;
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
 * 数据字典 - 控制器
 */
@Tag(name = "数据字典", description = "数据字典接口" )
@RestController
@RequestMapping("/api/dict" )
public class DictController extends BaseController<Dict, DictService>{

    @Resource
    private DictService dictService;

    @Resource
    private DictConvert dictConvert;


    @Operation(summary = "分页查询" )
    @GetMapping("/page" )
    public Result<Page<DictVO>> page(@Valid DictQueryDTO dto, @RequestParam(defaultValue = "1") long pageNo, @RequestParam(defaultValue = "10") long pageSize) {
    LambdaQueryWrapper<Dict> qw = new LambdaQueryWrapper<>();
    Dict cond = dictConvert.toEntity(dto);
    qw.setEntity(cond);

    Page<Dict> page = new Page<>(pageNo, pageSize);
    IPage<Dict> entityPage = dictService.page(page, qw);

    List<DictVO> voList = dictConvert.toVOs(entityPage.getRecords());
    Page<DictVO> voPage = new Page<>(pageNo, pageSize, entityPage.getTotal());
    voPage.setRecords(voList);

    return Result.ok(voPage);
}

    @Operation(summary = "新增" )
    @PostMapping
    public Result<DictVO> create(@Valid @RequestBody DictCreateDTO dto) {
        Dict entity = dictConvert.toEntity(dto);
        dictService.save(entity);
        return Result.ok(dictConvert.toVO(entity));
    }

    @Operation(summary = "更新" )
    @PutMapping("/{id}" )
    public Result<Boolean> update(@PathVariable String id,
            @Valid @RequestBody DictUpdateDTO dto) {
        Dict entity = dictConvert.toEntity(dto);
        entity.setId(id);
        return Result.ok(dictService.updateById(entity));
    }

    @Operation(summary = "删除" )
    @DeleteMapping("/{id}" )
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(dictService.removeById(id));
    }

    @Operation(summary = "批量删除" )
    @DeleteMapping
    public Result<Boolean> deleteBatch(@RequestParam("ids") String ids) {
        return Result.ok(dictService.removeByIds(Arrays.asList(ids.split(","))));
    }

    @Operation(summary = "详情" )
    @GetMapping("/{id}" )
    public Result<DictVO> detail(@PathVariable String id) {
        Dict entity = dictService.getById(id);
        return Result.ok(dictConvert.toVO(entity));
    }

    @Operation(summary = "导出Excel" )
    @GetMapping("/exportXls" )
    public ModelAndView exportXls(HttpServletRequest request, DictQueryDTO dto) {
        Dict cond = dictConvert.toEntity(dto);
        String exportFields = null;
        Integer pageNumPerSheet = 500;
        return super.exportXlsSheet(request, cond, Dict.class, "数据字典", exportFields, pageNumPerSheet);
    }

    @Operation(summary = "导入Excel" )
    @PostMapping("/importExcel" )
    public Result<String> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, Dict.class);
    }

    @Override
    protected DictService service() {
        return this.dictService;
    }
}
