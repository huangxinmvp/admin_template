export interface BackendResponse<T> {
  success: boolean;
  message: string;
  code: number;
  errorKey?: string;
  result: T;
  timestamp: number;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages?: number;
}

export interface AuthPayload {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  sessionId?: string;
  userId: string;
  tenantId?: string;
  tenantCode?: string;
  tenantName?: string;
  username: string;
  realname?: string;
  roles?: string[];
  depts?: string[];
  permissions?: string[];
}

export interface CurrentUserProfile {
  userId: string;
  tenantId?: string;
  tenantCode?: string;
  tenantName?: string;
  username: string;
  realname?: string;
  avatar?: string;
  email?: string;
  phone?: string;
  roles?: string[];
  depts?: string[];
  permissions?: string[];
}

export interface DashboardStats {
  tenantCount: number;
  userCount: number;
  enabledUserCount: number;
  roleCount: number;
  permissionCount: number;
  departCount: number;
  announcementCount: number;
  quartzJobCount: number;
  overview?: DashboardOverview;
  stageDistribution?: DashboardStageDistributionItem[];
  bottleneckSummary?: DashboardBottleneckSummary;
  budgetHealth?: DashboardBudgetHealthSummary;
  attentionProjects?: DashboardAttentionProjectItem[];
  recentGovernanceActivities?: DashboardRecentActivityItem[];
}

export interface DashboardOverview {
  totalProjectCount: number;
  activeProjectCount: number;
  pendingDecisionCount: number;
  pendingApprovalCount: number;
  blockedProjectCount: number;
  highRiskProjectCount: number;
  budgetWarningProjectCount: number;
  budgetExceededProjectCount: number;
}

export interface DashboardStageDistributionItem {
  stageCode?: string | null;
  stageName?: string | null;
  projectCount?: number | null;
}

export interface DashboardBottleneckSummary {
  blockerClarificationProjectCount: number;
  pendingDecisionProjectCount: number;
  blockingApprovalProjectCount: number;
  budgetWarningOrExceededProjectCount: number;
  missingCriticalRoleProjectCount: number;
  failedGateConditionProjectCount: number;
}

export interface DashboardBudgetHealthSummary {
  unplannedProjectCount: number;
  pendingProjectCount: number;
  healthyProjectCount: number;
  warningProjectCount: number;
  overrunProjectCount: number;
}

export interface DashboardAttentionProjectItem {
  projectId: string;
  projectCode?: string | null;
  projectName?: string | null;
  currentStageCode?: string | null;
  currentStageName?: string | null;
  riskLevel?: string | null;
  blockingReason?: string | null;
  budgetStatus?: string | null;
  pendingApprovalCount?: number | null;
  pendingDecisionCount?: number | null;
  updateTime?: string | null;
}

export interface DashboardRecentActivityItem {
  projectId?: string | null;
  projectName?: string | null;
  activityType?: string | null;
  title?: string | null;
  description?: string | null;
  status?: string | null;
  occurredAt?: string | null;
}

export interface OptionItem {
  label: string;
  value: string | number;
  code?: string;
}

export interface TreeOption {
  title: string;
  label: string;
  value: string | number;
  key: string;
  children?: TreeOption[];
}

export interface PermissionTreeNode {
  id: string;
  parentId?: string | null;
  name: string;
  url?: string;
  component?: string;
  perms?: string;
  type?: number;
  icon?: string;
  sortNo?: number;
  hidden?: number;
  alwaysShow?: number;
  status?: number;
  remark?: string;
  children?: PermissionTreeNode[];
}

export interface PermissionTreeSaveNode {
  id: string;
  children?: PermissionTreeSaveNode[];
}

export interface MenuTree {
  id: string;
  parentId?: string | null;
  name: string;
  path?: string | null;
  component?: string | null;
  perms?: string | null;
  type?: number;
  sortNo?: number;
  hidden?: number;
  alwaysShow?: number;
  icon?: string | null;
  children?: MenuTree[];
}

