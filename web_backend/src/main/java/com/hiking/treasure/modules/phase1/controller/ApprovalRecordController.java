package com.hiking.treasure.modules.phase1.controller;

import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.common.security.SecurityUtils;
import com.hiking.treasure.modules.phase1.domain.convert.ApprovalActionLogConvert;
import com.hiking.treasure.modules.phase1.domain.convert.ApprovalRecordConvert;
import com.hiking.treasure.modules.phase1.domain.dto.action.ApprovalActionRequestDTO;
import com.hiking.treasure.modules.phase1.domain.dto.create.ApprovalRecordCreateDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.ApprovalRecordQueryDTO;
import com.hiking.treasure.modules.phase1.domain.dto.update.ApprovalRecordUpdateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.ApprovalActionLogVO;
import com.hiking.treasure.modules.phase1.domain.vo.ApprovalRecordVO;
import com.hiking.treasure.modules.phase1.entity.ApprovalRecord;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ApprovalSourceObjectType;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ApprovalStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ApprovalType;
import com.hiking.treasure.modules.phase1.service.ApprovalRecordService;
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

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "AICoOS 审批记录", description = "AICoOS 审批记录基础 CRUD 接口")
@RestController
@RequestMapping("/api/aicoos/approvalRecord")
@PreAuthorize("hasRole('ADMIN')")
public class ApprovalRecordController extends AbstractPhase1CrudController<
        ApprovalRecord, ApprovalRecordService, ApprovalRecordCreateDTO, ApprovalRecordUpdateDTO,
        ApprovalRecordQueryDTO, ApprovalRecordVO> {

    @Resource
    private ApprovalRecordService approvalRecordService;

    @Resource
    private ApprovalRecordConvert approvalRecordConvert;

    @Resource
    private ApprovalActionLogConvert approvalActionLogConvert;

    @Operation(summary = "审批动作历史")
    @GetMapping("/{id}/actions")
    public Result<List<ApprovalActionLogVO>> actionHistory(@PathVariable String id) {
        return Result.ok(approvalActionLogConvert.toVOs(approvalRecordService.listActionLogs(id)));
    }

    @Operation(summary = "执行审批动作")
    @PostMapping("/{id}/actions")
    public Result<ApprovalRecordVO> actOnApproval(
            @PathVariable String id,
            @Valid @RequestBody ApprovalActionRequestDTO dto) {
        return Result.ok(approvalRecordConvert.toVO(approvalRecordService.takeAction(id, dto)));
    }

    @Operation(summary = "按来源对象查询关联审批")
    @GetMapping("/source")
    public Result<List<ApprovalRecordVO>> listBySource(
            @RequestParam String sourceObjectType,
            @RequestParam String sourceObjectId) {
        return Result.ok(approvalRecordConvert.toVOs(
                approvalRecordService.listBySource(sourceObjectType, sourceObjectId)));
    }

    @Operation(summary = "由决策事项创建或打开关联审批")
    @PostMapping("/decisionItem/{decisionItemId}/link")
    public Result<ApprovalRecordVO> linkDecisionItem(@PathVariable String decisionItemId) {
        return Result.ok(approvalRecordConvert.toVO(
                approvalRecordService.createOrOpenForDecision(decisionItemId)));
    }

    @Operation(summary = "由预算计划创建或打开关联审批")
    @PostMapping("/budgetPlan/{budgetPlanId}/link")
    public Result<ApprovalRecordVO> linkBudgetPlan(@PathVariable String budgetPlanId) {
        return Result.ok(approvalRecordConvert.toVO(
                approvalRecordService.createOrOpenForBudgetPlan(budgetPlanId)));
    }

    @Override
    protected ApprovalRecord toCreateEntity(ApprovalRecordCreateDTO dto) {
        return approvalRecordConvert.toEntity(dto);
    }

    @Override
    protected ApprovalRecord toUpdateEntity(ApprovalRecordUpdateDTO dto) {
        return approvalRecordConvert.toEntity(dto);
    }

    @Override
    protected ApprovalRecord toQueryEntity(ApprovalRecordQueryDTO dto) {
        return approvalRecordConvert.toEntity(dto);
    }

    @Override
    protected ApprovalRecordVO toVO(ApprovalRecord entity) {
        return approvalRecordConvert.toVO(entity);
    }

    @Override
    protected List<ApprovalRecordVO> toVOs(List<ApprovalRecord> entities) {
        return approvalRecordConvert.toVOs(entities);
    }

    @Override
    protected void setEntityId(ApprovalRecord entity, String id) {
        entity.setId(id);
    }

    @Override
    protected void applyCreateDefaults(ApprovalRecord entity) {
        if (entity.getApprovalType() == null || entity.getApprovalType().isBlank()) {
            entity.setApprovalType(ApprovalType.REQUIREMENT.getCode());
        }
        if (entity.getSourceObjectType() == null || entity.getSourceObjectType().isBlank()) {
            entity.setSourceObjectType(ApprovalSourceObjectType.MANUAL.getCode());
        }
        if (entity.getApprovalStatus() == null || entity.getApprovalStatus().isBlank()) {
            entity.setApprovalStatus(ApprovalStatus.SUBMITTED.getCode());
        }
        if (entity.getTitle() == null || entity.getTitle().isBlank()) {
            entity.setTitle("审批事项");
        }
        if (entity.getBlockerFlag() == null) {
            entity.setBlockerFlag(0);
        }
        if (entity.getRequesterUserId() == null || entity.getRequesterUserId().isBlank()) {
            entity.setRequesterUserId(SecurityUtils.getUserId());
        }
        if (entity.getOperatorUserId() == null || entity.getOperatorUserId().isBlank()) {
            entity.setOperatorUserId(SecurityUtils.getUserId());
        }
        if (entity.getSubmittedAt() == null) {
            entity.setSubmittedAt(LocalDateTime.now());
        }
    }

    @Override
    protected ApprovalRecordService service() {
        return approvalRecordService;
    }
}
