/**
 * @name umi 的路由配置
 * @description 当前仅保留 SaaS 后台管理模板所需路由
 */
export default [
  {
    path: '/user',
    layout: false,
    routes: [
      {
        path: '/user/login',
        layout: false,
        name: '登录',
        component: './user/login',
      },
      {
        path: '/user/register',
        layout: false,
        name: '注册',
        component: './user/register',
      },
      {
        path: '/user/register-result',
        layout: false,
        name: '注册结果',
        component: './user/register-result',
      },
      {
        path: '/user',
        redirect: '/user/login',
      },
      {
        component: '404',
        path: '/user/*',
      },
    ],
  },
  {
    path: '/',
    redirect: '/dashboard/analysis',
  },
  {
    path: '/dashboard',
    name: '工作台',
    icon: 'dashboard',
    routes: [
      {
        path: '/dashboard',
        redirect: '/dashboard/analysis',
      },
      {
        name: '数据概览',
        path: '/dashboard/analysis',
        component: './dashboard/analysis',
      },
    ],
  },
  {
    path: '/system',
    name: '系统管理',
    icon: 'setting',
    routes: [
      {
        path: '/system',
        redirect: '/system/users',
      },
      {
        name: '用户管理',
        path: '/system/users',
        component: './resource',
      },
      {
        name: '角色管理',
        path: '/system/roles',
        component: './resource',
      },
      {
        name: '权限菜单',
        path: '/system/permissions',
        component: './system/permissions',
      },
      {
        name: '部门管理',
        path: '/system/departs',
        component: './resource',
      },
      {
        name: '租户管理',
        path: '/system/tenants',
        component: './resource',
      },
      {
        name: '字典管理',
        path: '/system/dicts',
        component: './resource',
      },
      {
        name: '字典项管理',
        path: '/system/dict-items',
        component: './resource',
      },
      {
        name: '消息中心',
        path: '/system/message-center',
        component: './system/message-center',
      },
      {
        name: '公告管理',
        path: '/system/announcements',
        component: './resource',
      },
      {
        name: '公告送达',
        path: '/system/announcement-sends',
        component: './resource',
      },
      {
        name: '文件管理',
        path: '/system/files',
        component: './system/files',
      },
      {
        name: '系统配置',
        path: '/system/config-center',
        component: './system/config-center',
      },
      {
        name: '安全中心',
        path: '/system/security-center',
        component: './system/security-center',
      },
      {
        name: '数据传输',
        path: '/system/data-transfer',
        component: './system/data-transfer',
      },
      {
        name: '数据权限',
        path: '/system/data-rules',
        component: './resource',
      },
      {
        name: '定时任务',
        path: '/system/jobs',
        component: './resource',
      },
      {
        name: '任务日志',
        path: '/system/job-logs',
        component: './resource',
      },
      {
        name: '系统日志',
        path: '/system/logs',
        component: './resource',
      },
    ],
  },
  {
    path: '/workflow',
    name: '流程中心',
    icon: 'cluster',
    routes: [
      {
        path: '/workflow',
        redirect: '/workflow/drafts',
      },
      {
        path: '/workflow/designer',
        component: './workflow/designer',
        hideInMenu: true,
      },
      {
        name: '流程草稿',
        path: '/workflow/drafts',
        component: './workflow/drafts',
      },
      {
        name: '流程定义',
        path: '/workflow/definitions',
        component: './workflow/definitions',
      },
      {
        name: '我的待办',
        path: '/workflow/todo',
        component: './workflow/todo',
      },
      {
        name: '我的已办',
        path: '/workflow/done',
        component: './workflow/done',
      },
      {
        name: '我发起的流程',
        path: '/workflow/instances',
        component: './workflow/instances',
      },
    ],
  },
  {
    path: '/aicoos',
    name: 'AICoOS',
    icon: 'appstore',
    routes: [
      {
        path: '/aicoos',
        redirect: '/aicoos/projects',
      },
      {
        name: '项目中心',
        path: '/aicoos/projects',
        component: './aicoos/projects',
      },
      {
        name: '需求接收',
        path: '/aicoos/requirement-intake',
        component: './aicoos/requirement-intake',
      },
      {
        name: '澄清中心',
        path: '/aicoos/clarification-center',
        component: './aicoos/clarification-center',
      },
      {
        name: '项目阶段',
        path: '/aicoos/project-stages',
        component: './resource',
      },
      {
        name: '决策中心',
        path: '/aicoos/decision-center',
        component: './aicoos/decision-center',
      },
      {
        path: '/aicoos/decision-items',
        redirect: '/aicoos/decision-center',
        hideInMenu: true,
      },
      {
        name: '审批中心',
        path: '/aicoos/approval-center',
        component: './aicoos/approval-center',
      },
      {
        path: '/aicoos/approval-records',
        redirect: '/aicoos/approval-center',
        hideInMenu: true,
      },
      {
        name: '预算中心',
        path: '/aicoos/budget-center',
        component: './aicoos/budget-center',
      },
      {
        name: '预算计划',
        path: '/aicoos/budget-plans',
        component: './resource',
        hideInMenu: true,
      },
      {
        name: '预算流水',
        path: '/aicoos/budget-ledgers',
        component: './resource',
        hideInMenu: true,
      },
      {
        name: 'Agent 角色',
        path: '/aicoos/agent-roles',
        component: './aicoos/agent-role-management',
      },
      {
        name: '工作流模板',
        path: '/aicoos/workflow-templates',
        component: './aicoos/workflow-template-center',
      },
    ],
  },
  {
    path: '/account',
    name: '我的账户',
    icon: 'user',
    routes: [
      {
        path: '/account',
        redirect: '/account/center',
      },
      {
        name: '个人中心',
        path: '/account/center',
        component: './account/center',
      },
      {
        name: '安全设置',
        path: '/account/settings',
        component: './account/settings',
      },
      {
        name: '公告中心',
        path: '/account/inbox',
        component: './account/inbox',
      },
      {
        name: '会话管理',
        path: '/account/sessions',
        component: './account/sessions',
      },
    ],
  },
  {
    path: '*',
    layout: false,
    component: './404',
  },
];