export interface SystemConfigItem {
  configKey: string;
  configName: string;
  configValue?: string | null;
  defaultValue?: string | null;
  valueType?: string | null;
  configured?: boolean;
  requiredFlag?: number;
  placeholder?: string | null;
  description?: string | null;
  optionsJson?: string | null;
  sortNo?: number;
  environmentOverride?: boolean;
  valueSource?: string | null;
}

export interface SystemConfigGroup {
  groupCode: string;
  groupName: string;
  groupSort?: number;
  items: SystemConfigItem[];
}

export interface BrandingConfig {
  siteName: string;
  siteSubtitle?: string | null;
  logoUrl?: string | null;
  primaryColor?: string | null;
  copyrightText?: string | null;
}

export interface UserSession {
  id: string;
  tenantId?: string | null;
  userId: string;
  clientIp?: string | null;
  userAgent?: string | null;
  deviceName?: string | null;
  loginTime?: string | null;
  lastActiveTime?: string | null;
  accessExpiresAt?: string | null;
  refreshExpiresAt?: string | null;
  status?: number;
  logoutReason?: string | null;
  logoutBy?: string | null;
  logoutTime?: string | null;
}

export interface FileCenterMetric {
  label: string;
  count: number;
  totalSize: number;
}

export interface FileCenterStats {
  totalCount: number;
  totalSize: number;
  todayUploadCount: number;
  todayUploadSize: number;
  bizTypeCount: number;
  providerCount: number;
  storageProvider?: string | null;
  defaultBizType?: string | null;
  maxSizeMb?: number | null;
  allowedTypes?: string[];
  bizTypeMetrics?: FileCenterMetric[];
  providerMetrics?: FileCenterMetric[];
}

export interface MessageCenterChannel {
  code: string;
  name: string;
  configured: boolean;
  summary: string;
  targetPath?: string | null;
}

export interface MessageCenterSummary {
  announcementCount: number;
  publishedAnnouncementCount: number;
  draftAnnouncementCount: number;
  revokedAnnouncementCount: number;
  activeAnnouncementCount: number;
  deliveryCount: number;
  readDeliveryCount: number;
  unreadDeliveryCount: number;
  personalUnreadCount: number;
  readRate: number;
  channels?: MessageCenterChannel[];
}

export interface WorkflowDefinition {
  id: string;
  key: string;
  name: string;
  version: number;
  deploymentId: string;
  category?: string | null;
  resourceName?: string | null;
  diagramResourceName?: string | null;
  suspended?: boolean;
}

export interface WorkflowDeployment {
  deploymentId: string;
  name: string;
  category?: string | null;
  tenantId?: string | null;
  deploymentTime?: string | null;
  resourceNames?: string[];
  processDefinitionCount?: number;
}

export interface WorkflowDraft {
  id: string;
  draftName: string;
  processDefinitionKey?: string | null;
  processDefinitionName?: string | null;
  sourceDefinitionId?: string | null;
  sourceDefinitionKey?: string | null;
  category?: string | null;
  bpmnXml?: string | null;
  remark?: string | null;
  createTime?: string | null;
  updateTime?: string | null;
}

export interface WorkflowInstance {
  processInstanceId: string;
  processDefinitionId: string;
  processDefinitionKey: string;
  processDefinitionName?: string | null;
  businessKey?: string | null;
  startUserId?: string | null;
  startTime?: string | null;
  endTime?: string | null;
  finished?: boolean;
  suspended?: boolean;
  currentTaskId?: string | null;
  currentTaskName?: string | null;
}

