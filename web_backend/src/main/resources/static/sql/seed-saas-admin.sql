-- ===========================================
-- SaaS 后台初始化种子数据
-- 默认租户: default
-- 默认管理员: admin
-- 默认密码: Admin@123456
-- ===========================================

SET NAMES utf8mb4;

SET @tenant_id = 'seed_tenant_default';
SET @depart_root_id = 'seed_depart_root';
SET @role_admin_id = 'seed_role_admin';
SET @role_tenant_admin_id = 'seed_role_tenant_admin';
SET @user_admin_id = 'seed_user_admin';
SET @now = NOW();

-- 1) 清理旧关系，便于重复执行
DELETE FROM sys_role_permission
WHERE role_id IN (@role_admin_id, @role_tenant_admin_id);

DELETE FROM sys_user_role
WHERE user_id = @user_admin_id
   OR role_id IN (@role_admin_id, @role_tenant_admin_id);

DELETE FROM sys_user_depart
WHERE user_id = @user_admin_id
   OR depart_id = @depart_root_id;

DELETE FROM sys_system_config
WHERE id IN (
    'cfg_site_name',
    'cfg_site_subtitle',
    'cfg_brand_logo',
    'cfg_brand_primary_color',
    'cfg_copyright_text',
    'cfg_password_min_length',
    'cfg_password_max_length',
    'cfg_password_require_uppercase',
    'cfg_password_require_lowercase',
    'cfg_password_require_digit',
    'cfg_password_require_special',
    'cfg_jwt_exp_seconds',
    'cfg_jwt_refresh_exp_seconds',
    'cfg_file_max_size_mb',
    'cfg_file_allowed_types',
    'cfg_file_default_biz_type',
    'cfg_file_storage_provider',
    'cfg_notify_email_from_name',
    'cfg_notify_email_from_address',
    'cfg_notify_sms_sign',
    'cfg_notify_webhook_url'
);

DELETE FROM sys_permission
WHERE id IN (
    'perm_menu_dashboard',
    'perm_dir_system',
    'perm_menu_tenant',
    'perm_btn_tenant_view',
    'perm_btn_tenant_create',
    'perm_btn_tenant_update',
    'perm_btn_tenant_delete',
    'perm_menu_user',
    'perm_btn_user_view',
    'perm_btn_user_create',
    'perm_btn_user_update',
    'perm_btn_user_delete',
    'perm_menu_role',
    'perm_btn_role_view',
    'perm_btn_role_create',
    'perm_btn_role_update',
    'perm_btn_role_delete',
    'perm_menu_permission',
    'perm_btn_permission_view',
    'perm_btn_permission_create',
    'perm_btn_permission_update',
    'perm_btn_permission_delete',
    'perm_menu_depart',
    'perm_btn_depart_view',
    'perm_btn_depart_create',
    'perm_btn_depart_update',
    'perm_btn_depart_delete',
    'perm_menu_dict',
    'perm_btn_dict_view',
    'perm_btn_dict_create',
    'perm_btn_dict_update',
    'perm_btn_dict_delete',
    'perm_menu_dict_item',
    'perm_btn_dict_item_view',
    'perm_btn_dict_item_create',
    'perm_btn_dict_item_update',
    'perm_btn_dict_item_delete',
    'perm_menu_announcement',
    'perm_menu_message_center',
    'perm_btn_message_center_view',
    'perm_btn_announcement_view',
    'perm_btn_announcement_create',
    'perm_btn_announcement_update',
    'perm_btn_announcement_delete',
    'perm_btn_announcement_publish',
    'perm_menu_announcement_send',
    'perm_btn_announcement_send_view',
    'perm_menu_file',
    'perm_btn_file_view',
    'perm_btn_file_create',
    'perm_btn_file_update',
    'perm_btn_file_delete',
    'perm_menu_data_rule',
    'perm_btn_data_rule_view',
    'perm_btn_data_rule_create',
    'perm_btn_data_rule_update',
    'perm_btn_data_rule_delete',
    'perm_menu_job',
    'perm_btn_job_view',
    'perm_btn_job_create',
    'perm_btn_job_update',
    'perm_btn_job_delete',
    'perm_btn_job_trigger',
    'perm_menu_job_log',
    'perm_btn_job_log_view',
    'perm_menu_log',
    'perm_btn_log_view',
    'perm_menu_config_center',
    'perm_btn_config_view',
    'perm_btn_config_update',
    'perm_menu_security_center',
    'perm_btn_security_view',
    'perm_menu_data_transfer',
    'perm_btn_data_transfer_view',
    'perm_dir_workflow',
    'perm_menu_workflow_designer',
    'perm_menu_workflow_draft',
    'perm_btn_workflow_draft_view',
    'perm_btn_workflow_draft_save',
    'perm_btn_workflow_draft_delete',
    'perm_menu_workflow_definition',
    'perm_btn_workflow_definition_view',
    'perm_btn_workflow_deployment_create',
    'perm_btn_workflow_deployment_delete',
    'perm_btn_workflow_instance_start',
    'perm_menu_workflow_todo',
    'perm_btn_workflow_task_todo_view',
    'perm_btn_workflow_task_complete',
    'perm_menu_workflow_done',
    'perm_btn_workflow_task_done_view',
    'perm_menu_workflow_instance',
    'perm_btn_workflow_instance_view',
    'perm_btn_workflow_instance_detail'
);

DELETE FROM sys_user WHERE id = @user_admin_id;
DELETE FROM sys_role WHERE id IN (@role_admin_id, @role_tenant_admin_id);
DELETE FROM sys_depart WHERE id = @depart_root_id;
DELETE FROM sys_tenant WHERE id = @tenant_id;

