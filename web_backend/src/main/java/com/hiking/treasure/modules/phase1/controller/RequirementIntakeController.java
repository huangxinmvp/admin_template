package com.hiking.treasure.modules.phase1.controller;

import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.modules.phase1.domain.convert.ClarificationItemConvert;
import com.hiking.treasure.modules.phase1.domain.convert.RequirementIntakeConvert;
import com.hiking.treasure.modules.phase1.domain.dto.command.ApplyClarificationSuggestionsDTO;
import com.hiking.treasure.modules.phase1.domain.dto.create.RequirementIntakeCreateDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.RequirementIntakeQueryDTO;
import com.hiking.treasure.modules.phase1.domain.dto.update.RequirementIntakeUpdateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.ClarificationItemVO;
import com.hiking.treasure.modules.phase1.domain.vo.ClarificationSuggestionVO;
import com.hiking.treasure.modules.phase1.domain.vo.RequirementClarificationReviewVO;
import com.hiking.treasure.modules.phase1.domain.vo.RequirementIntakeVO;
import com.hiking.treasure.modules.phase1.entity.RequirementIntake;
import com.hiking.treasure.modules.phase1.service.AgentRuntimeSuggestionService;
import com.hiking.treasure.modules.phase1.service.RequirementIntakeService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "AICoOS 需求接收", description = "AICoOS 项目需求接收接口")
@RestController
@RequestMapping("/api/aicoos/requirementIntake")
@PreAuthorize("hasRole('ADMIN')")
public class RequirementIntakeController extends AbstractPhase1CrudController<
        RequirementIntake, RequirementIntakeService, RequirementIntakeCreateDTO, RequirementIntakeUpdateDTO,
        RequirementIntakeQueryDTO, RequirementIntakeVO> {

    @Resource
    private RequirementIntakeService requirementIntakeService;

    @Resource
    private RequirementIntakeConvert requirementIntakeConvert;

    @Resource
    private ClarificationItemConvert clarificationItemConvert;

    @Resource
    private AgentRuntimeSuggestionService agentRuntimeSuggestionService;

    @Operation(summary = "按项目查询需求接收")
    @GetMapping("/project/{projectId}")
    public Result<RequirementIntakeVO> getByProjectId(@PathVariable String projectId) {
        return Result.ok(requirementIntakeConvert.toVO(requirementIntakeService.getByProjectId(projectId)));
    }

    @Operation(summary = "按项目保存需求接收")
    @PutMapping("/project/{projectId}")
    public Result<RequirementIntakeVO> saveProjectIntake(
            @PathVariable String projectId,
            @Valid @RequestBody RequirementIntakeCreateDTO dto) {
        RequirementIntake intake = requirementIntakeConvert.toEntity(dto);
        intake.setProjectId(projectId);
        return Result.ok(requirementIntakeConvert.toVO(requirementIntakeService.saveProjectIntake(projectId, intake)));
    }

    @Operation(summary = "生成澄清建议")
    @PostMapping("/project/{projectId}/clarification-suggestions")
    public Result<List<ClarificationSuggestionVO>> generateClarificationSuggestions(@PathVariable String projectId) {
        return Result.ok(agentRuntimeSuggestionService.generateClarificationSuggestions(projectId));
    }

    @Operation(summary = "生成需求澄清协作审阅")
    @PostMapping("/project/{projectId}/clarification-collaboration")
    public Result<RequirementClarificationReviewVO> generateClarificationCollaboration(
            @PathVariable String projectId) {
        return Result.ok(agentRuntimeSuggestionService.generateRequirementClarificationReview(projectId));
    }

    @Operation(summary = "应用澄清建议")
    @PostMapping("/project/{projectId}/clarification-suggestions/apply")
    public Result<List<ClarificationItemVO>> applyClarificationSuggestions(
            @PathVariable String projectId,
            @Valid @RequestBody ApplyClarificationSuggestionsDTO dto) {
        return Result.ok(clarificationItemConvert.toVOs(
                agentRuntimeSuggestionService.applyClarificationSuggestions(projectId, dto)));
    }

    @Override
    protected RequirementIntake toCreateEntity(RequirementIntakeCreateDTO dto) {
        return requirementIntakeConvert.toEntity(dto);
    }

    @Override
    protected RequirementIntake toUpdateEntity(RequirementIntakeUpdateDTO dto) {
        return requirementIntakeConvert.toEntity(dto);
    }

    @Override
    protected RequirementIntake toQueryEntity(RequirementIntakeQueryDTO dto) {
        return requirementIntakeConvert.toEntity(dto);
    }

    @Override
    protected RequirementIntakeVO toVO(RequirementIntake entity) {
        return requirementIntakeConvert.toVO(entity);
    }

    @Override
    protected List<RequirementIntakeVO> toVOs(List<RequirementIntake> entities) {
        return requirementIntakeConvert.toVOs(entities);
    }

    @Override
    protected void setEntityId(RequirementIntake entity, String id) {
        entity.setId(id);
    }

    @Override
    protected RequirementIntakeService service() {
        return requirementIntakeService;
    }
}
