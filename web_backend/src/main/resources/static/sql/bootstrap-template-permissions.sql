SET @tenant_id = (
    SELECT `id`
    FROM `sys_tenant`
    WHERE `tenant_code` = 'default' AND `del_flag` = 0
    ORDER BY `create_time`
    LIMIT 1
);
SET @role_admin_id = (
    SELECT `id`
    FROM `sys_role`
    WHERE `tenant_id` = @tenant_id AND `role_code` = 'ADMIN' AND `del_flag` = 0
    ORDER BY `create_time`
    LIMIT 1
);
SET @role_tenant_admin_id = (
    SELECT `id`
    FROM `sys_role`
    WHERE `tenant_id` = @tenant_id AND `role_code` = 'TENANT_ADMIN' AND `del_flag` = 0
    ORDER BY `create_time`
    LIMIT 1
);
SET @now = NOW();

INSERT INTO `sys_permission` (
    `id`, `tenant_id`, `parent_id`, `name`, `url`, `component`, `scope_type`, `built_in`,
    `perms`, `type`, `icon`, `sort_no`, `hidden`, `always_show`, `status`, `del_flag`,
    `remark`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT
    seed.`id`, @tenant_id, seed.`parent_id`, seed.`name`, seed.`url`, seed.`component`,
    'TENANT', 1, seed.`perms`, seed.`type`, seed.`icon`, seed.`sort_no`, seed.`hidden`,
    seed.`always_show`, 1, 0, seed.`remark`, 'bootstrap', @now, 'bootstrap', @now
FROM (
    SELECT 'perm_menu_dashboard' AS `id`, NULL AS `parent_id`, '工作台' AS `name`, '/dashboard' AS `url`, 'dashboard/index' AS `component`, 'sys:dashboard:view' AS `perms`, 1 AS `type`, 'dashboard' AS `icon`, 1 AS `sort_no`, 0 AS `hidden`, 0 AS `always_show`, '后台首页' AS `remark`
    UNION ALL SELECT 'perm_dir_system', NULL, '系统管理', '/system', 'Layout', NULL, 0, 'setting', 10, 0, 1, '系统管理目录'
    UNION ALL SELECT 'perm_menu_tenant', 'perm_dir_system', '租户管理', '/system/tenants', 'system/tenant/index', NULL, 1, 'apartment', 11, 0, 0, '租户管理菜单'
    UNION ALL SELECT 'perm_btn_tenant_view', 'perm_menu_tenant', '查看租户', '/api/tenant/page', NULL, 'sys:tenant:view', 2, NULL, 111, 1, 0, '租户查看'
    UNION ALL SELECT 'perm_btn_tenant_create', 'perm_menu_tenant', '新增租户', '/api/tenant', NULL, 'sys:tenant:create', 2, NULL, 112, 1, 0, '租户新增'
    UNION ALL SELECT 'perm_btn_tenant_update', 'perm_menu_tenant', '编辑租户', '/api/tenant/{id}', NULL, 'sys:tenant:update', 2, NULL, 113, 1, 0, '租户编辑'
    UNION ALL SELECT 'perm_btn_tenant_delete', 'perm_menu_tenant', '删除租户', '/api/tenant/{id}', NULL, 'sys:tenant:delete', 2, NULL, 114, 1, 0, '租户删除'
    UNION ALL SELECT 'perm_menu_user', 'perm_dir_system', '用户管理', '/system/users', 'system/user/index', NULL, 1, 'user', 12, 0, 0, '用户管理菜单'
    UNION ALL SELECT 'perm_btn_user_view', 'perm_menu_user', '查看用户', '/api/user/page', NULL, 'sys:user:view', 2, NULL, 121, 1, 0, '用户查看'
    UNION ALL SELECT 'perm_btn_user_create', 'perm_menu_user', '新增用户', '/api/user', NULL, 'sys:user:create', 2, NULL, 122, 1, 0, '用户新增'
    UNION ALL SELECT 'perm_btn_user_update', 'perm_menu_user', '编辑用户', '/api/user/{id}', NULL, 'sys:user:update', 2, NULL, 123, 1, 0, '用户编辑'
    UNION ALL SELECT 'perm_btn_user_delete', 'perm_menu_user', '删除用户', '/api/user/{id}', NULL, 'sys:user:delete', 2, NULL, 124, 1, 0, '用户删除'
    UNION ALL SELECT 'perm_menu_role', 'perm_dir_system', '角色管理', '/system/roles', 'system/role/index', NULL, 1, 'team', 13, 0, 0, '角色管理菜单'
    UNION ALL SELECT 'perm_btn_role_view', 'perm_menu_role', '查看角色', '/api/role/page', NULL, 'sys:role:view', 2, NULL, 131, 1, 0, '角色查看'
    UNION ALL SELECT 'perm_btn_role_create', 'perm_menu_role', '新增角色', '/api/role', NULL, 'sys:role:create', 2, NULL, 132, 1, 0, '角色新增'
    UNION ALL SELECT 'perm_btn_role_update', 'perm_menu_role', '编辑角色', '/api/role/{id}', NULL, 'sys:role:update', 2, NULL, 133, 1, 0, '角色编辑'
    UNION ALL SELECT 'perm_btn_role_delete', 'perm_menu_role', '删除角色', '/api/role/{id}', NULL, 'sys:role:delete', 2, NULL, 134, 1, 0, '角色删除'
    UNION ALL SELECT 'perm_menu_permission', 'perm_dir_system', '菜单权限', '/system/permissions', 'system/permission/index', NULL, 1, 'safety', 14, 0, 0, '菜单权限菜单'
    UNION ALL SELECT 'perm_btn_permission_view', 'perm_menu_permission', '查看权限', '/api/permission/page', NULL, 'sys:permission:view', 2, NULL, 141, 1, 0, '权限查看'
    UNION ALL SELECT 'perm_btn_permission_create', 'perm_menu_permission', '新增权限', '/api/permission', NULL, 'sys:permission:create', 2, NULL, 142, 1, 0, '权限新增'
    UNION ALL SELECT 'perm_btn_permission_update', 'perm_menu_permission', '编辑权限', '/api/permission/{id}', NULL, 'sys:permission:update', 2, NULL, 143, 1, 0, '权限编辑'
    UNION ALL SELECT 'perm_btn_permission_delete', 'perm_menu_permission', '删除权限', '/api/permission/{id}', NULL, 'sys:permission:delete', 2, NULL, 144, 1, 0, '权限删除'
    UNION ALL SELECT 'perm_menu_depart', 'perm_dir_system', '部门管理', '/system/departs', 'system/depart/index', NULL, 1, 'cluster', 15, 0, 0, '部门管理菜单'
    UNION ALL SELECT 'perm_btn_depart_view', 'perm_menu_depart', '查看部门', '/api/depart/page', NULL, 'sys:depart:view', 2, NULL, 151, 1, 0, '部门查看'
    UNION ALL SELECT 'perm_btn_depart_create', 'perm_menu_depart', '新增部门', '/api/depart', NULL, 'sys:depart:create', 2, NULL, 152, 1, 0, '部门新增'
    UNION ALL SELECT 'perm_btn_depart_update', 'perm_menu_depart', '编辑部门', '/api/depart/{id}', NULL, 'sys:depart:update', 2, NULL, 153, 1, 0, '部门编辑'
    UNION ALL SELECT 'perm_btn_depart_delete', 'perm_menu_depart', '删除部门', '/api/depart/{id}', NULL, 'sys:depart:delete', 2, NULL, 154, 1, 0, '部门删除'
    UNION ALL SELECT 'perm_menu_dict', 'perm_dir_system', '数据字典', '/system/dicts', 'system/dict/index', NULL, 1, 'book', 16, 0, 0, '数据字典菜单'
    UNION ALL SELECT 'perm_btn_dict_view', 'perm_menu_dict', '查看字典', '/api/dict/page', NULL, 'sys:dict:view', 2, NULL, 161, 1, 0, '数据字典查看'
    UNION ALL SELECT 'perm_btn_dict_create', 'perm_menu_dict', '新增字典', '/api/dict', NULL, 'sys:dict:create', 2, NULL, 162, 1, 0, '数据字典新增'
    UNION ALL SELECT 'perm_btn_dict_update', 'perm_menu_dict', '编辑字典', '/api/dict/{id}', NULL, 'sys:dict:update', 2, NULL, 163, 1, 0, '数据字典编辑'
    UNION ALL SELECT 'perm_btn_dict_delete', 'perm_menu_dict', '删除字典', '/api/dict/{id}', NULL, 'sys:dict:delete', 2, NULL, 164, 1, 0, '数据字典删除'
    UNION ALL SELECT 'perm_menu_dict_item', 'perm_dir_system', '字典项管理', '/system/dict-items', 'system/dict-item/index', NULL, 1, 'unordered-list', 17, 0, 0, '字典项管理菜单'
    UNION ALL SELECT 'perm_btn_dict_item_view', 'perm_menu_dict_item', '查看字典项', '/api/dictItem/page', NULL, 'sys:dictItem:view', 2, NULL, 171, 1, 0, '字典项查看'
    UNION ALL SELECT 'perm_btn_dict_item_create', 'perm_menu_dict_item', '新增字典项', '/api/dictItem', NULL, 'sys:dictItem:create', 2, NULL, 172, 1, 0, '字典项新增'
    UNION ALL SELECT 'perm_btn_dict_item_update', 'perm_menu_dict_item', '编辑字典项', '/api/dictItem/{id}', NULL, 'sys:dictItem:update', 2, NULL, 173, 1, 0, '字典项编辑'
    UNION ALL SELECT 'perm_btn_dict_item_delete', 'perm_menu_dict_item', '删除字典项', '/api/dictItem/{id}', NULL, 'sys:dictItem:delete', 2, NULL, 174, 1, 0, '字典项删除'
    UNION ALL SELECT 'perm_menu_message_center', 'perm_dir_system', '消息中心', '/system/message-center', 'system/message-center/index', NULL, 1, 'notification', 18, 0, 0, '消息中心菜单'
    UNION ALL SELECT 'perm_btn_message_center_view', 'perm_menu_message_center', '查看消息中心', '/api/system/message-center/summary', NULL, 'sys:messageCenter:view', 2, NULL, 181, 1, 0, '查看消息中心'
    UNION ALL SELECT 'perm_menu_announcement', 'perm_dir_system', '公告管理', '/system/announcements', 'system/announcement/index', NULL, 1, 'notification', 19, 0, 0, '公告管理菜单'
    UNION ALL SELECT 'perm_btn_announcement_view', 'perm_menu_announcement', '查看公告', '/api/announcement/page', NULL, 'sys:announcement:view', 2, NULL, 191, 1, 0, '公告查看'
    UNION ALL SELECT 'perm_btn_announcement_create', 'perm_menu_announcement', '新增公告', '/api/announcement', NULL, 'sys:announcement:create', 2, NULL, 192, 1, 0, '公告新增'
    UNION ALL SELECT 'perm_btn_announcement_update', 'perm_menu_announcement', '编辑公告', '/api/announcement/{id}', NULL, 'sys:announcement:update', 2, NULL, 193, 1, 0, '公告编辑'
    UNION ALL SELECT 'perm_btn_announcement_delete', 'perm_menu_announcement', '删除公告', '/api/announcement/{id}', NULL, 'sys:announcement:delete', 2, NULL, 194, 1, 0, '公告删除'
    UNION ALL SELECT 'perm_btn_announcement_publish', 'perm_menu_announcement', '发布/撤销公告', '/api/announcement/{id}/publish', NULL, 'sys:announcement:publish', 2, NULL, 195, 1, 0, '公告发布/撤销'
    UNION ALL SELECT 'perm_menu_announcement_send', 'perm_dir_system', '公告送达', '/system/announcement-sends', 'system/announcement-send/index', NULL, 1, 'mail', 20, 0, 0, '公告送达菜单'
    UNION ALL SELECT 'perm_btn_announcement_send_view', 'perm_menu_announcement_send', '查看送达记录', '/api/announcementSend/page', NULL, 'sys:announcementSend:view', 2, NULL, 201, 1, 0, '公告送达查看'
    UNION ALL SELECT 'perm_menu_file', 'perm_dir_system', '文件中心', '/system/files', 'system/file/index', NULL, 1, 'folder-open', 21, 0, 0, '文件中心菜单'
    UNION ALL SELECT 'perm_btn_file_view', 'perm_menu_file', '查看文件', '/api/file/page', NULL, 'sys:file:view', 2, NULL, 211, 1, 0, '文件查看'
    UNION ALL SELECT 'perm_btn_file_create', 'perm_menu_file', '上传文件', '/api/file/upload', NULL, 'sys:file:create', 2, NULL, 212, 1, 0, '文件上传'
    UNION ALL SELECT 'perm_btn_file_update', 'perm_menu_file', '编辑文件', '/api/file/{id}', NULL, 'sys:file:update', 2, NULL, 213, 1, 0, '文件编辑'
    UNION ALL SELECT 'perm_btn_file_delete', 'perm_menu_file', '删除文件', '/api/file/{id}', NULL, 'sys:file:delete', 2, NULL, 214, 1, 0, '文件删除'
    UNION ALL SELECT 'perm_menu_data_rule', 'perm_dir_system', '数据权限', '/system/data-rules', 'system/data-rule/index', NULL, 1, 'filter', 22, 0, 0, '数据权限规则菜单'
    UNION ALL SELECT 'perm_btn_data_rule_view', 'perm_menu_data_rule', '查看数据规则', '/api/permissionDataRule/page', NULL, 'sys:dataRule:view', 2, NULL, 221, 1, 0, '数据权限查看'
    UNION ALL SELECT 'perm_btn_data_rule_create', 'perm_menu_data_rule', '新增数据规则', '/api/permissionDataRule', NULL, 'sys:dataRule:create', 2, NULL, 222, 1, 0, '数据权限新增'
    UNION ALL SELECT 'perm_btn_data_rule_update', 'perm_menu_data_rule', '编辑数据规则', '/api/permissionDataRule/{id}', NULL, 'sys:dataRule:update', 2, NULL, 223, 1, 0, '数据权限编辑'
    UNION ALL SELECT 'perm_btn_data_rule_delete', 'perm_menu_data_rule', '删除数据规则', '/api/permissionDataRule/{id}', NULL, 'sys:dataRule:delete', 2, NULL, 224, 1, 0, '数据权限删除'
    UNION ALL SELECT 'perm_menu_job', 'perm_dir_system', '定时任务', '/system/jobs', 'system/job/index', NULL, 1, 'clock-circle', 23, 0, 0, '定时任务菜单'
    UNION ALL SELECT 'perm_btn_job_view', 'perm_menu_job', '查看任务', '/api/quartzJob/page', NULL, 'sys:job:view', 2, NULL, 221, 1, 0, '任务查看'
    UNION ALL SELECT 'perm_btn_job_create', 'perm_menu_job', '新增任务', '/api/quartzJob', NULL, 'sys:job:create', 2, NULL, 222, 1, 0, '任务新增'
    UNION ALL SELECT 'perm_btn_job_update', 'perm_menu_job', '编辑任务', '/api/quartzJob/{id}', NULL, 'sys:job:update', 2, NULL, 223, 1, 0, '任务编辑'
    UNION ALL SELECT 'perm_btn_job_delete', 'perm_menu_job', '删除任务', '/api/quartzJob/{id}', NULL, 'sys:job:delete', 2, NULL, 224, 1, 0, '任务删除'
    UNION ALL SELECT 'perm_btn_job_trigger', 'perm_menu_job', '触发任务', '/api/quartzJob/{id}/trigger', NULL, 'sys:job:trigger', 2, NULL, 225, 1, 0, '任务触发'
    UNION ALL SELECT 'perm_menu_job_log', 'perm_dir_system', '任务日志', '/system/job-logs', 'system/job-log/index', NULL, 1, 'profile', 23, 0, 0, '任务日志菜单'
    UNION ALL SELECT 'perm_btn_job_log_view', 'perm_menu_job_log', '查看任务日志', '/api/quartzJobLog/page', NULL, 'sys:jobLog:view', 2, NULL, 231, 1, 0, '任务日志查看'
    UNION ALL SELECT 'perm_menu_log', 'perm_dir_system', '系统日志', '/system/logs', 'system/log/index', NULL, 1, 'file-search', 24, 0, 0, '系统日志菜单'
    UNION ALL SELECT 'perm_btn_log_view', 'perm_menu_log', '查看系统日志', '/api/log/page', NULL, 'sys:log:view', 2, NULL, 241, 1, 0, '系统日志查看'
    UNION ALL SELECT 'perm_menu_config_center', 'perm_dir_system', '系统配置', '/system/config-center', 'system/config-center/index', NULL, 1, 'control', 25, 0, 0, '系统配置中心'
    UNION ALL SELECT 'perm_btn_config_view', 'perm_menu_config_center', '查看系统配置', '/api/systemConfig/groups', NULL, 'sys:config:view', 2, NULL, 251, 1, 0, '查看系统配置'
    UNION ALL SELECT 'perm_btn_config_update', 'perm_menu_config_center', '更新系统配置', '/api/systemConfig/batch', NULL, 'sys:config:update', 2, NULL, 252, 1, 0, '更新系统配置'
    UNION ALL SELECT 'perm_menu_security_center', 'perm_dir_system', '安全中心', '/system/security-center', 'system/security-center/index', NULL, 1, 'safety-certificate', 26, 0, 0, '用户安全中心'
    UNION ALL SELECT 'perm_btn_security_view', 'perm_menu_security_center', '查看安全中心', '/api/user/page', NULL, 'sys:security:view', 2, NULL, 261, 1, 0, '查看安全中心'
    UNION ALL SELECT 'perm_menu_data_transfer', 'perm_dir_system', '数据传输', '/system/data-transfer', 'system/data-transfer/index', NULL, 1, 'swap', 27, 0, 0, '导入导出中心'
    UNION ALL SELECT 'perm_btn_data_transfer_view', 'perm_menu_data_transfer', '查看数据传输', '/api/system/data-transfer', NULL, 'sys:dataTransfer:view', 2, NULL, 271, 1, 0, '查看数据传输'
) AS seed
WHERE @tenant_id IS NOT NULL AND @tenant_id <> ''
ON DUPLICATE KEY UPDATE
    `tenant_id` = VALUES(`tenant_id`),
    `parent_id` = VALUES(`parent_id`),
    `name` = VALUES(`name`),
    `url` = VALUES(`url`),
    `component` = VALUES(`component`),
    `scope_type` = VALUES(`scope_type`),
    `built_in` = VALUES(`built_in`),
    `perms` = VALUES(`perms`),
    `type` = VALUES(`type`),
    `icon` = VALUES(`icon`),
    `sort_no` = VALUES(`sort_no`),
    `hidden` = VALUES(`hidden`),
    `always_show` = VALUES(`always_show`),
    `status` = VALUES(`status`),
    `del_flag` = 0,
    `remark` = VALUES(`remark`),
    `update_by` = VALUES(`update_by`),
    `update_time` = VALUES(`update_time`);

INSERT INTO `sys_role_permission` (
    `id`, `tenant_id`, `role_id`, `permission_id`, `create_by`, `create_time`
)
SELECT
    UUID(), @tenant_id, @role_admin_id, p.`id`, 'bootstrap', @now
FROM `sys_permission` p
WHERE @tenant_id IS NOT NULL AND @tenant_id <> ''
  AND @role_admin_id IS NOT NULL AND @role_admin_id <> ''
  AND p.`tenant_id` = @tenant_id
  AND p.`id` IN (
      'perm_menu_dashboard', 'perm_dir_system',
      'perm_menu_tenant', 'perm_btn_tenant_view', 'perm_btn_tenant_create', 'perm_btn_tenant_update', 'perm_btn_tenant_delete',
      'perm_menu_user', 'perm_btn_user_view', 'perm_btn_user_create', 'perm_btn_user_update', 'perm_btn_user_delete',
      'perm_menu_role', 'perm_btn_role_view', 'perm_btn_role_create', 'perm_btn_role_update', 'perm_btn_role_delete',
      'perm_menu_permission', 'perm_btn_permission_view', 'perm_btn_permission_create', 'perm_btn_permission_update', 'perm_btn_permission_delete',
      'perm_menu_depart', 'perm_btn_depart_view', 'perm_btn_depart_create', 'perm_btn_depart_update', 'perm_btn_depart_delete',
      'perm_menu_dict', 'perm_btn_dict_view', 'perm_btn_dict_create', 'perm_btn_dict_update', 'perm_btn_dict_delete',
      'perm_menu_dict_item', 'perm_btn_dict_item_view', 'perm_btn_dict_item_create', 'perm_btn_dict_item_update', 'perm_btn_dict_item_delete',
      'perm_menu_message_center', 'perm_btn_message_center_view',
      'perm_menu_announcement', 'perm_btn_announcement_view', 'perm_btn_announcement_create', 'perm_btn_announcement_update', 'perm_btn_announcement_delete', 'perm_btn_announcement_publish',
      'perm_menu_announcement_send', 'perm_btn_announcement_send_view',
      'perm_menu_file', 'perm_btn_file_view', 'perm_btn_file_create', 'perm_btn_file_update', 'perm_btn_file_delete',
      'perm_menu_data_rule', 'perm_btn_data_rule_view', 'perm_btn_data_rule_create', 'perm_btn_data_rule_update', 'perm_btn_data_rule_delete',
      'perm_menu_job', 'perm_btn_job_view', 'perm_btn_job_create', 'perm_btn_job_update', 'perm_btn_job_delete', 'perm_btn_job_trigger',
      'perm_menu_job_log', 'perm_btn_job_log_view',
      'perm_menu_log', 'perm_btn_log_view',
      'perm_menu_config_center', 'perm_btn_config_view', 'perm_btn_config_update',
      'perm_menu_security_center', 'perm_btn_security_view',
      'perm_menu_data_transfer', 'perm_btn_data_transfer_view'
  )
  AND NOT EXISTS (
      SELECT 1
      FROM `sys_role_permission` rp
      WHERE rp.`tenant_id` = @tenant_id
        AND rp.`role_id` = @role_admin_id
        AND rp.`permission_id` = p.`id`
  );

INSERT INTO `sys_role_permission` (
    `id`, `tenant_id`, `role_id`, `permission_id`, `create_by`, `create_time`
)
SELECT
    UUID(), @tenant_id, @role_tenant_admin_id, p.`id`, 'bootstrap', @now
FROM `sys_permission` p
WHERE @tenant_id IS NOT NULL AND @tenant_id <> ''
  AND @role_tenant_admin_id IS NOT NULL AND @role_tenant_admin_id <> ''
  AND p.`tenant_id` = @tenant_id
  AND p.`id` IN (
      'perm_menu_dashboard', 'perm_dir_system',
      'perm_menu_user', 'perm_btn_user_view', 'perm_btn_user_create', 'perm_btn_user_update', 'perm_btn_user_delete',
      'perm_menu_role', 'perm_btn_role_view', 'perm_btn_role_create', 'perm_btn_role_update', 'perm_btn_role_delete',
      'perm_menu_permission', 'perm_btn_permission_view', 'perm_btn_permission_create', 'perm_btn_permission_update', 'perm_btn_permission_delete',
      'perm_menu_depart', 'perm_btn_depart_view', 'perm_btn_depart_create', 'perm_btn_depart_update', 'perm_btn_depart_delete',
      'perm_menu_dict', 'perm_btn_dict_view', 'perm_btn_dict_create', 'perm_btn_dict_update', 'perm_btn_dict_delete',
      'perm_menu_dict_item', 'perm_btn_dict_item_view', 'perm_btn_dict_item_create', 'perm_btn_dict_item_update', 'perm_btn_dict_item_delete',
      'perm_menu_message_center', 'perm_btn_message_center_view',
      'perm_menu_announcement', 'perm_btn_announcement_view', 'perm_btn_announcement_create', 'perm_btn_announcement_update', 'perm_btn_announcement_delete', 'perm_btn_announcement_publish',
      'perm_menu_announcement_send', 'perm_btn_announcement_send_view',
      'perm_menu_file', 'perm_btn_file_view', 'perm_btn_file_create', 'perm_btn_file_update', 'perm_btn_file_delete',
      'perm_menu_data_rule', 'perm_btn_data_rule_view', 'perm_btn_data_rule_create', 'perm_btn_data_rule_update', 'perm_btn_data_rule_delete',
      'perm_menu_job', 'perm_btn_job_view', 'perm_btn_job_create', 'perm_btn_job_update', 'perm_btn_job_delete', 'perm_btn_job_trigger',
      'perm_menu_job_log', 'perm_btn_job_log_view',
      'perm_menu_log', 'perm_btn_log_view',
      'perm_menu_config_center', 'perm_btn_config_view', 'perm_btn_config_update',
      'perm_menu_security_center', 'perm_btn_security_view',
      'perm_menu_data_transfer', 'perm_btn_data_transfer_view'
  )
  AND NOT EXISTS (
      SELECT 1
      FROM `sys_role_permission` rp
      WHERE rp.`tenant_id` = @tenant_id
        AND rp.`role_id` = @role_tenant_admin_id
        AND rp.`permission_id` = p.`id`
  );