export interface WorkflowTask {
  taskId: string;
  taskDefinitionKey?: string | null;
  name?: string | null;
  description?: string | null;
  processInstanceId: string;
  processDefinitionId?: string | null;
  processDefinitionKey?: string | null;
  processDefinitionName?: string | null;
  businessKey?: string | null;
  assignee?: string | null;
  owner?: string | null;
  createTime?: string | null;
  dueDate?: string | null;
  endTime?: string | null;
  finished?: boolean;
  suspended?: boolean;
}

export interface WorkflowProcessInstanceDetail {
  instance: WorkflowInstance;
  currentTasks: WorkflowTask[];
  historyTasks: WorkflowTask[];
  variables: GenericRecord;
}

export interface WorkflowStartProcessPayload {
  definitionKey: string;
  businessKey?: string;
  title?: string;
  approver?: string;
  variables?: GenericRecord;
}

export interface WorkflowCompleteTaskPayload {
  approved?: boolean;
  comment?: string;
  variables?: GenericRecord;
}

export interface WorkflowSaveDraftPayload {
  id?: string;
  draftName: string;
  processDefinitionKey?: string;
  processDefinitionName?: string;
  sourceDefinitionId?: string;
  sourceDefinitionKey?: string;
  category?: string;
  bpmnXml: string;
  remark?: string;
}

export interface ProjectCenterListItem {
  id: string;
  projectCode?: string | null;
  projectName?: string | null;
  projectType?: string | null;
  status?: string | null;
  governanceStatus?: string | null;
  blockedFlag?: number | null;
  atRiskFlag?: number | null;
  riskLevel?: string | null;
  ownerDisplayName?: string | null;
  currentStageCode?: string | null;
  currentStageName?: string | null;
  currentStageStatus?: string | null;
  currentGateStatus?: string | null;
  budgetStatus?: string | null;
  budgetApprovedAmount?: number | null;
  budgetConsumedAmount?: number | null;
  budgetRemainingAmount?: number | null;
  pendingDecisionItemCount?: number | null;
  blockerDecisionItemCount?: number | null;
  blockerReasonSummary?: string | null;
  recommendedNextStep?: string | null;
  recommendedActorRole?: string | null;
  recommendedPriority?: string | null;
  updateTime?: string | null;
}

export interface ProjectApprovalSummary {
  pendingCount?: number | null;
  blockerCount?: number | null;
  approvedCount?: number | null;
  rejectedCount?: number | null;
}

export interface ProjectDecisionSummary {
  pendingCount?: number | null;
  openCount?: number | null;
  blockerCount?: number | null;
  resolvedCount?: number | null;
}

export interface ProjectBudgetSummary {
  status?: string | null;
  totalBudgetAmount?: number | null;
  planName?: string | null;
  currencyCode?: string | null;
  proposedAmount?: number | null;
  approvedAmount?: number | null;
  reservedAmount?: number | null;
  lockedAmount?: number | null;
  consumedAmount?: number | null;
  pendingIncreaseAmount?: number | null;
  remainingAmount?: number | null;
  effectiveAt?: string | null;
  lastUpdatedAt?: string | null;
}

export interface ProjectGovernanceSummary {
  currentStageCode?: string | null;
  currentStageName?: string | null;
  currentStageStatus?: string | null;
  currentGateStatus?: string | null;
  governanceStatus?: string | null;
  blockedFlag?: number | null;
  atRiskFlag?: number | null;
  pendingDecisionItemCount?: number | null;
  pendingApprovalCount?: number | null;
  blockerApprovalCount?: number | null;
  blockerDecisionCount?: number | null;
  blockingGateConditionCount?: number | null;
  failedGateConditionCount?: number | null;
  missingCriticalRoleCount?: number | null;
  budgetStatus?: string | null;
  requirementCompletenessScore?: number | null;
  clarificationCount?: number | null;
  blockerClarificationCount?: number | null;
  blockerReasonSummary?: string | null;
  lastRecomputedAt?: string | null;
}

export interface ProjectGateConditionItem {
  key?: string | null;
  title?: string | null;
  status?: string | null;
  summary?: string | null;
}