-- 2) 基础租户/组织/角色/管理员
REPLACE INTO sys_tenant (
    id, tenant_name, tenant_code, contact_name, package_name, status, del_flag, remark, create_by, create_time, update_by, update_time
) VALUES (
    @tenant_id, '默认租户', 'default', '系统管理员', '基础版', 1, 0, 'SaaS 后台初始化租户', 'seed', @now, 'seed', @now
);

REPLACE INTO sys_depart (
    id, tenant_id, parent_id, depart_name, org_code, depart_order, org_category, status, del_flag, remark, create_by, create_time, update_by, update_time
) VALUES (
    @depart_root_id, @tenant_id, NULL, '默认组织', 'A01', 1, 1, 1, 0, '初始化根部门', 'seed', @now, 'seed', @now
);

REPLACE INTO sys_role (
    id, tenant_id, role_name, role_code, description, scope_type, built_in, status, del_flag, remark, create_by, create_time, update_by, update_time
) VALUES
(
    @role_admin_id, @tenant_id, '超级管理员', 'ADMIN', '拥有后台全部权限', 'TENANT', 1, 1, 0, '初始化内置角色', 'seed', @now, 'seed', @now
),
(
    @role_tenant_admin_id, @tenant_id, '租户管理员', 'TENANT_ADMIN', '租户级系统管理员', 'TENANT', 1, 1, 0, '初始化内置角色', 'seed', @now, 'seed', @now
);

REPLACE INTO sys_user (
    id, tenant_id, username, realname, password, avatar, email, phone, pwd_update_time, status, del_flag, remark, create_by, create_time, update_by, update_time, sys_org_code
) VALUES (
    @user_admin_id,
    @tenant_id,
    'admin',
    '系统管理员',
    '$2a$10$AT7k8wJdRHqUx5xZIm0DXuhC6x7q5R.I3TGufRHwD4EGP6Jd/6M3m',
    NULL,
    'admin@example.com',
    '13800000000',
    @now,
    1,
    0,
    '初始化管理员',
    'seed',
    @now,
    'seed',
    @now,
    'A01'
);

REPLACE INTO sys_user_role (id, tenant_id, user_id, role_id, create_by, create_time) VALUES
('seed_user_role_admin', @tenant_id, @user_admin_id, @role_admin_id, 'seed', @now);

REPLACE INTO sys_user_depart (id, tenant_id, user_id, depart_id, rel_type, create_by, create_time) VALUES
('seed_user_depart_admin', @tenant_id, @user_admin_id, @depart_root_id, 1, 'seed', @now);

