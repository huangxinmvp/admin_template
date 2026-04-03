package com.hiking.treasure.modules.phase1.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.modules.phase1.domain.dto.command.AgentRoleCenterSaveDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.AgentRoleCenterQueryDTO;
import com.hiking.treasure.modules.phase1.domain.vo.AgentRoleCenterDetailVO;
import com.hiking.treasure.modules.phase1.domain.vo.AgentRoleCenterListVO;
import com.hiking.treasure.modules.phase1.service.AgentRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AICoOS Agent 角色中心", description = "AICoOS Agent 角色中心聚合接口")
@RestController
@RequestMapping("/api/aicoos/agentRole/center")
@PreAuthorize("hasRole('ADMIN')")
public class AgentRoleCenterController {

    @Resource
    private AgentRoleService agentRoleService;

    @Operation(summary = "Agent 角色中心分页")
    @GetMapping("/page")
    public Result<Page<AgentRoleCenterListVO>> page(
            @Valid AgentRoleCenterQueryDTO dto,
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(agentRoleService.pageAgentRoleCenter(dto, pageNo, pageSize));
    }

    @Operation(summary = "Agent 角色中心详情")
    @GetMapping("/{id}")
    public Result<AgentRoleCenterDetailVO> detail(@PathVariable String id) {
        return Result.ok(agentRoleService.getAgentRoleCenterDetail(id));
    }

    @Operation(summary = "创建 Agent 角色")
    @PostMapping
    public Result<AgentRoleCenterDetailVO> create(@RequestBody AgentRoleCenterSaveDTO dto) {
        return Result.ok(agentRoleService.createAgentRoleCenter(dto));
    }

    @Operation(summary = "更新 Agent 角色")
    @PutMapping("/{id}")
    public Result<AgentRoleCenterDetailVO> update(
            @PathVariable String id,
            @RequestBody AgentRoleCenterSaveDTO dto) {
        return Result.ok(agentRoleService.updateAgentRoleCenter(id, dto));
    }
}
