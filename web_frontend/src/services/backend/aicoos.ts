import { request } from '@umijs/max';
import type {
  AgentRoleCenterDetail,
  AgentRoleCenterListItem,
  ApprovalActionLogItem,
  ApprovalRecordItem,
  BackendResponse,
  BudgetImpactSuggestion,
  BudgetCenterDetail,
  BudgetCenterListItem,
  ClarificationItemRecord,
  ClarificationSuggestionItem,
  DecisionActionLogItem,
  DecisionItemRecord,
  DecisionPromotionSuggestionItem,
  FigmaContextPreview,
  LinearRepresentationPreview,
  LinearWritePreview,
  MeetingDecisionCandidateItem,
  MeetingRecordItem,
  MeetingSummarySuggestion,
  ProductArchitectureBrief,
  ProjectToolBindingItem,
  PageResult,
  RequirementClarificationReview,
  ProjectCenterDetail,
  ProjectCenterListItem,
  RequirementIntakeRecord,
  DecisionBudgetReview,
  ToolIntegrationAuditItem,
  WorkflowTemplateCenterDetail,
  WorkflowTemplateCenterListItem,
} from './types';

export const getProjectCenterPage = async (params: {
  current?: number;
  pageSize?: number;
  keyword?: string;
  projectType?: string;
  currentStageCode?: string;
  status?: string;
  riskLevel?: string;
}) =>
  request<BackendResponse<PageResult<ProjectCenterListItem>>>(
    '/api/aicoos/project/center/page',
    {
      method: 'GET',
      params: {
        pageNo: params.current,
        pageSize: params.pageSize,
        keyword: params.keyword,
        projectType: params.projectType,
        currentStageCode: params.currentStageCode,
        status: params.status,
        riskLevel: params.riskLevel,
      },
    },
  );

export const getProjectCenterDetail = async (id: string | number) =>
  request<BackendResponse<ProjectCenterDetail>>(`/api/aicoos/project/center/${id}`, {
    method: 'GET',
  });

export const getAgentRoleCenterPage = async (params: {
  current?: number;
  pageSize?: number;
  keyword?: string;
  roleCategory?: string;
  status?: string;
  defaultFlag?: number;
}) =>
  request<BackendResponse<PageResult<AgentRoleCenterListItem>>>(
    '/api/aicoos/agentRole/center/page',
    {
      method: 'GET',
      params: {
        pageNo: params.current,
        pageSize: params.pageSize,
        keyword: params.keyword,
        roleCategory: params.roleCategory,
        status: params.status,
        defaultFlag: params.defaultFlag,
      },
    },
  );

export const getAgentRoleCenterDetail = async (id: string | number) =>
  request<BackendResponse<AgentRoleCenterDetail>>(`/api/aicoos/agentRole/center/${id}`, {
    method: 'GET',
  });

export const createAgentRoleCenter = async (data: Record<string, any>) =>
  request<BackendResponse<AgentRoleCenterDetail>>('/api/aicoos/agentRole/center', {
    method: 'POST',
    data,
  });

export const updateAgentRoleCenter = async (id: string | number, data: Record<string, any>) =>
  request<BackendResponse<AgentRoleCenterDetail>>(`/api/aicoos/agentRole/center/${id}`, {
    method: 'PUT',
    data,
  });

export const getWorkflowTemplateCenterPage = async (params: {
  current?: number;
  pageSize?: number;
  keyword?: string;
  projectType?: string;
  status?: string;
  defaultFlag?: number;
}) =>
  request<BackendResponse<PageResult<WorkflowTemplateCenterListItem>>>(
    '/api/aicoos/workflowTemplate/center/page',
    {
      method: 'GET',
      params: {
        pageNo: params.current,
        pageSize: params.pageSize,
        keyword: params.keyword,
        projectType: params.projectType,
        status: params.status,
        defaultFlag: params.defaultFlag,
      },
    },
  );

export const getWorkflowTemplateCenterDetail = async (id: string | number) =>
  request<BackendResponse<WorkflowTemplateCenterDetail>>(
    `/api/aicoos/workflowTemplate/center/${id}`,
    {
      method: 'GET',
    },
  );

export const createWorkflowTemplateCenter = async (data: Record<string, any>) =>
  request<BackendResponse<WorkflowTemplateCenterDetail>>('/api/aicoos/workflowTemplate/center', {
    method: 'POST',
    data,
  });

