SET NAMES utf8mb4;

SET @tenant_id = 'seed_tenant_default';
SET @admin_user_id = 'seed_user_admin';
SET @now = NOW();

SET @template_delivery_id = 'demo_wft_delivery_v1';
SET @template_internal_id = 'demo_wft_internal_v1';

SET @role_req_analyst_id = 'demo_role_req_analyst';
SET @role_product_manager_id = 'demo_role_product_manager';
SET @role_architect_id = 'demo_role_architect';
SET @role_budget_analyst_id = 'demo_role_budget_analyst';
SET @role_frontend_id = 'demo_role_frontend';
SET @role_backend_id = 'demo_role_backend';
SET @role_qa_id = 'demo_role_qa';

SET @project_alpha_id = 'demo_project_alpha';
SET @project_beta_id = 'demo_project_beta';
SET @project_gamma_id = 'demo_project_gamma';
SET @project_delta_id = 'demo_project_delta';

INSERT IGNORE INTO `ai_workflow_template` (
    `id`, `tenant_id`, `template_code`, `template_name`, `project_type`, `version_no`, `stage_count`, `status`,
    `default_flag`, `description`, `gate_checks_config`, `blocking_decision_config`, `blocking_approval_config`,
    `budget_threshold_config`, `high_risk_approval_config`, `remark`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`
) VALUES
(
    @template_delivery_id, @tenant_id, 'WF_DELIVERY_V1', '标准交付模板', 'delivery', 1, 12, 'active',
    1, '适用于标准软件交付项目的治理模板',
    '{"gate":"stage-check"}',
    '{"rule":"decision-blocker"}',
    '{"rule":"approval-blocker"}',
    '{"rule":"budget-threshold"}',
    '{"rule":"high-risk-approval"}',
    'Demo 交付模板', 0, 'seed', @now, 'seed', @now
),
(
    @template_internal_id, @tenant_id, 'WF_INTERNAL_V1', '内部试点模板', 'internal', 1, 8, 'active',
    0, '适用于内部试点和验证项目',
    '{"gate":"light"}',
    '{"rule":"decision-blocker"}',
    '{"rule":"approval-blocker"}',
    '{"rule":"budget-threshold"}',
    '{"rule":"high-risk-approval"}',
    'Demo 内部模板', 0, 'seed', @now, 'seed', @now
);

INSERT IGNORE INTO `ai_workflow_template_stage` (
    `id`, `tenant_id`, `template_id`, `stage_code`, `stage_name`, `stage_order`, `enabled_flag`,
    `stage_description`, `stage_note`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`
) VALUES
('demo_wfts_delivery_01', @tenant_id, @template_delivery_id, 'intake', '需求接收', 1, 1, '记录项目背景和目标', NULL, 0, 'seed', @now, 'seed', @now),
('demo_wfts_delivery_02', @tenant_id, @template_delivery_id, 'clarification', '需求澄清', 2, 1, '确认不明确需求和阻塞问题', NULL, 0, 'seed', @now, 'seed', @now),
('demo_wfts_delivery_03', @tenant_id, @template_delivery_id, 'feasibility', '可行性评估', 3, 1, '评估方案和实施约束', NULL, 0, 'seed', @now, 'seed', @now),
('demo_wfts_delivery_04', @tenant_id, @template_delivery_id, 'estimation', '预算估算', 4, 1, '形成预算建议和资源拆分', NULL, 0, 'seed', @now, 'seed', @now),
('demo_wfts_delivery_05', @tenant_id, @template_delivery_id, 'approval', '审批', 5, 1, '触发关键预算和范围审批', NULL, 0, 'seed', @now, 'seed', @now),
('demo_wfts_delivery_06', @tenant_id, @template_delivery_id, 'planning', '计划', 6, 1, '形成交付计划', NULL, 0, 'seed', @now, 'seed', @now),
('demo_wfts_delivery_07', @tenant_id, @template_delivery_id, 'design', '设计', 7, 1, '形成产品与架构设计', NULL, 0, 'seed', @now, 'seed', @now),
('demo_wfts_delivery_08', @tenant_id, @template_delivery_id, 'development', '开发', 8, 1, '进入研发执行', NULL, 0, 'seed', @now, 'seed', @now),
('demo_wfts_delivery_09', @tenant_id, @template_delivery_id, 'testing', '测试', 9, 1, '完成测试与缺陷收敛', NULL, 0, 'seed', @now, 'seed', @now),
('demo_wfts_delivery_10', @tenant_id, @template_delivery_id, 'release_approval', '发布审批', 10, 1, '发布前进行最终确认', NULL, 0, 'seed', @now, 'seed', @now),
('demo_wfts_delivery_11', @tenant_id, @template_delivery_id, 'release', '发布', 11, 1, '完成发布上线', NULL, 0, 'seed', @now, 'seed', @now),
('demo_wfts_delivery_12', @tenant_id, @template_delivery_id, 'retrospective', '复盘', 12, 1, '形成交付复盘', NULL, 0, 'seed', @now, 'seed', @now),
('demo_wfts_internal_01', @tenant_id, @template_internal_id, 'intake', '需求接收', 1, 1, '记录内部试点目标', NULL, 0, 'seed', @now, 'seed', @now),
('demo_wfts_internal_02', @tenant_id, @template_internal_id, 'clarification', '需求澄清', 2, 1, '澄清内部试点边界', NULL, 0, 'seed', @now, 'seed', @now),
('demo_wfts_internal_03', @tenant_id, @template_internal_id, 'feasibility', '可行性评估', 3, 1, '快速评估技术风险', NULL, 0, 'seed', @now, 'seed', @now),
('demo_wfts_internal_04', @tenant_id, @template_internal_id, 'planning', '计划', 4, 1, '形成轻量执行计划', NULL, 0, 'seed', @now, 'seed', @now),
('demo_wfts_internal_05', @tenant_id, @template_internal_id, 'development', '开发', 5, 1, '进入试点开发', NULL, 0, 'seed', @now, 'seed', @now),
('demo_wfts_internal_06', @tenant_id, @template_internal_id, 'testing', '测试', 6, 1, '验证试点效果', NULL, 0, 'seed', @now, 'seed', @now),
('demo_wfts_internal_07', @tenant_id, @template_internal_id, 'release', '发布', 7, 1, '内部发布试点成果', NULL, 0, 'seed', @now, 'seed', @now),
('demo_wfts_internal_08', @tenant_id, @template_internal_id, 'retrospective', '复盘', 8, 1, '形成试点总结', NULL, 0, 'seed', @now, 'seed', @now);

