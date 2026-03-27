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
  requiredFlag?: number;
  placeholder?: string | null;
  description?: string | null;
  optionsJson?: string | null;
  sortNo?: number;
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

export type GenericRecord = Record<string, any>;
