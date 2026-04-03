package com.hiking.treasure.modules.phase1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.modules.phase1.domain.dto.command.WorkflowTemplateCenterSaveDTO;
import com.hiking.treasure.modules.phase1.domain.dto.command.WorkflowTemplateStageInputDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.WorkflowTemplateCenterQueryDTO;
import com.hiking.treasure.modules.phase1.domain.vo.WorkflowTemplateCenterDetailVO;
import com.hiking.treasure.modules.phase1.domain.vo.WorkflowTemplateCenterListVO;
import com.hiking.treasure.modules.phase1.domain.vo.WorkflowTemplateStageConfigVO;
import com.hiking.treasure.modules.phase1.entity.WorkflowTemplate;
import com.hiking.treasure.modules.phase1.entity.WorkflowTemplateStage;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ProjectLifecycleStage;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ProjectType;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.WorkflowTemplateStatus;
import com.hiking.treasure.modules.phase1.mapper.WorkflowTemplateMapper;
import com.hiking.treasure.modules.phase1.service.ProjectGovernanceLinkageService;
import com.hiking.treasure.modules.phase1.service.WorkflowTemplateService;
import com.hiking.treasure.modules.phase1.service.WorkflowTemplateStageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkflowTemplateServiceImpl extends ServiceImpl<WorkflowTemplateMapper, WorkflowTemplate>
        implements WorkflowTemplateService {

    private final WorkflowTemplateStageService workflowTemplateStageService;
    private final ProjectGovernanceLinkageService projectGovernanceLinkageService;

    @Override
    public Page<WorkflowTemplateCenterListVO> pageTemplateCenter(
            WorkflowTemplateCenterQueryDTO dto,
            long pageNo,
            long pageSize) {
        LambdaQueryWrapper<WorkflowTemplate> wrapper = new LambdaQueryWrapper<>();
        if (dto != null) {
            if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
                wrapper.and(query -> query.like(WorkflowTemplate::getTemplateName, dto.getKeyword())
                        .or()
                        .like(WorkflowTemplate::getTemplateCode, dto.getKeyword())
                        .or()
                        .like(WorkflowTemplate::getDescription, dto.getKeyword()));
            }
            if (dto.getProjectType() != null && !dto.getProjectType().isBlank()) {
                wrapper.eq(WorkflowTemplate::getProjectType, dto.getProjectType());
            }
            if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
                wrapper.eq(WorkflowTemplate::getStatus, dto.getStatus());
            }
            if (dto.getDefaultFlag() != null) {
                wrapper.eq(WorkflowTemplate::getDefaultFlag, dto.getDefaultFlag());
            }
        }
        wrapper.orderByDesc(WorkflowTemplate::getDefaultFlag)
                .orderByDesc(WorkflowTemplate::getUpdateTime)
                .orderByDesc(WorkflowTemplate::getCreateTime);

        Page<WorkflowTemplate> page = page(new Page<>(pageNo, pageSize), wrapper);
        Page<WorkflowTemplateCenterListVO> result = new Page<>(pageNo, pageSize, page.getTotal());
        if (page.getRecords() == null || page.getRecords().isEmpty()) {
            result.setRecords(List.of());
            return result;
        }

        List<String> templateIds = page.getRecords().stream()
                .map(WorkflowTemplate::getId)
                .filter(Objects::nonNull)
                .toList();
        Map<String, List<WorkflowTemplateStage>> stagesByTemplate = loadStagesByTemplate(templateIds);

        result.setRecords(page.getRecords().stream()
                .map(template -> buildListVO(template, stagesByTemplate.get(template.getId())))
                .toList());
        return result;
    }

    @Override
    public WorkflowTemplateCenterDetailVO getTemplateCenterDetail(String id) {
        WorkflowTemplate template = getById(id);
        if (template == null) {
            throw new BusinessException(404, "工作流模板不存在");
        }
        List<WorkflowTemplateStage> stages = listStages(template.getId());
        return buildDetailVO(template, stages);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowTemplateCenterDetailVO createTemplateCenter(WorkflowTemplateCenterSaveDTO dto) {
        WorkflowTemplate template = new WorkflowTemplate();
        applyTemplateFields(template, dto);
        if (template.getTemplateCode() == null || template.getTemplateCode().isBlank()) {
            throw new BusinessException(400, "模板编码不能为空");
        }
        if (template.getTemplateName() == null || template.getTemplateName().isBlank()) {
            throw new BusinessException(400, "模板名称不能为空");
        }

        List<WorkflowTemplateStageInputDTO> stages = normalizeStages(dto == null ? null : dto.getStages());
        template.setStageCount(stages.size());
        save(template);
        replaceStages(template.getId(), stages);
        normalizeDefaultTemplate(template);
        projectGovernanceLinkageService.recomputeProjectsByWorkflowTemplate(template.getId());
        return getTemplateCenterDetail(template.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowTemplateCenterDetailVO updateTemplateCenter(String id, WorkflowTemplateCenterSaveDTO dto) {
        WorkflowTemplate current = getById(id);
        if (current == null) {
            throw new BusinessException(404, "工作流模板不存在");
        }

        applyTemplateFields(current, dto);
        List<WorkflowTemplateStageInputDTO> stages = dto != null && dto.getStages() != null
                ? normalizeStages(dto.getStages())
                : toStageInputs(listStages(id));
        current.setStageCount(stages.size());
        updateById(current);
        replaceStages(id, stages);
        normalizeDefaultTemplate(current);
        projectGovernanceLinkageService.recomputeProjectsByWorkflowTemplate(id);
        return getTemplateCenterDetail(id);
    }

    private WorkflowTemplateCenterListVO buildListVO(
            WorkflowTemplate template,
            List<WorkflowTemplateStage> stages) {
        List<WorkflowTemplateStage> safeStages = stages == null ? List.of() : stages;
        WorkflowTemplateCenterListVO vo = new WorkflowTemplateCenterListVO();
        vo.setId(template.getId());
        vo.setTemplateCode(template.getTemplateCode());
        vo.setTemplateName(template.getTemplateName());
        vo.setProjectType(template.getProjectType());
        vo.setVersionNo(template.getVersionNo());
        vo.setStageCount(firstNonNull(template.getStageCount(), safeStages.size(), 0));
        vo.setEnabledStageCount((int) safeStages.stream()
                .filter(stage -> Objects.equals(stage.getEnabledFlag(), 1))
                .count());
        vo.setStatus(template.getStatus());
        vo.setDefaultFlag(template.getDefaultFlag());
        vo.setDescription(template.getDescription());
        vo.setUpdateTime(firstNonNull(template.getUpdateTime(), template.getCreateTime()));
        return vo;
    }

    private WorkflowTemplateCenterDetailVO buildDetailVO(
            WorkflowTemplate template,
            List<WorkflowTemplateStage> stages) {
        WorkflowTemplateCenterDetailVO vo = new WorkflowTemplateCenterDetailVO();
        vo.setId(template.getId());
        vo.setTemplateCode(template.getTemplateCode());
        vo.setTemplateName(template.getTemplateName());
        vo.setProjectType(template.getProjectType());
        vo.setVersionNo(template.getVersionNo());
        vo.setStageCount(firstNonNull(template.getStageCount(), stages.size(), 0));
        vo.setStatus(template.getStatus());
        vo.setDefaultFlag(template.getDefaultFlag());
        vo.setDescription(template.getDescription());
        vo.setGateChecksConfig(template.getGateChecksConfig());
        vo.setBlockingDecisionConfig(template.getBlockingDecisionConfig());
        vo.setBlockingApprovalConfig(template.getBlockingApprovalConfig());
        vo.setBudgetThresholdConfig(template.getBudgetThresholdConfig());
        vo.setHighRiskApprovalConfig(template.getHighRiskApprovalConfig());
        vo.setRemark(template.getRemark());
        vo.setStages(stages.stream().map(this::toStageVO).toList());
        vo.setCreateTime(template.getCreateTime());
        vo.setUpdateTime(template.getUpdateTime());
        return vo;
    }

    private WorkflowTemplateStageConfigVO toStageVO(WorkflowTemplateStage stage) {
        WorkflowTemplateStageConfigVO vo = new WorkflowTemplateStageConfigVO();
        vo.setId(stage.getId());
        vo.setTemplateId(stage.getTemplateId());
        vo.setStageCode(stage.getStageCode());
        vo.setStageName(stage.getStageName());
        vo.setStageOrder(stage.getStageOrder());
        vo.setEnabledFlag(stage.getEnabledFlag());
        vo.setStageDescription(stage.getStageDescription());
        vo.setStageNote(stage.getStageNote());
        return vo;
    }

    private void applyTemplateFields(WorkflowTemplate target, WorkflowTemplateCenterSaveDTO dto) {
        target.setTemplateCode(trimToNull(dto == null ? null : dto.getTemplateCode()));
        target.setTemplateName(trimToNull(dto == null ? null : dto.getTemplateName()));
        target.setProjectType(resolveProjectType(dto == null ? null : dto.getProjectType()));
        target.setVersionNo(firstNonNull(dto == null ? null : dto.getVersionNo(), target.getVersionNo(), 1));
        target.setStatus(firstNonBlank(
                dto == null ? null : dto.getStatus(),
                target.getStatus(),
                WorkflowTemplateStatus.DRAFT.getCode()));
        target.setDefaultFlag(firstNonNull(dto == null ? null : dto.getDefaultFlag(), target.getDefaultFlag(), 0));
        target.setDescription(trimToNull(dto == null ? null : dto.getDescription()));
        target.setGateChecksConfig(trimToNull(dto == null ? null : dto.getGateChecksConfig()));
        target.setBlockingDecisionConfig(trimToNull(dto == null ? null : dto.getBlockingDecisionConfig()));
        target.setBlockingApprovalConfig(trimToNull(dto == null ? null : dto.getBlockingApprovalConfig()));
        target.setBudgetThresholdConfig(trimToNull(dto == null ? null : dto.getBudgetThresholdConfig()));
        target.setHighRiskApprovalConfig(trimToNull(dto == null ? null : dto.getHighRiskApprovalConfig()));
        target.setRemark(trimToNull(dto == null ? null : dto.getRemark()));
    }

    private String resolveProjectType(String projectType) {
        String candidate = trimToNull(projectType);
        if (candidate != null) {
            return candidate;
        }
        return ProjectType.DELIVERY.getCode();
    }

    private void normalizeDefaultTemplate(WorkflowTemplate template) {
        if (!Objects.equals(template.getDefaultFlag(), 1)) {
            return;
        }
        LambdaUpdateWrapper<WorkflowTemplate> wrapper = new LambdaUpdateWrapper<>();
        wrapper.ne(WorkflowTemplate::getId, template.getId())
                .set(WorkflowTemplate::getDefaultFlag, 0);
        applyProjectTypeScope(wrapper, template.getProjectType());
        update(wrapper);
    }

    private void applyProjectTypeScope(LambdaUpdateWrapper<WorkflowTemplate> wrapper, String projectType) {
        String resolvedProjectType = trimToNull(projectType);
        if (resolvedProjectType == null) {
            wrapper.and(query -> query.isNull(WorkflowTemplate::getProjectType)
                    .or()
                    .eq(WorkflowTemplate::getProjectType, ""));
            return;
        }
        wrapper.eq(WorkflowTemplate::getProjectType, resolvedProjectType);
    }

    private List<WorkflowTemplateStage> listStages(String templateId) {
        return workflowTemplateStageService.list(new LambdaQueryWrapper<WorkflowTemplateStage>()
                .eq(WorkflowTemplateStage::getTemplateId, templateId)
                .orderByAsc(WorkflowTemplateStage::getStageOrder)
                .orderByAsc(WorkflowTemplateStage::getCreateTime));
    }

    private Map<String, List<WorkflowTemplateStage>> loadStagesByTemplate(List<String> templateIds) {
        if (templateIds == null || templateIds.isEmpty()) {
            return Map.of();
        }
        Map<String, List<WorkflowTemplateStage>> grouped = new LinkedHashMap<>();
        workflowTemplateStageService.list(new LambdaQueryWrapper<WorkflowTemplateStage>()
                        .in(WorkflowTemplateStage::getTemplateId, templateIds)
                        .orderByAsc(WorkflowTemplateStage::getStageOrder)
                        .orderByAsc(WorkflowTemplateStage::getCreateTime))
                .forEach(stage -> grouped.computeIfAbsent(stage.getTemplateId(), key -> new ArrayList<>()).add(stage));
        return grouped;
    }

    private void replaceStages(String templateId, List<WorkflowTemplateStageInputDTO> stageInputs) {
        workflowTemplateStageService.remove(new LambdaQueryWrapper<WorkflowTemplateStage>()
                .eq(WorkflowTemplateStage::getTemplateId, templateId));
        if (stageInputs == null || stageInputs.isEmpty()) {
            return;
        }

        List<WorkflowTemplateStage> stageEntities = new ArrayList<>();
        for (WorkflowTemplateStageInputDTO stageInput : stageInputs) {
            WorkflowTemplateStage entity = new WorkflowTemplateStage();
            entity.setTemplateId(templateId);
            entity.setStageCode(stageInput.getStageCode());
            entity.setStageName(stageInput.getStageName());
            entity.setStageOrder(stageInput.getStageOrder());
            entity.setEnabledFlag(stageInput.getEnabledFlag());
            entity.setStageDescription(trimToNull(stageInput.getStageDescription()));
            entity.setStageNote(trimToNull(stageInput.getStageNote()));
            stageEntities.add(entity);
        }
        workflowTemplateStageService.saveBatch(stageEntities);
    }

    private List<WorkflowTemplateStageInputDTO> toStageInputs(List<WorkflowTemplateStage> stages) {
        if (stages == null || stages.isEmpty()) {
            return defaultStages();
        }
        return stages.stream().map(stage -> {
            WorkflowTemplateStageInputDTO item = new WorkflowTemplateStageInputDTO();
            item.setStageCode(stage.getStageCode());
            item.setStageName(stage.getStageName());
            item.setStageOrder(stage.getStageOrder());
            item.setEnabledFlag(stage.getEnabledFlag());
            item.setStageDescription(stage.getStageDescription());
            item.setStageNote(stage.getStageNote());
            return item;
        }).toList();
    }

    private List<WorkflowTemplateStageInputDTO> normalizeStages(List<WorkflowTemplateStageInputDTO> stages) {
        List<WorkflowTemplateStageInputDTO> source = stages == null || stages.isEmpty() ? defaultStages() : stages;
        Map<String, WorkflowTemplateStageInputDTO> normalized = new LinkedHashMap<>();
        int order = 1;
        for (WorkflowTemplateStageInputDTO item : source) {
            if (item == null || trimToNull(item.getStageCode()) == null) {
                continue;
            }
            String stageCode = trimToNull(item.getStageCode());
            WorkflowTemplateStageInputDTO normalizedItem = new WorkflowTemplateStageInputDTO();
            normalizedItem.setStageCode(stageCode);
            normalizedItem.setStageName(firstNonBlank(item.getStageName(), lifecycleStageLabel(stageCode), stageCode));
            normalizedItem.setStageOrder(firstNonNull(item.getStageOrder(), order));
            normalizedItem.setEnabledFlag(firstNonNull(item.getEnabledFlag(), 1));
            normalizedItem.setStageDescription(trimToNull(item.getStageDescription()));
            normalizedItem.setStageNote(trimToNull(item.getStageNote()));
            normalized.put(stageCode, normalizedItem);
            order++;
        }
        if (normalized.isEmpty()) {
            return defaultStages();
        }
        return normalized.values().stream()
                .sorted((left, right) -> Integer.compare(
                        firstNonNull(left.getStageOrder(), Integer.MAX_VALUE),
                        firstNonNull(right.getStageOrder(), Integer.MAX_VALUE)))
                .map(item -> {
                    item.setStageOrder(firstNonNull(item.getStageOrder(), 0));
                    return item;
                })
                .toList();
    }

    private List<WorkflowTemplateStageInputDTO> defaultStages() {
        List<WorkflowTemplateStageInputDTO> defaults = new ArrayList<>();
        int order = 1;
        for (ProjectLifecycleStage lifecycleStage : ProjectLifecycleStage.values()) {
            WorkflowTemplateStageInputDTO item = new WorkflowTemplateStageInputDTO();
            item.setStageCode(lifecycleStage.getCode());
            item.setStageName(lifecycleStage.getLabel());
            item.setStageOrder(order++);
            item.setEnabledFlag(1);
            defaults.add(item);
        }
        return defaults;
    }

    private String lifecycleStageLabel(String stageCode) {
        if (stageCode == null || stageCode.isBlank()) {
            return null;
        }
        for (ProjectLifecycleStage stage : ProjectLifecycleStage.values()) {
            if (Objects.equals(stage.getCode(), stageCode)) {
                return stage.getLabel();
            }
        }
        return null;
    }

    private String trimToNull(String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .orElse(null);
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... candidates) {
        if (candidates == null) {
            return null;
        }
        for (T candidate : candidates) {
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    private String firstNonBlank(String... candidates) {
        if (candidates == null) {
            return null;
        }
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate;
            }
        }
        return null;
    }
}
