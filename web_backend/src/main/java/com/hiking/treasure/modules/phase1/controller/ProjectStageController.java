package com.hiking.treasure.modules.phase1.controller;

import com.hiking.treasure.modules.phase1.domain.convert.ProjectStageConvert;
import com.hiking.treasure.modules.phase1.domain.dto.create.ProjectStageCreateDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.ProjectStageQueryDTO;
import com.hiking.treasure.modules.phase1.domain.dto.update.ProjectStageUpdateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectStageVO;
import com.hiking.treasure.modules.phase1.entity.ProjectStage;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.GateStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ProjectStageStatus;
import com.hiking.treasure.modules.phase1.service.ProjectStageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "AICoOS 项目阶段", description = "AICoOS 项目阶段基础 CRUD 接口")
@RestController
@RequestMapping("/api/aicoos/projectStage")
@PreAuthorize("hasRole('ADMIN')")
public class ProjectStageController extends AbstractPhase1CrudController<
        ProjectStage, ProjectStageService, ProjectStageCreateDTO, ProjectStageUpdateDTO, ProjectStageQueryDTO,
        ProjectStageVO> {

    @Resource
    private ProjectStageService projectStageService;

    @Resource
    private ProjectStageConvert projectStageConvert;

    @Override
    protected ProjectStage toCreateEntity(ProjectStageCreateDTO dto) {
        return projectStageConvert.toEntity(dto);
    }

    @Override
    protected ProjectStage toUpdateEntity(ProjectStageUpdateDTO dto) {
        return projectStageConvert.toEntity(dto);
    }

    @Override
    protected ProjectStage toQueryEntity(ProjectStageQueryDTO dto) {
        return projectStageConvert.toEntity(dto);
    }

    @Override
    protected ProjectStageVO toVO(ProjectStage entity) {
        return projectStageConvert.toVO(entity);
    }

    @Override
    protected List<ProjectStageVO> toVOs(List<ProjectStage> entities) {
        return projectStageConvert.toVOs(entities);
    }

    @Override
    protected void setEntityId(ProjectStage entity, String id) {
        entity.setId(id);
    }

    @Override
    protected void applyCreateDefaults(ProjectStage entity) {
        if (entity.getStageStatus() == null || entity.getStageStatus().isBlank()) {
            entity.setStageStatus(ProjectStageStatus.PENDING.getCode());
        }
        if (entity.getGateStatus() == null || entity.getGateStatus().isBlank()) {
            entity.setGateStatus(GateStatus.PENDING.getCode());
        }
    }

    @Override
    protected ProjectStageService service() {
        return projectStageService;
    }
}
