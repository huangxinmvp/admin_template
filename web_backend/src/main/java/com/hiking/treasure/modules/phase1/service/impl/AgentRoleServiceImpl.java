package com.hiking.treasure.modules.phase1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.modules.phase1.domain.dto.command.AgentRoleAllowedActionInputDTO;
import com.hiking.treasure.modules.phase1.domain.dto.command.AgentRoleCenterSaveDTO;
import com.hiking.treasure.modules.phase1.domain.dto.command.AgentRoleStageParticipationInputDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.AgentRoleCenterQueryDTO;
import com.hiking.treasure.modules.phase1.domain.vo.AgentRoleAllowedActionVO;
import com.hiking.treasure.modules.phase1.domain.vo.AgentRoleCenterDetailVO;
import com.hiking.treasure.modules.phase1.domain.vo.AgentRoleCenterListVO;
import com.hiking.treasure.modules.phase1.domain.vo.AgentRoleStageParticipationVO;
import com.hiking.treasure.modules.phase1.entity.AgentRole;
import com.hiking.treasure.modules.phase1.entity.AgentRoleAllowedAction;
import com.hiking.treasure.modules.phase1.entity.AgentRoleStageParticipation;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.AgentAllowedActionCode;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.AgentRoleCategory;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.AgentRoleStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.AgentStageParticipationType;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ProjectLifecycleStage;
import com.hiking.treasure.modules.phase1.mapper.AgentRoleMapper;
import com.hiking.treasure.modules.phase1.service.AgentRoleAllowedActionService;
import com.hiking.treasure.modules.phase1.service.ProjectGovernanceLinkageService;
import com.hiking.treasure.modules.phase1.service.AgentRoleService;
import com.hiking.treasure.modules.phase1.service.AgentRoleStageParticipationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgentRoleServiceImpl extends ServiceImpl<AgentRoleMapper, AgentRole> implements AgentRoleService {

    private final AgentRoleStageParticipationService agentRoleStageParticipationService;
    private final AgentRoleAllowedActionService agentRoleAllowedActionService;
    private final ProjectGovernanceLinkageService projectGovernanceLinkageService;

    @Override
    public Page<AgentRoleCenterListVO> pageAgentRoleCenter(AgentRoleCenterQueryDTO dto, long pageNo, long pageSize) {
        LambdaQueryWrapper<AgentRole> wrapper = new LambdaQueryWrapper<>();
        if (dto != null) {
            if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
                wrapper.and(query -> query.like(AgentRole::getRoleName, dto.getKeyword())
                        .or()
                        .like(AgentRole::getRoleCode, dto.getKeyword())
                        .or()
                        .like(AgentRole::getDescription, dto.getKeyword())
                        .or()
                        .like(AgentRole::getCapabilitySummary, dto.getKeyword()));
            }
            if (dto.getRoleCategory() != null && !dto.getRoleCategory().isBlank()) {
                wrapper.eq(AgentRole::getRoleCategory, dto.getRoleCategory());
            }
            if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
                wrapper.eq(AgentRole::getStatus, dto.getStatus());
            }
            if (dto.getDefaultFlag() != null) {
                wrapper.eq(AgentRole::getDefaultFlag, dto.getDefaultFlag());
            }
        }
        wrapper.orderByDesc(AgentRole::getDefaultFlag)
                .orderByDesc(AgentRole::getUpdateTime)
                .orderByDesc(AgentRole::getCreateTime);

        Page<AgentRole> page = page(new Page<>(pageNo, pageSize), wrapper);
        Page<AgentRoleCenterListVO> result = new Page<>(pageNo, pageSize, page.getTotal());
        if (page.getRecords() == null || page.getRecords().isEmpty()) {
            result.setRecords(List.of());
            return result;
        }

        List<String> roleIds = page.getRecords().stream().map(AgentRole::getId).filter(Objects::nonNull).toList();
        Map<String, List<AgentRoleStageParticipation>> participationsByRole = loadStageParticipations(roleIds);
        Map<String, List<AgentRoleAllowedAction>> actionsByRole = loadAllowedActions(roleIds);

        result.setRecords(page.getRecords().stream()
                .map(role -> buildListVO(
                        role,
                        participationsByRole.get(role.getId()),
                        actionsByRole.get(role.getId())))
                .toList());
        return result;
    }

    @Override
    public AgentRoleCenterDetailVO getAgentRoleCenterDetail(String id) {
        AgentRole role = getById(id);
        if (role == null) {
            throw new BusinessException(404, "Agent 角色不存在");
        }
        return buildDetailVO(
                role,
                listStageParticipations(id),
                listAllowedActions(id)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentRoleCenterDetailVO createAgentRoleCenter(AgentRoleCenterSaveDTO dto) {
        AgentRole role = new AgentRole();
        applyRoleFields(role, dto);
        if (role.getRoleCode() == null || role.getRoleCode().isBlank()) {
            throw new BusinessException(400, "角色编码不能为空");
        }
        if (role.getRoleName() == null || role.getRoleName().isBlank()) {
            throw new BusinessException(400, "角色名称不能为空");
        }
        save(role);
        replaceStageParticipations(role.getId(), normalizeStageParticipations(dto == null ? null : dto.getStageParticipations()));
        replaceAllowedActions(role.getId(), normalizeAllowedActions(dto == null ? null : dto.getAllowedActions()));
        normalizeDefaultRole(role);
        projectGovernanceLinkageService.recomputeAllProjects();
        return getAgentRoleCenterDetail(role.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentRoleCenterDetailVO updateAgentRoleCenter(String id, AgentRoleCenterSaveDTO dto) {
        AgentRole role = getById(id);
        if (role == null) {
            throw new BusinessException(404, "Agent 角色不存在");
        }
        applyRoleFields(role, dto);
        updateById(role);
        replaceStageParticipations(id, normalizeStageParticipations(
                dto != null && dto.getStageParticipations() != null
                        ? dto.getStageParticipations()
                        : toStageInputList(listStageParticipations(id))
        ));
        replaceAllowedActions(id, normalizeAllowedActions(
                dto != null && dto.getAllowedActions() != null
                        ? dto.getAllowedActions()
                        : toActionInputList(listAllowedActions(id))
        ));
        normalizeDefaultRole(role);
        projectGovernanceLinkageService.recomputeAllProjects();
        return getAgentRoleCenterDetail(id);
    }

    private AgentRoleCenterListVO buildListVO(
            AgentRole role,
            List<AgentRoleStageParticipation> participations,
            List<AgentRoleAllowedAction> allowedActions) {
        AgentRoleCenterListVO vo = new AgentRoleCenterListVO();
        vo.setId(role.getId());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setRoleCategory(role.getRoleCategory());
        vo.setDescription(role.getDescription());
        vo.setStatus(role.getStatus());
        vo.setDefaultFlag(role.getDefaultFlag());
        vo.setBudgetFactor(role.getBudgetFactor());
        vo.setApprovalCollaborationFlag(role.getApprovalCollaborationFlag());
        vo.setInvolvedStageCount((int) safe(participations).stream()
                .filter(item -> !Objects.equals(item.getParticipationType(), AgentStageParticipationType.NOT_INVOLVED.getCode()))
                .count());
        vo.setAllowedActionCount((int) safe(allowedActions).stream()
                .filter(item -> Objects.equals(item.getAllowedFlag(), 1))
                .count());
        vo.setUpdateTime(firstNonNull(role.getUpdateTime(), role.getCreateTime()));
        return vo;
    }

    private AgentRoleCenterDetailVO buildDetailVO(
            AgentRole role,
            List<AgentRoleStageParticipation> participations,
            List<AgentRoleAllowedAction> actions) {
        AgentRoleCenterDetailVO vo = new AgentRoleCenterDetailVO();
        vo.setId(role.getId());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setRoleCategory(role.getRoleCategory());
        vo.setDescription(role.getDescription());
        vo.setCapabilitySummary(role.getCapabilitySummary());
        vo.setStatus(role.getStatus());
        vo.setDefaultFlag(role.getDefaultFlag());
        vo.setBudgetFactor(role.getBudgetFactor());
        vo.setApprovalCollaborationFlag(role.getApprovalCollaborationFlag());
        vo.setApprovalRequired(role.getApprovalRequired());
        vo.setMaxConcurrency(role.getMaxConcurrency());
        vo.setRemark(role.getRemark());
        vo.setStageParticipations(safe(participations).stream().map(this::toStageVO).toList());
        vo.setAllowedActions(safe(actions).stream().map(this::toActionVO).toList());
        vo.setCreateTime(role.getCreateTime());
        vo.setUpdateTime(role.getUpdateTime());
        return vo;
    }

    private AgentRoleStageParticipationVO toStageVO(AgentRoleStageParticipation entity) {
        AgentRoleStageParticipationVO vo = new AgentRoleStageParticipationVO();
        vo.setId(entity.getId());
        vo.setAgentRoleId(entity.getAgentRoleId());
        vo.setStageCode(entity.getStageCode());
        vo.setStageName(entity.getStageName());
        vo.setParticipationType(entity.getParticipationType());
        vo.setNote(entity.getNote());
        return vo;
    }

    private AgentRoleAllowedActionVO toActionVO(AgentRoleAllowedAction entity) {
        AgentRoleAllowedActionVO vo = new AgentRoleAllowedActionVO();
        vo.setId(entity.getId());
        vo.setAgentRoleId(entity.getAgentRoleId());
        vo.setActionCode(entity.getActionCode());
        vo.setActionName(entity.getActionName());
        vo.setAllowedFlag(entity.getAllowedFlag());
        vo.setApprovalRequiredFlag(entity.getApprovalRequiredFlag());
        vo.setNote(entity.getNote());
        return vo;
    }

    private void applyRoleFields(AgentRole role, AgentRoleCenterSaveDTO dto) {
        role.setRoleCode(trimToNull(dto == null ? null : dto.getRoleCode()));
        role.setRoleName(trimToNull(dto == null ? null : dto.getRoleName()));
        role.setRoleCategory(firstNonBlank(
                dto == null ? null : dto.getRoleCategory(),
                role.getRoleCategory(),
                AgentRoleCategory.DELIVERY.getCode()));
        role.setDescription(trimToNull(dto == null ? null : dto.getDescription()));
        role.setCapabilitySummary(trimToNull(dto == null ? null : dto.getCapabilitySummary()));
        role.setStatus(firstNonBlank(
                dto == null ? null : dto.getStatus(),
                role.getStatus(),
                AgentRoleStatus.ACTIVE.getCode()));
        role.setDefaultFlag(firstNonNull(dto == null ? null : dto.getDefaultFlag(), role.getDefaultFlag(), 0));
        role.setBudgetFactor(firstNonNull(
                dto == null ? null : dto.getBudgetFactor(),
                role.getBudgetFactor(),
                BigDecimal.ONE));
        role.setApprovalCollaborationFlag(firstNonNull(
                dto == null ? null : dto.getApprovalCollaborationFlag(),
                role.getApprovalCollaborationFlag(),
                1));
        role.setApprovalRequired(firstNonNull(
                dto == null ? null : dto.getApprovalRequired(),
                role.getApprovalRequired(),
                1));
        role.setMaxConcurrency(firstNonNull(
                dto == null ? null : dto.getMaxConcurrency(),
                role.getMaxConcurrency(),
                1));
        role.setRemark(trimToNull(dto == null ? null : dto.getRemark()));
    }

    private void normalizeDefaultRole(AgentRole role) {
        if (!Objects.equals(role.getDefaultFlag(), 1)) {
            return;
        }
        LambdaUpdateWrapper<AgentRole> wrapper = new LambdaUpdateWrapper<>();
        wrapper.ne(AgentRole::getId, role.getId())
                .eq(AgentRole::getRoleCategory, role.getRoleCategory())
                .set(AgentRole::getDefaultFlag, 0);
        update(wrapper);
    }

    private Map<String, List<AgentRoleStageParticipation>> loadStageParticipations(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Map.of();
        }
        Map<String, List<AgentRoleStageParticipation>> grouped = new LinkedHashMap<>();
        agentRoleStageParticipationService.list(new LambdaQueryWrapper<AgentRoleStageParticipation>()
                        .in(AgentRoleStageParticipation::getAgentRoleId, roleIds)
                        .orderByAsc(AgentRoleStageParticipation::getStageCode))
                .forEach(item -> grouped.computeIfAbsent(item.getAgentRoleId(), key -> new ArrayList<>()).add(item));
        return grouped;
    }

    private Map<String, List<AgentRoleAllowedAction>> loadAllowedActions(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Map.of();
        }
        Map<String, List<AgentRoleAllowedAction>> grouped = new LinkedHashMap<>();
        agentRoleAllowedActionService.list(new LambdaQueryWrapper<AgentRoleAllowedAction>()
                        .in(AgentRoleAllowedAction::getAgentRoleId, roleIds)
                        .orderByAsc(AgentRoleAllowedAction::getActionCode))
                .forEach(item -> grouped.computeIfAbsent(item.getAgentRoleId(), key -> new ArrayList<>()).add(item));
        return grouped;
    }

    private List<AgentRoleStageParticipation> listStageParticipations(String roleId) {
        return agentRoleStageParticipationService.list(new LambdaQueryWrapper<AgentRoleStageParticipation>()
                .eq(AgentRoleStageParticipation::getAgentRoleId, roleId)
                .orderByAsc(AgentRoleStageParticipation::getStageCode));
    }

    private List<AgentRoleAllowedAction> listAllowedActions(String roleId) {
        return agentRoleAllowedActionService.list(new LambdaQueryWrapper<AgentRoleAllowedAction>()
                .eq(AgentRoleAllowedAction::getAgentRoleId, roleId)
                .orderByAsc(AgentRoleAllowedAction::getActionCode));
    }

    private void replaceStageParticipations(String roleId, List<AgentRoleStageParticipationInputDTO> items) {
        agentRoleStageParticipationService.remove(new LambdaQueryWrapper<AgentRoleStageParticipation>()
                .eq(AgentRoleStageParticipation::getAgentRoleId, roleId));
        if (items == null || items.isEmpty()) {
            return;
        }
        List<AgentRoleStageParticipation> entities = new ArrayList<>();
        for (AgentRoleStageParticipationInputDTO item : items) {
            AgentRoleStageParticipation entity = new AgentRoleStageParticipation();
            entity.setAgentRoleId(roleId);
            entity.setStageCode(item.getStageCode());
            entity.setStageName(item.getStageName());
            entity.setParticipationType(item.getParticipationType());
            entity.setNote(trimToNull(item.getNote()));
            entities.add(entity);
        }
        agentRoleStageParticipationService.saveBatch(entities);
    }

    private void replaceAllowedActions(String roleId, List<AgentRoleAllowedActionInputDTO> items) {
        agentRoleAllowedActionService.remove(new LambdaQueryWrapper<AgentRoleAllowedAction>()
                .eq(AgentRoleAllowedAction::getAgentRoleId, roleId));
        if (items == null || items.isEmpty()) {
            return;
        }
        List<AgentRoleAllowedAction> entities = new ArrayList<>();
        for (AgentRoleAllowedActionInputDTO item : items) {
            AgentRoleAllowedAction entity = new AgentRoleAllowedAction();
            entity.setAgentRoleId(roleId);
            entity.setActionCode(item.getActionCode());
            entity.setActionName(item.getActionName());
            entity.setAllowedFlag(firstNonNull(item.getAllowedFlag(), 0));
            entity.setApprovalRequiredFlag(firstNonNull(item.getApprovalRequiredFlag(), 0));
            entity.setNote(trimToNull(item.getNote()));
            entities.add(entity);
        }
        agentRoleAllowedActionService.saveBatch(entities);
    }

    private List<AgentRoleStageParticipationInputDTO> normalizeStageParticipations(
            List<AgentRoleStageParticipationInputDTO> items) {
        List<AgentRoleStageParticipationInputDTO> source = items == null || items.isEmpty()
                ? defaultStageParticipations()
                : items;
        Map<String, AgentRoleStageParticipationInputDTO> normalized = new LinkedHashMap<>();
        for (AgentRoleStageParticipationInputDTO item : source) {
            String stageCode = trimToNull(item == null ? null : item.getStageCode());
            if (stageCode == null) {
                continue;
            }
            AgentRoleStageParticipationInputDTO next = new AgentRoleStageParticipationInputDTO();
            next.setStageCode(stageCode);
            next.setStageName(firstNonBlank(
                    item.getStageName(),
                    lifecycleStageLabel(stageCode),
                    stageCode));
            next.setParticipationType(firstNonBlank(
                    item.getParticipationType(),
                    AgentStageParticipationType.NOT_INVOLVED.getCode()));
            next.setNote(trimToNull(item.getNote()));
            normalized.put(stageCode, next);
        }
        return normalized.isEmpty() ? defaultStageParticipations() : new ArrayList<>(normalized.values());
    }

    private List<AgentRoleAllowedActionInputDTO> normalizeAllowedActions(List<AgentRoleAllowedActionInputDTO> items) {
        List<AgentRoleAllowedActionInputDTO> source = items == null || items.isEmpty()
                ? defaultAllowedActions()
                : items;
        Map<String, AgentRoleAllowedActionInputDTO> normalized = new LinkedHashMap<>();
        for (AgentRoleAllowedActionInputDTO item : source) {
            String actionCode = trimToNull(item == null ? null : item.getActionCode());
            if (actionCode == null) {
                continue;
            }
            AgentRoleAllowedActionInputDTO next = new AgentRoleAllowedActionInputDTO();
            next.setActionCode(actionCode);
            next.setActionName(firstNonBlank(item.getActionName(), actionLabel(actionCode), actionCode));
            next.setAllowedFlag(firstNonNull(item.getAllowedFlag(), 0));
            next.setApprovalRequiredFlag(firstNonNull(item.getApprovalRequiredFlag(), 0));
            next.setNote(trimToNull(item.getNote()));
            normalized.put(actionCode, next);
        }
        return normalized.isEmpty() ? defaultAllowedActions() : new ArrayList<>(normalized.values());
    }

    private List<AgentRoleStageParticipationInputDTO> toStageInputList(List<AgentRoleStageParticipation> entities) {
        if (entities == null || entities.isEmpty()) {
            return defaultStageParticipations();
        }
        return entities.stream().map(entity -> {
            AgentRoleStageParticipationInputDTO item = new AgentRoleStageParticipationInputDTO();
            item.setStageCode(entity.getStageCode());
            item.setStageName(entity.getStageName());
            item.setParticipationType(entity.getParticipationType());
            item.setNote(entity.getNote());
            return item;
        }).toList();
    }

    private List<AgentRoleAllowedActionInputDTO> toActionInputList(List<AgentRoleAllowedAction> entities) {
        if (entities == null || entities.isEmpty()) {
            return defaultAllowedActions();
        }
        return entities.stream().map(entity -> {
            AgentRoleAllowedActionInputDTO item = new AgentRoleAllowedActionInputDTO();
            item.setActionCode(entity.getActionCode());
            item.setActionName(entity.getActionName());
            item.setAllowedFlag(entity.getAllowedFlag());
            item.setApprovalRequiredFlag(entity.getApprovalRequiredFlag());
            item.setNote(entity.getNote());
            return item;
        }).toList();
    }

    private List<AgentRoleStageParticipationInputDTO> defaultStageParticipations() {
        List<AgentRoleStageParticipationInputDTO> defaults = new ArrayList<>();
        for (ProjectLifecycleStage stage : ProjectLifecycleStage.values()) {
            AgentRoleStageParticipationInputDTO item = new AgentRoleStageParticipationInputDTO();
            item.setStageCode(stage.getCode());
            item.setStageName(stage.getLabel());
            item.setParticipationType(AgentStageParticipationType.NOT_INVOLVED.getCode());
            defaults.add(item);
        }
        return defaults;
    }

    private List<AgentRoleAllowedActionInputDTO> defaultAllowedActions() {
        List<AgentRoleAllowedActionInputDTO> defaults = new ArrayList<>();
        for (AgentAllowedActionCode action : AgentAllowedActionCode.values()) {
            AgentRoleAllowedActionInputDTO item = new AgentRoleAllowedActionInputDTO();
            item.setActionCode(action.getCode());
            item.setActionName(action.getLabel());
            item.setAllowedFlag(0);
            item.setApprovalRequiredFlag(Objects.equals(action, AgentAllowedActionCode.HIGH_RISK_ACTION) ? 1 : 0);
            defaults.add(item);
        }
        return defaults;
    }

    private String lifecycleStageLabel(String stageCode) {
        for (ProjectLifecycleStage stage : ProjectLifecycleStage.values()) {
            if (Objects.equals(stage.getCode(), stageCode)) {
                return stage.getLabel();
            }
        }
        return stageCode;
    }

    private String actionLabel(String actionCode) {
        for (AgentAllowedActionCode action : AgentAllowedActionCode.values()) {
            if (Objects.equals(action.getCode(), actionCode)) {
                return action.getLabel();
            }
        }
        return actionCode;
    }

    private String trimToNull(String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .orElse(null);
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        if (values == null) {
            return null;
        }
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private <T> List<T> safe(List<T> values) {
        return values == null ? List.of() : values;
    }
}
