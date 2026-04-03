package com.hiking.treasure.modules.phase1.controller;

import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.modules.phase1.domain.convert.DecisionActionLogConvert;
import com.hiking.treasure.modules.phase1.domain.convert.DecisionItemConvert;
import com.hiking.treasure.modules.phase1.domain.dto.action.DecisionActionRequestDTO;
import com.hiking.treasure.modules.phase1.domain.dto.command.BudgetImpactSuggestionApplyDTO;
import com.hiking.treasure.modules.phase1.domain.dto.command.DecisionBudgetReviewApplyDTO;
import com.hiking.treasure.modules.phase1.domain.dto.create.DecisionItemCreateDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.DecisionItemQueryDTO;
import com.hiking.treasure.modules.phase1.domain.dto.update.DecisionItemUpdateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.BudgetImpactSuggestionVO;
import com.hiking.treasure.modules.phase1.domain.vo.DecisionActionLogVO;
import com.hiking.treasure.modules.phase1.domain.vo.DecisionBudgetReviewVO;
import com.hiking.treasure.modules.phase1.domain.vo.DecisionItemVO;
import com.hiking.treasure.modules.phase1.entity.DecisionItem;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.DecisionItemStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.DecisionSourceType;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.DecisionItemType;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.DecisionPriority;
import com.hiking.treasure.modules.phase1.service.AgentRuntimeSuggestionService;
import com.hiking.treasure.modules.phase1.service.DecisionItemService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "AICoOS 决策事项", description = "AICoOS 决策事项基础 CRUD 接口")
@RestController
@RequestMapping("/api/aicoos/decisionItem")
@PreAuthorize("hasRole('ADMIN')")
public class DecisionItemController extends AbstractPhase1CrudController<
        DecisionItem, DecisionItemService, DecisionItemCreateDTO, DecisionItemUpdateDTO, DecisionItemQueryDTO,
        DecisionItemVO> {

    @Resource
    private DecisionItemService decisionItemService;

    @Resource
    private DecisionItemConvert decisionItemConvert;

    @Resource
    private DecisionActionLogConvert decisionActionLogConvert;

    @Resource
    private AgentRuntimeSuggestionService agentRuntimeSuggestionService;

    @Operation(summary = "决策动作历史")
    @GetMapping("/{id}/actions")
    public Result<List<DecisionActionLogVO>> actionHistory(@PathVariable String id) {
        return Result.ok(decisionActionLogConvert.toVOs(decisionItemService.listActionLogs(id)));
    }

    @Operation(summary = "执行决策动作")
    @PostMapping("/{id}/actions")
    public Result<DecisionItemVO> actOnDecision(
            @PathVariable String id,
            @Valid @RequestBody DecisionActionRequestDTO dto) {
        return Result.ok(decisionItemConvert.toVO(decisionItemService.takeAction(id, dto)));
    }

    @Operation(summary = "生成预算影响建议")
    @PostMapping("/{id}/budget-impact-suggestion")
    public Result<BudgetImpactSuggestionVO> generateBudgetImpactSuggestion(@PathVariable String id) {
        return Result.ok(agentRuntimeSuggestionService.generateDecisionBudgetImpactSuggestion(id));
    }

    @Operation(summary = "生成决策与预算协作审阅")
    @PostMapping("/{id}/decision-budget-review")
    public Result<DecisionBudgetReviewVO> generateDecisionBudgetReview(@PathVariable String id) {
        return Result.ok(agentRuntimeSuggestionService.generateDecisionBudgetReview(id));
    }

    @Operation(summary = "应用决策与预算协作审阅")
    @PostMapping("/{id}/decision-budget-review/apply")
    public Result<DecisionItemVO> applyDecisionBudgetReview(
            @PathVariable String id,
            @Valid @RequestBody DecisionBudgetReviewApplyDTO dto) {
        return Result.ok(decisionItemConvert.toVO(agentRuntimeSuggestionService.applyDecisionBudgetReview(id, dto)));
    }

    @Operation(summary = "应用预算影响建议")
    @PostMapping("/{id}/budget-impact-suggestion/apply")
    public Result<DecisionItemVO> applyBudgetImpactSuggestion(
            @PathVariable String id,
            @Valid @RequestBody BudgetImpactSuggestionApplyDTO dto) {
        return Result.ok(decisionItemConvert.toVO(
                agentRuntimeSuggestionService.applyDecisionBudgetImpactSuggestion(id, dto)));
    }

    @Override
    protected DecisionItem toCreateEntity(DecisionItemCreateDTO dto) {
        return decisionItemConvert.toEntity(dto);
    }

    @Override
    protected DecisionItem toUpdateEntity(DecisionItemUpdateDTO dto) {
        return decisionItemConvert.toEntity(dto);
    }

    @Override
    protected DecisionItem toQueryEntity(DecisionItemQueryDTO dto) {
        return decisionItemConvert.toEntity(dto);
    }

    @Override
    protected DecisionItemVO toVO(DecisionItem entity) {
        return decisionItemConvert.toVO(entity);
    }

    @Override
    protected List<DecisionItemVO> toVOs(List<DecisionItem> entities) {
        return decisionItemConvert.toVOs(entities);
    }

    @Override
    protected void setEntityId(DecisionItem entity, String id) {
        entity.setId(id);
    }

    @Override
    protected void applyCreateDefaults(DecisionItem entity) {
        if (entity.getItemType() == null || entity.getItemType().isBlank()) {
            entity.setItemType(DecisionItemType.CLARIFICATION.getCode());
        }
        if (entity.getSourceType() == null || entity.getSourceType().isBlank()) {
            entity.setSourceType(DecisionSourceType.MANUAL.getCode());
        }
        if (entity.getPriority() == null || entity.getPriority().isBlank()) {
            entity.setPriority(DecisionPriority.MEDIUM.getCode());
        }
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus(DecisionItemStatus.OPEN.getCode());
        }
        if (entity.getBlockerFlag() == null) {
            entity.setBlockerFlag(0);
        }
    }

    @Override
    protected DecisionItemService service() {
        return decisionItemService;
    }
}
