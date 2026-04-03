import { Tag } from 'antd';

export const projectTypeOptions = [
  { label: '交付项目', value: 'delivery' },
  { label: '咨询项目', value: 'consulting' },
  { label: '内部项目', value: 'internal' },
];

export const projectStatusOptions = [
  { label: '草稿', value: 'draft' },
  { label: '进行中', value: 'active' },
  { label: '已暂停', value: 'paused' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' },
];

export const lifecycleStageOptions = [
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

export const riskLevelOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '关键', value: 'critical' },
];

export const governanceStatusOptions = [
  { label: '正常', value: 'healthy' },
  { label: '风险', value: 'at_risk' },
  { label: '阻塞', value: 'blocked' },
];

export const nextStepPriorityOptions = [
  { label: '紧急', value: 'urgent' },
  { label: '高优先级', value: 'high' },
  { label: '常规', value: 'normal' },
];

export const budgetStatusOptions = [
  { label: '未规划', value: 'unplanned' },
  { label: '待审批', value: 'pending' },
  { label: '正常', value: 'healthy' },
  { label: '预警', value: 'warning' },
  { label: '超支', value: 'overrun' },
];

export const budgetPlanStatusOptions = [
  { label: '草稿', value: 'draft' },
  { label: '待审批', value: 'pending_approval' },
  { label: '已批准', value: 'approved' },
  { label: '已关闭', value: 'closed' },
];

export const budgetLedgerEntryTypeOptions = [
  { label: '预算创建', value: 'create' },
  { label: '预算锁定', value: 'lock' },
  { label: '待增补预算', value: 'pending_increase' },
  { label: '人工修正', value: 'manual_correction' },
  { label: '预算预留', value: 'reserve' },
  { label: '预算消耗', value: 'consume' },
  { label: '预算调整', value: 'adjust' },
  { label: '预算回退', value: 'refund' },
];

export const budgetRoleOptions = [
  { label: '产品 / 分析', value: 'product_analysis' },
  { label: '架构', value: 'architect' },
  { label: 'UI / UX', value: 'ui_ux' },
  { label: '前端', value: 'frontend' },
  { label: '后端', value: 'backend' },
  { label: 'QA', value: 'qa' },
  { label: 'DevOps', value: 'devops' },
  { label: '项目协同', value: 'project_coordination' },
];

export const gateStatusOptions = [
  { label: '无需门禁', value: 'not_required' },
  { label: '待确认', value: 'pending' },
  { label: '已通过', value: 'approved' },
  { label: '已拒绝', value: 'rejected' },
];

export const stageStatusOptions = [
  { label: '待开始', value: 'pending' },
  { label: '进行中', value: 'active' },
  { label: '已完成', value: 'completed' },
  { label: '已阻塞', value: 'blocked' },
];

export const decisionStatusOptions = [
  { label: '待确认', value: 'open' },
  { label: '待审批', value: 'pending_approval' },
  { label: '已确认', value: 'confirmed' },
  { label: '已拒绝', value: 'rejected' },
  { label: '已暂缓', value: 'deferred' },
  { label: '已解决', value: 'resolved' },
];

export const decisionTypeOptions = [
  { label: '澄清事项', value: 'clarification' },
  { label: '需求确认', value: 'requirement_confirmation' },
  { label: '方案确认', value: 'solution_direction' },
  { label: '排期确认', value: 'timeline_confirmation' },
  { label: '范围变更', value: 'scope_change' },
  { label: '预算变更', value: 'budget_change' },
  { label: '发布决策', value: 'release' },
];

export const decisionSourceOptions = [
  { label: '人工创建', value: 'manual' },
  { label: '来自澄清项', value: 'clarification' },
  { label: '来自需求接收', value: 'intake' },
  { label: '来自项目中心', value: 'project_center' },
];

export const approvalStatusOptions = [
  { label: '草稿', value: 'draft' },
  { label: '已提交', value: 'submitted' },
  { label: '要求修改', value: 'request_changes' },
  { label: '已暂缓', value: 'deferred' },
  { label: '已批准', value: 'approved' },
  { label: '已拒绝', value: 'rejected' },
  { label: '已取消', value: 'cancelled' },
];