export interface ProjectAgentRoleSummaryItem {
  id: string;
  roleName?: string | null;
  roleCategory?: string | null;
  status?: string | null;
  defaultFlag?: number | null;
  budgetFactor?: number | null;
  approvalCollaborationFlag?: number | null;
  currentStageParticipationType?: string | null;
  allowedActionCodes?: string[];
}

export interface ProjectActivityItem {
  activityType?: string | null;
  title?: string | null;
  description?: string | null;
  status?: string | null;
  externalUrl?: string | null;
  occurredAt?: string | null;
}

export interface ProjectStageRecord {
  id: string;
  projectId?: string | null;
  stageCode?: string | null;
  stageName?: string | null;
  stageOrder?: number | null;
  stageStatus?: string | null;
  gateStatus?: string | null;
  ownerUserId?: string | null;
  startedAt?: string | null;
  endedAt?: string | null;
  remark?: string | null;
  createTime?: string | null;
  updateTime?: string | null;
}

export interface DecisionItemRecord {
  id: string;
  projectId?: string | null;
  projectStageId?: string | null;
  title?: string | null;
  itemType?: string | null;
  sourceType?: string | null;
  sourceId?: string | null;
  description?: string | null;
  impactSummary?: string | null;
  suggestedOptions?: string | null;
  recommendedOption?: string | null;
  budgetImpactSummary?: string | null;
  projectImpactSummary?: string | null;
  blockerFlag?: number | null;
  priority?: string | null;
  status?: string | null;
  requestedByUserId?: string | null;
  assigneeUserId?: string | null;
  dueAt?: string | null;
  remark?: string | null;
  createBy?: string | null;
  updateBy?: string | null;
  createTime?: string | null;
  updateTime?: string | null;
}

export interface RequirementIntakeRecord {
  id: string;
  projectId?: string | null;
  projectName?: string | null;
  projectType?: string | null;
  businessGoal?: string | null;
  featureSummary?: string | null;
  referenceProducts?: string | null;
  timelineExpectation?: string | null;
  budgetRange?: string | null;
  technicalConstraints?: string | null;
  notes?: string | null;
  attachmentPlaceholders?: string | null;
  createTime?: string | null;
  updateTime?: string | null;
}

export interface ClarificationItemRecord {
  id: string;
  projectId?: string | null;
  title?: string | null;
  question?: string | null;
  category?: string | null;
  severity?: string | null;
  suggestedOptions?: string | null;
  userResponse?: string | null;
  status?: string | null;
  generatedFlag?: number | null;
  promotedDecisionItemId?: string | null;
  remark?: string | null;
  createTime?: string | null;
  updateTime?: string | null;
}

export interface ClarificationSuggestionItem {
  title?: string | null;
  question?: string | null;
  category?: string | null;
  severity?: string | null;
  suggestedOptions?: string | null;
  blockerFlag?: number | null;
  reason?: string | null;
  governanceReason?: string | null;
  followUpModule?: string | null;
  escalationRecommended?: number | null;
}

export interface RequirementClarificationReview {
  participants?: string[];
  scenarioLabel?: string | null;
  collaborationSummary?: string | null;
  recommendedOperatorAction?: string | null;
  governanceLinkage?: Record<string, any> | null;
  roleInsights?: string[];
  selectionGuidance?: string | null;
  governanceInterpretation?: string | null;
  decisionEscalationAdvised?: number | null;
  followUpHints?: string[];
  blockerAssessment?: string | null;
  nextQuestions?: string[];
  suggestions?: ClarificationSuggestionItem[];
}

export interface DecisionActionLogItem {
  id: string;
  decisionItemId?: string | null;
  actionType?: string | null;
  actionComment?: string | null;
  previousStatus?: string | null;
  nextStatus?: string | null;
  operatorUserId?: string | null;
  operatedAt?: string | null;
  createTime?: string | null;
}