-- 3) 平台级系统配置
REPLACE INTO sys_system_config (
    id, tenant_id, group_code, group_name, group_sort, config_key, config_name, config_value, default_value, value_type, required_flag, placeholder, description, options_json, sort_no, built_in, status, del_flag, create_by, create_time, update_by, update_time
) VALUES
('cfg_site_name', NULL, 'branding', '品牌信息', 10, 'branding.siteName', '站点名称', 'SaaS Admin Template', 'SaaS Admin Template', 'text', 1, '请输入站点名称', '用于登录页标题、后台页头与品牌展示。', NULL, 11, 1, 1, 0, 'seed', @now, 'seed', @now),
('cfg_site_subtitle', NULL, 'branding', '品牌信息', 10, 'branding.siteSubtitle', '站点副标题', '开箱即用的 SaaS 后台管理模板', '开箱即用的 SaaS 后台管理模板', 'textarea', 0, '请输入站点副标题', '用于登录页与平台简介说明。', NULL, 12, 1, 1, 0, 'seed', @now, 'seed', @now),
('cfg_brand_logo', NULL, 'branding', '品牌信息', 10, 'branding.logoUrl', 'Logo 地址', '/logo.svg', '/logo.svg', 'url', 0, '请输入 Logo 地址', '支持填写相对路径或完整 URL。', NULL, 13, 1, 1, 0, 'seed', @now, 'seed', @now),
('cfg_brand_primary_color', NULL, 'branding', '品牌信息', 10, 'branding.primaryColor', '品牌主色', '#1677ff', '#1677ff', 'color', 0, '#1677ff', '用于后续主题与品牌色配置。', NULL, 14, 1, 1, 0, 'seed', @now, 'seed', @now),
('cfg_copyright_text', NULL, 'branding', '品牌信息', 10, 'branding.copyrightText', '版权文案', 'SaaS Admin Template 版权所有', 'SaaS Admin Template 版权所有', 'text', 0, '请输入版权文案', '用于页脚版权展示。', NULL, 15, 1, 1, 0, 'seed', @now, 'seed', @now),
('cfg_password_min_length', NULL, 'security', '安全策略', 20, 'security.password.minLength', '密码最小长度', '8', '8', 'number', 1, '请输入最小长度', '影响注册、重置密码和修改密码的校验。', NULL, 21, 1, 1, 0, 'seed', @now, 'seed', @now),
('cfg_password_max_length', NULL, 'security', '安全策略', 20, 'security.password.maxLength', '密码最大长度', '32', '32', 'number', 1, '请输入最大长度', '影响注册、重置密码和修改密码的校验。', NULL, 22, 1, 1, 0, 'seed', @now, 'seed', @now),
('cfg_password_require_uppercase', NULL, 'security', '安全策略', 20, 'security.password.requireUppercase', '要求大写字母', 'true', 'true', 'boolean', 1, NULL, '密码是否必须包含大写字母。', NULL, 23, 1, 1, 0, 'seed', @now, 'seed', @now),
('cfg_password_require_lowercase', NULL, 'security', '安全策略', 20, 'security.password.requireLowercase', '要求小写字母', 'true', 'true', 'boolean', 1, NULL, '密码是否必须包含小写字母。', NULL, 24, 1, 1, 0, 'seed', @now, 'seed', @now),
('cfg_password_require_digit', NULL, 'security', '安全策略', 20, 'security.password.requireDigit', '要求数字', 'true', 'true', 'boolean', 1, NULL, '密码是否必须包含数字。', NULL, 25, 1, 1, 0, 'seed', @now, 'seed', @now),
('cfg_password_require_special', NULL, 'security', '安全策略', 20, 'security.password.requireSpecial', '要求特殊字符', 'true', 'true', 'boolean', 1, NULL, '密码是否必须包含特殊字符。', NULL, 26, 1, 1, 0, 'seed', @now, 'seed', @now),
('cfg_jwt_exp_seconds', NULL, 'security', '安全策略', 20, 'security.jwt.expSeconds', '访问令牌时长(秒)', '3600', '3600', 'number', 1, '请输入秒数', '用于前端展示当前访问令牌的默认时长。', NULL, 27, 1, 1, 0, 'seed', @now, 'seed', @now),
('cfg_jwt_refresh_exp_seconds', NULL, 'security', '安全策略', 20, 'security.jwt.refreshExpSeconds', '刷新令牌时长(秒)', '1209600', '1209600', 'number', 1, '请输入秒数', '用于前端展示当前刷新令牌的默认时长。', NULL, 28, 1, 1, 0, 'seed', @now, 'seed', @now),
('cfg_file_max_size_mb', NULL, 'file', '文件策略', 30, 'file.maxSizeMb', '文件大小上限(MB)', '20', '20', 'number', 1, '请输入 MB 数', '文件上传会按此限制大小。', NULL, 31, 1, 1, 0, 'seed', @now, 'seed', @now),
('cfg_file_allowed_types', NULL, 'file', '文件策略', 30, 'file.allowedTypes', '允许上传类型', '.png,.jpg,.jpeg,.pdf,.doc,.docx,.xls,.xlsx,.zip', '.png,.jpg,.jpeg,.pdf,.doc,.docx,.xls,.xlsx,.zip', 'textarea', 0, '请输入扩展名，逗号分隔', '用于前端上传提示与后端扩展名校验。', NULL, 32, 1, 1, 0, 'seed', @now, 'seed', @now),
('cfg_file_default_biz_type', NULL, 'file', '文件策略', 30, 'file.defaultBizType', '默认业务类型', 'system', 'system', 'text', 0, '请输入默认业务类型', '上传文件时未指定业务类型的默认值。', NULL, 33, 1, 1, 0, 'seed', @now, 'seed', @now),
('cfg_file_storage_provider', NULL, 'file', '文件策略', 30, 'file.storageProvider', '存储提供方', 'local', 'local', 'text', 0, '请输入存储提供方', '用于展示当前文件中心默认存储提供方。', NULL, 34, 1, 1, 0, 'seed', @now, 'seed', @now),
('cfg_notify_email_from_name', NULL, 'notification', '通知配置', 40, 'notification.email.fromName', '邮件发送者名称', 'SaaS Admin', 'SaaS Admin', 'text', 0, '请输入邮件发送者名称', '用于后续邮件通知模块扩展。', NULL, 41, 1, 1, 0, 'seed', @now, 'seed', @now),
('cfg_notify_email_from_address', NULL, 'notification', '通知配置', 40, 'notification.email.fromAddress', '邮件发送地址', 'no-reply@example.com', 'no-reply@example.com', 'email', 0, '请输入邮件发送地址', '用于后续邮件通知模块扩展。', NULL, 42, 1, 1, 0, 'seed', @now, 'seed', @now),
('cfg_notify_sms_sign', NULL, 'notification', '通知配置', 40, 'notification.sms.sign', '短信签名', 'SaaS后台', 'SaaS后台', 'text', 0, '请输入短信签名', '用于后续短信通知模块扩展。', NULL, 43, 1, 1, 0, 'seed', @now, 'seed', @now),
('cfg_notify_webhook_url', NULL, 'notification', '通知配置', 40, 'notification.webhook.url', 'Webhook 地址', '', '', 'url', 0, '请输入 Webhook 地址', '用于后续系统告警或站外通知扩展。', NULL, 44, 1, 1, 0, 'seed', @now, 'seed', @now);

-- 3) 菜单与按钮权限
INSERT INTO sys_permission (
    id, tenant_id, parent_id, name, url, component, scope_type, built_in, perms, type, icon, sort_no, hidden, always_show, status, del_flag, remark, create_by, create_time, update_by, update_time
) VALUES
('perm_menu_dashboard', @tenant_id, NULL, '工作台', '/dashboard', 'dashboard/index', 'TENANT', 1, 'sys:dashboard:view', 1, 'dashboard', 1, 0, 0, 1, 0, '后台首页', 'seed', @now, 'seed', @now),
('perm_dir_system', @tenant_id, NULL, '系统管理', '/system', 'Layout', 'TENANT', 1, NULL, 0, 'setting', 10, 0, 1, 1, 0, '系统管理目录', 'seed', @now, 'seed', @now),