INSERT IGNORE INTO `ai_agent_role` (
    `id`, `tenant_id`, `role_code`, `role_name`, `role_category`, `description`, `status`, `capability_summary`,
    `default_flag`, `budget_factor`, `approval_collaboration_flag`, `approval_required`, `max_concurrency`,
    `remark`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`
) VALUES
(@role_req_analyst_id, @tenant_id, 'REQ_ANALYST', '需求分析师', 'governance', '负责需求理解、拆解和澄清', 'active', '聚焦需求澄清与验收标准', 1, 1.00, 1, 1, 2, 'Demo 角色', 0, 'seed', @now, 'seed', @now),
(@role_product_manager_id, @tenant_id, 'PRODUCT_MANAGER', '产品经理', 'governance', '负责产品范围、优先级和决策确认', 'active', '聚焦范围、优先级与决策推进', 1, 1.10, 1, 1, 2, 'Demo 角色', 0, 'seed', @now, 'seed', @now),
(@role_architect_id, @tenant_id, 'ARCHITECT', '架构师', 'delivery', '负责架构方案和技术约束评估', 'active', '聚焦方案评审与架构风险识别', 1, 1.20, 1, 1, 2, 'Demo 角色', 0, 'seed', @now, 'seed', @now),
(@role_budget_analyst_id, @tenant_id, 'BUDGET_ANALYST', '预算分析师', 'support', '负责预算影响分析和预算确认建议', 'active', '聚焦预算变化、阈值与健康评估', 0, 1.05, 1, 1, 1, 'Demo 角色', 0, 'seed', @now, 'seed', @now),
(@role_frontend_id, @tenant_id, 'FRONTEND_ENGINEER', '前端工程师', 'delivery', '负责前端交付实现', 'active', '聚焦前端页面与交互实现', 0, 1.00, 0, 0, 3, 'Demo 角色', 0, 'seed', @now, 'seed', @now),
(@role_backend_id, @tenant_id, 'BACKEND_ENGINEER', '后端工程师', 'delivery', '负责后端服务与集成实现', 'active', '聚焦后端能力与集成接口', 0, 1.10, 0, 0, 3, 'Demo 角色', 0, 'seed', @now, 'seed', @now),
(@role_qa_id, @tenant_id, 'QA_ENGINEER', '测试工程师', 'support', '负责测试验证和上线质量把关', 'active', '聚焦测试、缺陷收敛与发布质量', 0, 0.95, 0, 0, 2, 'Demo 角色', 0, 'seed', @now, 'seed', @now);

INSERT IGNORE INTO `ai_agent_role_stage_participation` (
    `id`, `tenant_id`, `agent_role_id`, `stage_code`, `stage_name`, `participation_type`, `note`,
    `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`
) VALUES
('demo_rsp_01', @tenant_id, @role_req_analyst_id, 'intake', '需求接收', 'required', NULL, 0, 'seed', @now, 'seed', @now),
('demo_rsp_02', @tenant_id, @role_req_analyst_id, 'clarification', '需求澄清', 'required', NULL, 0, 'seed', @now, 'seed', @now),
('demo_rsp_03', @tenant_id, @role_req_analyst_id, 'feasibility', '可行性评估', 'optional', NULL, 0, 'seed', @now, 'seed', @now),
('demo_rsp_04', @tenant_id, @role_product_manager_id, 'clarification', '需求澄清', 'required', NULL, 0, 'seed', @now, 'seed', @now),
('demo_rsp_05', @tenant_id, @role_product_manager_id, 'approval', '审批', 'required', NULL, 0, 'seed', @now, 'seed', @now),
('demo_rsp_06', @tenant_id, @role_product_manager_id, 'planning', '计划', 'required', NULL, 0, 'seed', @now, 'seed', @now),
('demo_rsp_07', @tenant_id, @role_architect_id, 'feasibility', '可行性评估', 'required', NULL, 0, 'seed', @now, 'seed', @now),
('demo_rsp_08', @tenant_id, @role_architect_id, 'design', '设计', 'required', NULL, 0, 'seed', @now, 'seed', @now),
('demo_rsp_09', @tenant_id, @role_architect_id, 'development', '开发', 'optional', NULL, 0, 'seed', @now, 'seed', @now),
('demo_rsp_10', @tenant_id, @role_budget_analyst_id, 'estimation', '预算估算', 'required', NULL, 0, 'seed', @now, 'seed', @now),
('demo_rsp_11', @tenant_id, @role_budget_analyst_id, 'approval', '审批', 'required', NULL, 0, 'seed', @now, 'seed', @now),
('demo_rsp_12', @tenant_id, @role_frontend_id, 'development', '开发', 'optional', NULL, 0, 'seed', @now, 'seed', @now),
('demo_rsp_13', @tenant_id, @role_frontend_id, 'testing', '测试', 'optional', NULL, 0, 'seed', @now, 'seed', @now),
('demo_rsp_14', @tenant_id, @role_backend_id, 'development', '开发', 'required', NULL, 0, 'seed', @now, 'seed', @now),
('demo_rsp_15', @tenant_id, @role_backend_id, 'testing', '测试', 'optional', NULL, 0, 'seed', @now, 'seed', @now),
('demo_rsp_16', @tenant_id, @role_qa_id, 'testing', '测试', 'required', NULL, 0, 'seed', @now, 'seed', @now),
('demo_rsp_17', @tenant_id, @role_qa_id, 'release_approval', '发布审批', 'required', NULL, 0, 'seed', @now, 'seed', @now),
('demo_rsp_18', @tenant_id, @role_qa_id, 'release', '发布', 'optional', NULL, 0, 'seed', @now, 'seed', @now);

INSERT IGNORE INTO `ai_agent_role_allowed_action` (
    `id`, `tenant_id`, `agent_role_id`, `action_code`, `action_name`, `allowed_flag`, `approval_required_flag`, `note`,
    `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`
) VALUES
('demo_raa_01', @tenant_id, @role_req_analyst_id, 'clarification_manage', '创建/更新澄清项', 1, 0, NULL, 0, 'seed', @now, 'seed', @now),
('demo_raa_02', @tenant_id, @role_product_manager_id, 'decision_create', '创建决策事项', 1, 0, NULL, 0, 'seed', @now, 'seed', @now),
('demo_raa_03', @tenant_id, @role_budget_analyst_id, 'budget_change_propose', '提出预算变更', 1, 1, NULL, 0, 'seed', @now, 'seed', @now),
('demo_raa_04', @tenant_id, @role_product_manager_id, 'approval_request', '发起审批请求', 1, 0, NULL, 0, 'seed', @now, 'seed', @now),
('demo_raa_05', @tenant_id, @role_architect_id, 'stage_suggestion_update', '更新阶段建议', 1, 0, NULL, 0, 'seed', @now, 'seed', @now),
('demo_raa_06', @tenant_id, @role_qa_id, 'release_item_prepare', '准备发布事项', 1, 0, NULL, 0, 'seed', @now, 'seed', @now),
('demo_raa_07', @tenant_id, @role_product_manager_id, 'high_risk_action', '高风险动作', 1, 1, NULL, 0, 'seed', @now, 'seed', @now);

INSERT IGNORE INTO `ai_project` (
    `id`, `tenant_id`, `project_code`, `project_name`, `project_type`, `intake_summary`, `current_stage_code`,
    `status`, `risk_level`, `owner_user_id`, `workflow_template_id`, `remark`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`
) VALUES
(
    @project_alpha_id, @tenant_id, 'AIC-ALPHA', '智慧工厂运维驾驶舱', 'delivery',
    '面向制造企业的运维驾驶舱，需要整合设备告警、工单、巡检和管理驾驶舱视图。',
    'clarification', 'active', 'high', @admin_user_id, @template_delivery_id,
    '用于演示阻塞澄清、阻塞决策和外部工具上下文', 0, 'seed', @now, 'seed', @now
),
(
    @project_beta_id, @tenant_id, 'AIC-BETA', '连锁零售会员平台升级', 'delivery',
    '升级会员与营销平台，重点关注预算控制、审批和交付节奏。',
    'approval', 'active', 'medium', @admin_user_id, @template_delivery_id,
    '用于演示预算待确认和审批风险', 0, 'seed', @now, 'seed', @now
),
(
    @project_gamma_id, @tenant_id, 'AIC-GAMMA', '内部 Copilot 研发提效试点', 'internal',
    '内部试点项目，用于提升研发团队需求澄清、方案确认和会议纪要效率。',
    'development', 'active', 'low', @admin_user_id, @template_internal_id,
    '用于演示相对健康的进行中项目', 0, 'seed', @now, 'seed', @now
),
(
    @project_delta_id, @tenant_id, 'AIC-DELTA', '品牌官网重构与发布治理', 'delivery',
    '官网改版项目，重点展示设计上下文、外部工具映射、预算调整和发布审批联动。',
    'release_approval', 'active', 'high', @admin_user_id, @template_delivery_id,
    '用于演示审批、预算预警、Linear/Figma 工具集成和发布前治理', 0, 'seed', @now, 'seed', @now
);

