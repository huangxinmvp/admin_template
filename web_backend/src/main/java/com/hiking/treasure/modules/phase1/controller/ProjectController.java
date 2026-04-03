package com.hiking.treasure.modules.phase1.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.modules.phase1.domain.convert.ProjectConvert;
import com.hiking.treasure.modules.phase1.domain.dto.create.ProjectCreateDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.ProjectCenterQueryDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.ProjectQueryDTO;
import com.hiking.treasure.modules.phase1.domain.dto.update.ProjectUpdateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectCenterDetailVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectCenterListVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectVO;
import com.hiking.treasure.modules.phase1.entity.Project;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ProjectLifecycleStage;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ProjectStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ProjectType;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.RiskLevel;
import com.hiking.treasure.modules.phase1.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "AICoOS 项目", description = "AICoOS 项目基础 CRUD 接口")
@RestController
@RequestMapping("/api/aicoos/project")
@PreAuthorize("hasRole('ADMIN')")
public class ProjectController extends AbstractPhase1CrudController<
        Project, ProjectService, ProjectCreateDTO, ProjectUpdateDTO, ProjectQueryDTO, ProjectVO> {

    @Resource
    private ProjectService projectService;

    @Resource
    private ProjectConvert projectConvert;

    @Override
    protected Project toCreateEntity(ProjectCreateDTO dto) {
        return projectConvert.toEntity(dto);
    }

    @Override
    protected Project toUpdateEntity(ProjectUpdateDTO dto) {
        return projectConvert.toEntity(dto);
    }

    @Override
    protected Project toQueryEntity(ProjectQueryDTO dto) {
        return projectConvert.toEntity(dto);
    }

    @Override
    protected ProjectVO toVO(Project entity) {
        return projectConvert.toVO(entity);
    }

    @Override
    protected List<ProjectVO> toVOs(List<Project> entities) {
        return projectConvert.toVOs(entities);
    }

    @Override
    protected void setEntityId(Project entity, String id) {
        entity.setId(id);
    }

    @Override
    protected void applyCreateDefaults(Project entity) {
        if (entity.getProjectType() == null || entity.getProjectType().isBlank()) {
            entity.setProjectType(ProjectType.DELIVERY.getCode());
        }
        if (entity.getCurrentStageCode() == null || entity.getCurrentStageCode().isBlank()) {
            entity.setCurrentStageCode(ProjectLifecycleStage.INTAKE.getCode());
        }
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus(ProjectStatus.DRAFT.getCode());
        }
        if (entity.getRiskLevel() == null || entity.getRiskLevel().isBlank()) {
            entity.setRiskLevel(RiskLevel.MEDIUM.getCode());
        }
    }

    @Operation(summary = "项目中心分页")
    @GetMapping("/center/page")
    public Result<Page<ProjectCenterListVO>> centerPage(
            @Valid ProjectCenterQueryDTO dto,
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(projectService.pageProjectCenter(dto, pageNo, pageSize));
    }

    @Operation(summary = "项目中心详情")
    @GetMapping("/center/{id}")
    public Result<ProjectCenterDetailVO> centerDetail(@PathVariable String id) {
        return Result.ok(projectService.getProjectCenterDetail(id));
    }

    @Override
    protected ProjectService service() {
        return projectService;
    }
}
