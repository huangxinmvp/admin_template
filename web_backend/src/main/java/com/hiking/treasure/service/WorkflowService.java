package com.hiking.treasure.service;

import com.hiking.treasure.domain.dto.WorkflowDraftSaveDTO;
import com.hiking.treasure.domain.dto.create.WorkflowStartProcessDTO;
import com.hiking.treasure.domain.dto.update.WorkflowTaskCompleteDTO;
import com.hiking.treasure.domain.vo.workflow.WorkflowDefinitionVO;
import com.hiking.treasure.domain.vo.workflow.WorkflowDeploymentVO;
import com.hiking.treasure.domain.vo.workflow.WorkflowDraftVO;
import com.hiking.treasure.domain.vo.workflow.WorkflowInstanceVO;
import com.hiking.treasure.domain.vo.workflow.WorkflowProcessInstanceDetailVO;
import com.hiking.treasure.domain.vo.workflow.WorkflowTaskVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface WorkflowService {
    List<WorkflowDefinitionVO> listDefinitions(String definitionKey, String definitionName);

    String getDefinitionXml(String definitionId);

    List<WorkflowDraftVO> listDrafts(String keyword);

    WorkflowDraftVO getDraft(String draftId);

    WorkflowDraftVO saveDraft(WorkflowDraftSaveDTO dto);

    boolean deleteDraft(String draftId);

    WorkflowDeploymentVO deployProcess(String deploymentName, String category, MultipartFile file);

    boolean deleteDeployment(String deploymentId, boolean cascade);

    WorkflowInstanceVO startProcess(WorkflowStartProcessDTO dto);

    List<WorkflowTaskVO> listMyTodoTasks();

    List<WorkflowTaskVO> listMyDoneTasks();

    boolean completeTask(String taskId, WorkflowTaskCompleteDTO dto);

    List<WorkflowInstanceVO> listMyStartedInstances();

    WorkflowProcessInstanceDetailVO getProcessInstanceDetail(String processInstanceId);
}