INSERT IGNORE INTO `ai_requirement_intake` (
    `id`, `tenant_id`, `project_id`, `project_name`, `project_type`, `business_goal`, `feature_summary`,
    `reference_products`, `timeline_expectation`, `budget_range`, `technical_constraints`, `notes`,
    `attachment_placeholders`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`
) VALUES
(
    'demo_intake_alpha', @tenant_id, @project_alpha_id, '智慧工厂运维驾驶舱', 'delivery',
    '帮助制造企业把分散的设备、工单和巡检信息统一到一个运营驾驶舱。',
    '需要设备状态看板、告警追踪、工单流转、移动巡检和管理层驾驶舱。',
    'MES / EAM / 工单系统 / 竞品运维平台链接',
    '希望 10 周内上线首个版本',
    '300k-450k token',
    '优先复用现有企业账号体系；必须支持 PC 大屏和平板巡检。',
    '客户更关注高层可视化和异常追踪闭环。',
    '["alpha-prd.docx","alpha-figma-link"]',
    0, 'seed', @now, 'seed', @now
),
(
    'demo_intake_beta', @tenant_id, @project_beta_id, '连锁零售会员平台升级', 'delivery',
    '提升会员转化、营销自动化和门店活动协同效率。',
    '需要会员中心、权益配置、活动编排、门店任务联动和数据报表。',
    '某头部零售会员平台 / 企业微信营销方案',
    '希望在双十一前完成核心升级',
    '220k-320k token',
    '必须兼容现有 POS 与 CRM 接口；变更窗口有限。',
    '预算和活动节奏是主要风险点。',
    '["beta-scope.xlsx"]',
    0, 'seed', @now, 'seed', @now
),
(
    'demo_intake_gamma', @tenant_id, @project_gamma_id, '内部 Copilot 研发提效试点', 'internal',
    '在内部研发流程中验证 AI 辅助澄清、决策和纪要整理能力。',
    '需要会议总结、需求澄清建议、决策建议和轻量工具集成。',
    '现有内部研发流程 / ChatGPT / Figma / Linear',
    '希望 4 周内完成试点',
    '80k-120k token',
    '必须保持治理在主系统内，不允许 AI 直接写高影响数据。',
    '优先验证 suggestion-first 模式。',
    '["gamma-notes.md"]',
    0, 'seed', @now, 'seed', @now
),
(
    'demo_intake_delta', @tenant_id, @project_delta_id, '品牌官网重构与发布治理', 'delivery',
    '统一品牌官网视觉、内容和发布流程，并将设计与交付治理接到同一套控制台中。',
    '需要品牌首页改版、案例页模板、发布检查清单、站点上线审批和外部工具联动。',
    '企业官网 / Figma 设计稿 / Linear 交付任务',
    '希望在发布窗口前 6 周完成改版上线',
    '180k-260k token',
    '必须与现有 CMS 保持兼容；上线窗口固定；设计稿需要与 Figma 保持可追溯。',
    '该项目适合演示设计上下文、预算预警和发布审批联动。',
    '["delta-website-scope.pdf","delta-figma-link"]',
    0, 'seed', @now, 'seed', @now
);

INSERT IGNORE INTO `ai_project_stage` (
    `id`, `tenant_id`, `project_id`, `stage_code`, `stage_name`, `stage_order`, `stage_status`, `gate_status`,
    `owner_user_id`, `started_at`, `ended_at`, `remark`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`
) VALUES
('demo_stage_alpha_01', @tenant_id, @project_alpha_id, 'intake', '需求接收', 1, 'completed', 'approved', @admin_user_id, DATE_SUB(@now, INTERVAL 18 DAY), DATE_SUB(@now, INTERVAL 16 DAY), NULL, 0, 'seed', @now, 'seed', @now),
('demo_stage_alpha_02', @tenant_id, @project_alpha_id, 'clarification', '需求澄清', 2, 'active', 'pending', @admin_user_id, DATE_SUB(@now, INTERVAL 15 DAY), NULL, '存在阻塞澄清待确认', 0, 'seed', @now, 'seed', @now),
('demo_stage_alpha_03', @tenant_id, @project_alpha_id, 'feasibility', '可行性评估', 3, 'pending', 'pending', @admin_user_id, NULL, NULL, NULL, 0, 'seed', @now, 'seed', @now),
('demo_stage_beta_01', @tenant_id, @project_beta_id, 'intake', '需求接收', 1, 'completed', 'approved', @admin_user_id, DATE_SUB(@now, INTERVAL 30 DAY), DATE_SUB(@now, INTERVAL 27 DAY), NULL, 0, 'seed', @now, 'seed', @now),
('demo_stage_beta_02', @tenant_id, @project_beta_id, 'clarification', '需求澄清', 2, 'completed', 'approved', @admin_user_id, DATE_SUB(@now, INTERVAL 26 DAY), DATE_SUB(@now, INTERVAL 23 DAY), NULL, 0, 'seed', @now, 'seed', @now),
('demo_stage_beta_03', @tenant_id, @project_beta_id, 'estimation', '预算估算', 4, 'completed', 'approved', @admin_user_id, DATE_SUB(@now, INTERVAL 22 DAY), DATE_SUB(@now, INTERVAL 19 DAY), NULL, 0, 'seed', @now, 'seed', @now),
('demo_stage_beta_04', @tenant_id, @project_beta_id, 'approval', '审批', 5, 'active', 'pending', @admin_user_id, DATE_SUB(@now, INTERVAL 18 DAY), NULL, '预算与范围仍待确认', 0, 'seed', @now, 'seed', @now),
('demo_stage_gamma_01', @tenant_id, @project_gamma_id, 'intake', '需求接收', 1, 'completed', 'approved', @admin_user_id, DATE_SUB(@now, INTERVAL 20 DAY), DATE_SUB(@now, INTERVAL 18 DAY), NULL, 0, 'seed', @now, 'seed', @now),
('demo_stage_gamma_02', @tenant_id, @project_gamma_id, 'clarification', '需求澄清', 2, 'completed', 'approved', @admin_user_id, DATE_SUB(@now, INTERVAL 17 DAY), DATE_SUB(@now, INTERVAL 14 DAY), NULL, 0, 'seed', @now, 'seed', @now),
('demo_stage_gamma_03', @tenant_id, @project_gamma_id, 'planning', '计划', 4, 'completed', 'approved', @admin_user_id, DATE_SUB(@now, INTERVAL 13 DAY), DATE_SUB(@now, INTERVAL 10 DAY), NULL, 0, 'seed', @now, 'seed', @now),
('demo_stage_gamma_04', @tenant_id, @project_gamma_id, 'development', '开发', 5, 'active', 'approved', @admin_user_id, DATE_SUB(@now, INTERVAL 9 DAY), NULL, '试点能力持续迭代中', 0, 'seed', @now, 'seed', @now),
('demo_stage_delta_01', @tenant_id, @project_delta_id, 'intake', '需求接收', 1, 'completed', 'approved', @admin_user_id, DATE_SUB(@now, INTERVAL 26 DAY), DATE_SUB(@now, INTERVAL 24 DAY), NULL, 0, 'seed', @now, 'seed', @now),
('demo_stage_delta_02', @tenant_id, @project_delta_id, 'clarification', '需求澄清', 2, 'completed', 'approved', @admin_user_id, DATE_SUB(@now, INTERVAL 23 DAY), DATE_SUB(@now, INTERVAL 20 DAY), NULL, 0, 'seed', @now, 'seed', @now),
('demo_stage_delta_03', @tenant_id, @project_delta_id, 'design', '设计', 7, 'completed', 'approved', @admin_user_id, DATE_SUB(@now, INTERVAL 19 DAY), DATE_SUB(@now, INTERVAL 13 DAY), '已完成官网主视觉与组件设计稿', 0, 'seed', @now, 'seed', @now),
('demo_stage_delta_04', @tenant_id, @project_delta_id, 'development', '开发', 8, 'completed', 'approved', @admin_user_id, DATE_SUB(@now, INTERVAL 12 DAY), DATE_SUB(@now, INTERVAL 6 DAY), '开发完成并进入发布准备', 0, 'seed', @now, 'seed', @now),
('demo_stage_delta_05', @tenant_id, @project_delta_id, 'release_approval', '发布审批', 10, 'active', 'pending', @admin_user_id, DATE_SUB(@now, INTERVAL 5 DAY), NULL, '等待发布审批与预算追加确认', 0, 'seed', @now, 'seed', @now);

