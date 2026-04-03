package com.hiking.treasure.modules.phase1.controller;

import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.modules.phase1.domain.dto.command.FigmaContextPreviewDTO;
import com.hiking.treasure.modules.phase1.domain.dto.command.LinearRepresentationPreviewDTO;
import com.hiking.treasure.modules.phase1.domain.dto.command.LinearWritePreviewDTO;
import com.hiking.treasure.modules.phase1.domain.vo.FigmaContextPreviewVO;
import com.hiking.treasure.modules.phase1.domain.vo.LinearRepresentationPreviewVO;
import com.hiking.treasure.modules.phase1.domain.vo.LinearWritePreviewVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectToolBindingVO;
import com.hiking.treasure.modules.phase1.domain.vo.ToolIntegrationAuditVO;
import com.hiking.treasure.modules.phase1.service.ToolIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "AICoOS 工具集成", description = "AICoOS 外部工具预览、写入和上下文绑定接口")
@RestController
@RequestMapping("/api/aicoos/toolIntegration")
@PreAuthorize("hasRole('ADMIN')")
public class ToolIntegrationController {

    @Resource
    private ToolIntegrationService toolIntegrationService;

    @Operation(summary = "按项目查询工具绑定")
    @GetMapping("/project/{projectId}/bindings")
    public Result<List<ProjectToolBindingVO>> listProjectBindings(@PathVariable String projectId) {
        return Result.ok(toolIntegrationService.listProjectBindings(projectId));
    }

    @Operation(summary = "按项目查询集成审计")
    @GetMapping("/project/{projectId}/audits")
    public Result<List<ToolIntegrationAuditVO>> listProjectAudits(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "8") int limit) {
        return Result.ok(toolIntegrationService.listProjectAudits(projectId, limit));
    }

    @Operation(summary = "预览 Linear 项目映射")
    @PostMapping("/project/{projectId}/linear/representation/preview")
    public Result<LinearRepresentationPreviewVO> previewLinearRepresentation(
            @PathVariable String projectId,
            @Valid @RequestBody LinearRepresentationPreviewDTO dto) {
        return Result.ok(toolIntegrationService.previewLinearProjectRepresentation(projectId, dto));
    }

    @Operation(summary = "应用 Linear 项目映射")
    @PostMapping("/project/{projectId}/linear/representation/apply")
    public Result<ProjectToolBindingVO> applyLinearRepresentation(
            @PathVariable String projectId,
            @Valid @RequestBody LinearRepresentationPreviewDTO dto) {
        return Result.ok(toolIntegrationService.applyLinearProjectRepresentation(projectId, dto));
    }

    @Operation(summary = "预览澄清项的 Linear 写入")
    @PostMapping("/clarification/{clarificationItemId}/linear/preview")
    public Result<LinearWritePreviewVO> previewClarificationLinearWrite(
            @PathVariable String clarificationItemId,
            @Valid @RequestBody LinearWritePreviewDTO dto) {
        return Result.ok(toolIntegrationService.previewClarificationLinearWrite(clarificationItemId, dto));
    }

    @Operation(summary = "应用澄清项的 Linear 写入")
    @PostMapping("/clarification/{clarificationItemId}/linear/apply")
    public Result<ToolIntegrationAuditVO> applyClarificationLinearWrite(
            @PathVariable String clarificationItemId,
            @Valid @RequestBody LinearWritePreviewDTO dto) {
        return Result.ok(toolIntegrationService.applyClarificationLinearWrite(clarificationItemId, dto));
    }

    @Operation(summary = "预览决策事项的 Linear 写入")
    @PostMapping("/decision/{decisionItemId}/linear/preview")
    public Result<LinearWritePreviewVO> previewDecisionLinearWrite(
            @PathVariable String decisionItemId,
            @Valid @RequestBody LinearWritePreviewDTO dto) {
        return Result.ok(toolIntegrationService.previewDecisionLinearWrite(decisionItemId, dto));
    }

    @Operation(summary = "应用决策事项的 Linear 写入")
    @PostMapping("/decision/{decisionItemId}/linear/apply")
    public Result<ToolIntegrationAuditVO> applyDecisionLinearWrite(
            @PathVariable String decisionItemId,
            @Valid @RequestBody LinearWritePreviewDTO dto) {
        return Result.ok(toolIntegrationService.applyDecisionLinearWrite(decisionItemId, dto));
    }

    @Operation(summary = "预览 Figma 上下文")
    @PostMapping("/project/{projectId}/figma/context/preview")
    public Result<FigmaContextPreviewVO> previewFigmaContext(
            @PathVariable String projectId,
            @Valid @RequestBody FigmaContextPreviewDTO dto) {
        return Result.ok(toolIntegrationService.previewFigmaContext(projectId, dto));
    }

    @Operation(summary = "应用 Figma 上下文绑定")
    @PostMapping("/project/{projectId}/figma/context/apply")
    public Result<ProjectToolBindingVO> applyFigmaContext(
            @PathVariable String projectId,
            @Valid @RequestBody FigmaContextPreviewDTO dto) {
        return Result.ok(toolIntegrationService.applyFigmaContext(projectId, dto));
    }
}
