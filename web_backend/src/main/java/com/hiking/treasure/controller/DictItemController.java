package com.hiking.treasure.controller;

import java.util.Arrays;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.entity.DictItem;
import com.hiking.treasure.service.DictItemService;
import com.hiking.treasure.domain.dto.create.DictItemCreateDTO;
import com.hiking.treasure.domain.dto.update.DictItemUpdateDTO;
import com.hiking.treasure.domain.dto.query.DictItemQueryDTO;
import com.hiking.treasure.domain.vo.DictItemVO;
import com.hiking.treasure.domain.convert.DictItemConvert;
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
 * 数据字典项 - 控制器
 */
@Tag(name = "数据字典项", description = "数据字典项接口" )
@RestController
@RequestMapping("/api/dictItem" )
public class DictItemController extends BaseController<DictItem, DictItemService>{

    @Resource
    private DictItemService dictItemService;

    @Resource
    private DictItemConvert dictItemConvert;


    @Operation(summary = "分页查询" )
    @GetMapping("/page" )
    public Result<Page<DictItemVO>> page(@Valid DictItemQueryDTO dto, @RequestParam(defaultValue = "1") long pageNo, @RequestParam(defaultValue = "10") long pageSize) {
    LambdaQueryWrapper<DictItem> qw = new LambdaQueryWrapper<>();
    DictItem cond = dictItemConvert.toEntity(dto);
    qw.setEntity(cond);

    Page<DictItem> page = new Page<>(pageNo, pageSize);
    IPage<DictItem> entityPage = dictItemService.page(page, qw);

    List<DictItemVO> voList = dictItemConvert.toVOs(entityPage.getRecords());
    Page<DictItemVO> voPage = new Page<>(pageNo, pageSize, entityPage.getTotal());
    voPage.setRecords(voList);

    return Result.ok(voPage);
}

    @Operation(summary = "新增" )
    @PostMapping
    public Result<DictItemVO> create(@Valid @RequestBody DictItemCreateDTO dto) {
        DictItem entity = dictItemConvert.toEntity(dto);
        dictItemService.save(entity);
        return Result.ok(dictItemConvert.toVO(entity));
    }

    @Operation(summary = "更新" )
    @PutMapping("/{id}" )
    public Result<Boolean> update(@PathVariable String id,
            @Valid @RequestBody DictItemUpdateDTO dto) {
        DictItem entity = dictItemConvert.toEntity(dto);
        entity.setId(id);
        return Result.ok(dictItemService.updateById(entity));
    }

    @Operation(summary = "删除" )
    @DeleteMapping("/{id}" )
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(dictItemService.removeById(id));
    }

    @Operation(summary = "批量删除" )
    @DeleteMapping
    public Result<Boolean> deleteBatch(@RequestParam("ids") String ids) {
        return Result.ok(dictItemService.removeByIds(Arrays.asList(ids.split(","))));
    }

    @Operation(summary = "详情" )
    @GetMapping("/{id}" )
    public Result<DictItemVO> detail(@PathVariable String id) {
        DictItem entity = dictItemService.getById(id);
        return Result.ok(dictItemConvert.toVO(entity));
    }

    @Operation(summary = "导出Excel" )
    @GetMapping("/exportXls" )
    public ModelAndView exportXls(HttpServletRequest request, DictItemQueryDTO dto) {
        DictItem cond = dictItemConvert.toEntity(dto);
        String exportFields = null;
        Integer pageNumPerSheet = 500;
        return super.exportXlsSheet(request, cond, DictItem.class, "数据字典项", exportFields, pageNumPerSheet);
    }

    @Operation(summary = "导入Excel" )
    @PostMapping("/importExcel" )
    public Result<String> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, DictItem.class);
    }

    @Override
    protected DictItemService service() {
        return this.dictItemService;
    }
}