INSERT IGNORE INTO `ai_clarification_item` (
    `id`, `tenant_id`, `project_id`, `title`, `question`, `category`, `severity`, `suggested_options`, `user_response`,
    `status`, `generated_flag`, `promoted_decision_item_id`, `remark`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`
) VALUES
(
    'demo_clar_alpha_01', @tenant_id, @project_alpha_id, '确认设备告警范围',
    '首期版本是否需要纳入所有设备类型，还是仅覆盖关键生产线设备？',
    'feature_scope', 'blocker', '只覆盖关键生产线 / 覆盖全部设备 / 先接入核心三类设备',
    NULL, 'open', 1, 'demo_decision_alpha_01', '阻塞当前方案评估', 0, 'seed', @now, 'seed', @now
),
(
    'demo_clar_alpha_02', @tenant_id, @project_alpha_id, '确认巡检验收标准',
    '移动巡检模块上线时，验收标准是“记录可用”还是“闭环可追踪”？',
    'acceptance', 'medium', '记录可用 / 闭环可追踪 / 先上线基础记录再二期完善',
    '客户倾向于首期先上线基础记录能力。', 'awaiting_response', 0, NULL, NULL, 0, 'seed', DATE_SUB(@now, INTERVAL 2 DAY), 'seed', @now
),
(
    'demo_clar_beta_01', @tenant_id, @project_beta_id, '确认会员活动优先级',
    '营销活动编排与门店任务联动是否必须在首期同时交付？',
    'timeline', 'high', '首期同时交付 / 先交付营销活动 / 先交付门店任务联动',
    '业务方接受分期，但需要明确预算影响。', 'answered', 0, 'demo_decision_beta_01', NULL, 0, 'seed', DATE_SUB(@now, INTERVAL 5 DAY), 'seed', @now
),
(
    'demo_clar_gamma_01', @tenant_id, @project_gamma_id, '确认试点成功标准',
    '内部试点的成功标准是使用率、节省时间，还是减少需求往返次数？',
    'business_goal', 'medium', '使用率 / 时间节省 / 往返次数下降',
    '以减少需求往返次数和会议整理时间为主。', 'resolved', 1, NULL, NULL, 0, 'seed', DATE_SUB(@now, INTERVAL 8 DAY), 'seed', DATE_SUB(@now, INTERVAL 6 DAY)
),
(
    'demo_clar_delta_01', @tenant_id, @project_delta_id, '确认首期发布范围',
    '官网首期上线是否必须包含案例页模板和多语言能力，还是只上线主站改版？',
    'feature_scope', 'high', '只上线主站改版 / 主站+案例页 / 主站+案例页+多语言',
    '市场团队希望至少包含案例页模板，但多语言可后置。', 'answered', 0, 'demo_decision_delta_01', '影响发布时间窗口和预算', 0, 'seed', DATE_SUB(@now, INTERVAL 6 DAY), 'seed', @now
),
(
    'demo_clar_delta_02', @tenant_id, @project_delta_id, '确认上线回滚要求',
    '发布审批前是否必须准备完整回滚方案和内容冻结清单？',
    'risk_control', 'blocker', '完整回滚方案 + 内容冻结 / 仅技术回滚 / 仅内容冻结',
    NULL, 'open', 1, NULL, '阻塞发布审批收口', 0, 'seed', DATE_SUB(@now, INTERVAL 2 DAY), 'seed', @now
);

INSERT IGNORE INTO `ai_decision_item` (
    `id`, `tenant_id`, `project_id`, `project_stage_id`, `title`, `item_type`, `source_type`, `source_id`, `description`,
    `impact_summary`, `suggested_options`, `recommended_option`, `budget_impact_summary`, `project_impact_summary`,
    `blocker_flag`, `priority`, `status`, `requested_by_user_id`, `assignee_user_id`, `due_at`, `remark`,
    `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`
) VALUES
(
    'demo_decision_alpha_01', @tenant_id, @project_alpha_id, 'demo_stage_alpha_02', '确认设备接入范围',
    'requirement_confirmation', 'clarification', 'demo_clar_alpha_01',
    '需要确认首期接入设备类型和边界，否则无法冻结方案与预算。',
    '接入范围直接影响集成工作量和首期交付边界。',
    '只覆盖关键生产线 / 核心三类设备优先 / 全量纳入',
    '核心三类设备优先',
    '若扩大到全量设备，预算需要增加约 20%。',
    '若不冻结范围，当前可行性评估和预算无法收敛。',
    1, 'high', 'open', @admin_user_id, @admin_user_id, DATE_ADD(@now, INTERVAL 2 DAY), '阻塞性决策', 0, 'seed', @now, 'seed', @now
),
(
    'demo_decision_beta_01', @tenant_id, @project_beta_id, 'demo_stage_beta_04', '确认会员活动首期交付边界',
    'budget_change', 'clarification', 'demo_clar_beta_01',
    '需要在预算约束下决定首期交付范围和排期窗口。',
    '范围不同会影响活动引擎复杂度与预算释放节奏。',
    '首期同时交付 / 首期只交付营销活动 / 首期只交付门店联动',
    '首期只交付营销活动',
    '若首期同时交付，预算需追加 60k token。',
    '如不拆分范围，审批周期可能延长并影响双十一节点。',
    0, 'medium', 'pending_approval', @admin_user_id, @admin_user_id, DATE_ADD(@now, INTERVAL 4 DAY), '等待预算相关审批', 0, 'seed', DATE_SUB(@now, INTERVAL 4 DAY), 'seed', @now
),
(
    'demo_decision_gamma_01', @tenant_id, @project_gamma_id, 'demo_stage_gamma_04', '确认试点推广范围',
    'solution_direction', 'manual', NULL,
    '确认内部试点完成后是否扩展到更多研发团队。',
    '推广范围影响后续支持和集成优先级。',
    '仅研发一组 / 扩展到全研发中心 / 按季度逐步扩大',
    '按季度逐步扩大',
    '当前预算内可支持研发一组和一条试点流程。',
    '渐进推广更利于验证治理与协作模式。',
    0, 'low', 'confirmed', @admin_user_id, @admin_user_id, DATE_SUB(@now, INTERVAL 3 DAY), NULL, 0, 'seed', DATE_SUB(@now, INTERVAL 7 DAY), 'seed', DATE_SUB(@now, INTERVAL 3 DAY)
),
(
    'demo_decision_delta_01', @tenant_id, @project_delta_id, 'demo_stage_delta_05', '确认官网首期发布边界',
    'release_scope', 'clarification', 'demo_clar_delta_01',
    '需要在固定发布窗口前确认官网首期是否包含案例页模板，以便冻结设计与上线计划。',
    '若将案例页模板纳入首期，将增加设计联调和内容配置工作量。',
    '只上线主站改版 / 主站+案例页 / 主站+案例页+多语言',
    '主站+案例页',
    '纳入案例页模板预计增加约 25k token；多语言则需增加约 55k token。',
    '若继续摇摆范围，发布审批和内容冻结都会延后。',
    1, 'high', 'pending_approval', @admin_user_id, @admin_user_id, DATE_ADD(@now, INTERVAL 1 DAY), '发布前关键范围决策', 0, 'seed', DATE_SUB(@now, INTERVAL 4 DAY), 'seed', @now
);

INSERT IGNORE INTO `ai_decision_action_log` (
    `id`, `tenant_id`, `decision_item_id`, `action_type`, `action_comment`, `previous_status`, `next_status`,
    `operator_user_id`, `operated_at`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`
) VALUES
('demo_decision_log_01', @tenant_id, 'demo_decision_alpha_01', 'promoted', '由阻塞澄清项提升为决策事项', NULL, 'open', @admin_user_id, DATE_SUB(@now, INTERVAL 1 DAY), 0, 'seed', @now, 'seed', @now),
('demo_decision_log_02', @tenant_id, 'demo_decision_gamma_01', 'confirm', '确认先按季度逐步扩大试点', 'open', 'confirmed', @admin_user_id, DATE_SUB(@now, INTERVAL 3 DAY), 0, 'seed', @now, 'seed', @now),
('demo_decision_log_03', @tenant_id, 'demo_decision_delta_01', 'promoted', '由发布范围澄清项提升为决策事项', NULL, 'pending_approval', @admin_user_id, DATE_SUB(@now, INTERVAL 4 DAY), 0, 'seed', @now, 'seed', @now);

