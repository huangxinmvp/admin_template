-- ================================
-- 1) 租户表 sys_tenant
-- ================================
DROP TABLE IF EXISTS `sys_tenant`;
CREATE TABLE `sys_tenant` (
                              `id`            varchar(36)  NOT NULL COMMENT '主键ID',
                              `tenant_name`   varchar(100) NOT NULL COMMENT '租户名称',
                              `tenant_code`   varchar(64)  NOT NULL COMMENT '租户编码',
                              `contact_name`  varchar(50)           COMMENT '联系人',
                              `contact_phone` varchar(20)           COMMENT '联系电话',
                              `contact_email` varchar(100)          COMMENT '联系邮箱',
                              `package_name`  varchar(100)          COMMENT '套餐名称',
                              `expire_time`   datetime              COMMENT '到期时间',
                              `status`        tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态:1启用 0停用',
                              `del_flag`      tinyint(1)   NOT NULL DEFAULT 0 COMMENT '删除状态',
                              `remark`        varchar(500)          COMMENT '备注',
                              `create_by`     varchar(50)           COMMENT '创建人',
                              `create_time`   datetime              COMMENT '创建时间',
                              `update_by`     varchar(50)           COMMENT '更新人',
                              `update_time`   datetime              COMMENT '更新时间',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_tenant_code` (`tenant_code`),
                              KEY `idx_tenant_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

-- ================================
-- 2) 用户表 sys_user
-- ================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
                            `id`            varchar(36)  NOT NULL COMMENT '主键ID',
                            `tenant_id`     varchar(36)           COMMENT '租户ID',
                            `username`      varchar(50)  NOT NULL COMMENT '用户名(登录账户)',
                            `realname`      varchar(100)          COMMENT '真实姓名',
                            `password`      varchar(100) NOT NULL COMMENT '密码(加密后)',
                            `salt`          varchar(32)           COMMENT '密码盐(可选)',
                            `avatar`        varchar(255)          COMMENT '头像',
                            `email`         varchar(100)          COMMENT '邮箱',
                            `phone`         varchar(20)           COMMENT '手机号',
                            `last_login_time` datetime            COMMENT '最后登录时间',
                            `last_login_ip` varchar(64)           COMMENT '最后登录IP',
                            `pwd_update_time` datetime            COMMENT '密码更新时间',
                            `lock_until`    datetime              COMMENT '锁定截止时间',
                            `status`        tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态: 1正常 0冻结',
                            `del_flag`      tinyint(1)   NOT NULL DEFAULT 0 COMMENT '删除状态: 0正常 1删除',
                            `remark`        varchar(500)          COMMENT '备注',
                            `create_by`     varchar(50)           COMMENT '创建人',
                            `create_time`   datetime              COMMENT '创建时间',
                            `update_by`     varchar(50)           COMMENT '更新人',
                            `update_time`   datetime              COMMENT '更新时间',
                            `sys_org_code`  varchar(64)           COMMENT '所属部门编码(数据权限)',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_user_tenant_username` (`tenant_id`,`username`),
                            KEY `idx_user_tenant` (`tenant_id`),
                            KEY `idx_user_phone` (`phone`),
                            KEY `idx_user_status` (`status`),
                            KEY `idx_user_org` (`sys_org_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ================================
-- 2) 角色表 sys_role
-- ================================
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
                            `id`            varchar(36) NOT NULL COMMENT '主键ID',
                            `tenant_id`     varchar(36)          COMMENT '租户ID',
                            `role_name`     varchar(100) NOT NULL COMMENT '角色名称',
                            `role_code`     varchar(64)  NOT NULL COMMENT '角色编码(英文/唯一)',
                            `description`   varchar(255)         COMMENT '角色描述',
                            `scope_type`    varchar(20)  NOT NULL DEFAULT 'TENANT' COMMENT '作用域:TENANT/PLATFORM',
                            `built_in`      tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否内置',
                            `status`        tinyint(1)  NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0停用',
                            `del_flag`      tinyint(1)  NOT NULL DEFAULT 0 COMMENT '删除状态: 0正常 1删除',
                            `remark`        varchar(500)         COMMENT '备注',
                            `create_by`     varchar(50)          COMMENT '创建人',
                            `create_time`   datetime             COMMENT '创建时间',
                            `update_by`     varchar(50)          COMMENT '更新人',
                            `update_time`   datetime             COMMENT '更新时间',
                            `sys_org_code`  varchar(64)          COMMENT '所属部门编码(可选)',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_role_tenant_code` (`tenant_id`,`role_code`),
                            KEY `idx_role_status` (`status`),
                            KEY `idx_role_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ================================