export const approvalTypeOptions = [
  { label: '需求审批', value: 'requirement' },
  { label: '决策审批', value: 'decision' },
  { label: '预算审批', value: 'budget' },
  { label: '预算变更审批', value: 'budget_change' },
  { label: '风险操作审批', value: 'risk_operation' },
  { label: '关卡审批', value: 'gate' },
  { label: '发布审批', value: 'release' },
];

export const approvalSourceObjectTypeOptions = [
  { label: '人工发起', value: 'manual' },
  { label: '决策事项', value: 'decision_item' },
  { label: '预算计划', value: 'budget_plan' },
  { label: '预算流水', value: 'budget_ledger' },
  { label: '项目', value: 'project' },
  { label: '项目阶段', value: 'project_stage' },
];

export const workflowTemplateStatusOptions = [
  { label: '草稿', value: 'draft' },
  { label: '启用', value: 'active' },
  { label: '归档', value: 'archived' },
];

export const agentRoleTypeOptions = [
  { label: '治理角色', value: 'governance' },
  { label: '交付角色', value: 'delivery' },
  { label: '支持角色', value: 'support' },
];

export const agentRoleStatusOptions = [
  { label: '启用', value: 'active' },
  { label: '停用', value: 'inactive' },
];

export const agentStageParticipationOptions = [
  { label: '必需', value: 'required' },
  { label: '可选', value: 'optional' },
  { label: '不参与', value: 'not_involved' },
];

export const agentAllowedActionOptions = [
  { label: '创建/更新澄清项', value: 'clarification_manage' },
  { label: '创建决策事项', value: 'decision_create' },
  { label: '提出预算变更', value: 'budget_change_propose' },
  { label: '发起审批请求', value: 'approval_request' },
  { label: '更新阶段建议', value: 'stage_suggestion_update' },
  { label: '准备发布事项', value: 'release_item_prepare' },
  { label: '高风险动作', value: 'high_risk_action' },
];

export const clarificationCategoryOptions = [
  { label: '业务目标', value: 'business_goal' },
  { label: '功能范围', value: 'feature_scope' },
  { label: '参考产品', value: 'reference_benchmark' },
  { label: '时间预期', value: 'timeline' },
  { label: '预算约束', value: 'budget' },
  { label: '技术约束', value: 'technical_constraint' },
  { label: '集成依赖', value: 'integration' },
  { label: '验收标准', value: 'acceptance' },
];

export const clarificationSeverityOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '阻塞', value: 'blocker' },
];

export const clarificationStatusOptions = [
  { label: '待澄清', value: 'open' },
  { label: '待用户回复', value: 'awaiting_response' },
  { label: '已回复待整理', value: 'answered' },
  { label: '已解决', value: 'resolved' },
];

export const meetingSourceTypeOptions = [
  { label: '项目中心', value: 'project_center' },
  { label: '需求接收', value: 'requirement_intake' },
  { label: '澄清中心', value: 'clarification_center' },
  { label: '人工笔记', value: 'manual_notes' },
];

export const toolTypeOptions = [
  { label: 'Linear', value: 'linear' },
  { label: 'Figma', value: 'figma' },
];

export const toolBindingTypeOptions = [
  { label: 'Linear 主工作项', value: 'linear_primary_issue' },
  { label: 'Figma 文件', value: 'figma_file' },
  { label: 'Figma 节点', value: 'figma_node' },
];

export const toolBindingStatusOptions = [
  { label: '已关联', value: 'linked' },
  { label: '仅预览', value: 'preview_only' },
  { label: '异常', value: 'error' },
];

export const toolIntegrationActionOptions = [
  { label: 'Linear 项目映射预览', value: 'linear_project_preview' },
  { label: 'Linear 项目映射写入', value: 'linear_project_apply' },
  { label: 'Linear Issue 预览', value: 'linear_issue_preview' },
  { label: 'Linear Issue 写入', value: 'linear_issue_apply' },
  { label: 'Linear Comment 预览', value: 'linear_comment_preview' },
  { label: 'Linear Comment 写入', value: 'linear_comment_apply' },
  { label: 'Figma 上下文预览', value: 'figma_context_preview' },
  { label: 'Figma 上下文关联', value: 'figma_context_apply' },
];

