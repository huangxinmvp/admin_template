# 前后端页面接口映射

## 当前结构判断

- 前端原始代码来自 `Ant Design Pro` 示例工程，包含大量 demo 页面与 mock 服务。
- 后端是独立的 `Spring Boot + Spring Security + MyBatis Plus` 业务后台，接口集中在 `/api/**`。
- 本次改造已将前端主路由切换为真实后台信息架构，并统一接入真实请求、认证态与 CRUD 页面能力。

## 核心认证链路

- 登录页 `/user/login` -> `POST /api/auth/login`
- 注册页 `/user/register` -> `POST /api/auth/register`
- 请求层自动续期 -> `POST /api/auth/refresh`
- 品牌配置驱动登录页/页头/页脚 -> `GET /api/systemConfig/branding`
- 当前用户资料 -> `GET /api/system/profile`
- 当前会话列表 `/account/sessions` -> `GET /api/auth/sessions`
- 当前会话退出登录 -> `POST /api/auth/logout`
- 手动下线指定会话 -> `DELETE /api/auth/sessions/{sessionId}`
- 当前用户修改密码 -> `PUT /api/auth/password`
- 未读公告数 -> `GET /api/announcement/unreadCount`
- 公告中心 `/account/inbox` -> `GET /api/announcement/inbox/page`
- 公告详情/已读 -> `GET /api/announcement/{id}`、`POST /api/announcement/{id}/read`

## 仪表盘

- `/dashboard/analysis` -> `GET /api/system/dashboard`

## 系统管理

- `/system/users` -> `/api/user`
- `/system/roles` -> `/api/role`
- `/system/permissions` -> `/api/permission`
- `/system/permissions` 导入/导出 -> `GET /api/permission/exportXls`、`POST /api/permission/importExcel`
- `/system/permissions` 排序保存/整树保存 -> `POST /api/permission/sort`、`POST /api/permission/tree/save`
- `/system/departs` -> `/api/depart`
- `/system/tenants` -> `/api/tenant`
- `/system/dicts` -> `/api/dict`
- `/system/dict-items` -> `/api/dictItem`
- `/system/message-center` -> `GET /api/system/message-center/summary`
- `/system/announcements` -> `/api/announcement`
- `/system/announcement-sends` -> `/api/announcementSend`
- `/system/files` -> `/api/file`
- `/system/files` 文件中心统计 -> `GET /api/file/stats`
- `/system/files` 批量上传 -> `POST /api/file/upload`
- `/system/files` 预览/下载 -> `GET /api/file/preview/{id}`、`GET /api/file/download/{id}`
- `/system/config-center` -> `GET /api/systemConfig/groups`、`PUT /api/systemConfig/batch`
- `/system/security-center` -> `GET /api/user/page`、`GET /api/auth/sessions/user-counts`、`PUT /api/user/{id}/password`、`PUT /api/user/{id}/lock`、`PUT /api/user/{id}/unlock`、`POST /api/auth/sessions/users/{userId}/force-logout`
- `/system/data-transfer` -> 通用资源导入导出中心，复用各资源 `exportXls / importExcel`
- `/system/data-rules` -> `/api/permissionDataRule`
- `/system/jobs` -> `/api/quartzJob`
- `/system/job-logs` -> `/api/quartzJobLog`
- `/system/logs` -> `/api/log`

## 流程中心

- `/workflow/designer` 纯设计工作台 -> `GET /api/workflow/definitions/{definitionId}/xml`
- `/workflow/designer` 打开草稿 -> `GET /api/workflow/drafts/{draftId}`
- `/workflow/designer` 保存草稿 -> `POST /api/workflow/drafts`
- `/workflow/designer` 上传部署 -> `POST /api/workflow/deployments`
- `/workflow/drafts` 草稿箱列表 -> `GET /api/workflow/drafts`
- `/workflow/drafts` 导入草稿 JSON -> `POST /api/workflow/drafts`
- `/workflow/drafts` 删除草稿 -> `DELETE /api/workflow/drafts/{draftId}`
- `/workflow/drafts` 继续编辑 -> `/workflow/designer?draftId={draftId}`
- `/workflow/drafts` 导出草稿 JSON -> 复用已加载草稿数据在前端直接下载
- `/workflow/definitions` 已部署定义列表 -> `GET /api/workflow/definitions`
- `/workflow/definitions` 导入 BPMN 部署 -> `POST /api/workflow/deployments`
- `/workflow/definitions` 导出 BPMN XML -> `GET /api/workflow/definitions/{definitionId}/xml`
- `/workflow/definitions` 删除部署 -> `DELETE /api/workflow/deployments/{deploymentId}`
- `/workflow/definitions` 设计器打开 -> `/workflow/designer?definitionId={definitionId}`
- `/workflow/definitions` 发起流程 -> `POST /api/workflow/instances`
- `/workflow/todo` -> `GET /api/workflow/tasks/todo`
- `/workflow/todo` 办理任务 -> `POST /api/workflow/tasks/{taskId}/complete`
- `/workflow/done` -> `GET /api/workflow/tasks/done`
- `/workflow/instances` -> `GET /api/workflow/instances/my-started`
- 三个流程业务页统一详情抽屉 -> `GET /api/workflow/instances/{processInstanceId}`

