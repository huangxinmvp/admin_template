package com.hiking.treasure.controller;

import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.common.diagnostics.IntegrationDiagnostics;
import com.hiking.treasure.common.web.RequestCorrelation;
import com.hiking.treasure.domain.vo.system.CurrentUserProfileVO;
import com.hiking.treasure.domain.vo.system.DiagnosticsSnapshotVO;
import com.hiking.treasure.domain.vo.system.DashboardStatsVO;
import com.hiking.treasure.domain.vo.system.MenuTreeVO;
import com.hiking.treasure.domain.vo.system.SystemHealthVO;
import com.hiking.treasure.service.SystemHealthService;
import com.hiking.treasure.service.SystemPortalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "系统门户")
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemPortalController {

    private final SystemPortalService systemPortalService;
    private final SystemHealthService systemHealthService;

    @Operation(summary = "系统健康检查")
    @GetMapping("/health")
    public ResponseEntity<SystemHealthVO> health() {
        SystemHealthVO health = systemHealthService.getHealth();
        if (Boolean.TRUE.equals(health.getDatabaseReady())) {
            return ResponseEntity.ok(health);
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
    }

    @Operation(summary = "当前用户资料")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public Result<CurrentUserProfileVO> profile() {
        return Result.ok(systemPortalService.getCurrentUserProfile());
    }

    @Operation(summary = "当前用户菜单树")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/menus")
    public Result<List<MenuTreeVO>> menus() {
        return Result.ok(systemPortalService.getCurrentUserMenus());
    }

    @Operation(summary = "后台首页统计")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:dashboard:view')")
    @GetMapping("/dashboard")
    public Result<DashboardStatsVO> dashboard() {
        return Result.ok(systemPortalService.getDashboardStats());
    }

    @Operation(summary = "基础诊断快照")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/diagnostics")
    public Result<DiagnosticsSnapshotVO> diagnostics() {
        return Result.ok(IntegrationDiagnostics.snapshot(RequestCorrelation.currentRequestId()));
    }
}
