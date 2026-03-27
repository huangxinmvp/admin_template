import { request } from '@umijs/max';
import type {
  BackendResponse,
  WorkflowCompleteTaskPayload,
  WorkflowDefinition,
  WorkflowDeployment,
  WorkflowDraft,
  WorkflowInstance,
  WorkflowProcessInstanceDetail,
  WorkflowSaveDraftPayload,
  WorkflowStartProcessPayload,
  WorkflowTask,
} from './types';

export const getWorkflowDefinitions = async (params?: {
  key?: string;
  name?: string;
}) =>
  request<BackendResponse<WorkflowDefinition[]>>('/api/workflow/definitions', {
    method: 'GET',
    params,
  });

export const getWorkflowDefinitionXml = async (definitionId: string) =>
  request<BackendResponse<string>>(`/api/workflow/definitions/${definitionId}/xml`, {
    method: 'GET',
  });

export const getWorkflowDrafts = async (params?: { keyword?: string }) =>
  request<BackendResponse<WorkflowDraft[]>>('/api/workflow/drafts', {
    method: 'GET',
    params,
  });

export const getWorkflowDraft = async (draftId: string) =>
  request<BackendResponse<WorkflowDraft>>(`/api/workflow/drafts/${draftId}`, {
    method: 'GET',
  });

export const saveWorkflowDraft = async (data: WorkflowSaveDraftPayload) =>
  request<BackendResponse<WorkflowDraft>>('/api/workflow/drafts', {
    method: 'POST',
    data,
  });

export const deleteWorkflowDraft = async (draftId: string) =>
  request<BackendResponse<boolean>>(`/api/workflow/drafts/${draftId}`, {
    method: 'DELETE',
  });

export const deployWorkflowDefinition = async (params: {
  file: File;
  name?: string;
  category?: string;
}) => {
  const formData = new FormData();
  formData.append('file', params.file);
  if (params.name) {
    formData.append('name', params.name);
  }
  if (params.category) {
    formData.append('category', params.category);
  }

  return request<BackendResponse<WorkflowDeployment>>('/api/workflow/deployments', {
    method: 'POST',
    data: formData,
  });
};

export const deleteWorkflowDeployment = async (
  deploymentId: string,
  cascade = true,
) =>
  request<BackendResponse<boolean>>(`/api/workflow/deployments/${deploymentId}`, {
    method: 'DELETE',
    params: { cascade },
  });

export const startWorkflowInstance = async (data: WorkflowStartProcessPayload) =>
  request<BackendResponse<WorkflowInstance>>('/api/workflow/instances', {
    method: 'POST',
    data,
  });

export const getMyWorkflowStartedInstances = async () =>
  request<BackendResponse<WorkflowInstance[]>>('/api/workflow/instances/my-started', {
    method: 'GET',
  });

export const getWorkflowInstanceDetail = async (processInstanceId: string) =>
  request<BackendResponse<WorkflowProcessInstanceDetail>>(
    `/api/workflow/instances/${processInstanceId}`,
    {
      method: 'GET',
    },
  );

export const getMyWorkflowTodoTasks = async () =>
  request<BackendResponse<WorkflowTask[]>>('/api/workflow/tasks/todo', {
    method: 'GET',
  });

export const getMyWorkflowDoneTasks = async () =>
  request<BackendResponse<WorkflowTask[]>>('/api/workflow/tasks/done', {
    method: 'GET',
  });

export const completeWorkflowTask = async (
  taskId: string,
  data: WorkflowCompleteTaskPayload,
) =>
  request<BackendResponse<boolean>>(`/api/workflow/tasks/${taskId}/complete`, {
    method: 'POST',
    data,
  });
