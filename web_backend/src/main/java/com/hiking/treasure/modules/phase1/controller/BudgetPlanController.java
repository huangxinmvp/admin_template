package com.hiking.treasure.modules.phase1.controller;

import com.hiking.treasure.modules.phase1.domain.convert.BudgetPlanConvert;
import com.hiking.treasure.modules.phase1.domain.dto.create.BudgetPlanCreateDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.BudgetPlanQueryDTO;
import com.hiking.treasure.modules.phase1.domain.dto.update.BudgetPlanUpdateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.BudgetPlanVO;
import com.hiking.treasure.modules.phase1.entity.BudgetPlan;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.BudgetPlanStatus;
import com.hiking.treasure.modules.phase1.service.BudgetPlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "AICoOS 预算计划", description = "AICoOS 预算计划基础 CRUD 接口")
@RestController
@RequestMapping("/api/aicoos/budgetPlan")
@PreAuthorize("hasRole('ADMIN')")
public class BudgetPlanController extends AbstractPhase1CrudController<
        BudgetPlan, BudgetPlanService, BudgetPlanCreateDTO, BudgetPlanUpdateDTO, BudgetPlanQueryDTO, BudgetPlanVO> {

    @Resource
    private BudgetPlanService budgetPlanService;

    @Resource
    private BudgetPlanConvert budgetPlanConvert;

    @Override
    protected BudgetPlan toCreateEntity(BudgetPlanCreateDTO dto) {
        return budgetPlanConvert.toEntity(dto);
    }

    @Override
    protected BudgetPlan toUpdateEntity(BudgetPlanUpdateDTO dto) {
        return budgetPlanConvert.toEntity(dto);
    }

    @Override
    protected BudgetPlan toQueryEntity(BudgetPlanQueryDTO dto) {
        return budgetPlanConvert.toEntity(dto);
    }

    @Override
    protected BudgetPlanVO toVO(BudgetPlan entity) {
        return budgetPlanConvert.toVO(entity);
    }

    @Override
    protected List<BudgetPlanVO> toVOs(List<BudgetPlan> entities) {
        return budgetPlanConvert.toVOs(entities);
    }

    @Override
    protected void setEntityId(BudgetPlan entity, String id) {
        entity.setId(id);
    }

    @Override
    protected void applyCreateDefaults(BudgetPlan entity) {
        if (entity.getCurrencyCode() == null || entity.getCurrencyCode().isBlank()) {
            entity.setCurrencyCode("TOKEN");
        }
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus(BudgetPlanStatus.DRAFT.getCode());
        }
    }

    @Override
    protected BudgetPlanService service() {
        return budgetPlanService;
    }
}
