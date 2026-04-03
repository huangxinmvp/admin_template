import {
  downloadProtectedResource,
  getResourceDetail,
  pickLocalFile,
  previewProtectedResource,
  uploadResourceBinary,
} from '@/services/backend/resources';
import {
  getDepartOptions,
  getDepartTree,
  getPermissionTree,
  getRoleOptions,
  getRolePermissionIds,
  getUserAggregate,
  publishAnnouncement,
  queryPageOptions,
  revokeAnnouncement,
  saveRolePermissionIds,
  triggerQuartzJob,
} from '@/services/backend/system';
import type { BackendFieldConfig, ResourceConfig } from './types';

const field = (
  name: string,
  label: string,
  config: Partial<BackendFieldConfig> = {},
): BackendFieldConfig => ({
  name,
  label,
  type: 'text',
  ...config,
});

const enabledStatusOptions = [
  { label: '禁用', value: 0 },
  { label: '启用', value: 1 },
];

const yesNoOptions = [
  { label: '否', value: 0 },
  { label: '是', value: 1 },
];

const announcementStatusOptions = [
  { label: '草稿', value: 0 },
  { label: '已发布', value: 1 },
  { label: '已撤销', value: 2 },
];

const announcementReadFlagOptions = [
  { label: '未读', value: 0 },
  { label: '已读', value: 1 },
];

const permissionTypeOptions = [
  { label: '目录', value: 0 },
  { label: '菜单', value: 1 },
  { label: '按钮', value: 2 },
];

const dataRuleConditionOptions = [
  { label: '=', value: '=' },
  { label: '!=', value: '!=' },
  { label: 'in', value: 'in' },
  { label: 'like', value: 'like' },
  { label: '<=', value: '<=' },
  { label: '>=', value: '>=' },
  { label: 'between', value: 'between' },
];

const projectTypeOptions = [
  { label: '交付项目', value: 'delivery' },
  { label: '咨询项目', value: 'consulting' },
  { label: '内部项目', value: 'internal' },
];

const riskLevelOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '关键', value: 'critical' },
];

const projectStatusOptions = [
  { label: '草稿', value: 'draft' },
  { label: '进行中', value: 'active' },
  { label: '已暂停', value: 'paused' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' },
];

const lifecycleStageOptions = [
  { label: '需求接收', value: 'intake' },
  { label: '需求澄清', value: 'clarification' },
  { label: '可行性评估', value: 'feasibility' },
  { label: '预算估算', value: 'estimation' },
  { label: '审批', value: 'approval' },
  { label: '计划', value: 'planning' },
  { label: '设计', value: 'design' },
  { label: '开发', value: 'development' },
  { label: '测试', value: 'testing' },
  { label: '发布审批', value: 'release_approval' },
  { label: '发布', value: 'release' },
  { label: '复盘', value: 'retrospective' },
];

const projectStageStatusOptions = [
  { label: '待开始', value: 'pending' },
  { label: '进行中', value: 'active' },
  { label: '已完成', value: 'completed' },
  { label: '已阻塞', value: 'blocked' },
];

const gateStatusOptions = [
  { label: '无需门禁', value: 'not_required' },
  { label: '待确认', value: 'pending' },
  { label: '已通过', value: 'approved' },
  { label: '已拒绝', value: 'rejected' },
];

const decisionItemTypeOptions = [
  { label: '澄清事项', value: 'clarification' },
  { label: '范围变更', value: 'scope_change' },
  { label: '预算变更', value: 'budget_change' },
  { label: '发布决策', value: 'release' },
];

const decisionPriorityOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '关键', value: 'critical' },
];

const decisionItemStatusOptions = [
  { label: '待处理', value: 'open' },
  { label: '待审批', value: 'pending_approval' },
  { label: '已批准', value: 'approved' },
  { label: '已拒绝', value: 'rejected' },
  { label: '已解决', value: 'resolved' },
];

const approvalTypeOptions = [
  { label: '需求审批', value: 'requirement' },
  { label: '预算审批', value: 'budget' },
  { label: '关卡审批', value: 'gate' },
  { label: '发布审批', value: 'release' },
];

const approvalStatusOptions = [
  { label: '草稿', value: 'draft' },
  { label: '已提交', value: 'submitted' },
  { label: '已批准', value: 'approved' },
  { label: '已拒绝', value: 'rejected' },
  { label: '已取消', value: 'cancelled' },
];