export interface DecisionPromotionSuggestionItem {
  clarificationId?: string | null;
  suggestedTitle?: string | null;
  type?: string | null;
  impactSummary?: string | null;
  suggestedOptions?: string | null;
  recommendedOption?: string | null;
  blockerFlag?: number | null;
  reason?: string | null;
}

export interface DecisionBudgetReview {
  participants?: string[];
  scenarioLabel?: string | null;
  collaborationSummary?: string | null;
  recommendedOperatorAction?: string | null;
  governanceLinkage?: Record<string, any> | null;
  roleInsights?: Record<string, string> | null;
  riskFlags?: string[];
  decisionLinkageSummary?: string | null;
  budgetLinkageSummary?: string | null;
  followUpHints?: string[];
  decisionRecommendation?: string | null;
  budgetImpactNote?: string | null;
  budgetConfirmationAdvised?: number | null;
  recommendedOption?: string | null;
  projectImpactNote?: string | null;
  blockerAssessment?: string | null;
  nextSteps?: string[];
}

export interface BudgetImpactSuggestion {
  budgetImpactSummary?: string | null;
  projectImpactSummary?: string | null;
  deltaRange?: string | null;
  affectedRoles?: string[];
  confidence?: string | null;
}

export interface MeetingDecisionCandidateItem {
  title?: string | null;
  decisionType?: string | null;
  blockerFlag?: number | null;
  recommendedOption?: string | null;
}

export interface MeetingSummarySuggestion {
  meetingTitle?: string | null;
  summary?: string | null;
  actionItems?: string[];
  openQuestions?: string[];
  decisionCandidates?: MeetingDecisionCandidateItem[];
}

export interface ProductArchitectureBrief {
  participants?: string[];
  scenarioLabel?: string | null;
  collaborationSummary?: string | null;
  recommendedOperatorAction?: string | null;
  governanceLinkage?: Record<string, any> | null;
  roleInsights?: Record<string, string> | null;
  architectureFocusAreas?: string[];
  deliveryImplications?: string[];
  projectGovernanceLinkageSummary?: string | null;
  followUpHints?: string[];
  briefTitle?: string | null;
  solutionBrief?: string | null;
  risks?: string[];
  openQuestions?: string[];
  nextSteps?: string[];
}

export interface MeetingRecordItem {
  id: string;
  projectId?: string | null;
  meetingTitle?: string | null;
  rawNotes?: string | null;
  summary?: string | null;
  actionItems?: string[];
  openQuestions?: string[];
  decisionCandidates?: MeetingDecisionCandidateItem[];
  sourceType?: string | null;
  sourceObjectId?: string | null;
  generatedBy?: string | null;
  generatedAt?: string | null;
  remark?: string | null;
  createTime?: string | null;
  updateTime?: string | null;
}

export interface ProjectToolBindingItem {
  id: string;
  projectId?: string | null;
  toolType?: string | null;
  bindingType?: string | null;
  externalId?: string | null;
  externalKey?: string | null;
  externalName?: string | null;
  externalUrl?: string | null;
  bindingStatus?: string | null;
  defaultFlag?: number | null;
  metadataJson?: string | null;
  remark?: string | null;
  createTime?: string | null;
  updateTime?: string | null;
}

export interface ToolIntegrationAuditItem {
  id: string;
  projectId?: string | null;
  toolType?: string | null;
  actionType?: string | null;
  sourceObjectType?: string | null;
  sourceObjectId?: string | null;
  bindingId?: string | null;
  previewFlag?: number | null;
  confirmedFlag?: number | null;
  auditStatus?: string | null;
  externalObjectId?: string | null;
  externalObjectUrl?: string | null;
  operatorUserId?: string | null;
  errorMessage?: string | null;
  requestPayloadJson?: string | null;
  responsePayloadJson?: string | null;
  createTime?: string | null;
}