## 特殊补充接口

- 用户编辑时额外加载聚合详情 -> `GET /api/user/{id}/aggregate`
- 角色编辑时额外加载权限 ID -> `GET /api/role/{id}/permissionIds`
- 角色保存权限树 -> `PUT /api/role/{id}/permissionIds`
- 公告发布 -> `POST /api/announcement/{id}/publish`
- 公告撤销 -> `POST /api/announcement/{id}/revoke`
- 定时任务立即执行 -> `POST /api/quartzJob/{id}/trigger`
- 表单选项数据：
  - `GET /api/role/options`
  - `GET /api/depart/options`
  - `GET /api/depart/tree`
  - `GET /api/permission/tree`

## 当前实现方式

- 统一请求层：`src/requestErrorConfig.ts`
- 认证与用户态：`src/services/backend/auth.ts`
- 系统公告与门户请求：`src/services/backend/system.ts`
- 通用资源 CRUD：`src/features/backend/CrudPage.tsx`
- 页面资源配置：`src/features/backend/resourceMeta.ts`
- 通用资源页入口：`src/pages/resource/index.tsx`
- 会话管理页面：`src/pages/account/sessions/index.tsx`
- 系统配置中心：`src/pages/system/config-center/index.tsx`
- 消息中心：`src/pages/system/message-center/index.tsx`
- 用户安全中心：`src/pages/system/security-center/index.tsx`
- 数据传输中心：`src/pages/system/data-transfer/index.tsx`
- 公告中心页面：`src/pages/account/inbox/index.tsx`
- Flowable 请求层：`src/services/backend/workflow.ts`
- Flowable 共享组件：`src/features/workflow/*`
- Flowable 页面入口：`src/pages/workflow/*`
- 流程设计器参考了仓库内 `camunda-modeler` 的工作区形态，但前端实现改成了适配 Flowable 的浏览器版 BPMN 编辑器，避免把桌面版 Electron 依赖直接带入后台项目
- 流程设计草稿会落到后端 `wf_model_draft` 表，并通过真实账号隔离展示
- 流程中心已拆成 3 张独立页面：设计器、草稿箱、定义库，避免一个页面塞入过多管理模块

## 当前模板注意点

- 各枚举字段的业务含义目前以常见后台语义接入，最终显示文案可按实际业务字典继续细化。
- 前端菜单已经接入 `/api/system/menus`，当前采用“后端菜单优先、静态路由兜底”的渲染方式；后续如需完全动态路由化，还可以继续把页面权限控制进一步下沉到菜单数据。
- 品牌配置已实时驱动登录页、页头与页脚；在系统配置中心更新品牌相关字段后，前端用户态刷新后即可生效。
- 会话管理已经启用基于 `sid` 的令牌校验；密码重置、锁定用户、手动强制下线后，旧 token 会立即返回 `SESSION_REVOKED`。
- 文件中心当前上传入口走“直接选择文件后上传”，如果后续要支持文件夹、分片或业务分类表单，可再扩展成独立上传弹窗。
- 文件中心已升级成独立页面，支持统计卡片、上传策略面板、业务/存储分布、批量上传和元数据导入导出。
- 消息中心已升级成独立页面，统一展示公告投放、送达阅读率、通知渠道配置和快捷入口。
- 权限菜单、流程草稿、流程定义这类独立页已经补上专属导入导出入口，不再只能依赖数据传输中心。

## 尚未对接或仅部分对接

- 认证相关：
  - `GET /api/auth/me` 当前未使用，前端统一走 `GET /api/system/profile`。
- 关系维护表：
  - `/api/userRole`、`/api/userDepart`、`/api/rolePermission` 没有独立 CRUD 页面
  - 其中用户角色/部门、角色权限已经分别在用户页和角色页里通过聚合接口方式维护
- 导入导出：
  - 通用 CRUD 资源页以及权限菜单、流程定义、流程草稿都已补入口
  - 仍未覆盖的主要是流程待办、流程已办、流程实例这类业务页
