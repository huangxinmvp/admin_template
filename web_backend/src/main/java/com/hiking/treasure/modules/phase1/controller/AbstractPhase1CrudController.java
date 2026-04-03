package com.hiking.treasure.modules.phase1.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.common.web.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractPhase1CrudController<T, S extends IService<T>, C, U, Q, V>
        extends BaseController<T, S> {

    protected abstract T toCreateEntity(C dto);

    protected abstract T toUpdateEntity(U dto);

    protected abstract T toQueryEntity(Q dto);

    protected abstract V toVO(T entity);

    protected abstract List<V> toVOs(List<T> entities);

    protected abstract void setEntityId(T entity, String id);

    protected void applyCreateDefaults(T entity) {
    }

    protected void applyUpdateDefaults(T entity) {
    }

    @Operation(summary = "分页查询")
    @GetMapping("/page")
    public Result<Page<V>> page(
            @Valid Q dto,
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.setEntity(toQueryEntity(dto));

        Page<T> page = new Page<>(pageNo, pageSize);
        IPage<T> entityPage = service().page(page, wrapper);

        Page<V> voPage = new Page<>(pageNo, pageSize, entityPage.getTotal());
        voPage.setRecords(toVOs(entityPage.getRecords()));
        return Result.ok(voPage);
    }

    @Operation(summary = "新增")
    @PostMapping
    public Result<V> create(@Valid @RequestBody C dto) {
        T entity = toCreateEntity(dto);
        applyCreateDefaults(entity);
        service().save(entity);
        return Result.ok(toVO(entity));
    }

    @Operation(summary = "更新")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable String id, @Valid @RequestBody U dto) {
        T entity = toUpdateEntity(dto);
        setEntityId(entity, id);
        applyUpdateDefaults(entity);
        return Result.ok(service().updateById(entity));
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(service().removeById(id));
    }

    @Operation(summary = "批量删除")
    @DeleteMapping
    public Result<Boolean> deleteBatch(@RequestParam("ids") String ids) {
        return Result.ok(service().removeByIds(Arrays.asList(ids.split(","))));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public Result<V> detail(@PathVariable String id) {
        return Result.ok(toVO(service().getById(id)));
    }
}