export interface LinearRepresentationPreview {
  mode?: string | null;
  projectId?: string | null;
  teamId?: string | null;
  representationTitle?: string | null;
  representationDescription?: string | null;
  existingIssueId?: string | null;
  existingIssueIdentifier?: string | null;
  existingIssueUrl?: string | null;
  operationSummary?: string | null;
}

export interface LinearWritePreview {
  writeMode?: string | null;
  toolType?: string | null;
  sourceObjectType?: string | null;
  sourceObjectId?: string | null;
  projectId?: string | null;
  teamId?: string | null;
  targetIssueId?: string | null;
  targetIssueIdentifier?: string | null;
  targetIssueUrl?: string | null;
  title?: string | null;
  body?: string | null;
  operationSummary?: string | null;
}

export interface FigmaContextPreview {
  projectId?: string | null;
  bindingType?: string | null;
  bindingName?: string | null;
  figmaUrl?: string | null;
  fileKey?: string | null;
  fileName?: string | null;
  nodeId?: string | null;
  nodeName?: string | null;
  externalUrl?: string | null;
  lastModifiedAt?: string | null;
  operationSummary?: string | null;
  metadataJson?: string | null;
}

export interface ProjectRequirementSummary {
  requirementIntakeId?: string | null;
  hasRequirementIntake?: boolean | null;
  completenessScore?: number | null;
  completedCoreFieldCount?: number | null;
  totalCoreFieldCount?: number | null;
  clarificationCount?: number | null;
  openClarificationCount?: number | null;
  blockerCount?: number | null;
  missingCoreFields?: string[];
  lastIntakeUpdatedAt?: string | null;
}

export interface ProjectRequirementCollaborationSummary {
  appliedClarificationCount?: number | null;
  openClarificationCount?: number | null;
  blockerClarificationCount?: number | null;
  decisionEscalationSuggestionCount?: number | null;
  followUpModules?: string[];
  latestAppliedAt?: string | null;
}

export interface ProjectDecisionBudgetCollaborationSummary {
  appliedDecisionCount?: number | null;
  openDecisionCount?: number | null;
  blockerDecisionCount?: number | null;
  budgetConfirmationSuggestedCount?: number | null;
  latestDecisionTitle?: string | null;
  latestRecommendedOption?: string | null;
  latestAppliedAt?: string | null;
}

export interface ProjectProductArchitectureCollaborationSummary {
  briefCount?: number | null;
  latestBriefTitle?: string | null;
  latestBriefSummary?: string | null;
  latestOpenQuestionCount?: number | null;
  latestActionItemCount?: number | null;
  latestDecisionCandidateCount?: number | null;
  latestSavedAt?: string | null;
}

export interface ProjectCollaborationSummary {
  appliedCollaborationCount?: number | null;
  latestCollaborationAt?: string | null;
  governanceHighlights?: string[];
  requirementClarification?: ProjectRequirementCollaborationSummary | null;
  decisionBudget?: ProjectDecisionBudgetCollaborationSummary | null;
  productArchitecture?: ProjectProductArchitectureCollaborationSummary | null;
}

export interface ProjectNextStepActionItem {
  actionOrder?: number | null;
  actionLabel?: string | null;
  targetModule?: string | null;
  recommendedReason?: string | null;
  addressedRisk?: string | null;
  expectedChange?: string | null;
}

export interface ProjectNextStepGuidance {
  recommendedNextStep?: string | null;
  recommendedReason?: string | null;
  currentBlockers?: string[];
  recommendedActorRole?: string | null;
  recommendedTargetModule?: string | null;
  recommendedPriority?: string | null;
  priorityActions?: ProjectNextStepActionItem[];
}

