package com.hiking.treasure.modules.phase1.controller;

import com.hiking.treasure.modules.phase1.domain.convert.BudgetLedgerConvert;
import com.hiking.treasure.modules.phase1.domain.dto.create.BudgetLedgerCreateDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.BudgetLedgerQueryDTO;
import com.hiking.treasure.modules.phase1.domain.dto.update.BudgetLedgerUpdateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.BudgetLedgerVO;
import com.hiking.treasure.modules.phase1.entity.BudgetLedger;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.BudgetLedgerEntryType;
import com.hiking.treasure.modules.phase1.service.BudgetLedgerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "AICoOS 预算流水", description = "AICoOS 预算流水基础 CRUD 接口")
@RestController
@RequestMapping("/api/aicoos/budgetLedger")
@PreAuthorize("hasRole('ADMIN')")
public class BudgetLedgerController extends AbstractPhase1CrudController<
        BudgetLedger, BudgetLedgerService, BudgetLedgerCreateDTO, BudgetLedgerUpdateDTO, BudgetLedgerQueryDTO,
        BudgetLedgerVO> {

    @Resource
    private BudgetLedgerService budgetLedgerService;

    @Resource
    private BudgetLedgerConvert budgetLedgerConvert;

    @Override
    protected BudgetLedger toCreateEntity(BudgetLedgerCreateDTO dto) {
        return budgetLedgerConvert.toEntity(dto);
    }

    @Override
    protected BudgetLedger toUpdateEntity(BudgetLedgerUpdateDTO dto) {
        return budgetLedgerConvert.toEntity(dto);
    }

    @Override
    protected BudgetLedger toQueryEntity(BudgetLedgerQueryDTO dto) {
        return budgetLedgerConvert.toEntity(dto);
    }

    @Override
    protected BudgetLedgerVO toVO(BudgetLedger entity) {
        return budgetLedgerConvert.toVO(entity);
    }

    @Override
    protected List<BudgetLedgerVO> toVOs(List<BudgetLedger> entities) {
        return budgetLedgerConvert.toVOs(entities);
    }

    @Override
    protected void setEntityId(BudgetLedger entity, String id) {
        entity.setId(id);
    }

    @Override
    protected void applyCreateDefaults(BudgetLedger entity) {
        if (entity.getEntryType() == null || entity.getEntryType().isBlank()) {
            entity.setEntryType(BudgetLedgerEntryType.RESERVE.getCode());
        }
        if (entity.getOccurredAt() == null) {
            entity.setOccurredAt(LocalDateTime.now());
        }
    }

    @Override
    protected BudgetLedgerService service() {
        return budgetLedgerService;
    }
}
