package com.hiking.treasure.controller;

import java.util.Arrays;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.entity.UserDepart;
import com.hiking.treasure.service.UserDepartService;
import com.hiking.treasure.domain.dto.create.UserDepartCreateDTO;
import com.hiking.treasure.domain.dto.update.UserDepartUpdateDTO;
import com.hiking.treasure.domain.dto.query.UserDepartQueryDTO;
import com.hiking.treasure.domain.vo.UserDepartVO;
import com.hiking.treasure.domain.convert.UserDepartConvert;
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
 * 用户-部门关系 - 控制器
 */
@Tag(name = "用户-部门关系", description = "用户-部门关系接口" )
@RestController
@RequestMapping("/api/userDepart" )
public class UserDepartController extends BaseController<UserDepart, UserDepartService>{

    @Resource
    private UserDepartService userDepartService;

    @Resource
    private UserDepartConvert userDepartConvert;


    @Operation(summary = "分页查询" )
    @GetMapping("/page" )
    public Result<Page<UserDepartVO>> page(@Valid UserDepartQueryDTO dto, @RequestParam(defaultValue = "1") long pageNo, @RequestParam(defaultValue = "10") long pageSize) {
    LambdaQueryWrapper<UserDepart> qw = new LambdaQueryWrapper<>();
    UserDepart cond = userDepartConvert.toEntity(dto);
    qw.setEntity(cond);

    Page<UserDepart> page = new Page<>(pageNo, pageSize);
    IPage<UserDepart> entityPage = userDepartService.page(page, qw);

    List<UserDepartVO> voList = userDepartConvert.toVOs(entityPage.getRecords());
    Page<UserDepartVO> voPage = new Page<>(pageNo, pageSize, entityPage.getTotal());
    voPage.setRecords(voList);

    return Result.ok(voPage);
}

    @Operation(summary = "新增" )
    @PostMapping
    public Result<UserDepartVO> create(@Valid @RequestBody UserDepartCreateDTO dto) {
        UserDepart entity = userDepartConvert.toEntity(dto);
        userDepartService.save(entity);
        return Result.ok(userDepartConvert.toVO(entity));
    }

    @Operation(summary = "更新" )
    @PutMapping("/{id}" )
    public Result<Boolean> update(@PathVariable String id,
            @Valid @RequestBody UserDepartUpdateDTO dto) {
        UserDepart entity = userDepartConvert.toEntity(dto);
        entity.setId(id);
        return Result.ok(userDepartService.updateById(entity));
    }

    @Operation(summary = "删除" )
    @DeleteMapping("/{id}" )
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(userDepartService.removeById(id));
    }

    @Operation(summary = "批量删除" )
    @DeleteMapping
    public Result<Boolean> deleteBatch(@RequestParam("ids") String ids) {
        return Result.ok(userDepartService.removeByIds(Arrays.asList(ids.split(","))));
    }

    @Operation(summary = "详情" )
    @GetMapping("/{id}" )
    public Result<UserDepartVO> detail(@PathVariable String id) {
        UserDepart entity = userDepartService.getById(id);
        return Result.ok(userDepartConvert.toVO(entity));
    }

    @Operation(summary = "导出Excel" )
    @GetMapping("/exportXls" )
    public ModelAndView exportXls(HttpServletRequest request, UserDepartQueryDTO dto) {
        UserDepart cond = userDepartConvert.toEntity(dto);
        String exportFields = null;
        Integer pageNumPerSheet = 500;
        return super.exportXlsSheet(request, cond, UserDepart.class, "用户-部门关系", exportFields, pageNumPerSheet);
    }

    @Operation(summary = "导入Excel" )
    @PostMapping("/importExcel" )
    public Result<String> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, UserDepart.class);
    }

    @Override
    protected UserDepartService service() {
        return this.userDepartService;
    }
}
