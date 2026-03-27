package com.hiking.treasure.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.common.web.BaseController;
import com.hiking.treasure.domain.convert.AnnouncementConvert;
import com.hiking.treasure.domain.dto.create.AnnouncementCreateDTO;
import com.hiking.treasure.domain.dto.query.AnnouncementQueryDTO;
import com.hiking.treasure.domain.dto.update.AnnouncementUpdateDTO;
import com.hiking.treasure.domain.vo.AnnouncementVO;
import com.hiking.treasure.entity.Announcement;
import com.hiking.treasure.service.AnnouncementService;
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
 * 系统通告 - 控制器
 */
@Tag(name = "系统通告", description = "系统通告接口")
@RestController
@RequestMapping("/api/announcement")
public class AnnouncementController extends BaseController<Announcement, AnnouncementService> {

    @Resource
    private AnnouncementService announcementService;

    @Resource
    private AnnouncementConvert announcementConvert;

    @Operation(summary = "分页查询")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:announcement:view')")
    @GetMapping("/page")
    public Result<Page<AnnouncementVO>> page(@Valid AnnouncementQueryDTO dto,
                                             @RequestParam(defaultValue = "1") long pageNo,
                                             @RequestParam(defaultValue = "10") long pageSize) {
        LambdaQueryWrapper<Announcement> qw = new LambdaQueryWrapper<>();
        qw.setEntity(announcementConvert.toEntity(dto));
        qw.orderByDesc(Announcement::getCreateTime);

        Page<Announcement> page = new Page<>(pageNo, pageSize);
        IPage<Announcement> entityPage = announcementService.page(page, qw);

        Page<AnnouncementVO> voPage = new Page<>(pageNo, pageSize, entityPage.getTotal());
        voPage.setRecords(announcementConvert.toVOs(entityPage.getRecords()));
        return Result.ok(voPage);
    }

    @Operation(summary = "我的收件箱")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/inbox/page")
    public Result<Page<AnnouncementVO>> inbox(@RequestParam(defaultValue = "1") long pageNo,
                                              @RequestParam(defaultValue = "10") long pageSize,
                                              @RequestParam(defaultValue = "false") boolean unreadOnly) {
        return Result.ok(announcementService.pageInbox(pageNo, pageSize, unreadOnly));
    }

    @Operation(summary = "未读数量")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/unreadCount")
    public Result<Long> unreadCount() {
        return Result.ok(announcementService.unreadCount());
    }

    @Operation(summary = "新增")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:announcement:create')")
    @PostMapping
    public Result<AnnouncementVO> create(@Valid @RequestBody AnnouncementCreateDTO dto) {
        Announcement entity = announcementConvert.toEntity(dto);
        if (entity.getSendStatus() == null) {
            entity.setSendStatus(0);
        }
        announcementService.save(entity);
        return Result.ok(announcementConvert.toVO(entity));
    }

    @Operation(summary = "更新")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:announcement:update')")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable String id, @Valid @RequestBody AnnouncementUpdateDTO dto) {
        Announcement entity = announcementConvert.toEntity(dto);
        entity.setId(id);
        return Result.ok(announcementService.updateById(entity));
    }

    @Operation(summary = "发布")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:announcement:publish')")
    @PostMapping("/{id}/publish")
    public Result<Boolean> publish(@PathVariable String id) {
        return Result.ok(announcementService.publish(id));
    }

    @Operation(summary = "撤销")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:announcement:publish')")
    @PostMapping("/{id}/revoke")
    public Result<Boolean> revoke(@PathVariable String id) {
        return Result.ok(announcementService.revoke(id));
    }

    @Operation(summary = "标记已读")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/read")
    public Result<Boolean> read(@PathVariable String id) {
        return Result.ok(announcementService.markRead(id));
    }

    @Operation(summary = "删除")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:announcement:delete')")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(announcementService.removeById(id));
    }

    @Operation(summary = "批量删除")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:announcement:delete')")
    @DeleteMapping
    public Result<Boolean> deleteBatch(@RequestParam("ids") String ids) {
        return Result.ok(announcementService.removeByIds(Arrays.asList(ids.split(","))));
    }

    @Operation(summary = "详情")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public Result<AnnouncementVO> detail(@PathVariable String id,
                                         @RequestParam(defaultValue = "true") boolean autoRead) {
        return Result.ok(announcementService.getDetail(id, autoRead));
    }

    @Operation(summary = "导出Excel")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:announcement:view')")
    @GetMapping("/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AnnouncementQueryDTO dto) {
        return super.exportXlsSheet(request, announcementConvert.toEntity(dto), Announcement.class, "系统通告", null, 500);
    }

    @Operation(summary = "导入Excel")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:announcement:create')")
    @PostMapping("/importExcel")
    public Result<String> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, Announcement.class);
    }

    @Override
    protected AnnouncementService service() {
        return announcementService;
    }
}