INSERT IGNORE INTO `ai_approval_record` (
    `id`, `tenant_id`, `project_id`, `title`, `decision_item_id`, `approval_type`, `source_object_type`, `source_object_id`,
    `requester_user_id`, `approver_user_id`, `description`, `risk_summary`, `budget_impact_summary`, `recommended_action`,
    `blocker_flag`, `approval_status`, `operator_user_id`, `submitted_at`, `decided_at`, `decision_note`, `remark`,
    `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`
) VALUES
(
    'demo_approval_alpha_01', @tenant_id, @project_alpha_id, '设备接入范围关卡审批', 'demo_decision_alpha_01',
    'gate', 'project_stage', 'demo_stage_alpha_02',
    @admin_user_id, @admin_user_id,
    '确认首期设备接入范围，才能冻结集成方案和预算。',
    '范围不清会导致预算和排期持续漂移。',
    '扩大范围将增加预算并延长交付周期。',
    '先确认核心三类设备范围',
    1, 'submitted', @admin_user_id, DATE_SUB(@now, INTERVAL 1 DAY), NULL, NULL, '阻塞审批', 0, 'seed', @now, 'seed', @now
),
(
    'demo_approval_beta_01', @tenant_id, @project_beta_id, '预算追加确认', 'demo_decision_beta_01',
    'budget_change', 'decision_item', 'demo_decision_beta_01',
    @admin_user_id, @admin_user_id,
    '零售会员平台若首期覆盖更多范围，需要额外预算确认。',
    '节点压力较大，预算确认会影响方案边界。',
    '需要确认是否接受额外 60k token 预算。',
    '建议先批准首期缩减范围方案',
    0, 'request_changes', @admin_user_id, DATE_SUB(@now, INTERVAL 3 DAY), NULL, '请补充分期影响说明', '预算审批仍在往返', 0, 'seed', DATE_SUB(@now, INTERVAL 3 DAY), 'seed', @now
),
(
    'demo_approval_gamma_01', @tenant_id, @project_gamma_id, '内部试点发布确认', 'demo_decision_gamma_01',
    'release', 'project', @project_gamma_id,
    @admin_user_id, @admin_user_id,
    '内部试点准备向研发一组发布，需要最终确认范围与推广策略。',
    '影响范围可控，风险较低。',
    '当前预算内可完成试点发布。',
    '批准按季度扩大策略',
    0, 'approved', @admin_user_id, DATE_SUB(@now, INTERVAL 6 DAY), DATE_SUB(@now, INTERVAL 5 DAY), '同意进入试点发布', NULL, 0, 'seed', DATE_SUB(@now, INTERVAL 6 DAY), 'seed', DATE_SUB(@now, INTERVAL 5 DAY)
),
(
    'demo_approval_delta_01', @tenant_id, @project_delta_id, '官网发布审批', 'demo_decision_delta_01',
    'release', 'project_stage', 'demo_stage_delta_05',
    @admin_user_id, @admin_user_id,
    '官网发布前需要确认首期范围、回滚方案和内容冻结清单。',
    '若范围继续扩大或回滚方案不完整，将影响上线窗口并增加发布风险。',
    '若纳入案例页模板，预算需额外确认 25k token。',
    '建议确认主站+案例页范围，并附带回滚检查清单后批准',
    1, 'submitted', @admin_user_id, DATE_SUB(@now, INTERVAL 2 DAY), NULL, NULL, '发布审批待处理', 0, 'seed', DATE_SUB(@now, INTERVAL 2 DAY), 'seed', @now
),
(
    'demo_approval_delta_02', @tenant_id, @project_delta_id, '官网预算追加审批', 'demo_decision_delta_01',
    'budget_change', 'budget_plan', 'demo_budget_plan_delta',
    @admin_user_id, @admin_user_id,
    '由于纳入案例页模板，需追加预算并确认是否保持原发布时间。',
    '预算追加若不确认，可能导致设计联调资源不足。',
    '需要确认额外 25k token 是否批准。',
    '建议批准预算追加并保留原发布时间',
    0, 'submitted', @admin_user_id, DATE_SUB(@now, INTERVAL 1 DAY), NULL, NULL, '预算审批待处理', 0, 'seed', DATE_SUB(@now, INTERVAL 1 DAY), 'seed', @now
);