('perm_menu_tenant', @tenant_id, 'perm_dir_system', '租户管理', '/system/tenants', 'system/tenant/index', 'TENANT', 1, NULL, 1, 'apartment', 11, 0, 0, 1, 0, '租户管理菜单', 'seed', @now, 'seed', @now),
('perm_btn_tenant_view', @tenant_id, 'perm_menu_tenant', '查看租户', '/api/tenant/page', NULL, 'TENANT', 1, 'sys:tenant:view', 2, NULL, 111, 1, 0, 1, 0, '租户查看', 'seed', @now, 'seed', @now),
('perm_btn_tenant_create', @tenant_id, 'perm_menu_tenant', '新增租户', '/api/tenant', NULL, 'TENANT', 1, 'sys:tenant:create', 2, NULL, 112, 1, 0, 1, 0, '租户新增', 'seed', @now, 'seed', @now),
('perm_btn_tenant_update', @tenant_id, 'perm_menu_tenant', '编辑租户', '/api/tenant/{id}', NULL, 'TENANT', 1, 'sys:tenant:update', 2, NULL, 113, 1, 0, 1, 0, '租户编辑', 'seed', @now, 'seed', @now),
('perm_btn_tenant_delete', @tenant_id, 'perm_menu_tenant', '删除租户', '/api/tenant/{id}', NULL, 'TENANT', 1, 'sys:tenant:delete', 2, NULL, 114, 1, 0, 1, 0, '租户删除', 'seed', @now, 'seed', @now),

('perm_menu_user', @tenant_id, 'perm_dir_system', '用户管理', '/system/users', 'system/user/index', 'TENANT', 1, NULL, 1, 'user', 12, 0, 0, 1, 0, '用户管理菜单', 'seed', @now, 'seed', @now),
('perm_btn_user_view', @tenant_id, 'perm_menu_user', '查看用户', '/api/user/page', NULL, 'TENANT', 1, 'sys:user:view', 2, NULL, 121, 1, 0, 1, 0, '用户查看', 'seed', @now, 'seed', @now),
('perm_btn_user_create', @tenant_id, 'perm_menu_user', '新增用户', '/api/user', NULL, 'TENANT', 1, 'sys:user:create', 2, NULL, 122, 1, 0, 1, 0, '用户新增', 'seed', @now, 'seed', @now),
('perm_btn_user_update', @tenant_id, 'perm_menu_user', '编辑用户', '/api/user/{id}', NULL, 'TENANT', 1, 'sys:user:update', 2, NULL, 123, 1, 0, 1, 0, '用户编辑', 'seed', @now, 'seed', @now),
('perm_btn_user_delete', @tenant_id, 'perm_menu_user', '删除用户', '/api/user/{id}', NULL, 'TENANT', 1, 'sys:user:delete', 2, NULL, 124, 1, 0, 1, 0, '用户删除', 'seed', @now, 'seed', @now),

('perm_menu_role', @tenant_id, 'perm_dir_system', '角色管理', '/system/roles', 'system/role/index', 'TENANT', 1, NULL, 1, 'team', 13, 0, 0, 1, 0, '角色管理菜单', 'seed', @now, 'seed', @now),
('perm_btn_role_view', @tenant_id, 'perm_menu_role', '查看角色', '/api/role/page', NULL, 'TENANT', 1, 'sys:role:view', 2, NULL, 131, 1, 0, 1, 0, '角色查看', 'seed', @now, 'seed', @now),
('perm_btn_role_create', @tenant_id, 'perm_menu_role', '新增角色', '/api/role', NULL, 'TENANT', 1, 'sys:role:create', 2, NULL, 132, 1, 0, 1, 0, '角色新增', 'seed', @now, 'seed', @now),
('perm_btn_role_update', @tenant_id, 'perm_menu_role', '编辑角色', '/api/role/{id}', NULL, 'TENANT', 1, 'sys:role:update', 2, NULL, 133, 1, 0, 1, 0, '角色编辑', 'seed', @now, 'seed', @now),
('perm_btn_role_delete', @tenant_id, 'perm_menu_role', '删除角色', '/api/role/{id}', NULL, 'TENANT', 1, 'sys:role:delete', 2, NULL, 134, 1, 0, 1, 0, '角色删除', 'seed', @now, 'seed', @now),

('perm_menu_permission', @tenant_id, 'perm_dir_system', '菜单权限', '/system/permissions', 'system/permission/index', 'TENANT', 1, NULL, 1, 'safety', 14, 0, 0, 1, 0, '菜单权限菜单', 'seed', @now, 'seed', @now),
('perm_btn_permission_view', @tenant_id, 'perm_menu_permission', '查看权限', '/api/permission/page', NULL, 'TENANT', 1, 'sys:permission:view', 2, NULL, 141, 1, 0, 1, 0, '权限查看', 'seed', @now, 'seed', @now),
('perm_btn_permission_create', @tenant_id, 'perm_menu_permission', '新增权限', '/api/permission', NULL, 'TENANT', 1, 'sys:permission:create', 2, NULL, 142, 1, 0, 1, 0, '权限新增', 'seed', @now, 'seed', @now),
('perm_btn_permission_update', @tenant_id, 'perm_menu_permission', '编辑权限', '/api/permission/{id}', NULL, 'TENANT', 1, 'sys:permission:update', 2, NULL, 143, 1, 0, 1, 0, '权限编辑', 'seed', @now, 'seed', @now),
('perm_btn_permission_delete', @tenant_id, 'perm_menu_permission', '删除权限', '/api/permission/{id}', NULL, 'TENANT', 1, 'sys:permission:delete', 2, NULL, 144, 1, 0, 1, 0, '权限删除', 'seed', @now, 'seed', @now),