const budgetPlanStatusOptions = [
  { label: '草稿', value: 'draft' },
  { label: '待审批', value: 'pending_approval' },
  { label: '已批准', value: 'approved' },
  { label: '已关闭', value: 'closed' },
];

const budgetLedgerEntryTypeOptions = [
  { label: '预算创建', value: 'create' },
  { label: '预算锁定', value: 'lock' },
  { label: '待增补预算', value: 'pending_increase' },
  { label: '人工修正', value: 'manual_correction' },
  { label: '预算预留', value: 'reserve' },
  { label: '预算消耗', value: 'consume' },
  { label: '预算调整', value: 'adjust' },
  { label: '预算回退', value: 'refund' },
];

const agentRoleCategoryOptions = [
  { label: '治理角色', value: 'governance' },
  { label: '交付角色', value: 'delivery' },
  { label: '支持角色', value: 'support' },
];

const agentRoleStatusOptions = [
  { label: '启用', value: 'active' },
  { label: '停用', value: 'inactive' },
];

const workflowTemplateStatusOptions = [
  { label: '草稿', value: 'draft' },
  { label: '启用', value: 'active' },
  { label: '归档', value: 'archived' },
];

const resourceConfigs: ResourceConfig[] = [
  {
    key: 'users',
    path: '/system/users',
    title: '用户管理',
    description: '对接 /api/user、/api/role/options、/api/depart/options 等接口。',
    resourcePath: '/api/user',
    fields: [
      field('id', 'ID', {
        hideInForm: true,
        hideInSearch: true,
        copyable: true,
        width: 180,
      }),
      field('username', '用户名', { required: true }),
      field('realname', '姓名'),
      field('password', '密码', {
        type: 'password',
        hideInTable: true,
        hideInSearch: true,
        requiredOnCreate: true,
      }),
      field('email', '邮箱'),
      field('phone', '手机号'),
      field('tenantId', '租户ID'),
      field('status', '状态', {
        type: 'select',
        options: enabledStatusOptions,
      }),
      field('lockUntil', '锁定到', {
        type: 'datetime',
      }),
      field('roleIds', '角色', {
        type: 'multiselect',
        hideInTable: true,
        hideInSearch: true,
        loadOptions: getRoleOptions,
      }),
      field('departIds', '部门', {
        type: 'multiselect',
        hideInTable: true,
        hideInSearch: true,
        loadOptions: getDepartOptions,
      }),
      field('remark', '备注', {
        type: 'textarea',
        hideInTable: true,
      }),
    ],
    detailLoader: async (id) => {
      const [detailResponse, aggregateResponse] = await Promise.all([
        getResourceDetail('/api/user', id),
        getUserAggregate(id),
      ]);
      return {
        ...detailResponse.result,
        roleIds: aggregateResponse.result.roleIds || [],
        departIds: aggregateResponse.result.departIds || [],
      };
    },
    transformSubmit: (values, context) => {
      const next = { ...values };
      if (context.mode === 'edit' && !next.password) {
        delete next.password;
      }
      return next;
    },
  },
  {
    key: 'roles',
    path: '/system/roles',
    title: '角色管理',
    description: '支持角色基础信息维护，并同步保存角色-权限关联。',
    resourcePath: '/api/role',
    fields: [
      field('id', 'ID', {
        hideInForm: true,
        hideInSearch: true,
        copyable: true,
        width: 180,
      }),
      field('roleName', '角色名称', { required: true }),
      field('roleCode', '角色编码', { required: true }),
      field('description', '描述'),
      field('scopeType', '作用域'),
      field('status', '状态', {
        type: 'select',
        options: enabledStatusOptions,
      }),
      field('permissionIds', '权限树', {
        type: 'treeMultiselect',
        hideInTable: true,
        hideInSearch: true,
        loadTreeOptions: getPermissionTree,
      }),
      field('remark', '备注', {
        type: 'textarea',
        hideInTable: true,
      }),
    ],
    detailLoader: async (id) => {
      const [detailResponse, permissionIds] = await Promise.all([
        getResourceDetail('/api/role', id),
        getRolePermissionIds(id),
      ]);
      return {
        ...detailResponse.result,
        permissionIds,
      };
    },
    afterSave: async ({ savedId, values }) => {
      if (!savedId) {
        return;
      }
      await saveRolePermissionIds(savedId, values.permissionIds || []);
    },
  },
  {
    key: 'permissions',
    path: '/system/permissions',
    title: '权限菜单',
    description: '对接 /api/permission 系列接口。',
    resourcePath: '/api/permission',
    fields: [
      field('id', 'ID', {
        hideInForm: true,
        hideInSearch: true,
        copyable: true,
        width: 180,
      }),
      field('parentId', '上级节点', {
        type: 'treeSelect',
        hideInTable: true,
        hideInSearch: true,
        loadTreeOptions: getPermissionTree,
      }),
      field('name', '名称', { required: true }),
      field('url', '路径'),
      field('component', '组件'),
      field('perms', '权限标识'),
      field('type', '类型', {
        type: 'select',
        options: permissionTypeOptions,
      }),
      field('icon', '图标'),
      field('sortNo', '排序', { type: 'number' }),
      field('hidden', '隐藏', {
        type: 'select',
        options: yesNoOptions,
      }),
      field('alwaysShow', '总是显示', {
        type: 'select',
        options: yesNoOptions,
      }),
      field('status', '状态', {
        type: 'select',
        options: enabledStatusOptions,
      }),
      field('remark', '备注', {
        type: 'textarea',
        hideInTable: true,
      }),
    ],
  },
  {
    key: 'departs',
    path: '/system/departs',
    title: '部门管理',
    description: '支持树状部门维护。',
    resourcePath: '/api/depart',
    fields: [
      field('id', 'ID', {
        hideInForm: true,
        hideInSearch: true,
        copyable: true,
        width: 180,
      }),
      field('parentId', '上级部门', {
        type: 'treeSelect',
        hideInTable: true,
        hideInSearch: true,
        loadTreeOptions: getDepartTree,
      }),
      field('departName', '部门名称', { required: true }),
      field('orgCode', '部门编码'),
      field('departOrder', '排序', { type: 'number' }),
      field('orgCategory', '组织类型', { type: 'number' }),
      field('status', '状态', {
        type: 'select',
        options: enabledStatusOptions,
      }),
      field('remark', '备注', {
        type: 'textarea',
        hideInTable: true,
      }),
    ],
  },
  {
    key: 'tenants',
    path: '/system/tenants',
    title: '租户管理',
    description: '对接 /api/tenant。',
    resourcePath: '/api/tenant',
    fields: [
      field('id', 'ID', {
        hideInForm: true,
        hideInSearch: true,
        copyable: true,
        width: 180,
      }),
      field('tenantName', '租户名称', { required: true }),
      field('tenantCode', '租户编码', { required: true }),
      field('contactName', '联系人'),
      field('contactPhone', '联系电话'),
      field('contactEmail', '联系邮箱'),
      field('packageName', '套餐名称'),
      field('expireTime', '到期时间', { type: 'datetime' }),
      field('status', '状态', {
        type: 'select',
        options: enabledStatusOptions,
      }),
      field('remark', '备注', {
        type: 'textarea',
        hideInTable: true,
      }),
    ],
  },
  {
    key: 'dicts',
    path: '/system/dicts',
    title: '字典管理',
    description: '对接 /api/dict。',
    resourcePath: '/api/dict',
    fields: [
      field('id', 'ID', {
        hideInForm: true,
        hideInSearch: true,
        copyable: true,
        width: 180,
      }),
      field('dictName', '字典名称', { required: true }),
      field('dictCode', '字典编码', { required: true }),
      field('description', '描述'),
      field('status', '状态', {
        type: 'select',
        options: enabledStatusOptions,
      }),
    ],
  },
  {
    key: 'dict-items',
    path: '/system/dict-items',
    title: '字典项管理',
    description: '对接 /api/dictItem。',
    resourcePath: '/api/dictItem',
    fields: [
      field('id', 'ID', {
        hideInForm: true,
        hideInSearch: true,
        copyable: true,
        width: 180,
      }),
      field('dictId', '所属字典', {
        type: 'select',
        required: true,
        loadOptions: () => queryPageOptions('/api/dict', 'dictName'),
      }),
      field('itemText', '显示文本', { required: true }),
      field('itemValue', '字典值', { required: true }),
      field('sortOrder', '排序', { type: 'number' }),
      field('status', '状态', {
        type: 'select',
        options: enabledStatusOptions,
      }),
    ],
  },
  {
    key: 'announcements',
    path: '/system/announcements',
    title: '公告管理',
    description: '对接 /api/announcement，支持发布和撤销。',
    resourcePath: '/api/announcement',
    fields: [
      field('id', 'ID', {
        hideInForm: true,
        hideInSearch: true,
        copyable: true,
        width: 180,
      }),
      field('title', '标题', { required: true }),
      field('msgContent', '内容', {
        type: 'textarea',
      }),
      field('startTime', '开始时间', { type: 'datetime' }),
      field('endTime', '结束时间', { type: 'datetime' }),
      field('sender', '发送人'),
      field('priority', '优先级', { type: 'number' }),
      field('msgCategory', '分类', { type: 'number' }),
      field('msgType', '类型', { type: 'number' }),
      field('receiverScope', '接收范围'),
      field('targetTenantId', '目标租户', {
        type: 'select',
        loadOptions: () => queryPageOptions('/api/tenant', 'tenantName'),
      }),
      field('sendStatus', '状态', {
        type: 'select',
        options: announcementStatusOptions,
      }),
      field('sendTime', '发布时间', {
        type: 'datetime',
        hideInForm: true,
      }),
      field('cancelTime', '撤销时间', {
        type: 'datetime',
        hideInForm: true,
      }),
    ],
    rowActions: [
      {
        key: 'publish',
        label: '发布',
        visible: (record) => Number(record.sendStatus) !== 1,
        onClick: async (record) => {
          await publishAnnouncement(record.id);
        },
      },
      {
        key: 'revoke',
        label: '撤销',
        visible: (record) => Number(record.sendStatus) === 1,
        onClick: async (record) => {
          await revokeAnnouncement(record.id);
        },
      },
    ],
  },
  {
    key: 'announcement-sends',
    path: '/system/announcement-sends',
    title: '公告送达',
    description: '查看公告送达记录与已读状态。',
    resourcePath: '/api/announcementSend',
    allowCreate: false,
    allowEdit: false,
    allowDelete: false,
    allowBatchDelete: false,
    allowImport: false,
    fields: [
      field('id', 'ID', {
        hideInForm: true,
        hideInSearch: true,
        copyable: true,
        width: 180,
      }),
      field('anntId', '公告标题', {
        type: 'select',
        loadOptions: () => queryPageOptions('/api/announcement', 'title'),
      }),
      field('userId', '接收用户', {
        type: 'select',
        loadOptions: () => queryPageOptions('/api/user', 'username'),
      }),
      field('readFlag', '阅读状态', {
        type: 'select',
        options: announcementReadFlagOptions,
      }),
      field('readTime', '阅读时间', {
        type: 'datetime',
        hideInForm: true,
      }),
    ],
  },
  {
    key: 'files',
    path: '/system/files',
    title: '文件管理',
    description: '支持上传、预览、下载、导出，文件元数据与存储信息均可追踪。',
    resourcePath: '/api/file',
    allowCreate: false,
    allowEdit: false,
    allowImport: false,
    fields: [
      field('id', 'ID', {
        hideInForm: true,
        hideInSearch: true,
        copyable: true,
        width: 180,
      }),
      field('fileName', '文件名'),
      field('bizType', '业务类型'),
      field('contentType', '内容类型'),
      field('fileSize', '文件大小', { type: 'number' }),
      field('storageType', '存储类型'),
      field('storageProvider', '存储提供商'),
      field('bucketName', '存储目录'),
      field('md5', 'MD5', {
        hideInForm: true,
      }),
      field('url', '访问地址', {
        hideInForm: true,
      }),
      field('status', '状态', {
        type: 'select',
        options: enabledStatusOptions,
      }),
      field('createTime', '创建时间', {
        type: 'datetime',
        hideInForm: true,
      }),
    ],
    toolbarActions: [
      {
        key: 'upload',
        label: '上传文件',
        type: 'primary',
        successMessage: '上传成功',
        onClick: async () => {
          const file = await pickLocalFile('*');
          if (!file) {
            return;
          }
          await uploadResourceBinary('/api/file/upload', file, {
            bizType: 'system',
          });
        },
      },
    ],
    rowActions: [
      {
        key: 'preview',
        label: '预览',
        successMessage: false,
        reloadAfterAction: false,
        onClick: async (record) => {
          await previewProtectedResource(
            `/api/file/preview/${record.id}`,
            record.fileName || 'preview',
          );
        },
      },
      {
        key: 'download',
        label: '下载',
        successMessage: false,
        reloadAfterAction: false,
        onClick: async (record) => {
          await downloadProtectedResource(
            `/api/file/download/${record.id}`,
            record.fileName || 'download',
          );
        },
      },
    ],
  },
  {
    key: 'data-rules',
    path: '/system/data-rules',
    title: '数据权限',
    description: '配置菜单级数据权限规则，限制不同角色和用户可见的数据范围。',
    resourcePath: '/api/permissionDataRule',
    fields: [
      field('id', 'ID', {
        hideInForm: true,
        hideInSearch: true,
        copyable: true,
        width: 180,
      }),
      field('permissionId', '菜单权限', {
        type: 'select',
        required: true,
        loadOptions: () => queryPageOptions('/api/permission', 'name'),
      }),
      field('ruleName', '规则名称', { required: true }),
      field('ruleColumn', '限定字段', { required: true }),
      field('condition', '条件', {
        type: 'select',
        required: true,
        options: dataRuleConditionOptions,
      }),
      field('ruleValue', '规则值', {
        required: true,
      }),
      field('status', '状态', {
        type: 'select',
        options: enabledStatusOptions,
      }),
      field('createTime', '创建时间', {
        type: 'datetime',
        hideInForm: true,
      }),
      field('updateTime', '更新时间', {
        type: 'datetime',
        hideInForm: true,
      }),
    ],
  },
  {
    key: 'jobs',
    path: '/system/jobs',
    title: '定时任务',
    description: '对接 /api/quartzJob，支持手动触发。',
    resourcePath: '/api/quartzJob',
    fields: [
      field('id', 'ID', {
        hideInForm: true,
        hideInSearch: true,
        copyable: true,
        width: 180,
      }),
      field('jobName', '任务名称', { required: true }),
      field('jobGroup', '任务分组'),
      field('invokeTarget', '执行目标', { required: true }),
      field('cronExpression', 'Cron 表达式'),
      field('misfirePolicy', '错失策略'),
      field('concurrent', '允许并发', {
        type: 'select',
        options: yesNoOptions,
      }),
      field('status', '状态', {
        type: 'select',
        options: enabledStatusOptions,
      }),
      field('description', '描述'),
      field('lastRunTime', '上次执行', {
        type: 'datetime',
        hideInForm: true,
      }),
      field('nextRunTime', '下次执行', {
        type: 'datetime',
        hideInForm: true,
      }),
      field('lastMessage', '最近结果', {
        hideInForm: true,
      }),
    ],
    rowActions: [
      {
        key: 'trigger',
        label: '立即执行',
        onClick: async (record) => {
          await triggerQuartzJob(record.id);
        },
      },
    ],
  },
  {
    key: 'job-logs',
    path: '/system/job-logs',
    title: '任务日志',
    description: '只读查看任务执行历史。',
    resourcePath: '/api/quartzJobLog',
    allowCreate: false,
    allowEdit: false,
    fields: [
      field('id', 'ID', {
        hideInForm: true,
        hideInSearch: true,
        copyable: true,
        width: 180,
      }),
      field('jobName', '任务名称'),
      field('jobGroup', '任务分组'),
      field('invokeTarget', '执行目标'),
      field('status', '状态', { type: 'number' }),
      field('exceptionInfo', '异常信息', {
        type: 'textarea',
      }),
      field('costTime', '耗时(ms)', { type: 'number' }),
      field('triggerType', '触发方式'),
      field('createTime', '执行时间', {
        type: 'datetime',
        hideInForm: true,
      }),
    ],
  },
  {
    key: 'logs',
    path: '/system/logs',
    title: '系统日志',
    description: '只读查看系统操作日志。',
    resourcePath: '/api/log',
    allowCreate: false,
    allowEdit: false,
    fields: [
      field('id', 'ID', {
        hideInForm: true,
        hideInSearch: true,
        copyable: true,
        width: 180,
      }),
      field('logType', '日志类型', { type: 'number' }),
      field('logContent', '日志内容'),
      field('operateType', '操作类型', { type: 'number' }),
      field('username', '操作人'),
      field('ip', 'IP'),
      field('method', '方法'),
      field('requestUrl', '请求地址'),
      field('requestType', '请求方式'),
      field('costTime', '耗时(ms)', { type: 'number' }),
      field('createTime', '创建时间', {
        type: 'datetime',
        hideInForm: true,
      }),
    ],
  },
  {
    key: 'aicoos-projects',
    path: '/aicoos/projects',
    title: '项目中心',
    description: 'AICoOS Phase 1 项目治理骨架，覆盖项目、阶段模板与预算配置入口。',
    resourcePath: '/api/aicoos/project',
    allowImport: false,
    allowExport: false,
    fields: [
      field('id', 'ID', {
        hideInForm: true,
        hideInSearch: true,
        copyable: true,
        width: 180,
      }),
      field('projectCode', '项目编码', { required: true }),
      field('projectName', '项目名称', { required: true }),
      field('projectType', '项目类型', {
        type: 'select',
        required: true,
        options: projectTypeOptions,
      }),
      field('intakeSummary', '需求摘要', {
        type: 'textarea',
      }),
      field('currentStageCode', '当前阶段', {
        type: 'select',
        options: lifecycleStageOptions,
      }),
      field('status', '项目状态', {
        type: 'select',
        options: projectStatusOptions,
      }),
      field('riskLevel', '风险等级', {
        type: 'select',
        options: riskLevelOptions,
      }),
      field('ownerUserId', '项目负责人', {
        type: 'select',
        loadOptions: () => queryPageOptions('/api/user', 'username'),
      }),
      field('workflowTemplateId', '工作流模板', {
        type: 'select',
        loadOptions: () => queryPageOptions('/api/aicoos/workflowTemplate', 'templateName'),
      }),
      field('remark', '备注', {
        type: 'textarea',
        hideInTable: true,
      }),
      field('createTime', '创建时间', {
        type: 'datetime',
        hideInForm: true,
      }),
      field('updateTime', '更新时间', {
        type: 'datetime',
        hideInForm: true,
        hideInTable: true,
      }),
    ],
  },
  {
    key: 'aicoos-project-stages',
    path: '/aicoos/project-stages',
    title: '项目阶段',
    description: '维护项目阶段、门禁状态与责任人，为后续治理流转提供结构骨架。',
    resourcePath: '/api/aicoos/projectStage',
    allowImport: false,
    allowExport: false,
    fields: [
      field('id', 'ID', {
        hideInForm: true,
        hideInSearch: true,
        copyable: true,
        width: 180,
      }),
      field('projectId', '所属项目', {
        type: 'select',
        required: true,
        loadOptions: () => queryPageOptions('/api/aicoos/project', 'projectName'),
      }),
      field('stageCode', '阶段编码', {
        type: 'select',
        required: true,
        options: lifecycleStageOptions,
      }),
      field('stageName', '阶段名称', { required: true }),
      field('stageOrder', '阶段顺序', { type: 'number' }),
      field('stageStatus', '阶段状态', {
        type: 'select',
        options: projectStageStatusOptions,
      }),
      field('gateStatus', '门禁状态', {
        type: 'select',
        options: gateStatusOptions,
      }),
      field('ownerUserId', '阶段负责人', {
        type: 'select',
        loadOptions: () => queryPageOptions('/api/user', 'username'),
      }),
      field('startedAt', '开始时间', { type: 'datetime' }),
      field('endedAt', '结束时间', { type: 'datetime' }),
      field('remark', '备注', {
        type: 'textarea',
        hideInTable: true,
      }),
      field('createTime', '创建时间', {
        type: 'datetime',
        hideInForm: true,
      }),
    ],
  },
  {
    key: 'aicoos-decision-items',
    path: '/aicoos/decision-items',
    title: '决策事项',
    description: '承载需求澄清、范围变更、预算调整等待确认事项的管理骨架。',
    resourcePath: '/api/aicoos/decisionItem',
    allowImport: false,
    allowExport: false,
    fields: [
      field('id', 'ID', {
        hideInForm: true,
        hideInSearch: true,
        copyable: true,
        width: 180,
      }),
      field('projectId', '所属项目', {
        type: 'select',
        required: true,
        loadOptions: () => queryPageOptions('/api/aicoos/project', 'projectName'),
      }),
      field('projectStageId', '所属阶段', {
        type: 'select',
        loadOptions: () => queryPageOptions('/api/aicoos/projectStage', 'stageName'),
      }),
      field('title', '事项标题', { required: true }),
      field('itemType', '事项类型', {
        type: 'select',
        options: decisionItemTypeOptions,
      }),
      field('priority', '优先级', {
        type: 'select',
        options: decisionPriorityOptions,
      }),
      field('status', '事项状态', {
        type: 'select',
        options: decisionItemStatusOptions,
      }),
      field('requestedByUserId', '发起人', {
        type: 'select',
        loadOptions: () => queryPageOptions('/api/user', 'username'),
      }),
      field('assigneeUserId', '处理人', {
        type: 'select',
        loadOptions: () => queryPageOptions('/api/user', 'username'),
      }),
      field('dueAt', '到期时间', { type: 'datetime' }),
      field('remark', '备注', {
        type: 'textarea',
        hideInTable: true,
      }),
      field('createTime', '创建时间', {
        type: 'datetime',
        hideInForm: true,
      }),
    ],
  },
  {
    key: 'aicoos-approval-records',
    path: '/aicoos/approval-records',
    title: '审批记录',
    description: '管理项目治理中的预算、关卡、发布等审批记录骨架。',
    resourcePath: '/api/aicoos/approvalRecord',
    allowImport: false,
    allowExport: false,
    fields: [
      field('id', 'ID', {
        hideInForm: true,
        hideInSearch: true,
        copyable: true,
        width: 180,
      }),
      field('projectId', '所属项目', {
        type: 'select',
        loadOptions: () => queryPageOptions('/api/aicoos/project', 'projectName'),
      }),
      field('decisionItemId', '决策事项', {
        type: 'select',
        loadOptions: () => queryPageOptions('/api/aicoos/decisionItem', 'title'),
      }),
      field('approvalType', '审批类型', {
        type: 'select',
        options: approvalTypeOptions,
      }),
      field('approverUserId', '审批人', {
        type: 'select',
        loadOptions: () => queryPageOptions('/api/user', 'username'),
      }),
      field('approvalStatus', '审批状态', {
        type: 'select',
        options: approvalStatusOptions,
      }),
      field('submittedAt', '提交时间', { type: 'datetime' }),
      field('decidedAt', '决策时间', { type: 'datetime' }),
      field('decisionNote', '决策说明', {
        type: 'textarea',
      }),
      field('remark', '备注', {
        type: 'textarea',
        hideInTable: true,
      }),
      field('createTime', '创建时间', {
        type: 'datetime',
        hideInForm: true,
      }),
    ],
  },
  {
    key: 'aicoos-budget-plans',
    path: '/aicoos/budget-plans',
    title: '预算计划',
    description: '配置项目的 Token 预算计划、审批状态与资金占用概览骨架。',
    resourcePath: '/api/aicoos/budgetPlan',
    allowImport: false,
    allowExport: false,
    fields: [
      field('id', 'ID', {
        hideInForm: true,
        hideInSearch: true,
        copyable: true,
        width: 180,
      }),
      field('projectId', '所属项目', {
        type: 'select',
        required: true,
        loadOptions: () => queryPageOptions('/api/aicoos/project', 'projectName'),
      }),
      field('planName', '计划名称', { required: true }),
      field('currencyCode', '币种', { required: true }),
      field('proposedAmount', '提议金额', { type: 'number' }),
      field('approvedAmount', '批准金额', { type: 'number' }),
      field('reservedAmount', '预留金额', { type: 'number' }),
      field('consumedAmount', '已消耗金额', { type: 'number' }),
      field('roleAllocationsJson', '角色预算分配', {
        type: 'textarea',
        hideInTable: true,
      }),
      field('status', '状态', {
        type: 'select',
        options: budgetPlanStatusOptions,
      }),
      field('effectiveAt', '生效时间', { type: 'datetime' }),
      field('remark', '备注', {
        type: 'textarea',
        hideInTable: true,
      }),
      field('createTime', '创建时间', {
        type: 'datetime',
        hideInForm: true,
      }),
    ],
  },
  {
    key: 'aicoos-budget-ledgers',
    path: '/aicoos/budget-ledgers',
    title: '预算流水',
    description: '记录预算预留、消耗、调整与退款等流水骨架。',
    resourcePath: '/api/aicoos/budgetLedger',
    allowImport: false,
    allowExport: false,
    fields: [
      field('id', 'ID', {
        hideInForm: true,
        hideInSearch: true,
        copyable: true,
        width: 180,
      }),
      field('projectId', '所属项目', {
        type: 'select',
        loadOptions: () => queryPageOptions('/api/aicoos/project', 'projectName'),
      }),
      field('budgetPlanId', '预算计划', {
        type: 'select',
        loadOptions: () => queryPageOptions('/api/aicoos/budgetPlan', 'planName'),
      }),
      field('entryType', '流水类型', {
        type: 'select',
        options: budgetLedgerEntryTypeOptions,
      }),
      field('amount', '金额', { type: 'number' }),
      field('balanceAfter', '流水后余额', { type: 'number' }),
      field('referenceType', '关联对象类型'),
      field('referenceId', '关联对象ID'),
      field('occurredAt', '发生时间', { type: 'datetime' }),
      field('description', '描述', {
        type: 'textarea',
      }),
      field('remark', '备注', {
        type: 'textarea',
        hideInTable: true,
      }),
      field('createTime', '创建时间', {
        type: 'datetime',
        hideInForm: true,
      }),
    ],
  },
  {
    key: 'aicoos-agent-roles',
    path: '/aicoos/agent-roles',
    title: 'Agent 角色',
    description: '维护 AICoOS 中的角色分类、能力摘要与审批约束骨架。',
    resourcePath: '/api/aicoos/agentRole',
    allowImport: false,
    allowExport: false,
    fields: [
      field('id', 'ID', {
        hideInForm: true,
        hideInSearch: true,
        copyable: true,
        width: 180,
      }),
      field('roleCode', '角色编码', { required: true }),
      field('roleName', '角色名称', { required: true }),
      field('roleCategory', '角色分类', {
        type: 'select',
        options: agentRoleCategoryOptions,
      }),
      field('status', '状态', {
        type: 'select',
        options: agentRoleStatusOptions,
      }),
      field('capabilitySummary', '能力摘要', {
        type: 'textarea',
      }),
      field('approvalRequired', '需要审批', {
        type: 'select',
        options: yesNoOptions,
      }),
      field('maxConcurrency', '最大并发数', {
        type: 'number',
      }),
      field('remark', '备注', {
        type: 'textarea',
        hideInTable: true,
      }),
      field('createTime', '创建时间', {
        type: 'datetime',
        hideInForm: true,
      }),
    ],
  },
  {
    key: 'aicoos-workflow-templates',
    path: '/aicoos/workflow-templates',
    title: '工作流模板',
    description: '维护 AICoOS 阶段治理模板，为后续流程映射和规则配置预留骨架。',
    resourcePath: '/api/aicoos/workflowTemplate',
    allowImport: false,
    allowExport: false,
    fields: [
      field('id', 'ID', {
        hideInForm: true,
        hideInSearch: true,
        copyable: true,
        width: 180,
      }),
      field('templateCode', '模板编码', { required: true }),
      field('templateName', '模板名称', { required: true }),
      field('versionNo', '版本号', { type: 'number' }),
      field('stageCount', '阶段数量', { type: 'number' }),
      field('status', '状态', {
        type: 'select',
        options: workflowTemplateStatusOptions,
      }),
      field('defaultFlag', '默认模板', {
        type: 'select',
        options: yesNoOptions,
      }),
      field('description', '描述', {
        type: 'textarea',
      }),
      field('remark', '备注', {
        type: 'textarea',
        hideInTable: true,
      }),
      field('createTime', '创建时间', {
        type: 'datetime',
        hideInForm: true,
      }),
    ],
  },
];

export const resourceConfigMap = Object.fromEntries(
  resourceConfigs.map((item) => [item.path, item]),
) as Record<string, ResourceConfig>;

export const getResourceConfigByPath = (pathname: string) =>
  resourceConfigMap[pathname];