INSERT IGNORE INTO `ai_approval_action_log` (
    `id`, `tenant_id`, `approval_record_id`, `action_type`, `action_comment`, `previous_status`, `next_status`,
    `operator_user_id`, `operated_at`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`
) VALUES
('demo_approval_log_01', @tenant_id, 'demo_approval_alpha_01', 'created', '已发起关卡审批', NULL, 'submitted', @admin_user_id, DATE_SUB(@now, INTERVAL 1 DAY), 0, 'seed', @now, 'seed', @now),
('demo_approval_log_02', @tenant_id, 'demo_approval_beta_01', 'request_changes', '请补充分期预算影响', 'submitted', 'request_changes', @admin_user_id, DATE_SUB(@now, INTERVAL 2 DAY), 0, 'seed', @now, 'seed', @now),
('demo_approval_log_03', @tenant_id, 'demo_approval_gamma_01', 'approve', '批准内部试点发布', 'submitted', 'approved', @admin_user_id, DATE_SUB(@now, INTERVAL 5 DAY), 0, 'seed', @now, 'seed', @now),
('demo_approval_log_04', @tenant_id, 'demo_approval_delta_01', 'created', '已发起官网发布审批', NULL, 'submitted', @admin_user_id, DATE_SUB(@now, INTERVAL 2 DAY), 0, 'seed', @now, 'seed', @now),
('demo_approval_log_05', @tenant_id, 'demo_approval_delta_02', 'created', '已发起官网预算追加审批', NULL, 'submitted', @admin_user_id, DATE_SUB(@now, INTERVAL 1 DAY), 0, 'seed', @now, 'seed', @now);

INSERT IGNORE INTO `ai_budget_plan` (
    `id`, `tenant_id`, `project_id`, `plan_name`, `currency_code`, `proposed_amount`, `approved_amount`, `reserved_amount`,
    `consumed_amount`, `role_allocations_json`, `status`, `effective_at`, `remark`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`
) VALUES
(
    'demo_budget_plan_alpha', @tenant_id, @project_alpha_id, '首期交付预算', 'TOKEN', 360000, 340000, 260000,
    295000,
    '[{"roleCode":"product_analysis","amount":50000},{"roleCode":"architect","amount":60000},{"roleCode":"frontend","amount":80000},{"roleCode":"backend","amount":110000},{"roleCode":"qa","amount":40000}]',
    'approved', DATE_SUB(@now, INTERVAL 10 DAY), '预算接近预警线', 0, 'seed', @now, 'seed', @now
),
(
    'demo_budget_plan_beta', @tenant_id, @project_beta_id, '首期升级预算', 'TOKEN', 280000, 220000, 150000,
    175000,
    '[{"roleCode":"product_analysis","amount":40000},{"roleCode":"architect","amount":30000},{"roleCode":"frontend","amount":60000},{"roleCode":"backend","amount":70000},{"roleCode":"qa","amount":20000}]',
    'pending_approval', DATE_SUB(@now, INTERVAL 12 DAY), '预算变更待确认', 0, 'seed', @now, 'seed', @now
),
(
    'demo_budget_plan_gamma', @tenant_id, @project_gamma_id, '试点预算', 'TOKEN', 120000, 120000, 50000,
    42000,
    '[{"roleCode":"product_analysis","amount":20000},{"roleCode":"architect","amount":20000},{"roleCode":"backend","amount":40000},{"roleCode":"qa","amount":20000},{"roleCode":"project_coordination","amount":20000}]',
    'approved', DATE_SUB(@now, INTERVAL 8 DAY), '预算健康', 0, 'seed', @now, 'seed', @now
),
(
    'demo_budget_plan_delta', @tenant_id, @project_delta_id, '官网重构预算', 'TOKEN', 235000, 210000, 165000,
    198000,
    '[{"roleCode":"product_analysis","amount":25000},{"roleCode":"architect","amount":25000},{"roleCode":"ui_ux","amount":45000},{"roleCode":"frontend","amount":55000},{"roleCode":"backend","amount":35000},{"roleCode":"qa","amount":25000}]',
    'pending_approval', DATE_SUB(@now, INTERVAL 14 DAY), '预算接近超限，等待追加确认', 0, 'seed', @now, 'seed', @now
);