export interface ApprovalRecordItem {
  id: string;
  projectId?: string | null;
  title?: string | null;
  decisionItemId?: string | null;
  approvalType?: string | null;
  sourceObjectType?: string | null;
  sourceObjectId?: string | null;
  requesterUserId?: string | null;
  approverUserId?: string | null;
  description?: string | null;
  riskSummary?: string | null;
  budgetImpactSummary?: string | null;
  recommendedAction?: string | null;
  blockerFlag?: number | null;
  approvalStatus?: string | null;
  operatorUserId?: string | null;
  submittedAt?: string | null;
  decidedAt?: string | null;
  decisionNote?: string | null;
  remark?: string | null;
  createBy?: string | null;
  createTime?: string | null;
  updateBy?: string | null;
  updateTime?: string | null;
}

export interface ApprovalActionLogItem {
  id: string;
  approvalRecordId?: string | null;
  actionType?: string | null;
  actionComment?: string | null;
  previousStatus?: string | null;
  nextStatus?: string | null;
  operatorUserId?: string | null;
  operatedAt?: string | null;
  createTime?: string | null;
}

export interface BudgetLedgerItem {
  id: string;
  projectId?: string | null;
  budgetPlanId?: string | null;
  entryType?: string | null;
  amount?: number | null;
  balanceAfter?: number | null;
  referenceType?: string | null;
  referenceId?: string | null;
  referenceDisplayName?: string | null;
  occurredAt?: string | null;
  description?: string | null;
  remark?: string | null;
  createTime?: string | null;
  updateTime?: string | null;
}

export interface BudgetPlanRecord {
  id: string;
  projectId?: string | null;
  planName?: string | null;
  currencyCode?: string | null;
  proposedAmount?: number | null;
  approvedAmount?: number | null;
  reservedAmount?: number | null;
  consumedAmount?: number | null;
  roleAllocationsJson?: string | null;
  status?: string | null;
  effectiveAt?: string | null;
  remark?: string | null;
  createTime?: string | null;
  updateTime?: string | null;
}

export interface BudgetRoleAllocationItem {
  roleCode?: string | null;
  roleName?: string | null;
  amount?: number | null;
  sharePercent?: number | null;
}

export interface BudgetCenterListItem {
  projectId: string;
  projectCode?: string | null;
  projectName?: string | null;
  projectType?: string | null;
  budgetPlanId?: string | null;
  planName?: string | null;
  currencyCode?: string | null;
  budgetStatus?: string | null;
  totalBudgetAmount?: number | null;
  lockedAmount?: number | null;
  consumedAmount?: number | null;
  pendingIncreaseAmount?: number | null;
  lastUpdatedTime?: string | null;
}

export interface BudgetCenterDetail {
  projectId: string;
  projectCode?: string | null;
  projectName?: string | null;
  projectType?: string | null;
  budgetSummary?: ProjectBudgetSummary;
  latestBudgetPlan?: BudgetPlanRecord | null;
  roleAllocations?: BudgetRoleAllocationItem[];
  recentLedgerEntries?: BudgetLedgerItem[];
}

export interface ProjectRecord {
  id: string;
  tenantId?: string | null;
  projectCode?: string | null;
  projectName?: string | null;
  projectType?: string | null;
  intakeSummary?: string | null;
  currentStageCode?: string | null;
  status?: string | null;
  riskLevel?: string | null;
  ownerUserId?: string | null;
  workflowTemplateId?: string | null;
  remark?: string | null;
  createTime?: string | null;
  updateTime?: string | null;
}

export interface WorkflowTemplateStageConfigItem {
  id?: string | null;
  templateId?: string | null;
  stageCode?: string | null;
  stageName?: string | null;
  stageOrder?: number | null;
  enabledFlag?: number | null;
  stageDescription?: string | null;
  stageNote?: string | null;
}

export interface WorkflowTemplateCenterListItem {
  id: string;
  templateCode?: string | null;
  templateName?: string | null;
  projectType?: string | null;
  versionNo?: number | null;
  stageCount?: number | null;
  enabledStageCount?: number | null;
  status?: string | null;
  defaultFlag?: number | null;
  description?: string | null;
  updateTime?: string | null;
}