-- 3) 权限表(菜单/按钮) sys_permission
--    type: 0=目录 1=菜单 2=按钮
--    perms: 前端/后端通用权限标识(如 route 或 @PreAuthorize 表达式)
-- ================================
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
                                  `id`            varchar(36)  NOT NULL COMMENT '主键ID',
                                  `tenant_id`     varchar(36)           COMMENT '租户ID',
                                  `parent_id`     varchar(36)           COMMENT '父ID(顶级为空)',
                                  `name`          varchar(100) NOT NULL COMMENT '名称',
                                  `url`           varchar(255)          COMMENT '路由/接口地址',
                                  `component`     varchar(255)          COMMENT '前端组件',
                                  `scope_type`    varchar(20)  NOT NULL DEFAULT 'TENANT' COMMENT '作用域:TENANT/PLATFORM',
                                  `built_in`      tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否内置',
                                  `perms`         varchar(128)          COMMENT '权限标识(如: user:list)',
                                  `type`          tinyint(1)   NOT NULL DEFAULT 1 COMMENT '类型:0目录 1菜单 2按钮',
                                  `icon`          varchar(100)          COMMENT '图标',
                                  `sort_no`       int                   COMMENT '排序',
                                  `hidden`        tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否隐藏:0否 1是',
                                  `always_show`   tinyint(1)   NOT NULL DEFAULT 0 COMMENT '始终显示目录',
                                  `status`        tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态:1启用 0停用',
                                  `del_flag`      tinyint(1)   NOT NULL DEFAULT 0 COMMENT '删除状态:0正常 1删除',
                                  `remark`        varchar(500)          COMMENT '备注',
                                  `create_by`     varchar(50)           COMMENT '创建人',
                                  `create_time`   datetime              COMMENT '创建时间',
                                  `update_by`     varchar(50)           COMMENT '更新人',
                                  `update_time`   datetime              COMMENT '更新时间',
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_perm_tenant_perms` (`tenant_id`,`perms`),
                                  KEY `idx_perm_parent` (`parent_id`),
                                  KEY `idx_perm_type` (`type`),
                                  KEY `idx_perm_perms` (`perms`),
                                  KEY `idx_perm_status` (`status`),
                                  KEY `idx_perm_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表(菜单/按钮)';

