package com.hiking.treasure.modules.phase1.controller;

import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.modules.phase1.domain.convert.ClarificationItemConvert;
import com.hiking.treasure.modules.phase1.domain.convert.DecisionItemConvert;
import com.hiking.treasure.modules.phase1.domain.dto.command.ApplyDecisionSuggestionsDTO;
import com.hiking.treasure.modules.phase1.domain.dto.create.ClarificationItemCreateDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.ClarificationItemQueryDTO;
import com.hiking.treasure.modules.phase1.domain.dto.update.ClarificationItemUpdateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.ClarificationItemVO;
import com.hiking.treasure.modules.phase1.domain.vo.DecisionPromotionSuggestionVO;
import com.hiking.treasure.modules.phase1.domain.vo.DecisionItemVO;
import com.hiking.treasure.modules.phase1.entity.ClarificationItem;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ClarificationSeverity;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ClarificationStatus;
import com.hiking.treasure.modules.phase1.service.AgentRuntimeSuggestionService;
import com.hiking.treasure.modules.phase1.service.ClarificationItemService;
import com.hiking.treasure.modules.phase1.service.DecisionItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "AICoOS 澄清项", description = "AICoOS 项目澄清项基础 CRUD 接口")
@RestController
@RequestMapping("/api/aicoos/clarificationItem")
@PreAuthorize("hasRole('ADMIN')")
public class ClarificationItemController extends AbstractPhase1CrudController<
        ClarificationItem, ClarificationItemService, ClarificationItemCreateDTO, ClarificationItemUpdateDTO,
        ClarificationItemQueryDTO, ClarificationItemVO> {

    @Resource
    private ClarificationItemService clarificationItemService;

    @Resource
    private ClarificationItemConvert clarificationItemConvert;

    @Resource
    private DecisionItemService decisionItemService;

    @Resource
    private DecisionItemConvert decisionItemConvert;

    @Resource
    private AgentRuntimeSuggestionService agentRuntimeSuggestionService;

    @Operation(summary = "生成示例澄清项")
    @PostMapping("/project/{projectId}/mock-generate")
    public Result<List<ClarificationItemVO>> generateMockItems(@PathVariable String projectId) {
        return Result.ok(clarificationItemConvert.toVOs(clarificationItemService.generateMockItems(projectId)));
    }

    @Operation(summary = "提升为正式决策事项")
    @PostMapping("/{id}/promote")
    public Result<DecisionItemVO> promoteToDecision(@PathVariable String id) {
        return Result.ok(decisionItemConvert.toVO(decisionItemService.promoteFromClarification(id)));
    }

    @Operation(summary = "分析待提升的决策建议")
    @PostMapping("/project/{projectId}/decision-suggestions")
    public Result<List<DecisionPromotionSuggestionVO>> generateDecisionSuggestions(@PathVariable String projectId) {
        return Result.ok(agentRuntimeSuggestionService.generateDecisionSuggestions(projectId));
    }

    @Operation(summary = "应用决策提升建议")
    @PostMapping("/project/{projectId}/decision-suggestions/apply")
    public Result<List<DecisionItemVO>> applyDecisionSuggestions(
            @PathVariable String projectId,
            @Valid @RequestBody ApplyDecisionSuggestionsDTO dto) {
        return Result.ok(decisionItemConvert.toVOs(
                agentRuntimeSuggestionService.applyDecisionSuggestions(projectId, dto)));
    }

    @Override
    protected ClarificationItem toCreateEntity(ClarificationItemCreateDTO dto) {
        return clarificationItemConvert.toEntity(dto);
    }

    @Override
    protected ClarificationItem toUpdateEntity(ClarificationItemUpdateDTO dto) {
        return clarificationItemConvert.toEntity(dto);
    }

    @Override
    protected ClarificationItem toQueryEntity(ClarificationItemQueryDTO dto) {
        return clarificationItemConvert.toEntity(dto);
    }

    @Override
    protected ClarificationItemVO toVO(ClarificationItem entity) {
        return clarificationItemConvert.toVO(entity);
    }

    @Override
    protected List<ClarificationItemVO> toVOs(List<ClarificationItem> entities) {
        return clarificationItemConvert.toVOs(entities);
    }

    @Override
    protected void setEntityId(ClarificationItem entity, String id) {
        entity.setId(id);
    }

    @Override
    protected void applyCreateDefaults(ClarificationItem entity) {
        if (entity.getSeverity() == null || entity.getSeverity().isBlank()) {
            entity.setSeverity(ClarificationSeverity.MEDIUM.getCode());
        }
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus(ClarificationStatus.OPEN.getCode());
        }
        if (entity.getGeneratedFlag() == null) {
            entity.setGeneratedFlag(0);
        }
    }

    @Override
    protected ClarificationItemService service() {
        return clarificationItemService;
    }
}
