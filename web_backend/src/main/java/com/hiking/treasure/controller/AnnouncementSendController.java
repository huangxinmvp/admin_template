package com.hiking.treasure.controller;

import java.util.Arrays;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.entity.AnnouncementSend;
import com.hiking.treasure.service.AnnouncementSendService;
import com.hiking.treasure.domain.dto.create.AnnouncementSendCreateDTO;
import com.hiking.treasure.domain.dto.update.AnnouncementSendUpdateDTO;
import com.hiking.treasure.domain.dto.query.AnnouncementSendQueryDTO;
import com.hiking.treasure.domain.vo.AnnouncementSendVO;
import com.hiking.treasure.domain.convert.AnnouncementSendConvert;
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
 * 通告-用户关系 - 控制器
 */
@Tag(name = "通告-用户关系", description = "通告-用户关系接口" )
@RestController
@RequestMapping("/api/announcementSend" )
public class AnnouncementSendController extends BaseController<AnnouncementSend, AnnouncementSendService>{

    @Resource
    private AnnouncementSendService announcementSendService;

    @Resource
    private AnnouncementSendConvert announcementSendConvert;


    @Operation(summary = "分页查询" )
    @GetMapping("/page" )
    public Result<Page<AnnouncementSendVO>> page(@Valid AnnouncementSendQueryDTO dto, @RequestParam(defaultValue = "1") long pageNo, @RequestParam(defaultValue = "10") long pageSize) {
    LambdaQueryWrapper<AnnouncementSend> qw = new LambdaQueryWrapper<>();
    AnnouncementSend cond = announcementSendConvert.toEntity(dto);
    qw.setEntity(cond);

    Page<AnnouncementSend> page = new Page<>(pageNo, pageSize);
    IPage<AnnouncementSend> entityPage = announcementSendService.page(page, qw);

    List<AnnouncementSendVO> voList = announcementSendConvert.toVOs(entityPage.getRecords());
    Page<AnnouncementSendVO> voPage = new Page<>(pageNo, pageSize, entityPage.getTotal());
    voPage.setRecords(voList);

    return Result.ok(voPage);
}

    @Operation(summary = "新增" )
    @PostMapping
    public Result<AnnouncementSendVO> create(@Valid @RequestBody AnnouncementSendCreateDTO dto) {
        AnnouncementSend entity = announcementSendConvert.toEntity(dto);
        announcementSendService.save(entity);
        return Result.ok(announcementSendConvert.toVO(entity));
    }

    @Operation(summary = "更新" )
    @PutMapping("/{id}" )
    public Result<Boolean> update(@PathVariable String id,
            @Valid @RequestBody AnnouncementSendUpdateDTO dto) {
        AnnouncementSend entity = announcementSendConvert.toEntity(dto);
        entity.setId(id);
        return Result.ok(announcementSendService.updateById(entity));
    }

    @Operation(summary = "删除" )
    @DeleteMapping("/{id}" )
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(announcementSendService.removeById(id));
    }

    @Operation(summary = "批量删除" )
    @DeleteMapping
    public Result<Boolean> deleteBatch(@RequestParam("ids") String ids) {
        return Result.ok(announcementSendService.removeByIds(Arrays.asList(ids.split(","))));
    }

    @Operation(summary = "详情" )
    @GetMapping("/{id}" )
    public Result<AnnouncementSendVO> detail(@PathVariable String id) {
        AnnouncementSend entity = announcementSendService.getById(id);
        return Result.ok(announcementSendConvert.toVO(entity));
    }

    @Operation(summary = "导出Excel" )
    @GetMapping("/exportXls" )
    public ModelAndView exportXls(HttpServletRequest request, AnnouncementSendQueryDTO dto) {
        AnnouncementSend cond = announcementSendConvert.toEntity(dto);
        String exportFields = null;
        Integer pageNumPerSheet = 500;
        return super.exportXlsSheet(request, cond, AnnouncementSend.class, "通告-用户关系", exportFields, pageNumPerSheet);
    }

    @Operation(summary = "导入Excel" )
    @PostMapping("/importExcel" )
    public Result<String> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AnnouncementSend.class);
    }

    @Override
    protected AnnouncementSendService service() {
        return this.announcementSendService;
    }
}