('perm_menu_depart', @tenant_id, 'perm_dir_system', '部门管理', '/system/departs', 'system/depart/index', 'TENANT', 1, NULL, 1, 'cluster', 15, 0, 0, 1, 0, '部门管理菜单', 'seed', @now, 'seed', @now),
('perm_btn_depart_view', @tenant_id, 'perm_menu_depart', '查看部门', '/api/depart/page', NULL, 'TENANT', 1, 'sys:depart:view', 2, NULL, 151, 1, 0, 1, 0, '部门查看', 'seed', @now, 'seed', @now),
('perm_btn_depart_create', @tenant_id, 'perm_menu_depart', '新增部门', '/api/depart', NULL, 'TENANT', 1, 'sys:depart:create', 2, NULL, 152, 1, 0, 1, 0, '部门新增', 'seed', @now, 'seed', @now),
('perm_btn_depart_update', @tenant_id, 'perm_menu_depart', '编辑部门', '/api/depart/{id}', NULL, 'TENANT', 1, 'sys:depart:update', 2, NULL, 153, 1, 0, 1, 0, '部门编辑', 'seed', @now, 'seed', @now),
('perm_btn_depart_delete', @tenant_id, 'perm_menu_depart', '删除部门', '/api/depart/{id}', NULL, 'TENANT', 1, 'sys:depart:delete', 2, NULL, 154, 1, 0, 1, 0, '部门删除', 'seed', @now, 'seed', @now),

('perm_menu_dict', @tenant_id, 'perm_dir_system', '字典管理', '/system/dicts', 'system/dict/index', 'TENANT', 1, NULL, 1, 'book', 16, 0, 0, 1, 0, '字典管理菜单', 'seed', @now, 'seed', @now),
('perm_btn_dict_view', @tenant_id, 'perm_menu_dict', '查看字典', '/api/dict/page', NULL, 'TENANT', 1, 'sys:dict:view', 2, NULL, 161, 1, 0, 1, 0, '字典查看', 'seed', @now, 'seed', @now),
('perm_btn_dict_create', @tenant_id, 'perm_menu_dict', '新增字典', '/api/dict', NULL, 'TENANT', 1, 'sys:dict:create', 2, NULL, 162, 1, 0, 1, 0, '字典新增', 'seed', @now, 'seed', @now),
('perm_btn_dict_update', @tenant_id, 'perm_menu_dict', '编辑字典', '/api/dict/{id}', NULL, 'TENANT', 1, 'sys:dict:update', 2, NULL, 163, 1, 0, 1, 0, '字典编辑', 'seed', @now, 'seed', @now),
('perm_btn_dict_delete', @tenant_id, 'perm_menu_dict', '删除字典', '/api/dict/{id}', NULL, 'TENANT', 1, 'sys:dict:delete', 2, NULL, 164, 1, 0, 1, 0, '字典删除', 'seed', @now, 'seed', @now),
('perm_menu_dict_item', @tenant_id, 'perm_dir_system', '字典项管理', '/system/dict-items', 'system/dict-item/index', 'TENANT', 1, NULL, 1, 'unordered-list', 17, 0, 0, 1, 0, '字典项管理菜单', 'seed', @now, 'seed', @now),
('perm_btn_dict_item_view', @tenant_id, 'perm_menu_dict_item', '查看字典项', '/api/dictItem/page', NULL, 'TENANT', 1, 'sys:dictItem:view', 2, NULL, 171, 1, 0, 1, 0, '字典项查看', 'seed', @now, 'seed', @now),
('perm_btn_dict_item_create', @tenant_id, 'perm_menu_dict_item', '新增字典项', '/api/dictItem', NULL, 'TENANT', 1, 'sys:dictItem:create', 2, NULL, 172, 1, 0, 1, 0, '字典项新增', 'seed', @now, 'seed', @now),
('perm_btn_dict_item_update', @tenant_id, 'perm_menu_dict_item', '编辑字典项', '/api/dictItem/{id}', NULL, 'TENANT', 1, 'sys:dictItem:update', 2, NULL, 173, 1, 0, 1, 0, '字典项编辑', 'seed', @now, 'seed', @now),
('perm_btn_dict_item_delete', @tenant_id, 'perm_menu_dict_item', '删除字典项', '/api/dictItem/{id}', NULL, 'TENANT', 1, 'sys:dictItem:delete', 2, NULL, 174, 1, 0, 1, 0, '字典项删除', 'seed', @now, 'seed', @now),