export const toolIntegrationAuditStatusOptions = [
  { label: '已预览', value: 'previewed' },
  { label: '成功', value: 'succeeded' },
  { label: '失败', value: 'failed' },
];

export const getOptionLabel = (
  options: Array<{ label: string; value: string }>,
  value?: string | null,
) => options.find((item) => item.value === value)?.label || value || '-';

export const formatBudgetAmount = (
  value?: number | null,
  currency = 'TOKEN',
) => `${value ?? 0} ${currency}`;

export const renderProjectStatusTag = (value?: string | null) => {
  const colorMap: Record<string, string> = {
    draft: 'default',
    active: 'processing',
    paused: 'warning',
    completed: 'success',
    cancelled: 'error',
  };
  return <Tag color={colorMap[value || ''] || 'default'}>{getOptionLabel(projectStatusOptions, value)}</Tag>;
};

export const renderRiskLevelTag = (value?: string | null) => {
  const colorMap: Record<string, string> = {
    low: 'green',
    medium: 'gold',
    high: 'orange',
    critical: 'red',
  };
  return <Tag color={colorMap[value || ''] || 'default'}>{getOptionLabel(riskLevelOptions, value)}</Tag>;
};

export const renderGovernanceStatusTag = (value?: string | null) => {
  const colorMap: Record<string, string> = {
    healthy: 'success',
    at_risk: 'warning',
    blocked: 'error',
  };
  return (
    <Tag color={colorMap[value || ''] || 'default'}>
      {getOptionLabel(governanceStatusOptions, value)}
    </Tag>
  );
};

export const renderNextStepPriorityTag = (value?: string | null) => {
  const colorMap: Record<string, string> = {
    urgent: 'red',
    high: 'orange',
    normal: 'blue',
  };
  return (
    <Tag color={colorMap[value || ''] || 'default'}>
      {getOptionLabel(nextStepPriorityOptions, value)}
    </Tag>
  );
};

export const renderBudgetStatusTag = (value?: string | null) => {
  const colorMap: Record<string, string> = {
    unplanned: 'default',
    pending: 'warning',
    healthy: 'success',
    warning: 'gold',
    overrun: 'error',
  };
  return <Tag color={colorMap[value || ''] || 'default'}>{getOptionLabel(budgetStatusOptions, value)}</Tag>;
};

export const renderBudgetLedgerEntryTag = (value?: string | null) => {
  const colorMap: Record<string, string> = {
    create: 'blue',
    lock: 'gold',
    pending_increase: 'orange',
    manual_correction: 'purple',
    reserve: 'gold',
    consume: 'red',
    adjust: 'processing',
    refund: 'green',
  };
  return (
    <Tag color={colorMap[value || ''] || 'default'}>
      {getOptionLabel(budgetLedgerEntryTypeOptions, value)}
    </Tag>
  );
};

export const renderToolTypeTag = (value?: string | null) => {
  const colorMap: Record<string, string> = {
    linear: 'cyan',
    figma: 'magenta',
  };
  return <Tag color={colorMap[value || ''] || 'default'}>{getOptionLabel(toolTypeOptions, value)}</Tag>;
};

export const renderToolBindingStatusTag = (value?: string | null) => {
  const colorMap: Record<string, string> = {
    linked: 'success',
    preview_only: 'processing',
    error: 'error',
  };
  return <Tag color={colorMap[value || ''] || 'default'}>{getOptionLabel(toolBindingStatusOptions, value)}</Tag>;
};

export const renderToolIntegrationAuditStatusTag = (value?: string | null) => {
  const colorMap: Record<string, string> = {
    previewed: 'processing',
    succeeded: 'success',
    failed: 'error',
  };
  return <Tag color={colorMap[value || ''] || 'default'}>{getOptionLabel(toolIntegrationAuditStatusOptions, value)}</Tag>;
};