export const updateWorkflowTemplateCenter = async (
  id: string | number,
  data: Record<string, any>,
) =>
  request<BackendResponse<WorkflowTemplateCenterDetail>>(
    `/api/aicoos/workflowTemplate/center/${id}`,
    {
      method: 'PUT',
      data,
    },
  );

export const getBudgetCenterPage = async (params: {
  current?: number;
  pageSize?: number;
  projectId?: string;
  keyword?: string;
  projectType?: string;
  budgetStatus?: string;
}) =>
  request<BackendResponse<PageResult<BudgetCenterListItem>>>(
    '/api/aicoos/budgetCenter/page',
    {
      method: 'GET',
      params: {
        pageNo: params.current,
        pageSize: params.pageSize,
        projectId: params.projectId,
        keyword: params.keyword,
        projectType: params.projectType,
        budgetStatus: params.budgetStatus,
      },
    },
  );

export const getBudgetCenterDetail = async (projectId: string | number) =>
  request<BackendResponse<BudgetCenterDetail>>(
    `/api/aicoos/budgetCenter/project/${projectId}`,
    {
      method: 'GET',
    },
  );

export const getProjectRequirementIntake = async (projectId: string | number) =>
  request<BackendResponse<RequirementIntakeRecord | null>>(
    `/api/aicoos/requirementIntake/project/${projectId}`,
    {
      method: 'GET',
    },
  );

export const saveProjectRequirementIntake = async (
  projectId: string | number,
  data: Record<string, any>,
) =>
  request<BackendResponse<RequirementIntakeRecord>>(
    `/api/aicoos/requirementIntake/project/${projectId}`,
    {
      method: 'PUT',
      data,
    },
  );

export const generateProjectClarificationItems = async (projectId: string | number) =>
  request<BackendResponse<ClarificationItemRecord[]>>(
    `/api/aicoos/clarificationItem/project/${projectId}/mock-generate`,
    {
      method: 'POST',
    },
  );

export const generateRequirementClarificationSuggestions = async (projectId: string | number) =>
  request<BackendResponse<ClarificationSuggestionItem[]>>(
    `/api/aicoos/requirementIntake/project/${projectId}/clarification-suggestions`,
    {
      method: 'POST',
    },
  );

export const generateRequirementClarificationCollaboration = async (
  projectId: string | number,
) =>
  request<BackendResponse<RequirementClarificationReview>>(
    `/api/aicoos/requirementIntake/project/${projectId}/clarification-collaboration`,
    {
      method: 'POST',
    },
  );

export const applyRequirementClarificationSuggestions = async (
  projectId: string | number,
  suggestions: ClarificationSuggestionItem[],
  sourceContext?: string,
) =>
  request<BackendResponse<ClarificationItemRecord[]>>(
    `/api/aicoos/requirementIntake/project/${projectId}/clarification-suggestions/apply`,
    {
      method: 'POST',
      data: {
        suggestions,
        sourceContext,
      },
    },
  );

export const promoteClarificationToDecision = async (clarificationItemId: string | number) =>
  request<BackendResponse<DecisionItemRecord>>(
    `/api/aicoos/clarificationItem/${clarificationItemId}/promote`,
    {
      method: 'POST',
    },
  );

export const generateProjectDecisionSuggestions = async (projectId: string | number) =>
  request<BackendResponse<DecisionPromotionSuggestionItem[]>>(
    `/api/aicoos/clarificationItem/project/${projectId}/decision-suggestions`,
    {
      method: 'POST',
    },
  );

export const applyProjectDecisionSuggestions = async (
  projectId: string | number,
  suggestions: DecisionPromotionSuggestionItem[],
) =>
  request<BackendResponse<DecisionItemRecord[]>>(
    `/api/aicoos/clarificationItem/project/${projectId}/decision-suggestions/apply`,
    {
      method: 'POST',
      data: {
        suggestions,
      },
    },
  );

export const getDecisionActionHistory = async (decisionItemId: string | number) =>
  request<BackendResponse<DecisionActionLogItem[]>>(
    `/api/aicoos/decisionItem/${decisionItemId}/actions`,
    {
      method: 'GET',
    },
  );

export const actOnDecision = async (
  decisionItemId: string | number,
  data: { actionType: string; comment?: string },
) =>
  request<BackendResponse<DecisionItemRecord>>(
    `/api/aicoos/decisionItem/${decisionItemId}/actions`,
    {
      method: 'POST',
      data,
    },
  );