('perm_menu_message_center', @tenant_id, 'perm_dir_system', '消息中心', '/system/message-center', 'system/message-center/index', 'TENANT', 1, NULL, 1, 'notification', 18, 0, 0, 1, 0, '消息中心菜单', 'seed', @now, 'seed', @now),
('perm_btn_message_center_view', @tenant_id, 'perm_menu_message_center', '查看消息中心', '/api/system/message-center/summary', NULL, 'TENANT', 1, 'sys:messageCenter:view', 2, NULL, 181, 1, 0, 1, 0, '查看消息中心', 'seed', @now, 'seed', @now),
('perm_menu_announcement', @tenant_id, 'perm_dir_system', '公告消息', '/system/announcements', 'system/announcement/index', 'TENANT', 1, NULL, 1, 'notification', 19, 0, 0, 1, 0, '公告消息菜单', 'seed', @now, 'seed', @now),
('perm_btn_announcement_view', @tenant_id, 'perm_menu_announcement', '查看公告', '/api/announcement/page', NULL, 'TENANT', 1, 'sys:announcement:view', 2, NULL, 191, 1, 0, 1, 0, '公告查看', 'seed', @now, 'seed', @now),
('perm_btn_announcement_create', @tenant_id, 'perm_menu_announcement', '新增公告', '/api/announcement', NULL, 'TENANT', 1, 'sys:announcement:create', 2, NULL, 192, 1, 0, 1, 0, '公告新增', 'seed', @now, 'seed', @now),
('perm_btn_announcement_update', @tenant_id, 'perm_menu_announcement', '编辑公告', '/api/announcement/{id}', NULL, 'TENANT', 1, 'sys:announcement:update', 2, NULL, 193, 1, 0, 1, 0, '公告编辑', 'seed', @now, 'seed', @now),
('perm_btn_announcement_delete', @tenant_id, 'perm_menu_announcement', '删除公告', '/api/announcement/{id}', NULL, 'TENANT', 1, 'sys:announcement:delete', 2, NULL, 194, 1, 0, 1, 0, '公告删除', 'seed', @now, 'seed', @now),
('perm_btn_announcement_publish', @tenant_id, 'perm_menu_announcement', '发布公告', '/api/announcement/{id}/publish', NULL, 'TENANT', 1, 'sys:announcement:publish', 2, NULL, 195, 1, 0, 1, 0, '公告发布', 'seed', @now, 'seed', @now),
('perm_menu_announcement_send', @tenant_id, 'perm_dir_system', '公告送达', '/system/announcement-sends', 'system/announcement-send/index', 'TENANT', 1, NULL, 1, 'mail', 20, 0, 0, 1, 0, '公告送达菜单', 'seed', @now, 'seed', @now),
('perm_btn_announcement_send_view', @tenant_id, 'perm_menu_announcement_send', '查看公告送达', '/api/announcementSend/page', NULL, 'TENANT', 1, 'sys:announcementSend:view', 2, NULL, 191, 1, 0, 1, 0, '公告送达查看', 'seed', @now, 'seed', @now),

('perm_menu_file', @tenant_id, 'perm_dir_system', '文件中心', '/system/files', 'system/file/index', 'TENANT', 1, NULL, 1, 'file', 21, 0, 0, 1, 0, '文件中心菜单', 'seed', @now, 'seed', @now),
('perm_btn_file_view', @tenant_id, 'perm_menu_file', '查看文件', '/api/file/page', NULL, 'TENANT', 1, 'sys:file:view', 2, NULL, 211, 1, 0, 1, 0, '文件查看', 'seed', @now, 'seed', @now),
('perm_btn_file_create', @tenant_id, 'perm_menu_file', '上传文件', '/api/file/upload', NULL, 'TENANT', 1, 'sys:file:create', 2, NULL, 212, 1, 0, 1, 0, '文件上传', 'seed', @now, 'seed', @now),
('perm_btn_file_update', @tenant_id, 'perm_menu_file', '编辑文件', '/api/file/{id}', NULL, 'TENANT', 1, 'sys:file:update', 2, NULL, 213, 1, 0, 1, 0, '文件编辑', 'seed', @now, 'seed', @now),
('perm_btn_file_delete', @tenant_id, 'perm_menu_file', '删除文件', '/api/file/{id}', NULL, 'TENANT', 1, 'sys:file:delete', 2, NULL, 214, 1, 0, 1, 0, '文件删除', 'seed', @now, 'seed', @now),
('perm_menu_data_rule', @tenant_id, 'perm_dir_system', '数据权限', '/system/data-rules', 'system/data-rule/index', 'TENANT', 1, NULL, 1, 'partition', 22, 0, 0, 1, 0, '数据权限菜单', 'seed', @now, 'seed', @now),
('perm_btn_data_rule_view', @tenant_id, 'perm_menu_data_rule', '查看数据权限', '/api/permissionDataRule/page', NULL, 'TENANT', 1, 'sys:dataRule:view', 2, NULL, 211, 1, 0, 1, 0, '数据权限查看', 'seed', @now, 'seed', @now),
('perm_btn_data_rule_create', @tenant_id, 'perm_menu_data_rule', '新增数据权限', '/api/permissionDataRule', NULL, 'TENANT', 1, 'sys:dataRule:create', 2, NULL, 212, 1, 0, 1, 0, '数据权限新增', 'seed', @now, 'seed', @now),
('perm_btn_data_rule_update', @tenant_id, 'perm_menu_data_rule', '编辑数据权限', '/api/permissionDataRule/{id}', NULL, 'TENANT', 1, 'sys:dataRule:update', 2, NULL, 213, 1, 0, 1, 0, '数据权限编辑', 'seed', @now, 'seed', @now),
('perm_btn_data_rule_delete', @tenant_id, 'perm_menu_data_rule', '删除数据权限', '/api/permissionDataRule/{id}', NULL, 'TENANT', 1, 'sys:dataRule:delete', 2, NULL, 214, 1, 0, 1, 0, '数据权限删除', 'seed', @now, 'seed', @now),

