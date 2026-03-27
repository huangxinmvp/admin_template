package com.hiking.treasure.controller;

import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.domain.dto.WorkflowDraftSaveDTO;
import com.hiking.treasure.domain.dto.create.WorkflowStartProcessDTO;
import com.hiking.treasure.domain.dto.update.WorkflowTaskCompleteDTO;
import com.hiking.treasure.domain.vo.workflow.WorkflowDefinitionVO;
import com.hiking.treasure.domain.vo.workflow.WorkflowDeploymentVO;
import com.hiking.treasure.domain.vo.workflow.WorkflowDraftVO;
import com.hiking.treasure.domain.vo.workflow.WorkflowInstanceVO;
import com.hiking.treasure.domain.vo.workflow.WorkflowProcessInstanceDetailVO;
import com.hiking.treasure.domain.vo.workflow.WorkflowTaskVO;
import com.hiking.treasure.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
@Tag(name = "流程引擎", description = "Flowable 流程定义、实例与任务接口")
public class WorkflowController {

    private final WorkflowService workflowService;

    @Operation(summary = "流程定义列表")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/definitions")
    public Result<List<WorkflowDefinitionVO>> listDefinitions(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "name", required = false) String name) {
        return Result.ok(workflowService.listDefinitions(key, name));
    }

    @Operation(summary = "获取流程定义 BPMN XML")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/definitions/{definitionId}/xml")
    public Result<String> getDefinitionXml(@PathVariable String definitionId) {
        Result<String> result = Result.ok();
        result.setResult(workflowService.getDefinitionXml(definitionId));
        return result;
    }

    @Operation(summary = "流程设计草稿列表")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/drafts")
    public Result<List<WorkflowDraftVO>> listDrafts(
            @RequestParam(value = "keyword", required = false) String keyword) {
        return Result.ok(workflowService.listDrafts(keyword));
    }

    @Operation(summary = "获取流程设计草稿")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/drafts/{draftId}")
    public Result<WorkflowDraftVO> getDraft(@PathVariable String draftId) {
        return Result.ok(workflowService.getDraft(draftId));
    }

    @Operation(summary = "保存流程设计草稿")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/drafts")
    public Result<WorkflowDraftVO> saveDraft(@Valid @RequestBody WorkflowDraftSaveDTO dto) {
        return Result.ok(workflowService.saveDraft(dto));
    }

    @Operation(summary = "删除流程设计草稿")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/drafts/{draftId}")
    public Result<Boolean> deleteDraft(@PathVariable String draftId) {
        return Result.ok(workflowService.deleteDraft(draftId));
    }

    @Operation(summary = "部署 BPMN 流程定义")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/deployments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<WorkflowDeploymentVO> deploy(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "category", required = false) String category) {
        return Result.ok(workflowService.deployProcess(name, category, file));
    }

    @Operation(summary = "删除部署")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deployments/{deploymentId}")
    public Result<Boolean> deleteDeployment(
            @PathVariable String deploymentId,
            @RequestParam(value = "cascade", defaultValue = "true") boolean cascade) {
        return Result.ok(workflowService.deleteDeployment(deploymentId, cascade));
    }

    @Operation(summary = "启动流程实例")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/instances")
    public Result<WorkflowInstanceVO> startProcess(@Valid @RequestBody WorkflowStartProcessDTO dto) {
        return Result.ok(workflowService.startProcess(dto));
    }

    @Operation(summary = "我发起的流程实例")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/instances/my-started")
    public Result<List<WorkflowInstanceVO>> myStartedInstances() {
        return Result.ok(workflowService.listMyStartedInstances());
    }

    @Operation(summary = "流程实例详情")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/instances/{processInstanceId}")
    public Result<WorkflowProcessInstanceDetailVO> processInstanceDetail(@PathVariable String processInstanceId) {
        return Result.ok(workflowService.getProcessInstanceDetail(processInstanceId));
    }

    @Operation(summary = "我的待办任务")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/tasks/todo")
    public Result<List<WorkflowTaskVO>> myTodoTasks() {
        return Result.ok(workflowService.listMyTodoTasks());
    }

    @Operation(summary = "我的已办任务")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/tasks/done")
    public Result<List<WorkflowTaskVO>> myDoneTasks() {
        return Result.ok(workflowService.listMyDoneTasks());
    }

    @Operation(summary = "办理任务")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/tasks/{taskId}/complete")
    public Result<Boolean> completeTask(
            @PathVariable String taskId,
            @RequestBody(required = false) WorkflowTaskCompleteDTO dto) {
        return Result.ok(workflowService.completeTask(taskId, dto == null ? new WorkflowTaskCompleteDTO() : dto));
    }
}