export const generateDecisionBudgetImpactSuggestion = async (decisionItemId: string | number) =>
  request<BackendResponse<BudgetImpactSuggestion>>(
    `/api/aicoos/decisionItem/${decisionItemId}/budget-impact-suggestion`,
    {
      method: 'POST',
    },
  );

export const generateDecisionBudgetReview = async (decisionItemId: string | number) =>
  request<BackendResponse<DecisionBudgetReview>>(
    `/api/aicoos/decisionItem/${decisionItemId}/decision-budget-review`,
    {
      method: 'POST',
    },
  );

export const applyDecisionBudgetReview = async (
  decisionItemId: string | number,
  data: DecisionBudgetReview,
) =>
  request<BackendResponse<DecisionItemRecord>>(
    `/api/aicoos/decisionItem/${decisionItemId}/decision-budget-review/apply`,
    {
      method: 'POST',
      data,
    },
  );

export const applyDecisionBudgetImpactSuggestion = async (
  decisionItemId: string | number,
  data: BudgetImpactSuggestion,
) =>
  request<BackendResponse<DecisionItemRecord>>(
    `/api/aicoos/decisionItem/${decisionItemId}/budget-impact-suggestion/apply`,
    {
      method: 'POST',
      data,
    },
  );

export const getApprovalActionHistory = async (approvalRecordId: string | number) =>
  request<BackendResponse<ApprovalActionLogItem[]>>(
    `/api/aicoos/approvalRecord/${approvalRecordId}/actions`,
    {
      method: 'GET',
    },
  );

export const actOnApproval = async (
  approvalRecordId: string | number,
  data: { actionType: string; comment?: string },
) =>
  request<BackendResponse<ApprovalRecordItem>>(
    `/api/aicoos/approvalRecord/${approvalRecordId}/actions`,
    {
      method: 'POST',
      data,
    },
  );

export const getApprovalsBySource = async (params: {
  sourceObjectType: string;
  sourceObjectId: string | number;
}) =>
  request<BackendResponse<ApprovalRecordItem[]>>('/api/aicoos/approvalRecord/source', {
    method: 'GET',
    params,
  });

export const createApprovalFromDecision = async (decisionItemId: string | number) =>
  request<BackendResponse<ApprovalRecordItem>>(
    `/api/aicoos/approvalRecord/decisionItem/${decisionItemId}/link`,
    {
      method: 'POST',
    },
  );

export const createApprovalFromBudgetPlan = async (budgetPlanId: string | number) =>
  request<BackendResponse<ApprovalRecordItem>>(
    `/api/aicoos/approvalRecord/budgetPlan/${budgetPlanId}/link`,
    {
      method: 'POST',
    },
  );

export const getMeetingRecordsByProject = async (projectId: string | number) =>
  request<BackendResponse<MeetingRecordItem[]>>(`/api/aicoos/meetingRecord/project/${projectId}`, {
    method: 'GET',
  });

export const generateProjectMeetingSummary = async (
  projectId: string | number,
  data: {
    meetingTitle?: string;
    rawNotes: string;
    sourceType?: string;
    sourceObjectId?: string;
  },
) =>
  request<BackendResponse<MeetingSummarySuggestion>>(
    `/api/aicoos/meetingRecord/project/${projectId}/summary-suggestion`,
    {
      method: 'POST',
      data,
    },
  );

export const generateProductArchitectureBrief = async (
  projectId: string | number,
  data?: {
    focusNotes?: string;
  },
) =>
  request<BackendResponse<ProductArchitectureBrief>>(
    `/api/aicoos/meetingRecord/project/${projectId}/product-architecture-brief`,
    {
      method: 'POST',
      data,
    },
  );

export const saveProjectMeetingRecord = async (
  projectId: string | number,
  data: {
    meetingTitle?: string;
    rawNotes?: string;
    summary?: string;
    actionItems?: string[];
    openQuestions?: string[];
    decisionCandidates?: MeetingDecisionCandidateItem[];
    sourceType?: string;
    sourceObjectId?: string;
    remark?: string;
  },
) =>
  request<BackendResponse<MeetingRecordItem>>(`/api/aicoos/meetingRecord/project/${projectId}`, {
    method: 'POST',
    data,
  });

export const getProjectToolBindings = async (projectId: string | number) =>
  request<BackendResponse<ProjectToolBindingItem[]>>(
    `/api/aicoos/toolIntegration/project/${projectId}/bindings`,
    {
      method: 'GET',
    },
  );