('perm_menu_job', @tenant_id, 'perm_dir_system', '定时任务', '/system/jobs', 'system/job/index', 'TENANT', 1, NULL, 1, 'clock-circle', 22, 0, 0, 1, 0, '定时任务菜单', 'seed', @now, 'seed', @now),
('perm_btn_job_view', @tenant_id, 'perm_menu_job', '查看任务', '/api/quartzJob/page', NULL, 'TENANT', 1, 'sys:job:view', 2, NULL, 181, 1, 0, 1, 0, '任务查看', 'seed', @now, 'seed', @now),
('perm_btn_job_create', @tenant_id, 'perm_menu_job', '新增任务', '/api/quartzJob', NULL, 'TENANT', 1, 'sys:job:create', 2, NULL, 182, 1, 0, 1, 0, '任务新增', 'seed', @now, 'seed', @now),
('perm_btn_job_update', @tenant_id, 'perm_menu_job', '编辑任务', '/api/quartzJob/{id}', NULL, 'TENANT', 1, 'sys:job:update', 2, NULL, 183, 1, 0, 1, 0, '任务编辑', 'seed', @now, 'seed', @now),
('perm_btn_job_delete', @tenant_id, 'perm_menu_job', '删除任务', '/api/quartzJob/{id}', NULL, 'TENANT', 1, 'sys:job:delete', 2, NULL, 184, 1, 0, 1, 0, '任务删除', 'seed', @now, 'seed', @now),
('perm_btn_job_trigger', @tenant_id, 'perm_menu_job', '执行任务', '/api/quartzJob/{id}/trigger', NULL, 'TENANT', 1, 'sys:job:trigger', 2, NULL, 185, 1, 0, 1, 0, '任务执行', 'seed', @now, 'seed', @now),
('perm_menu_job_log', @tenant_id, 'perm_dir_system', '任务日志', '/system/job-logs', 'system/job-log/index', 'TENANT', 1, NULL, 1, 'profile', 23, 0, 0, 1, 0, '任务日志菜单', 'seed', @now, 'seed', @now),
('perm_btn_job_log_view', @tenant_id, 'perm_menu_job_log', '查看任务日志', '/api/quartzJobLog/page', NULL, 'TENANT', 1, 'sys:jobLog:view', 2, NULL, 231, 1, 0, 1, 0, '任务日志查看', 'seed', @now, 'seed', @now),
('perm_menu_log', @tenant_id, 'perm_dir_system', '系统日志', '/system/logs', 'system/log/index', 'TENANT', 1, NULL, 1, 'file-search', 24, 0, 0, 1, 0, '系统日志菜单', 'seed', @now, 'seed', @now),
('perm_btn_log_view', @tenant_id, 'perm_menu_log', '查看系统日志', '/api/log/page', NULL, 'TENANT', 1, 'sys:log:view', 2, NULL, 241, 1, 0, 1, 0, '系统日志查看', 'seed', @now, 'seed', @now),
('perm_menu_config_center', @tenant_id, 'perm_dir_system', '系统配置', '/system/config-center', 'system/config-center/index', 'TENANT', 1, NULL, 1, 'control', 25, 0, 0, 1, 0, '系统配置中心', 'seed', @now, 'seed', @now),
('perm_btn_config_view', @tenant_id, 'perm_menu_config_center', '查看系统配置', '/api/systemConfig/groups', NULL, 'TENANT', 1, 'sys:config:view', 2, NULL, 251, 1, 0, 1, 0, '查看系统配置', 'seed', @now, 'seed', @now),
('perm_btn_config_update', @tenant_id, 'perm_menu_config_center', '更新系统配置', '/api/systemConfig/batch', NULL, 'TENANT', 1, 'sys:config:update', 2, NULL, 252, 1, 0, 1, 0, '更新系统配置', 'seed', @now, 'seed', @now),
('perm_menu_security_center', @tenant_id, 'perm_dir_system', '安全中心', '/system/security-center', 'system/security-center/index', 'TENANT', 1, NULL, 1, 'safety-certificate', 26, 0, 0, 1, 0, '用户安全中心', 'seed', @now, 'seed', @now),
('perm_btn_security_view', @tenant_id, 'perm_menu_security_center', '查看安全中心', '/api/user/page', NULL, 'TENANT', 1, 'sys:security:view', 2, NULL, 261, 1, 0, 1, 0, '查看安全中心', 'seed', @now, 'seed', @now),
('perm_menu_data_transfer', @tenant_id, 'perm_dir_system', '数据传输', '/system/data-transfer', 'system/data-transfer/index', 'TENANT', 1, NULL, 1, 'swap', 27, 0, 0, 1, 0, '导入导出中心', 'seed', @now, 'seed', @now),
('perm_btn_data_transfer_view', @tenant_id, 'perm_menu_data_transfer', '查看数据传输', '/api/system/data-transfer', NULL, 'TENANT', 1, 'sys:dataTransfer:view', 2, NULL, 271, 1, 0, 1, 0, '查看数据传输', 'seed', @now, 'seed', @now),

('perm_dir_workflow', @tenant_id, NULL, '流程中心', '/workflow', 'Layout', 'TENANT', 1, NULL, 0, 'cluster', 20, 0, 1, 1, 0, 'Flowable 流程中心目录', 'seed', @now, 'seed', @now),
('perm_menu_workflow_designer', @tenant_id, 'perm_dir_workflow', '流程设计器', '/workflow/designer', 'workflow/designer/index', 'TENANT', 1, NULL, 1, 'deployment-unit', 21, 1, 0, 1, 0, '流程设计器隐藏菜单', 'seed', @now, 'seed', @now),
('perm_menu_workflow_draft', @tenant_id, 'perm_dir_workflow', '流程草稿', '/workflow/drafts', 'workflow/drafts/index', 'TENANT', 1, NULL, 1, 'save', 22, 0, 0, 1, 0, '流程草稿菜单', 'seed', @now, 'seed', @now),
('perm_btn_workflow_draft_view', @tenant_id, 'perm_menu_workflow_draft', '查看流程草稿', '/api/workflow/drafts', NULL, 'TENANT', 1, 'wf:draft:view', 2, NULL, 221, 1, 0, 1, 0, '流程草稿查看', 'seed', @now, 'seed', @now),
('perm_btn_workflow_draft_save', @tenant_id, 'perm_menu_workflow_draft', '保存流程草稿', '/api/workflow/drafts', NULL, 'TENANT', 1, 'wf:draft:save', 2, NULL, 222, 1, 0, 1, 0, '流程草稿保存', 'seed', @now, 'seed', @now),
('perm_btn_workflow_draft_delete', @tenant_id, 'perm_menu_workflow_draft', '删除流程草稿', '/api/workflow/drafts/{draftId}', NULL, 'TENANT', 1, 'wf:draft:delete', 2, NULL, 223, 1, 0, 1, 0, '流程草稿删除', 'seed', @now, 'seed', @now),