export const renderGateStatusTag = (value?: string | null) => {
  const colorMap: Record<string, string> = {
    not_required: 'default',
    pending: 'warning',
    approved: 'success',
    rejected: 'error',
  };
  return <Tag color={colorMap[value || ''] || 'default'}>{getOptionLabel(gateStatusOptions, value)}</Tag>;
};

export const renderStageStatusTag = (value?: string | null) => {
  const colorMap: Record<string, string> = {
    pending: 'default',
    active: 'processing',
    completed: 'success',
    blocked: 'error',
  };
  return <Tag color={colorMap[value || ''] || 'default'}>{getOptionLabel(stageStatusOptions, value)}</Tag>;
};

export const renderDecisionStatusTag = (value?: string | null) => {
  const colorMap: Record<string, string> = {
    open: 'processing',
    pending_approval: 'warning',
    confirmed: 'success',
    rejected: 'error',
    deferred: 'default',
    resolved: 'default',
  };
  return <Tag color={colorMap[value || ''] || 'default'}>{getOptionLabel(decisionStatusOptions, value)}</Tag>;
};

export const renderWorkflowTemplateStatusTag = (value?: string | null) => {
  const colorMap: Record<string, string> = {
    draft: 'default',
    active: 'success',
    archived: 'warning',
  };
  return (
    <Tag color={colorMap[value || ''] || 'default'}>
      {getOptionLabel(workflowTemplateStatusOptions, value)}
    </Tag>
  );
};

export const renderAgentRoleStatusTag = (value?: string | null) => {
  const colorMap: Record<string, string> = {
    active: 'success',
    inactive: 'default',
  };
  return (
    <Tag color={colorMap[value || ''] || 'default'}>
      {getOptionLabel(agentRoleStatusOptions, value)}
    </Tag>
  );
};

export const renderAgentStageParticipationTag = (value?: string | null) => {
  const colorMap: Record<string, string> = {
    required: 'red',
    optional: 'blue',
    not_involved: 'default',
  };
  return (
    <Tag color={colorMap[value || ''] || 'default'}>
      {getOptionLabel(agentStageParticipationOptions, value)}
    </Tag>
  );
};

export const renderGateConditionStatusTag = (value?: string | null) => {
  const labelMap: Record<string, string> = {
    pass: '通过',
    warning: '关注',
    blocked: '阻塞',
    configured: '已配置',
  };
  const colorMap: Record<string, string> = {
    pass: 'success',
    warning: 'warning',
    blocked: 'error',
    configured: 'default',
  };
  return <Tag color={colorMap[value || ''] || 'default'}>{labelMap[value || ''] || value || '-'}</Tag>;
};

export const renderApprovalStatusTag = (value?: string | null) => {
  const colorMap: Record<string, string> = {
    draft: 'default',
    submitted: 'processing',
    request_changes: 'warning',
    deferred: 'default',
    approved: 'success',
    rejected: 'error',
    cancelled: 'default',
  };
  return <Tag color={colorMap[value || ''] || 'default'}>{getOptionLabel(approvalStatusOptions, value)}</Tag>;
};

export const renderApprovalBlockerTag = (value?: number | null) =>
  value ? <Tag color="red">阻塞审批</Tag> : <Tag>非阻塞</Tag>;

export const renderClarificationSeverityTag = (value?: string | null) => {
  const colorMap: Record<string, string> = {
    low: 'default',
    medium: 'gold',
    high: 'orange',
    blocker: 'red',
  };
  return (
    <Tag color={colorMap[value || ''] || 'default'}>
      {getOptionLabel(clarificationSeverityOptions, value)}
    </Tag>
  );
};

export const renderClarificationStatusTag = (value?: string | null) => {
  const colorMap: Record<string, string> = {
    open: 'processing',
    awaiting_response: 'warning',
    answered: 'blue',
    resolved: 'success',
  };
  return (
    <Tag color={colorMap[value || ''] || 'default'}>
      {getOptionLabel(clarificationStatusOptions, value)}
    </Tag>
  );
};

export const renderDecisionBlockerTag = (value?: number | null) =>
  value ? <Tag color="red">阻塞</Tag> : <Tag>非阻塞</Tag>;
