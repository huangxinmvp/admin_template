package com.hiking.treasure.controller;

import java.util.Arrays;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.domain.dto.UserLockDTO;
import com.hiking.treasure.domain.dto.UserPasswordResetDTO;
import com.hiking.treasure.entity.User;
import com.hiking.treasure.service.UserService;
import com.hiking.treasure.domain.dto.create.UserCreateDTO;
import com.hiking.treasure.domain.dto.update.UserUpdateDTO;
import com.hiking.treasure.domain.dto.query.UserQueryDTO;
import com.hiking.treasure.domain.vo.UserVO;
import com.hiking.treasure.domain.convert.UserConvert;
import com.hiking.treasure.domain.vo.system.UserDetailVO;
import com.hiking.treasure.domain.vo.system.PasswordActionResultVO;
import com.hiking.treasure.common.web.BaseController;
import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.common.util.PasswordUtil;
import com.hiking.treasure.common.util.PasswordPolicyValidator;
import com.hiking.treasure.service.UserSessionService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
/**
 * 用户表 - 控制器
 */
@Tag(name = "用户表", description = "用户表接口" )
@RestController
@RequestMapping("/api/user" )
public class UserController extends BaseController<User, UserService>{

    @Resource
    private UserService userService;

    @Resource
    private UserConvert userConvert;

    @Resource
    private PasswordUtil passwordUtil;

    @Resource
    private PasswordPolicyValidator passwordPolicyValidator;

    @Resource
    private UserSessionService userSessionService;


    @Operation(summary = "分页查询" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:user:view')")
    @GetMapping("/page" )
    public Result<Page<UserVO>> page(@Valid UserQueryDTO dto, @RequestParam(defaultValue = "1") long pageNo, @RequestParam(defaultValue = "10") long pageSize) {
    LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
    User cond = userConvert.toEntity(dto);
    qw.setEntity(cond);

    Page<User> page = new Page<>(pageNo, pageSize);
    IPage<User> entityPage = userService.page(page, qw);

    List<UserVO> voList = userService.fillUserSummary(userConvert.toVOs(entityPage.getRecords()));
    Page<UserVO> voPage = new Page<>(pageNo, pageSize, entityPage.getTotal());
    voPage.setRecords(voList);

    return Result.ok(voPage);
}

    @Operation(summary = "新增" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:user:create')")
    @PostMapping
    public Result<UserVO> create(@Valid @RequestBody UserCreateDTO dto) {
        User entity = userConvert.toEntity(dto);
        if (entity.getPassword() != null && !entity.getPassword().isBlank()) {
            passwordPolicyValidator.validateOrThrow(entity.getPassword());
            entity.setPassword(passwordUtil.encode(entity.getPassword()));
            entity.setPwdUpdateTime(LocalDateTime.now());
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        userService.saveUserWithRelations(entity, dto.getRoleIds(), dto.getDepartIds());
        return Result.ok(userConvert.toVO(entity));
    }

    @Operation(summary = "更新" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:user:update')")
    @PutMapping("/{id}" )
    public Result<Boolean> update(@PathVariable String id,
            @Valid @RequestBody UserUpdateDTO dto) {
        User entity = userConvert.toEntity(dto);
        if (entity.getPassword() != null && !entity.getPassword().isBlank()) {
            passwordPolicyValidator.validateOrThrow(entity.getPassword());
            entity.setPassword(passwordUtil.encode(entity.getPassword()));
            entity.setPwdUpdateTime(LocalDateTime.now());
        } else {
            entity.setPassword(null);
        }
        entity.setId(id);
        return Result.ok(userService.updateUserWithRelations(entity, dto.getRoleIds(), dto.getDepartIds()));
    }

    @Operation(summary = "删除" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:user:delete')")
    @DeleteMapping("/{id}" )
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(userService.deleteUserById(id));
    }

    @Operation(summary = "批量删除" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:user:delete')")
    @DeleteMapping
    public Result<Boolean> deleteBatch(@RequestParam("ids") String ids) {
        return Result.ok(userService.deleteUsersByIds(Arrays.asList(ids.split(","))));
    }

    @Operation(summary = "详情" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:user:view')")
    @GetMapping("/{id}" )
    public Result<UserVO> detail(@PathVariable String id) {
        User entity = userService.getById(id);
        return Result.ok(userConvert.toVO(entity));
    }

    @Operation(summary = "聚合详情")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:user:view')")
    @GetMapping("/{id}/aggregate")
    public Result<UserDetailVO> aggregate(@PathVariable String id) {
        return Result.ok(userService.getDetailAggregate(id));
    }

    @Operation(summary = "重置密码")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:user:update')")
    @PutMapping("/{id}/password")
    public Result<PasswordActionResultVO> resetPassword(@PathVariable String id,
                                                        @Valid @RequestBody UserPasswordResetDTO dto) {
        passwordPolicyValidator.validateOrThrow(dto.getPassword());
        userService.resetPassword(id, passwordUtil.encode(dto.getPassword()));
        userSessionService.revokeAllSessionsByUserId(id, "system", "管理员重置密码");
        return Result.ok(PasswordActionResultVO.of(true, "密码已重置，用户需重新登录"));
    }

    @Operation(summary = "锁定用户")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:user:update')")
    @PutMapping("/{id}/lock")
    public Result<Boolean> lock(@PathVariable String id,
                                @Valid @RequestBody UserLockDTO dto) {
        boolean locked = userService.lockUser(id, dto.getLockUntil());
        if (locked) {
            userSessionService.revokeAllSessionsByUserId(id, "system", "管理员锁定账户");
        }
        return Result.ok(locked);
    }

    @Operation(summary = "解除锁定")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:user:update')")
    @PutMapping("/{id}/unlock")
    public Result<Boolean> unlock(@PathVariable String id) {
        return Result.ok(userService.unlockUser(id));
    }

    @Operation(summary = "导出Excel" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:user:view')")
    @GetMapping("/exportXls" )
    public ModelAndView exportXls(HttpServletRequest request, UserQueryDTO dto) {
        User cond = userConvert.toEntity(dto);
        String exportFields = null;
        Integer pageNumPerSheet = 500;
        return super.exportXlsSheet(request, cond, User.class, "用户表", exportFields, pageNumPerSheet);
    }

    @Operation(summary = "导入Excel" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:user:create')")
    @PostMapping("/importExcel" )
    public Result<String> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, User.class);
    }

    @Override
    protected UserService service() {
        return this.userService;
    }
}