INSERT IGNORE INTO `ai_budget_ledger` (
    `id`, `tenant_id`, `project_id`, `budget_plan_id`, `entry_type`, `amount`, `balance_after`, `reference_type`,
    `reference_id`, `occurred_at`, `description`, `remark`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`
) VALUES
('demo_budget_ledger_alpha_01', @tenant_id, @project_alpha_id, 'demo_budget_plan_alpha', 'create', 340000, 340000, 'budget_plan', 'demo_budget_plan_alpha', DATE_SUB(@now, INTERVAL 10 DAY), '初始预算创建', NULL, 0, 'seed', @now, 'seed', @now),
('demo_budget_ledger_alpha_02', @tenant_id, @project_alpha_id, 'demo_budget_plan_alpha', 'lock', -260000, 80000, 'project_stage', 'demo_stage_alpha_02', DATE_SUB(@now, INTERVAL 6 DAY), '锁定首期执行预算', NULL, 0, 'seed', @now, 'seed', @now),
('demo_budget_ledger_alpha_03', @tenant_id, @project_alpha_id, 'demo_budget_plan_alpha', 'consume', -295000, 45000, 'decision_item', 'demo_decision_alpha_01', DATE_SUB(@now, INTERVAL 1 DAY), '接入范围未锁定导致评估消耗增加', NULL, 0, 'seed', @now, 'seed', @now),
('demo_budget_ledger_beta_01', @tenant_id, @project_beta_id, 'demo_budget_plan_beta', 'create', 220000, 220000, 'budget_plan', 'demo_budget_plan_beta', DATE_SUB(@now, INTERVAL 12 DAY), '初始预算创建', NULL, 0, 'seed', @now, 'seed', @now),
('demo_budget_ledger_beta_02', @tenant_id, @project_beta_id, 'demo_budget_plan_beta', 'pending_increase', 60000, 280000, 'decision_item', 'demo_decision_beta_01', DATE_SUB(@now, INTERVAL 3 DAY), '待确认的预算追加建议', NULL, 0, 'seed', @now, 'seed', @now),
('demo_budget_ledger_beta_03', @tenant_id, @project_beta_id, 'demo_budget_plan_beta', 'consume', -175000, 45000, 'project_stage', 'demo_stage_beta_04', DATE_SUB(@now, INTERVAL 1 DAY), '审批阶段消耗', NULL, 0, 'seed', @now, 'seed', @now),
('demo_budget_ledger_gamma_01', @tenant_id, @project_gamma_id, 'demo_budget_plan_gamma', 'create', 120000, 120000, 'budget_plan', 'demo_budget_plan_gamma', DATE_SUB(@now, INTERVAL 8 DAY), '试点预算创建', NULL, 0, 'seed', @now, 'seed', @now),
('demo_budget_ledger_gamma_02', @tenant_id, @project_gamma_id, 'demo_budget_plan_gamma', 'consume', -42000, 78000, 'project_stage', 'demo_stage_gamma_04', DATE_SUB(@now, INTERVAL 1 DAY), '试点开发消耗', NULL, 0, 'seed', @now, 'seed', @now),
('demo_budget_ledger_delta_01', @tenant_id, @project_delta_id, 'demo_budget_plan_delta', 'create', 210000, 210000, 'budget_plan', 'demo_budget_plan_delta', DATE_SUB(@now, INTERVAL 14 DAY), '官网改版初始预算创建', NULL, 0, 'seed', @now, 'seed', @now),
('demo_budget_ledger_delta_02', @tenant_id, @project_delta_id, 'demo_budget_plan_delta', 'lock', -165000, 45000, 'project_stage', 'demo_stage_delta_04', DATE_SUB(@now, INTERVAL 7 DAY), '锁定设计与开发执行预算', NULL, 0, 'seed', @now, 'seed', @now),
('demo_budget_ledger_delta_03', @tenant_id, @project_delta_id, 'demo_budget_plan_delta', 'pending_increase', 25000, 70000, 'decision_item', 'demo_decision_delta_01', DATE_SUB(@now, INTERVAL 1 DAY), '案例页模板纳入首期后的预算追加建议', NULL, 0, 'seed', @now, 'seed', @now),
('demo_budget_ledger_delta_04', @tenant_id, @project_delta_id, 'demo_budget_plan_delta', 'manual_correction', -8000, 62000, 'approval_record', 'demo_approval_delta_02', DATE_SUB(@now, INTERVAL 12 HOUR), '修正外包切图成本估算', NULL, 0, 'seed', @now, 'seed', @now);

INSERT IGNORE INTO `ai_meeting_record` (
    `id`, `tenant_id`, `project_id`, `meeting_title`, `raw_notes`, `summary`, `action_items_json`, `open_questions_json`,
    `decision_candidates_json`, `source_type`, `source_object_id`, `generated_by`, `generated_at`, `remark`,
    `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`
) VALUES
(
    'demo_meeting_alpha_01', @tenant_id, @project_alpha_id, '设备接入范围评审会',
    '讨论设备种类优先级、数据采集频率和首期展示范围。',
    '会议明确需要先冻结核心三类设备的接入范围，再推进预算和方案细化。',
    '["冻结核心三类设备范围","补充设备数据接入说明"]',
    '["二期是否纳入更多设备类型？"]',
    '["是否确认首期只接入核心三类设备"]',
    'meeting_note', NULL, @admin_user_id, DATE_SUB(@now, INTERVAL 1 DAY), NULL, 0, 'seed', @now, 'seed', @now
),
(
    'demo_meeting_gamma_01', @tenant_id, @project_gamma_id, '内部试点周会',
    '讨论本周试点效果、会议纪要自动化和下周推广范围。',
    '试点效果良好，建议继续在研发一组内迭代，并准备季度扩面方案。',
    '["补充一组试点反馈","整理季度推广条件"]',
    '["推广到第二个团队的触发条件是什么？"]',
    '["是否按季度扩大试点范围"]',
    'meeting_note', NULL, @admin_user_id, DATE_SUB(@now, INTERVAL 2 DAY), NULL, 0, 'seed', @now, 'seed', @now
),
(
    'demo_meeting_delta_01', @tenant_id, @project_delta_id, '官网发布前检查会',
    '讨论案例页模板是否纳入首期、Figma 设计稿锁版、CMS 发布窗口和回滚策略。',
    '会议结论是建议首期纳入案例页模板，但必须同步完成发布回滚方案和预算追加审批。',
    '["补充回滚方案清单","确认案例页模板上线内容","完成预算追加审批"]',
    '["是否允许案例页模板进入首期发布？","预算追加是否影响发布时间？"]',
    '["是否批准官网首期范围调整"]',
    'meeting_note', NULL, @admin_user_id, DATE_SUB(@now, INTERVAL 1 DAY), NULL, 0, 'seed', @now, 'seed', @now
);

INSERT IGNORE INTO `ai_project_tool_binding` (
    `id`, `tenant_id`, `project_id`, `tool_type`, `binding_type`, `external_id`, `external_key`, `external_name`,
    `external_url`, `binding_status`, `default_flag`, `metadata_json`, `remark`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`
) VALUES
(
    'demo_binding_alpha_linear', @tenant_id, @project_alpha_id, 'linear', 'linear_primary_issue',
    'LIN-101', 'LIN-101', '智慧工厂运维驾驶舱', 'https://linear.app/demo/issue/LIN-101',
    'linked', 1, '{"teamId":"demo-team","mode":"issue"}', 'Demo Linear 主工作项', 0, 'seed', @now, 'seed', @now
),
(
    'demo_binding_alpha_figma', @tenant_id, @project_alpha_id, 'figma', 'figma_file',
    'fig_demo_alpha', 'fig_demo_alpha', '工厂运维驾驶舱设计稿', 'https://www.figma.com/file/fig_demo_alpha/demo',
    'linked', 1, '{"fileKey":"fig_demo_alpha"}', 'Demo Figma 文件绑定', 0, 'seed', @now, 'seed', @now
),
(
    'demo_binding_delta_linear', @tenant_id, @project_delta_id, 'linear', 'linear_primary_issue',
    'LIN-245', 'LIN-245', '品牌官网重构与发布治理', 'https://linear.app/demo/issue/LIN-245',
    'linked', 1, '{"teamId":"demo-marketing","mode":"issue"}', 'Demo Linear 官网项目主工作项', 0, 'seed', @now, 'seed', @now
),
(
    'demo_binding_delta_figma', @tenant_id, @project_delta_id, 'figma', 'figma_file',
    'fig_demo_delta', 'fig_demo_delta', '官网重构设计稿', 'https://www.figma.com/file/fig_demo_delta/demo',
    'linked', 1, '{"fileKey":"fig_demo_delta","nodeId":"12:34"}', 'Demo 官网 Figma 设计稿绑定', 0, 'seed', @now, 'seed', @now
);

