package com.hiking.treasure.modules.phase1.controller;

import com.hiking.treasure.modules.phase1.domain.convert.WorkflowTemplateConvert;
import com.hiking.treasure.modules.phase1.domain.dto.create.WorkflowTemplateCreateDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.WorkflowTemplateQueryDTO;
import com.hiking.treasure.modules.phase1.domain.dto.update.WorkflowTemplateUpdateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.WorkflowTemplateVO;
import com.hiking.treasure.modules.phase1.entity.WorkflowTemplate;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.WorkflowTemplateStatus;
import com.hiking.treasure.modules.phase1.service.WorkflowTemplateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "AICoOS 工作流模板", description = "AICoOS 工作流模板基础 CRUD 接口")
@RestController
@RequestMapping("/api/aicoos/workflowTemplate")
@PreAuthorize("hasRole('ADMIN')")
public class WorkflowTemplateController extends AbstractPhase1CrudController<
        WorkflowTemplate, WorkflowTemplateService, WorkflowTemplateCreateDTO, WorkflowTemplateUpdateDTO,
        WorkflowTemplateQueryDTO, WorkflowTemplateVO> {

    @Resource
    private WorkflowTemplateService workflowTemplateService;

    @Resource
    private WorkflowTemplateConvert workflowTemplateConvert;

    @Override
    protected WorkflowTemplate toCreateEntity(WorkflowTemplateCreateDTO dto) {
        return workflowTemplateConvert.toEntity(dto);
    }

    @Override
    protected WorkflowTemplate toUpdateEntity(WorkflowTemplateUpdateDTO dto) {
        return workflowTemplateConvert.toEntity(dto);
    }

    @Override
    protected WorkflowTemplate toQueryEntity(WorkflowTemplateQueryDTO dto) {
        return workflowTemplateConvert.toEntity(dto);
    }

    @Override
    protected WorkflowTemplateVO toVO(WorkflowTemplate entity) {
        return workflowTemplateConvert.toVO(entity);
    }

    @Override
    protected List<WorkflowTemplateVO> toVOs(List<WorkflowTemplate> entities) {
        return workflowTemplateConvert.toVOs(entities);
    }

    @Override
    protected void setEntityId(WorkflowTemplate entity, String id) {
        entity.setId(id);
    }

    @Override
    protected void applyCreateDefaults(WorkflowTemplate entity) {
        if (entity.getVersionNo() == null) {
            entity.setVersionNo(1);
        }
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus(WorkflowTemplateStatus.DRAFT.getCode());
        }
        if (entity.getDefaultFlag() == null) {
            entity.setDefaultFlag(0);
        }
    }

    @Override
    protected WorkflowTemplateService service() {
        return workflowTemplateService;
    }
}