('perm_menu_workflow_definition', @tenant_id, 'perm_dir_workflow', '流程定义', '/workflow/definitions', 'workflow/definitions/index', 'TENANT', 1, NULL, 1, 'appstore', 23, 0, 0, 1, 0, '流程定义菜单', 'seed', @now, 'seed', @now),
('perm_btn_workflow_definition_view', @tenant_id, 'perm_menu_workflow_definition', '查看流程定义', '/api/workflow/definitions', NULL, 'TENANT', 1, 'wf:definition:view', 2, NULL, 231, 1, 0, 1, 0, '流程定义查看', 'seed', @now, 'seed', @now),
('perm_btn_workflow_deployment_create', @tenant_id, 'perm_menu_workflow_definition', '上传部署', '/api/workflow/deployments', NULL, 'TENANT', 1, 'wf:deployment:create', 2, NULL, 232, 1, 0, 1, 0, '流程部署上传', 'seed', @now, 'seed', @now),
('perm_btn_workflow_deployment_delete', @tenant_id, 'perm_menu_workflow_definition', '删除部署', '/api/workflow/deployments/{deploymentId}', NULL, 'TENANT', 1, 'wf:deployment:delete', 2, NULL, 233, 1, 0, 1, 0, '流程部署删除', 'seed', @now, 'seed', @now),
('perm_btn_workflow_instance_start', @tenant_id, 'perm_menu_workflow_definition', '发起流程', '/api/workflow/instances', NULL, 'TENANT', 1, 'wf:instance:start', 2, NULL, 234, 1, 0, 1, 0, '流程发起', 'seed', @now, 'seed', @now),

('perm_menu_workflow_todo', @tenant_id, 'perm_dir_workflow', '我的待办', '/workflow/todo', 'workflow/todo/index', 'TENANT', 1, NULL, 1, 'clock-circle', 24, 0, 0, 1, 0, '流程待办菜单', 'seed', @now, 'seed', @now),
('perm_btn_workflow_task_todo_view', @tenant_id, 'perm_menu_workflow_todo', '查看待办', '/api/workflow/tasks/todo', NULL, 'TENANT', 1, 'wf:task:todo:view', 2, NULL, 231, 1, 0, 1, 0, '流程待办查看', 'seed', @now, 'seed', @now),
('perm_btn_workflow_task_complete', @tenant_id, 'perm_menu_workflow_todo', '办理任务', '/api/workflow/tasks/{taskId}/complete', NULL, 'TENANT', 1, 'wf:task:complete', 2, NULL, 232, 1, 0, 1, 0, '流程任务办理', 'seed', @now, 'seed', @now),

('perm_menu_workflow_done', @tenant_id, 'perm_dir_workflow', '我的已办', '/workflow/done', 'workflow/done/index', 'TENANT', 1, NULL, 1, 'safety', 25, 0, 0, 1, 0, '流程已办菜单', 'seed', @now, 'seed', @now),
('perm_btn_workflow_task_done_view', @tenant_id, 'perm_menu_workflow_done', '查看已办', '/api/workflow/tasks/done', NULL, 'TENANT', 1, 'wf:task:done:view', 2, NULL, 241, 1, 0, 1, 0, '流程已办查看', 'seed', @now, 'seed', @now),

('perm_menu_workflow_instance', @tenant_id, 'perm_dir_workflow', '我发起的流程', '/workflow/instances', 'workflow/instances/index', 'TENANT', 1, NULL, 1, 'file', 26, 0, 0, 1, 0, '我发起的流程菜单', 'seed', @now, 'seed', @now),
('perm_btn_workflow_instance_view', @tenant_id, 'perm_menu_workflow_instance', '查看我发起的流程', '/api/workflow/instances/my-started', NULL, 'TENANT', 1, 'wf:instance:view', 2, NULL, 251, 1, 0, 1, 0, '我发起的流程查看', 'seed', @now, 'seed', @now),
('perm_btn_workflow_instance_detail', @tenant_id, 'perm_menu_workflow_instance', '查看流程详情', '/api/workflow/instances/{processInstanceId}', NULL, 'TENANT', 1, 'wf:instance:detail', 2, NULL, 252, 1, 0, 1, 0, '流程实例详情查看', 'seed', @now, 'seed', @now);

-- 4) 角色授权
INSERT INTO sys_role_permission (id, tenant_id, role_id, permission_id, create_by, create_time)
SELECT REPLACE(UUID(), '-', ''), @tenant_id, @role_admin_id, id, 'seed', @now
FROM sys_permission
WHERE tenant_id = @tenant_id;

INSERT INTO sys_role_permission (id, tenant_id, role_id, permission_id, create_by, create_time)
SELECT REPLACE(UUID(), '-', ''), @tenant_id, @role_tenant_admin_id, id, 'seed', @now
FROM sys_permission
WHERE tenant_id = @tenant_id
  AND id NOT IN (
    'perm_menu_tenant',
    'perm_btn_tenant_view',
    'perm_btn_tenant_create',
    'perm_btn_tenant_update',
    'perm_btn_tenant_delete'
  );