-- ================================
-- 4) 系统配置表 sys_system_config
-- ================================
DROP TABLE IF EXISTS `sys_system_config`;
CREATE TABLE `sys_system_config` (
                                     `id`            varchar(36)  NOT NULL COMMENT '主键ID',
                                     `tenant_id`     varchar(36)           COMMENT '租户ID，为空表示平台级配置',
                                     `group_code`    varchar(64)  NOT NULL COMMENT '分组编码',
                                     `group_name`    varchar(100) NOT NULL COMMENT '分组名称',
                                     `group_sort`    int          NOT NULL DEFAULT 0 COMMENT '分组排序',
                                     `config_key`    varchar(128) NOT NULL COMMENT '配置键',
                                     `config_name`   varchar(100) NOT NULL COMMENT '配置名称',
                                     `config_value`  text                  COMMENT '配置值',
                                     `default_value` text                  COMMENT '默认值',
                                     `value_type`    varchar(32)  NOT NULL DEFAULT 'text' COMMENT '值类型:text/textarea/number/boolean/color/url/email',
                                     `required_flag` tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否必填',
                                     `placeholder`   varchar(255)          COMMENT '占位提示',
                                     `description`   varchar(500)          COMMENT '说明',
                                     `options_json`  text                  COMMENT '选项JSON',
                                     `sort_no`       int          NOT NULL DEFAULT 0 COMMENT '组内排序',
                                     `built_in`      tinyint(1)   NOT NULL DEFAULT 1 COMMENT '是否内置',
                                     `status`        tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态',
                                     `del_flag`      tinyint(1)   NOT NULL DEFAULT 0 COMMENT '删除状态',
                                     `create_by`     varchar(50)           COMMENT '创建人',
                                     `create_time`   datetime              COMMENT '创建时间',
                                     `update_by`     varchar(50)           COMMENT '更新人',
                                     `update_time`   datetime              COMMENT '更新时间',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_system_config_tenant_key` (`tenant_id`,`config_key`),
                                     KEY `idx_system_config_group` (`group_code`, `group_sort`),
                                     KEY `idx_system_config_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ================================
-- 5) 用户登录会话 sys_user_session
-- ================================
DROP TABLE IF EXISTS `sys_user_session`;
CREATE TABLE `sys_user_session` (
                                    `id`                varchar(36)  NOT NULL COMMENT '主键ID',
                                    `tenant_id`         varchar(36)           COMMENT '租户ID',
                                    `user_id`           varchar(36)  NOT NULL COMMENT '用户ID',
                                    `client_ip`         varchar(64)           COMMENT '客户端IP',
                                    `user_agent`        varchar(500)          COMMENT '浏览器UA',
                                    `device_name`       varchar(100)          COMMENT '设备名称',
                                    `login_time`        datetime              COMMENT '登录时间',
                                    `last_active_time`  datetime              COMMENT '最后活跃时间',
                                    `access_expires_at` datetime              COMMENT '访问令牌过期时间',
                                    `refresh_expires_at` datetime             COMMENT '刷新令牌过期时间',
                                    `status`            tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态:1在线 0离线',
                                    `logout_reason`     varchar(255)          COMMENT '下线原因',
                                    `logout_by`         varchar(50)           COMMENT '下线操作人',
                                    `logout_time`       datetime              COMMENT '下线时间',
                                    `del_flag`          tinyint(1)   NOT NULL DEFAULT 0 COMMENT '删除状态',
                                    `create_by`         varchar(50)           COMMENT '创建人',
                                    `create_time`       datetime              COMMENT '创建时间',
                                    `update_by`         varchar(50)           COMMENT '更新人',
                                    `update_time`       datetime              COMMENT '更新时间',
                                    PRIMARY KEY (`id`),
                                    KEY `idx_user_session_user` (`user_id`),
                                    KEY `idx_user_session_tenant` (`tenant_id`),
                                    KEY `idx_user_session_status` (`status`),
                                    KEY `idx_user_session_refresh_exp` (`refresh_expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户登录会话';

-- ================================
-- 6) 流程设计草稿 wf_model_draft
-- ================================
DROP TABLE IF EXISTS `wf_model_draft`;
CREATE TABLE `wf_model_draft` (
                                  `id`                      varchar(36)  NOT NULL COMMENT '主键ID',
                                  `tenant_id`               varchar(36)           COMMENT '租户ID',
                                  `owner_user_id`           varchar(36)  NOT NULL COMMENT '草稿拥有者用户ID',
                                  `draft_name`              varchar(100) NOT NULL COMMENT '草稿名称',
                                  `process_definition_key`  varchar(100)          COMMENT '流程定义Key',
                                  `process_definition_name` varchar(100)          COMMENT '流程名称',
                                  `source_definition_id`    varchar(128)          COMMENT '来源流程定义ID',
                                  `source_definition_key`   varchar(100)          COMMENT '来源流程定义Key',
                                  `category`                varchar(100)          COMMENT '流程分类',
                                  `bpmn_xml`                longtext     NOT NULL COMMENT 'BPMN XML内容',
                                  `remark`                  varchar(500)          COMMENT '备注',
                                  `del_flag`                tinyint(1)   NOT NULL DEFAULT 0 COMMENT '删除状态',
                                  `create_by`               varchar(50)           COMMENT '创建人',
                                  `create_time`             datetime              COMMENT '创建时间',
                                  `update_by`               varchar(50)           COMMENT '更新人',
                                  `update_time`             datetime              COMMENT '更新时间',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_wf_draft_tenant_owner` (`tenant_id`, `owner_user_id`),
                                  KEY `idx_wf_draft_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程设计草稿';

-- ================================
-- 7) 用户-角色 关联表 sys_user_role
-- ================================
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
                                 `id`        varchar(36) NOT NULL COMMENT '主键ID',
                                 `tenant_id` varchar(36)          COMMENT '租户ID',
                                 `user_id`   varchar(36) NOT NULL COMMENT '用户ID',
                                 `role_id`   varchar(36) NOT NULL COMMENT '角色ID',
                                 `create_by` varchar(50)          COMMENT '创建人',
                                 `create_time` datetime           COMMENT '创建时间',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_user_role` (`tenant_id`,`user_id`,`role_id`),
                                 KEY `idx_userrole_tenant` (`tenant_id`),
                                 KEY `idx_userrole_user` (`user_id`),
                                 KEY `idx_userrole_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色 关联';

-- ================================
-- 8) 角色-权限 关联表 sys_role_permission
-- ================================
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
                                       `id`            varchar(36) NOT NULL COMMENT '主键ID',
                                       `tenant_id`     varchar(36)          COMMENT '租户ID',
                                       `role_id`       varchar(36) NOT NULL COMMENT '角色ID',
                                       `permission_id` varchar(36) NOT NULL COMMENT '权限ID',
                                       `data_rule_ids` varchar(255)         COMMENT '数据规则ID集合(可选)',
                                       `create_by`     varchar(50)          COMMENT '创建人',
                                       `create_time`   datetime             COMMENT '创建时间',
                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `uk_role_perm` (`tenant_id`,`role_id`,`permission_id`),
                                       KEY `idx_roleperm_tenant` (`tenant_id`),
                                       KEY `idx_roleperm_role` (`role_id`),
                                       KEY `idx_roleperm_perm` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限 关联';


/* ===========================================
 *  组织/部门
 * =========================================== */
DROP TABLE IF EXISTS `sys_depart`;
CREATE TABLE `sys_depart` (
                              `id`            varchar(36)  NOT NULL COMMENT '主键ID',
                              `tenant_id`     varchar(36)           COMMENT '租户ID',
                              `parent_id`     varchar(36)           COMMENT '父级部门ID',
                              `depart_name`   varchar(100) NOT NULL COMMENT '部门名称',
                              `org_code`      varchar(64)  NOT NULL COMMENT '机构编码(树/数据权限编码)',
                              `depart_order`  int                   COMMENT '排序',
                              `org_category`  tinyint(1)   DEFAULT 1 COMMENT '机构类别:1公司 2部门 3岗位',
                              `status`        tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态:1启用 0停用',
                              `del_flag`      tinyint(1)   NOT NULL DEFAULT 0 COMMENT '删除状态:0正常 1删除',
                              `remark`        varchar(500)          COMMENT '备注',
                              `create_by`     varchar(50)           COMMENT '创建人',
                              `create_time`   datetime              COMMENT '创建时间',
                              `update_by`     varchar(50)           COMMENT '更新人',
                              `update_time`   datetime              COMMENT '更新时间',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_depart_tenant_org_code` (`tenant_id`,`org_code`),
                              KEY `idx_depart_tenant` (`tenant_id`),
                              KEY `idx_depart_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门';

DROP TABLE IF EXISTS `sys_user_depart`;
CREATE TABLE `sys_user_depart` (
                                   `id`         varchar(36) NOT NULL COMMENT '主键ID',
                                   `tenant_id`  varchar(36)          COMMENT '租户ID',
                                   `user_id`    varchar(36) NOT NULL COMMENT '用户ID',
                                   `depart_id`  varchar(36) NOT NULL COMMENT '部门ID',
                                   `rel_type`   tinyint(1) DEFAULT 1 COMMENT '关系类型:1主部门 2兼任',
                                   `create_by`  varchar(50)        COMMENT '创建人',
                                   `create_time` datetime          COMMENT '创建时间',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_user_depart` (`tenant_id`,`user_id`,`depart_id`),
                                   KEY `idx_ud_tenant` (`tenant_id`),
                                   KEY `idx_ud_user` (`user_id`),
                                   KEY `idx_ud_depart` (`depart_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-部门关系';

/* ===========================================
 *  数据字典
 * =========================================== */
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
                            `id`          varchar(36)  NOT NULL COMMENT '主键ID',
                            `tenant_id`   varchar(36)           COMMENT '租户ID',
                            `dict_name`   varchar(100) NOT NULL COMMENT '字典名称',
                            `dict_code`   varchar(100) NOT NULL COMMENT '字典编码(唯一)',
                            `description` varchar(255)          COMMENT '描述',
                            `scope_type`  varchar(20)  NOT NULL DEFAULT 'TENANT' COMMENT '作用域:TENANT/PLATFORM',
                            `built_in`    tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否内置',
                            `status`      tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态:1启用 0停用',
                            `del_flag`    tinyint(1)   NOT NULL DEFAULT 0 COMMENT '删除状态',
                            `create_by`   varchar(50)           COMMENT '创建人',
                            `create_time` datetime              COMMENT '创建时间',
                            `update_by`   varchar(50)           COMMENT '更新人',
                            `update_time` datetime              COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_dict_tenant_code` (`tenant_id`,`dict_code`),
                            KEY `idx_dict_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典';

DROP TABLE IF EXISTS `sys_dict_item`;
CREATE TABLE `sys_dict_item` (
                                 `id`         varchar(36)  NOT NULL COMMENT '主键ID',
                                 `tenant_id`  varchar(36)           COMMENT '租户ID',
                                 `dict_id`    varchar(36)  NOT NULL COMMENT '字典ID',
                                 `item_text`  varchar(100) NOT NULL COMMENT '字典项文本',
                                 `item_value` varchar(100) NOT NULL COMMENT '字典项值',
                                 `sort_order` int                   COMMENT '排序',
                                 `status`     tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态:1启用 0停用',
                                 `del_flag`   tinyint(1)   NOT NULL DEFAULT 0 COMMENT '删除状态',
                                 `create_by`  varchar(50)           COMMENT '创建人',
                                 `create_time` datetime             COMMENT '创建时间',
                                 `update_by`  varchar(50)           COMMENT '更新人',
                                 `update_time` datetime             COMMENT '更新时间',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_dict_item` (`tenant_id`,`dict_id`,`item_value`),
                                 KEY `idx_dictitem_tenant` (`tenant_id`),
                                 KEY `idx_dictitem_dict` (`dict_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典项';

/* ===========================================
 *  菜单数据权限规则（与 sys_role_permission 的 data_rule_ids 对应）
 * =========================================== */
DROP TABLE IF EXISTS `sys_permission_data_rule`;
CREATE TABLE `sys_permission_data_rule` (
                                            `id`            varchar(36)  NOT NULL COMMENT '主键ID',
                                            `tenant_id`     varchar(36)           COMMENT '租户ID',
                                            `permission_id` varchar(36)  NOT NULL COMMENT '菜单权限ID',
                                            `rule_name`     varchar(100) NOT NULL COMMENT '规则名称',
                                            `rule_column`   varchar(100) NOT NULL COMMENT '限定字段',
                                            `condition`     varchar(32)  NOT NULL COMMENT '条件(=,!=,in,like,<=,>=,between)',
                                            `rule_value`    varchar(512) NOT NULL COMMENT '规则值(支持变量)',
                                            `status`        tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态:1启用 0停用',
                                            `del_flag`      tinyint(1)   NOT NULL DEFAULT 0 COMMENT '删除状态',
                                            `create_by`     varchar(50)           COMMENT '创建人',
                                            `create_time`   datetime              COMMENT '创建时间',
                                            `update_by`     varchar(50)           COMMENT '更新人',
                                            `update_time`   datetime              COMMENT '更新时间',
                                            PRIMARY KEY (`id`),
                                            KEY `idx_rule_tenant` (`tenant_id`),
                                            KEY `idx_rule_perm` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据权限规则';

/* ===========================================
 *  系统日志
 * =========================================== */
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
                           `id`            varchar(36)  NOT NULL COMMENT '主键ID',
                           `tenant_id`     varchar(36)           COMMENT '租户ID',
                           `log_type`      tinyint(1)   NOT NULL DEFAULT 1 COMMENT '日志类型:1登录 2操作',
                           `log_content`   varchar(2000)         COMMENT '日志内容',
                           `operate_type`  tinyint(1)            COMMENT '操作类型',
                           `userid`        varchar(36)           COMMENT '用户ID',
                           `username`      varchar(50)           COMMENT '用户名',
                           `ip`            varchar(64)           COMMENT 'IP',
                           `method`        varchar(500)          COMMENT '请求方法',
                           `request_url`   varchar(255)          COMMENT '请求路径',
                           `request_type`  varchar(10)           COMMENT '请求类型',
                           `request_param` text                  COMMENT '请求参数',
                           `cost_time`     bigint                COMMENT '耗时(毫秒)',
                           `create_time`   datetime              COMMENT '创建时间',
                           PRIMARY KEY (`id`),
                           KEY `idx_log_tenant` (`tenant_id`),
                           KEY `idx_log_user` (`userid`),
                           KEY `idx_log_ctime` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志';

/* ===========================================
 *  系统通告/消息
 * =========================================== */
DROP TABLE IF EXISTS `sys_announcement`;
CREATE TABLE `sys_announcement` (
                                    `id`           varchar(36)  NOT NULL COMMENT '主键ID',
                                    `tenant_id`    varchar(36)           COMMENT '租户ID',
                                    `title`        varchar(200) NOT NULL COMMENT '标题',
                                    `msg_content`  text                  COMMENT '内容',
                                    `start_time`   datetime              COMMENT '开始时间',
                                    `end_time`     datetime              COMMENT '结束时间',
                                    `sender`       varchar(50)           COMMENT '发布人',
                                    `priority`     tinyint(1)   DEFAULT 1 COMMENT '优先级:1低 2中 3高',
                                    `msg_category` tinyint(1)   DEFAULT 1 COMMENT '类别:1通知 2系统消息',
                                    `msg_type`     tinyint(1)   DEFAULT 1 COMMENT '类型:1通知公告 2系统消息',
                                    `receiver_scope` varchar(20) DEFAULT 'ALL' COMMENT '接收范围:ALL/TENANT/USER',
                                    `target_tenant_id` varchar(36)       COMMENT '目标租户ID',
                                    `send_status`  tinyint(1)   DEFAULT 0 COMMENT '发布状态:0未发 1已发 2撤销',
                                    `send_time`    datetime              COMMENT '发布时间',
                                    `cancel_time`  datetime              COMMENT '撤销时间',
                                    `del_flag`     tinyint(1)   NOT NULL DEFAULT 0 COMMENT '删除状态',
                                    `create_by`    varchar(50)           COMMENT '创建人',
                                    `create_time`  datetime              COMMENT '创建时间',
                                    `update_by`    varchar(50)           COMMENT '更新人',
                                    `update_time`  datetime              COMMENT '更新时间',
                                    PRIMARY KEY (`id`),
                                    KEY `idx_anno_tenant` (`tenant_id`),
                                    KEY `idx_anno_status` (`send_status`),
                                    KEY `idx_anno_time` (`start_time`,`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通告';

DROP TABLE IF EXISTS `sys_announcement_send`;
CREATE TABLE `sys_announcement_send` (
                                         `id`        varchar(36) NOT NULL COMMENT '主键ID',
                                         `tenant_id` varchar(36)          COMMENT '租户ID',
                                         `annt_id`   varchar(36) NOT NULL COMMENT '通告ID',
                                         `user_id`   varchar(36) NOT NULL COMMENT '用户ID',
                                         `read_flag` tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否已读:0未读 1已读',
                                         `read_time` datetime             COMMENT '阅读时间',
                                         PRIMARY KEY (`id`),
                                         UNIQUE KEY `uk_anno_user` (`tenant_id`,`annt_id`,`user_id`),
                                         KEY `idx_anno_send_tenant` (`tenant_id`),
                                         KEY `idx_anno_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通告-用户关系';

/* ===========================================
 *  文件存储记录（本地/对象存储）
 * =========================================== */
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file` (
                            `id`           varchar(36)  NOT NULL COMMENT '主键ID',
                            `tenant_id`    varchar(36)           COMMENT '租户ID',
                            `file_name`    varchar(255) NOT NULL COMMENT '原始文件名',
                            `url`          varchar(500) NOT NULL COMMENT '访问URL',
                            `object_name`  varchar(255)          COMMENT '存储对象名',
                            `content_type` varchar(100)          COMMENT 'Content-Type',
                            `file_size`    bigint                COMMENT '文件大小(字节)',
                            `storage_type` varchar(20)  DEFAULT 'local' COMMENT '存储类型:local/minio/aliyun/qiniu',
                            `storage_provider` varchar(20) DEFAULT 'local' COMMENT '存储提供方',
                            `bucket_name`  varchar(100)          COMMENT 'Bucket/目录',
                            `biz_type`     varchar(64)           COMMENT '业务类型',
                            `md5`          varchar(64)           COMMENT 'MD5',
                            `status`       tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态',
                            `del_flag`     tinyint(1)   NOT NULL DEFAULT 0 COMMENT '删除状态',
                            `create_by`    varchar(50)           COMMENT '创建人',
                            `create_time`  datetime              COMMENT '创建时间',
                            `update_by`    varchar(50)           COMMENT '更新人',
                            `update_time`  datetime              COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            KEY `idx_file_tenant` (`tenant_id`),
                            KEY `idx_file_biz_type` (`biz_type`),
                            KEY `idx_file_md5` (`md5`),
                            KEY `idx_file_ctime` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件存储记录';

/* ===========================================
 *  定时任务（Quartz）
 * =========================================== */
DROP TABLE IF EXISTS `sys_quartz_job`;
CREATE TABLE `sys_quartz_job` (
                                  `id`              varchar(36)  NOT NULL COMMENT '主键ID',
                                  `tenant_id`       varchar(36)           COMMENT '租户ID',
                                  `job_name`        varchar(100) NOT NULL COMMENT '任务名称',
                                  `job_group`       varchar(64)  DEFAULT 'DEFAULT' COMMENT '任务组',
                                  `invoke_target`   varchar(500) NOT NULL COMMENT '调用目标(类#方法/Bean#方法/HTTP等)',
                                  `cron_expression` varchar(100) NOT NULL COMMENT 'Cron表达式',
                                  `misfire_policy`  varchar(20)  DEFAULT 'SMART' COMMENT 'misfire策略:SMART/IGNORE/MISFIRE',
                                  `concurrent`      tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否并发:0否 1是',
                                  `status`          tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态:1启用 0停用',
                                  `description`     varchar(255)          COMMENT '描述',
                                  `last_run_time`   datetime              COMMENT '最近执行时间',
                                  `next_run_time`   datetime              COMMENT '下次执行时间',
                                  `run_count`       bigint       NOT NULL DEFAULT 0 COMMENT '累计执行次数',
                                  `last_status`     tinyint(1)            COMMENT '最近执行状态',
                                  `last_message`    varchar(1000)         COMMENT '最近执行信息',
                                  `create_by`       varchar(50)           COMMENT '创建人',
                                  `create_time`     datetime              COMMENT '创建时间',
                                  `update_by`       varchar(50)           COMMENT '更新人',
                                  `update_time`     datetime              COMMENT '更新时间',
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_job_name_group` (`tenant_id`,`job_name`,`job_group`),
                                  KEY `idx_job_tenant` (`tenant_id`),
                                  KEY `idx_job_next_run` (`next_run_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务';

DROP TABLE IF EXISTS `sys_quartz_job_log`;
CREATE TABLE `sys_quartz_job_log` (
                                      `id`             varchar(36) NOT NULL COMMENT '主键ID',
                                      `tenant_id`      varchar(36)          COMMENT '租户ID',
                                      `job_id`         varchar(36)          COMMENT '任务ID',
                                      `job_name`       varchar(100)         COMMENT '任务名称',
                                      `job_group`      varchar(64)          COMMENT '任务组',
                                      `invoke_target`  varchar(500)         COMMENT '调用目标',
                                      `cron_expression` varchar(100)        COMMENT 'Cron表达式',
                                      `trigger_type`   varchar(20)          COMMENT '触发类型',
                                      `status`         tinyint(1) DEFAULT 1 COMMENT '执行状态:1成功 0失败',
                                      `exception_info` text                 COMMENT '异常信息',
                                      `cost_time`      bigint               COMMENT '耗时(ms)',
                                      `create_time`    datetime             COMMENT '执行时间',
                                      PRIMARY KEY (`id`),
                                      KEY `idx_joblog_tenant` (`tenant_id`),
                                      KEY `idx_joblog_jobid` (`job_id`),
                                      KEY `idx_joblog_ctime` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务执行日志';
