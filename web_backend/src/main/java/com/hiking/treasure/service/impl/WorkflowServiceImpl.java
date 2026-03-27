package com.hiking.treasure.service.impl;

import com.hiking.treasure.common.api.ErrorCode;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.common.security.SecurityUtils;
import com.hiking.treasure.domain.dto.WorkflowDraftSaveDTO;
import com.hiking.treasure.domain.dto.create.WorkflowStartProcessDTO;
import com.hiking.treasure.domain.dto.update.WorkflowTaskCompleteDTO;
import com.hiking.treasure.domain.vo.workflow.WorkflowDefinitionVO;
import com.hiking.treasure.domain.vo.workflow.WorkflowDeploymentVO;
import com.hiking.treasure.domain.vo.workflow.WorkflowDraftVO;
import com.hiking.treasure.domain.vo.workflow.WorkflowInstanceVO;
import com.hiking.treasure.domain.vo.workflow.WorkflowProcessInstanceDetailVO;
import com.hiking.treasure.domain.vo.workflow.WorkflowTaskVO;
import com.hiking.treasure.entity.WorkflowDraft;
import com.hiking.treasure.mapper.WorkflowDraftMapper;
import com.hiking.treasure.service.WorkflowService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.Task;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private static final Set<String> BPMN_SUFFIXES = Set.of(".bpmn", ".bpmn20.xml", ".xml");

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final WorkflowDraftMapper workflowDraftMapper;

    @Override
    public List<WorkflowDefinitionVO> listDefinitions(String definitionKey, String definitionName) {
        var query = repositoryService.createProcessDefinitionQuery().latestVersion();
        if (StringUtils.hasText(definitionKey)) {
            query.processDefinitionKeyLike("%" + definitionKey.trim() + "%");
        }
        if (StringUtils.hasText(definitionName)) {
            query.processDefinitionNameLike("%" + definitionName.trim() + "%");
        }
        return query.orderByProcessDefinitionKey().asc()
                .orderByProcessDefinitionVersion().desc()
                .list()
                .stream()
                .map(this::toDefinitionVO)
                .toList();
    }

    @Override
    public String getDefinitionXml(String definitionId) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(definitionId)
                .singleResult();
        if (definition == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "流程定义不存在");
        }

        try (InputStream inputStream = repositoryService.getResourceAsStream(
                definition.getDeploymentId(),
                definition.getResourceName()
        )) {
            if (inputStream == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "流程定义 XML 资源不存在");
            }
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "读取流程定义 XML 失败: " + ex.getMessage());
        }
    }

    @Override
    public List<WorkflowDraftVO> listDrafts(String keyword) {
        String currentUserId = SecurityUtils.getRequiredUserId();
        QueryWrapper<WorkflowDraft> wrapper = new QueryWrapper<WorkflowDraft>()
                .eq("owner_user_id", currentUserId)
                .orderByDesc("update_time")
                .orderByDesc("create_time");
        if (StringUtils.hasText(keyword)) {
            String trimmedKeyword = keyword.trim();
            wrapper.and(query -> query
                    .like("draft_name", trimmedKeyword)
                    .or()
                    .like("process_definition_name", trimmedKeyword)
                    .or()
                    .like("process_definition_key", trimmedKeyword));
        }
        return workflowDraftMapper.selectList(wrapper)
                .stream()
                .map(draft -> toDraftVO(draft, false))
                .toList();
    }

    @Override
    public WorkflowDraftVO getDraft(String draftId) {
        return toDraftVO(getAccessibleDraft(draftId), true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowDraftVO saveDraft(WorkflowDraftSaveDTO dto) {
        WorkflowDraft draft;
        if (StringUtils.hasText(dto.getId())) {
            draft = getAccessibleDraft(dto.getId().trim());
        } else {
            draft = new WorkflowDraft()
                    .setOwnerUserId(SecurityUtils.getRequiredUserId());
        }

        draft.setDraftName(resolveDraftName(dto))
                .setProcessDefinitionKey(trimToNull(dto.getProcessDefinitionKey()))
                .setProcessDefinitionName(trimToNull(dto.getProcessDefinitionName()))
                .setSourceDefinitionId(trimToNull(dto.getSourceDefinitionId()))
                .setSourceDefinitionKey(trimToNull(dto.getSourceDefinitionKey()))
                .setCategory(trimToNull(dto.getCategory()))
                .setBpmnXml(dto.getBpmnXml().trim())
                .setRemark(trimToNull(dto.getRemark()));

        if (StringUtils.hasText(dto.getId())) {
            workflowDraftMapper.updateById(draft);
        } else {
            workflowDraftMapper.insert(draft);
        }
        return toDraftVO(getAccessibleDraft(draft.getId()), true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDraft(String draftId) {
        WorkflowDraft draft = getAccessibleDraft(draftId);
        return workflowDraftMapper.deleteById(draft.getId()) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowDeploymentVO deployProcess(String deploymentName, String category, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请上传 BPMN 流程文件");
        }
        String originalFilename = StringUtils.hasText(file.getOriginalFilename())
                ? Objects.requireNonNull(file.getOriginalFilename()).trim()
                : "process-" + UUID.randomUUID() + ".bpmn20.xml";
        if (!isSupportedBpmnFile(originalFilename)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅支持 .bpmn、.bpmn20.xml 或 .xml 流程定义文件");
        }

        String resolvedDeploymentName = StringUtils.hasText(deploymentName)
                ? deploymentName.trim()
                : stripExtension(originalFilename);

        try (InputStream inputStream = file.getInputStream()) {
            DeploymentBuilder builder = repositoryService.createDeployment()
                    .name(resolvedDeploymentName)
                    .addInputStream(originalFilename, inputStream);
            if (StringUtils.hasText(category)) {
                builder.category(category.trim());
            }
            Deployment deployment = builder.deploy();
            long processDefinitionCount = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployment.getId())
                    .count();
            if (processDefinitionCount == 0) {
                repositoryService.deleteDeployment(deployment.getId(), true);
                throw new BusinessException(ErrorCode.BAD_REQUEST, "部署成功但未识别到可执行流程定义，请检查 BPMN 文件内容");
            }
            return toDeploymentVO(deployment, processDefinitionCount);
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "读取流程文件失败: " + ex.getMessage());
        } catch (FlowableException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "流程部署失败: " + ex.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDeployment(String deploymentId, boolean cascade) {
        Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
        if (deployment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "流程部署不存在");
        }
        repositoryService.deleteDeployment(deploymentId, cascade);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowInstanceVO startProcess(WorkflowStartProcessDTO dto) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(dto.getDefinitionKey().trim())
                .latestVersion()
                .active()
                .singleResult();
        if (definition == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "流程定义不存在或未激活");
        }

        String currentUserId = SecurityUtils.getRequiredUserId();
        String currentUsername = getRequiredUsername();
        Map<String, Object> variables = new HashMap<>();
        if (dto.getVariables() != null) {
            variables.putAll(dto.getVariables());
        }
        variables.putIfAbsent("initiatorId", currentUserId);
        variables.putIfAbsent("initiator", currentUsername);
        if (StringUtils.hasText(dto.getApprover())) {
            variables.put("approver", dto.getApprover().trim());
        }
        if (StringUtils.hasText(dto.getTitle())) {
            variables.putIfAbsent("title", dto.getTitle().trim());
        }

        try {
            Authentication.setAuthenticatedUserId(currentUserId);
            ProcessInstance processInstance = runtimeService.startProcessInstanceById(
                    definition.getId(),
                    StringUtils.hasText(dto.getBusinessKey()) ? dto.getBusinessKey().trim() : null,
                    variables
            );
            HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstance.getProcessInstanceId())
                    .singleResult();
            Task currentTask = taskService.createTaskQuery()
                    .processInstanceId(processInstance.getProcessInstanceId())
                    .active()
                    .singleResult();
            return toInstanceVO(processInstance, definition, historicInstance, currentTask);
        } catch (FlowableException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "启动流程失败: " + ex.getMessage());
        } finally {
            Authentication.setAuthenticatedUserId(null);
        }
    }

    @Override
    public List<WorkflowTaskVO> listMyTodoTasks() {
        String currentUsername = getRequiredUsername();
        Map<String, ProcessDefinition> definitionCache = new HashMap<>();
        Map<String, HistoricProcessInstance> instanceCache = new HashMap<>();
        return taskService.createTaskQuery()
                .taskAssignee(currentUsername)
                .active()
                .orderByTaskCreateTime().desc()
                .list()
                .stream()
                .map(task -> toTaskVO(task, definitionCache, instanceCache))
                .toList();
    }

    @Override
    public List<WorkflowTaskVO> listMyDoneTasks() {
        String currentUsername = getRequiredUsername();
        Map<String, ProcessDefinition> definitionCache = new HashMap<>();
        Map<String, HistoricProcessInstance> instanceCache = new HashMap<>();
        return historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(currentUsername)
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc()
                .list()
                .stream()
                .map(task -> toTaskVO(task, definitionCache, instanceCache))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeTask(String taskId, WorkflowTaskCompleteDTO dto) {
        Task task = taskService.createTaskQuery().taskId(taskId).active().singleResult();
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在或已完成");
        }

        String currentUserId = SecurityUtils.getRequiredUserId();
        String currentUsername = getRequiredUsername();
        if (!SecurityUtils.hasRole("ADMIN")) {
            if (!StringUtils.hasText(task.getAssignee()) || !task.getAssignee().equals(currentUsername)) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "当前任务未分配给你，无法办理");
            }
        }

        Map<String, Object> variables = new HashMap<>();
        if (dto != null && dto.getVariables() != null) {
            variables.putAll(dto.getVariables());
        }
        if (dto != null && dto.getApproved() != null) {
            variables.put("approved", dto.getApproved());
        }

        try {
            Authentication.setAuthenticatedUserId(currentUserId);
            if (dto != null && StringUtils.hasText(dto.getComment())) {
                taskService.addComment(taskId, task.getProcessInstanceId(), dto.getComment().trim());
            }
            taskService.complete(taskId, variables);
            return true;
        } catch (FlowableException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "办理任务失败: " + ex.getMessage());
        } finally {
            Authentication.setAuthenticatedUserId(null);
        }
    }

    @Override
    public List<WorkflowInstanceVO> listMyStartedInstances() {
        String currentUserId = SecurityUtils.getRequiredUserId();
        Map<String, ProcessDefinition> definitionCache = new HashMap<>();
        return historyService.createHistoricProcessInstanceQuery()
                .startedBy(currentUserId)
                .orderByProcessInstanceStartTime().desc()
                .list()
                .stream()
                .map(instance -> toInstanceVO(instance, definitionCache))
                .toList();
    }

    @Override
    public WorkflowProcessInstanceDetailVO getProcessInstanceDetail(String processInstanceId) {
        HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (historicInstance == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "流程实例不存在");
        }
        ensureCanViewInstance(historicInstance);

        Map<String, ProcessDefinition> definitionCache = new HashMap<>();
        WorkflowProcessInstanceDetailVO detailVO = new WorkflowProcessInstanceDetailVO();
        detailVO.setInstance(toInstanceVO(historicInstance, definitionCache));

        Map<String, HistoricProcessInstance> instanceCache = Map.of(historicInstance.getId(), historicInstance);
        List<WorkflowTaskVO> currentTasks = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .active()
                .list()
                .stream()
                .map(task -> toTaskVO(task, definitionCache, instanceCache))
                .toList();
        detailVO.setCurrentTasks(currentTasks);

        List<WorkflowTaskVO> historyTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricTaskInstanceStartTime().asc()
                .list()
                .stream()
                .map(task -> toTaskVO(task, definitionCache, instanceCache))
                .toList();
        detailVO.setHistoryTasks(historyTasks);

        Map<String, Object> variables = new LinkedHashMap<>();
        List<HistoricVariableInstance> historicVariables = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();
        for (HistoricVariableInstance variable : historicVariables) {
            variables.put(variable.getVariableName(), variable.getValue());
        }
        detailVO.setVariables(variables);
        return detailVO;
    }

    private void ensureCanViewInstance(HistoricProcessInstance historicInstance) {
        if (SecurityUtils.hasRole("ADMIN")) {
            return;
        }
        String currentUserId = SecurityUtils.getRequiredUserId();
        if (currentUserId.equals(historicInstance.getStartUserId())) {
            return;
        }
        String currentUsername = getRequiredUsername();
        long activeTaskCount = taskService.createTaskQuery()
                .processInstanceId(historicInstance.getId())
                .taskAssignee(currentUsername)
                .count();
        if (activeTaskCount > 0) {
            return;
        }
        long historyTaskCount = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(historicInstance.getId())
                .taskAssignee(currentUsername)
                .count();
        if (historyTaskCount > 0) {
            return;
        }
        throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看该流程实例");
    }

    private WorkflowDraft getAccessibleDraft(String draftId) {
        WorkflowDraft draft = workflowDraftMapper.selectById(draftId);
        if (draft == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "流程草稿不存在");
        }
        if (!SecurityUtils.hasRole("ADMIN")
                && !SecurityUtils.getRequiredUserId().equals(draft.getOwnerUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该流程草稿");
        }
        return draft;
    }

    private WorkflowDraftVO toDraftVO(WorkflowDraft draft, boolean includeXml) {
        WorkflowDraftVO vo = new WorkflowDraftVO();
        vo.setId(draft.getId());
        vo.setDraftName(draft.getDraftName());
        vo.setProcessDefinitionKey(draft.getProcessDefinitionKey());
        vo.setProcessDefinitionName(draft.getProcessDefinitionName());
        vo.setSourceDefinitionId(draft.getSourceDefinitionId());
        vo.setSourceDefinitionKey(draft.getSourceDefinitionKey());
        vo.setCategory(draft.getCategory());
        vo.setRemark(draft.getRemark());
        vo.setCreateTime(draft.getCreateTime());
        vo.setUpdateTime(draft.getUpdateTime());
        if (includeXml) {
            vo.setBpmnXml(draft.getBpmnXml());
        }
        return vo;
    }

    private String resolveDraftName(WorkflowDraftSaveDTO dto) {
        String draftName = trimToNull(dto.getDraftName());
        if (draftName != null) {
            return draftName;
        }
        String processName = trimToNull(dto.getProcessDefinitionName());
        if (processName != null) {
            return processName;
        }
        String processKey = trimToNull(dto.getProcessDefinitionKey());
        if (processKey != null) {
            return processKey;
        }
        return "未命名草稿";
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private WorkflowDefinitionVO toDefinitionVO(ProcessDefinition definition) {
        WorkflowDefinitionVO vo = new WorkflowDefinitionVO();
        vo.setId(definition.getId());
        vo.setKey(definition.getKey());
        vo.setName(definition.getName());
        vo.setVersion(definition.getVersion());
        vo.setDeploymentId(definition.getDeploymentId());
        vo.setCategory(definition.getCategory());
        vo.setResourceName(definition.getResourceName());
        vo.setDiagramResourceName(definition.getDiagramResourceName());
        vo.setSuspended(definition.isSuspended());
        return vo;
    }

    private WorkflowDeploymentVO toDeploymentVO(Deployment deployment, long processDefinitionCount) {
        WorkflowDeploymentVO vo = new WorkflowDeploymentVO();
        vo.setDeploymentId(deployment.getId());
        vo.setName(deployment.getName());
        vo.setCategory(deployment.getCategory());
        vo.setTenantId(deployment.getTenantId());
        vo.setDeploymentTime(toLocalDateTime(deployment.getDeploymentTime()));
        vo.setResourceNames(new ArrayList<>(repositoryService.getDeploymentResourceNames(deployment.getId())));
        vo.setProcessDefinitionCount(processDefinitionCount);
        return vo;
    }

    private WorkflowInstanceVO toInstanceVO(ProcessInstance processInstance,
                                            ProcessDefinition definition,
                                            HistoricProcessInstance historicInstance,
                                            Task currentTask) {
        WorkflowInstanceVO vo = new WorkflowInstanceVO();
        vo.setProcessInstanceId(processInstance.getProcessInstanceId());
        vo.setProcessDefinitionId(processInstance.getProcessDefinitionId());
        vo.setProcessDefinitionKey(definition != null ? definition.getKey() : null);
        vo.setProcessDefinitionName(definition != null ? definition.getName() : null);
        vo.setBusinessKey(processInstance.getBusinessKey());
        vo.setStartUserId(historicInstance != null ? historicInstance.getStartUserId() : null);
        vo.setStartTime(historicInstance != null ? toLocalDateTime(historicInstance.getStartTime()) : LocalDateTime.now());
        vo.setEndTime(historicInstance != null ? toLocalDateTime(historicInstance.getEndTime()) : null);
        vo.setFinished(historicInstance != null && historicInstance.getEndTime() != null);
        vo.setSuspended(processInstance.isSuspended());
        vo.setCurrentTaskId(currentTask != null ? currentTask.getId() : null);
        vo.setCurrentTaskName(currentTask != null ? currentTask.getName() : null);
        return vo;
    }

    private WorkflowInstanceVO toInstanceVO(HistoricProcessInstance historicInstance, Map<String, ProcessDefinition> definitionCache) {
        ProcessDefinition definition = getProcessDefinition(definitionCache, historicInstance.getProcessDefinitionId());
        Task currentTask = taskService.createTaskQuery()
                .processInstanceId(historicInstance.getId())
                .active()
                .singleResult();
        WorkflowInstanceVO vo = new WorkflowInstanceVO();
        vo.setProcessInstanceId(historicInstance.getId());
        vo.setProcessDefinitionId(historicInstance.getProcessDefinitionId());
        vo.setProcessDefinitionKey(definition != null ? definition.getKey() : historicInstance.getProcessDefinitionKey());
        vo.setProcessDefinitionName(definition != null ? definition.getName() : historicInstance.getProcessDefinitionName());
        vo.setBusinessKey(historicInstance.getBusinessKey());
        vo.setStartUserId(historicInstance.getStartUserId());
        vo.setStartTime(toLocalDateTime(historicInstance.getStartTime()));
        vo.setEndTime(toLocalDateTime(historicInstance.getEndTime()));
        vo.setFinished(historicInstance.getEndTime() != null);
        vo.setSuspended(false);
        vo.setCurrentTaskId(currentTask != null ? currentTask.getId() : null);
        vo.setCurrentTaskName(currentTask != null ? currentTask.getName() : null);
        return vo;
    }

    private WorkflowTaskVO toTaskVO(Task task,
                                    Map<String, ProcessDefinition> definitionCache,
                                    Map<String, HistoricProcessInstance> instanceCache) {
        HistoricProcessInstance historicInstance = getHistoricProcessInstance(instanceCache, task.getProcessInstanceId());
        ProcessDefinition definition = getProcessDefinition(definitionCache, task.getProcessDefinitionId());
        WorkflowTaskVO vo = new WorkflowTaskVO();
        vo.setTaskId(task.getId());
        vo.setTaskDefinitionKey(task.getTaskDefinitionKey());
        vo.setName(task.getName());
        vo.setDescription(task.getDescription());
        vo.setProcessInstanceId(task.getProcessInstanceId());
        vo.setProcessDefinitionId(task.getProcessDefinitionId());
        vo.setProcessDefinitionKey(definition != null ? definition.getKey() : null);
        vo.setProcessDefinitionName(definition != null ? definition.getName() : null);
        vo.setBusinessKey(historicInstance != null ? historicInstance.getBusinessKey() : null);
        vo.setAssignee(task.getAssignee());
        vo.setOwner(task.getOwner());
        vo.setCreateTime(toLocalDateTime(task.getCreateTime()));
        vo.setDueDate(toLocalDateTime(task.getDueDate()));
        vo.setFinished(false);
        vo.setSuspended(task.isSuspended());
        return vo;
    }

    private WorkflowTaskVO toTaskVO(HistoricTaskInstance task,
                                    Map<String, ProcessDefinition> definitionCache,
                                    Map<String, HistoricProcessInstance> instanceCache) {
        HistoricProcessInstance historicInstance = getHistoricProcessInstance(instanceCache, task.getProcessInstanceId());
        ProcessDefinition definition = getProcessDefinition(definitionCache, task.getProcessDefinitionId());
        WorkflowTaskVO vo = new WorkflowTaskVO();
        vo.setTaskId(task.getId());
        vo.setTaskDefinitionKey(task.getTaskDefinitionKey());
        vo.setName(task.getName());
        vo.setDescription(task.getDescription());
        vo.setProcessInstanceId(task.getProcessInstanceId());
        vo.setProcessDefinitionId(task.getProcessDefinitionId());
        vo.setProcessDefinitionKey(definition != null ? definition.getKey() : null);
        vo.setProcessDefinitionName(definition != null ? definition.getName() : null);
        vo.setBusinessKey(historicInstance != null ? historicInstance.getBusinessKey() : null);
        vo.setAssignee(task.getAssignee());
        vo.setOwner(task.getOwner());
        vo.setCreateTime(toLocalDateTime(task.getCreateTime()));
        vo.setDueDate(toLocalDateTime(task.getDueDate()));
        vo.setEndTime(toLocalDateTime(task.getEndTime()));
        vo.setFinished(task.getEndTime() != null);
        vo.setSuspended(false);
        return vo;
    }

    private ProcessDefinition getProcessDefinition(Map<String, ProcessDefinition> cache, String processDefinitionId) {
        if (!StringUtils.hasText(processDefinitionId)) {
            return null;
        }
        return cache.computeIfAbsent(processDefinitionId,
                id -> repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult());
    }

    private HistoricProcessInstance getHistoricProcessInstance(Map<String, HistoricProcessInstance> cache, String processInstanceId) {
        if (!StringUtils.hasText(processInstanceId)) {
            return null;
        }
        if (cache.containsKey(processInstanceId)) {
            return cache.get(processInstanceId);
        }
        HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        cache.put(processInstanceId, instance);
        return instance;
    }

    private String getRequiredUsername() {
        String username = SecurityUtils.getUsername();
        if (!StringUtils.hasText(username)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "当前登录用户缺少用户名信息");
        }
        return username;
    }

    private boolean isSupportedBpmnFile(String filename) {
        String lowerFilename = filename.toLowerCase();
        return BPMN_SUFFIXES.stream().anyMatch(lowerFilename::endsWith);
    }

    private String stripExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex <= 0) {
            return filename;
        }
        return filename.substring(0, lastDotIndex);
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}