export const getProjectToolIntegrationAudits = async (
  projectId: string | number,
  limit = 8,
) =>
  request<BackendResponse<ToolIntegrationAuditItem[]>>(
    `/api/aicoos/toolIntegration/project/${projectId}/audits`,
    {
      method: 'GET',
      params: {
        limit,
      },
    },
  );

export const previewLinearProjectRepresentation = async (
  projectId: string | number,
  data: {
    mode?: string;
    teamId?: string;
    representationTitle?: string;
    representationDescription?: string;
    existingIssueId?: string;
    existingIssueIdentifier?: string;
    existingIssueUrl?: string;
    remark?: string;
  },
) =>
  request<BackendResponse<LinearRepresentationPreview>>(
    `/api/aicoos/toolIntegration/project/${projectId}/linear/representation/preview`,
    {
      method: 'POST',
      data,
      skipErrorHandler: true,
    },
  );

export const applyLinearProjectRepresentation = async (
  projectId: string | number,
  data: {
    mode?: string;
    teamId?: string;
    representationTitle?: string;
    representationDescription?: string;
    existingIssueId?: string;
    existingIssueIdentifier?: string;
    existingIssueUrl?: string;
    remark?: string;
  },
) =>
  request<BackendResponse<ProjectToolBindingItem>>(
    `/api/aicoos/toolIntegration/project/${projectId}/linear/representation/apply`,
    {
      method: 'POST',
      data,
      skipErrorHandler: true,
    },
  );

export const previewClarificationLinearWrite = async (
  clarificationItemId: string | number,
  data: {
    writeMode?: string;
    teamId?: string;
    title?: string;
    body?: string;
    targetIssueId?: string;
    targetIssueIdentifier?: string;
    targetIssueUrl?: string;
    remark?: string;
  },
) =>
  request<BackendResponse<LinearWritePreview>>(
    `/api/aicoos/toolIntegration/clarification/${clarificationItemId}/linear/preview`,
    {
      method: 'POST',
      data,
      skipErrorHandler: true,
    },
  );

export const applyClarificationLinearWrite = async (
  clarificationItemId: string | number,
  data: {
    writeMode?: string;
    teamId?: string;
    title?: string;
    body?: string;
    targetIssueId?: string;
    targetIssueIdentifier?: string;
    targetIssueUrl?: string;
    remark?: string;
  },
) =>
  request<BackendResponse<ToolIntegrationAuditItem>>(
    `/api/aicoos/toolIntegration/clarification/${clarificationItemId}/linear/apply`,
    {
      method: 'POST',
      data,
      skipErrorHandler: true,
    },
  );

export const previewDecisionLinearWrite = async (
  decisionItemId: string | number,
  data: {
    writeMode?: string;
    teamId?: string;
    title?: string;
    body?: string;
    targetIssueId?: string;
    targetIssueIdentifier?: string;
    targetIssueUrl?: string;
    remark?: string;
  },
) =>
  request<BackendResponse<LinearWritePreview>>(
    `/api/aicoos/toolIntegration/decision/${decisionItemId}/linear/preview`,
    {
      method: 'POST',
      data,
      skipErrorHandler: true,
    },
  );

export const applyDecisionLinearWrite = async (
  decisionItemId: string | number,
  data: {
    writeMode?: string;
    teamId?: string;
    title?: string;
    body?: string;
    targetIssueId?: string;
    targetIssueIdentifier?: string;
    targetIssueUrl?: string;
    remark?: string;
  },
) =>
  request<BackendResponse<ToolIntegrationAuditItem>>(
    `/api/aicoos/toolIntegration/decision/${decisionItemId}/linear/apply`,
    {
      method: 'POST',
      data,
      skipErrorHandler: true,
    },
  );

export const previewFigmaContext = async (
  projectId: string | number,
  data: {
    figmaUrl?: string;
    fileKey?: string;
    nodeId?: string;
    bindingName?: string;
    remark?: string;
  },
) =>
  request<BackendResponse<FigmaContextPreview>>(
    `/api/aicoos/toolIntegration/project/${projectId}/figma/context/preview`,
    {
      method: 'POST',
      data,
      skipErrorHandler: true,
    },
  );

export const applyFigmaContext = async (
  projectId: string | number,
  data: {
    figmaUrl?: string;
    fileKey?: string;
    nodeId?: string;
    bindingName?: string;
    remark?: string;
  },
) =>
  request<BackendResponse<ProjectToolBindingItem>>(
    `/api/aicoos/toolIntegration/project/${projectId}/figma/context/apply`,
    {
      method: 'POST',
      data,
      skipErrorHandler: true,
    },
  );