INSERT IGNORE INTO `ai_tool_integration_audit` (
    `id`, `tenant_id`, `project_id`, `tool_type`, `action_type`, `source_object_type`, `source_object_id`,
    `binding_id`, `preview_flag`, `confirmed_flag`, `audit_status`, `request_payload_json`, `response_payload_json`,
    `external_object_id`, `external_object_url`, `operator_user_id`, `error_message`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`
) VALUES
(
    'demo_tool_audit_alpha_01', @tenant_id, @project_alpha_id, 'linear', 'linear_project_apply', 'project', @project_alpha_id,
    'demo_binding_alpha_linear', 0, 1, 'succeeded',
    '{"mode":"issue"}', '{"identifier":"LIN-101"}',
    'LIN-101', 'https://linear.app/demo/issue/LIN-101', @admin_user_id, NULL, 0, 'seed', DATE_SUB(@now, INTERVAL 3 DAY), 'seed', DATE_SUB(@now, INTERVAL 3 DAY)
),
(
    'demo_tool_audit_alpha_02', @tenant_id, @project_alpha_id, 'figma', 'figma_context_apply', 'project', @project_alpha_id,
    'demo_binding_alpha_figma', 0, 1, 'succeeded',
    '{"fileKey":"fig_demo_alpha"}', '{"bindingType":"figma_file"}',
    'fig_demo_alpha', 'https://www.figma.com/file/fig_demo_alpha/demo', @admin_user_id, NULL, 0, 'seed', DATE_SUB(@now, INTERVAL 2 DAY), 'seed', DATE_SUB(@now, INTERVAL 2 DAY)
),
(
    'demo_tool_audit_delta_01', @tenant_id, @project_delta_id, 'linear', 'linear_project_apply', 'project', @project_delta_id,
    'demo_binding_delta_linear', 0, 1, 'succeeded',
    '{"mode":"issue","title":"品牌官网重构与发布治理"}', '{"identifier":"LIN-245"}',
    'LIN-245', 'https://linear.app/demo/issue/LIN-245', @admin_user_id, NULL, 0, 'seed', DATE_SUB(@now, INTERVAL 5 DAY), 'seed', DATE_SUB(@now, INTERVAL 5 DAY)
),
(
    'demo_tool_audit_delta_02', @tenant_id, @project_delta_id, 'figma', 'figma_context_apply', 'project', @project_delta_id,
    'demo_binding_delta_figma', 0, 1, 'succeeded',
    '{"fileKey":"fig_demo_delta","nodeId":"12:34"}', '{"bindingType":"figma_file","nodeName":"Home Hero"}',
    'fig_demo_delta', 'https://www.figma.com/file/fig_demo_delta/demo', @admin_user_id, NULL, 0, 'seed', DATE_SUB(@now, INTERVAL 4 DAY), 'seed', DATE_SUB(@now, INTERVAL 4 DAY)
),
(
    'demo_tool_audit_delta_03', @tenant_id, @project_delta_id, 'linear', 'linear_comment_apply', 'decision_item', 'demo_decision_delta_01',
    'demo_binding_delta_linear', 0, 1, 'succeeded',
    '{"mode":"comment","body":"首期范围建议调整为主站+案例页"}', '{"commentId":"lin-comment-901"}',
    'lin-comment-901', 'https://linear.app/demo/issue/LIN-245', @admin_user_id, NULL, 0, 'seed', DATE_SUB(@now, INTERVAL 1 DAY), 'seed', DATE_SUB(@now, INTERVAL 1 DAY)
);

INSERT IGNORE INTO `ai_project_governance_state` (
    `id`, `tenant_id`, `project_id`, `current_stage_code`, `current_stage_name`, `current_stage_status`, `current_gate_status`,
    `governance_status`, `blocked_flag`, `at_risk_flag`, `pending_decision_count`, `blocker_decision_count`,
    `pending_approval_count`, `blocker_approval_count`, `clarification_count`, `blocker_clarification_count`,
    `failed_gate_condition_count`, `blocking_gate_condition_count`, `missing_critical_role_count`, `budget_status`,
    `blocker_reason_summary`, `last_recomputed_at`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`
) VALUES
(
    'demo_governance_alpha', @tenant_id, @project_alpha_id, 'clarification', '需求澄清', 'active', 'pending',
    'blocked', 1, 0, 1, 1, 1, 1, 2, 1, 1, 1, 0, 'warning',
    '阻塞澄清项 1 / 阻塞决策 1 / 阻塞审批 1', @now, 0, 'seed', @now, 'seed', @now
),
(
    'demo_governance_beta', @tenant_id, @project_beta_id, 'approval', '审批', 'active', 'pending',
    'at_risk', 0, 1, 1, 0, 1, 0, 1, 0, 0, 0, 0, 'pending',
    '待处理决策 1 / 待处理审批 1 / 预算待确认', @now, 0, 'seed', @now, 'seed', @now
),
(
    'demo_governance_gamma', @tenant_id, @project_gamma_id, 'development', '开发', 'active', 'approved',
    'healthy', 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 'healthy',
    '治理状态正常', @now, 0, 'seed', @now, 'seed', @now
),
(
    'demo_governance_delta', @tenant_id, @project_delta_id, 'release_approval', '发布审批', 'active', 'pending',
    'blocked', 1, 1, 1, 1, 2, 1, 2, 1, 1, 1, 0, 'warning',
    '发布阻塞澄清 1 / 阻塞决策 1 / 待处理审批 2 / 预算预警', @now, 0, 'seed', @now, 'seed', @now
);