export interface WorkflowTemplateCenterDetail {
  id: string;
  templateCode?: string | null;
  templateName?: string | null;
  projectType?: string | null;
  versionNo?: number | null;
  stageCount?: number | null;
  status?: string | null;
  defaultFlag?: number | null;
  description?: string | null;
  gateChecksConfig?: string | null;
  blockingDecisionConfig?: string | null;
  blockingApprovalConfig?: string | null;
  budgetThresholdConfig?: string | null;
  highRiskApprovalConfig?: string | null;
  remark?: string | null;
  stages?: WorkflowTemplateStageConfigItem[];
  createTime?: string | null;
  updateTime?: string | null;
}

export interface AgentRoleStageParticipationItem {
  id?: string | null;
  agentRoleId?: string | null;
  stageCode?: string | null;
  stageName?: string | null;
  participationType?: string | null;
  note?: string | null;
}

export interface AgentRoleAllowedActionItem {
  id?: string | null;
  agentRoleId?: string | null;
  actionCode?: string | null;
  actionName?: string | null;
  allowedFlag?: number | null;
  approvalRequiredFlag?: number | null;
  note?: string | null;
}

export interface AgentRoleCenterListItem {
  id: string;
  roleCode?: string | null;
  roleName?: string | null;
  roleCategory?: string | null;
  description?: string | null;
  status?: string | null;
  defaultFlag?: number | null;
  budgetFactor?: number | null;
  approvalCollaborationFlag?: number | null;
  involvedStageCount?: number | null;
  allowedActionCount?: number | null;
  updateTime?: string | null;
}

export interface AgentRoleCenterDetail {
  id: string;
  roleCode?: string | null;
  roleName?: string | null;
  roleCategory?: string | null;
  description?: string | null;
  capabilitySummary?: string | null;
  status?: string | null;
  defaultFlag?: number | null;
  budgetFactor?: number | null;
  approvalCollaborationFlag?: number | null;
  approvalRequired?: number | null;
  maxConcurrency?: number | null;
  remark?: string | null;
  stageParticipations?: AgentRoleStageParticipationItem[];
  allowedActions?: AgentRoleAllowedActionItem[];
  createTime?: string | null;
  updateTime?: string | null;
}

export interface ProjectCenterDetail {
  project: ProjectRecord;
  ownerDisplayName?: string | null;
  workflowTemplateName?: string | null;
  workflowTemplateCurrentStageCode?: string | null;
  workflowTemplateCurrentStageName?: string | null;
  governanceSummary?: ProjectGovernanceSummary;
  decisionSummary?: ProjectDecisionSummary;
  approvalSummary?: ProjectApprovalSummary;
  budgetSummary?: ProjectBudgetSummary;
  requirementSummary?: ProjectRequirementSummary;
  collaborationSummary?: ProjectCollaborationSummary | null;
  nextStepGuidance?: ProjectNextStepGuidance | null;
  requirementIntake?: RequirementIntakeRecord;
  stages?: ProjectStageRecord[];
  recentClarificationItems?: ClarificationItemRecord[];
  recentDecisionItems?: DecisionItemRecord[];
  recentApprovalRecords?: ApprovalRecordItem[];
  recentBudgetLedgerEntries?: BudgetLedgerItem[];
  recentMeetingRecords?: MeetingRecordItem[];
  projectToolBindings?: ProjectToolBindingItem[];
  recentToolIntegrationAudits?: ToolIntegrationAuditItem[];
  linkedAgentRoles?: ProjectAgentRoleSummaryItem[];
  currentStageRecommendedRoles?: ProjectAgentRoleSummaryItem[];
  missingCriticalRoles?: string[];
  currentGateConditions?: ProjectGateConditionItem[];
  recentActivities?: ProjectActivityItem[];
}

export type GenericRecord = Record<string, any>;
