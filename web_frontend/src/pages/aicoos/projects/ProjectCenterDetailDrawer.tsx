import { ProCard, StatisticCard } from '@ant-design/pro-components';
import { history } from '@umijs/max';
import {
  App,
  Button,
  Descriptions,
  Drawer,
  Empty,
  List,
  Progress,
  Space,
  Spin,
  Steps,
  Timeline,
  Typography,
} from 'antd';
import dayjs from 'dayjs';
import React, { useEffect, useMemo, useState } from 'react';
import { FigmaContextModal } from './FigmaContextModal';
import { LinearProjectRepresentationModal } from './LinearProjectRepresentationModal';
import { MeetingSummaryModal } from './MeetingSummaryModal';
import { ProductArchitectureBriefModal } from './ProductArchitectureBriefModal';
import {
  agentAllowedActionOptions,
  agentRoleTypeOptions,
  clarificationCategoryOptions,
  formatBudgetAmount,
  getOptionLabel,
  lifecycleStageOptions,
  projectTypeOptions,
  renderToolBindingStatusTag,
  renderToolIntegrationAuditStatusTag,
  renderToolTypeTag,
  renderAgentRoleStatusTag,
  renderAgentStageParticipationTag,
  renderApprovalStatusTag,
  renderApprovalBlockerTag,
  renderBudgetLedgerEntryTag,
  renderBudgetStatusTag,
  renderGovernanceStatusTag,
  renderGateConditionStatusTag,
  renderClarificationSeverityTag,
  renderClarificationStatusTag,
  renderDecisionStatusTag,
  renderGateStatusTag,
  renderNextStepPriorityTag,
  renderProjectStatusTag,
  renderRiskLevelTag,
  renderStageStatusTag,
  toolBindingTypeOptions,
  toolIntegrationActionOptions,
} from '@/features/aicoos/projectCenter';
import { formatDateTime } from '@/features/workflow/utils';
import {
  applyFigmaContext,
  applyLinearProjectRepresentation,
  getProjectCenterDetail,
  previewFigmaContext,
  previewLinearProjectRepresentation,
} from '@/services/backend/aicoos';
import type {
  ProjectActivityItem,
  ProjectCenterDetail,
  ProjectAgentRoleSummaryItem,
  ProjectGateConditionItem,
  MeetingRecordItem,
  ProjectNextStepActionItem,
  ProjectStageRecord,
  ProjectToolBindingItem,
  ToolIntegrationAuditItem,
} from '@/services/backend/types';

interface ProjectCenterDetailDrawerProps {
  open: boolean;
  projectId?: string;
  onClose: () => void;
}

const formatTimelineTime = (value?: string | null) =>
  value ? dayjs(value).format('MM-DD HH:mm') : '-';

const integrationSourceTypeLabels: Record<string, string> = {
  project: '项目',
  clarification_item: '澄清项',
  decision_item: '决策事项',
};

const nextStepTargetModuleLabels: Record<string, string> = {
  'requirement-intake': '需求接收',
  'clarification-center': '澄清中心',
  'decision-center': '决策中心',
  'approval-center': '审批中心',
  'budget-center': '预算中心',
  'agent-roles': 'Agent 角色',
  'workflow-templates': '工作流模板',
  'project-center': '项目中心',
};

const nextStepActionButtonLabels: Record<string, string> = {
  'requirement-intake': '前往需求接收',
  'clarification-center': '前往澄清中心',
  'decision-center': '前往决策中心',
  'approval-center': '前往审批中心',
  'budget-center': '前往预算中心',
  'agent-roles': '前往 Agent 角色',
  'workflow-templates': '查看工作流模板',
};

const getTargetModuleLabel = (targetModule?: string | null) =>
  nextStepTargetModuleLabels[targetModule || ''] || targetModule || '项目中心';

const getTargetModuleActionButtonLabel = (targetModule?: string | null) =>
  nextStepActionButtonLabels[targetModule || ''];

const resolveTargetModulePath = (
  targetModule?: string | null,
  projectId?: string,
  workflowTemplateId?: string | null,
) => {
  switch (targetModule) {
    case 'requirement-intake':
      return projectId ? `/aicoos/requirement-intake?projectId=${projectId}` : '/aicoos/requirement-intake';
    case 'clarification-center':
      return projectId ? `/aicoos/clarification-center?projectId=${projectId}` : '/aicoos/clarification-center';
    case 'decision-center':
      return projectId ? `/aicoos/decision-center?projectId=${projectId}` : '/aicoos/decision-center';
    case 'approval-center':
      return projectId ? `/aicoos/approval-center?projectId=${projectId}` : '/aicoos/approval-center';
    case 'budget-center':
      return projectId ? `/aicoos/budget-center?projectId=${projectId}` : '/aicoos/budget-center';
    case 'agent-roles':
      return '/aicoos/agent-roles';
    case 'workflow-templates':
      return workflowTemplateId
        ? `/aicoos/workflow-templates?templateId=${workflowTemplateId}`
        : '/aicoos/workflow-templates';
    case 'project-center':
      return undefined;
    default:
      return undefined;
  }
};

const buildStageDescription = (stage: ProjectStageRecord) => (
  <Space direction="vertical" size={4} style={{ width: '100%' }}>
    <Space wrap size={[8, 4]}>
      {renderStageStatusTag(stage.stageStatus)}
      {renderGateStatusTag(stage.gateStatus)}
    </Space>
    <Typography.Text type="secondary">
      {formatDateTime(stage.startedAt)} ~ {formatDateTime(stage.endedAt)}
    </Typography.Text>
    {stage.remark ? (
      <Typography.Paragraph
        ellipsis={{ rows: 2, tooltip: stage.remark }}
        style={{ marginBottom: 0 }}
        type="secondary"
      >
        {stage.remark}
      </Typography.Paragraph>
    ) : null}
  </Space>
);

const getCurrentStageIndex = (
  stages: ProjectStageRecord[],
  currentStageCode?: string | null,
) => {
  const index = stages.findIndex((stage) => stage.stageCode === currentStageCode);
  return index >= 0 ? index : 0;
};

export const ProjectCenterDetailDrawer: React.FC<ProjectCenterDetailDrawerProps> = ({
  open,
  projectId,
  onClose,
}) => {
  const { message } = App.useApp();
  const [loading, setLoading] = useState(false);
  const [detail, setDetail] = useState<ProjectCenterDetail>();
  const [meetingModalOpen, setMeetingModalOpen] = useState(false);
  const [productArchitectureModalOpen, setProductArchitectureModalOpen] = useState(false);
  const [linearRepresentationModalOpen, setLinearRepresentationModalOpen] = useState(false);
  const [figmaContextModalOpen, setFigmaContextModalOpen] = useState(false);

  const loadDetail = async (targetProjectId: string) => {
    setLoading(true);
    try {
      const response = await getProjectCenterDetail(targetProjectId);
      setDetail(response.result);
    } catch (_error) {
      message.error('加载项目详情失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!open || !projectId) {
      if (!open) {
        setDetail(undefined);
      }
      return;
    }

    let active = true;
    void (async () => {
      setLoading(true);
      try {
        const response = await getProjectCenterDetail(projectId);
        if (active) {
          setDetail(response.result);
        }
      } catch (_error) {
        if (active) {
          message.error('加载项目详情失败');
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    })();
    return () => {
      active = false;
    };
  }, [message, open, projectId]);

  const project = detail?.project;
  const governance = detail?.governanceSummary;
  const budget = detail?.budgetSummary;
  const requirementSummary = detail?.requirementSummary;
  const collaborationSummary = detail?.collaborationSummary;
  const nextStepGuidance = detail?.nextStepGuidance;
  const requirementIntake = detail?.requirementIntake;
  const activities = detail?.recentActivities || [];
  const stages = detail?.stages || [];
  const gateConditions = detail?.currentGateConditions || [];
  const linkedAgentRoles = detail?.linkedAgentRoles || [];
  const currentStageRecommendedRoles = detail?.currentStageRecommendedRoles || [];
  const missingCriticalRoles = detail?.missingCriticalRoles || [];
  const meetingRecords = detail?.recentMeetingRecords || [];
  const toolBindings = detail?.projectToolBindings || [];
  const integrationAudits = detail?.recentToolIntegrationAudits || [];
  const requirementCollaboration = collaborationSummary?.requirementClarification;
  const decisionBudgetCollaboration = collaborationSummary?.decisionBudget;
  const productArchitectureCollaboration = collaborationSummary?.productArchitecture;
  const actionChain = nextStepGuidance?.priorityActions || [];
  const primaryAction = actionChain[0];
  const followUpActions = actionChain.slice(1);
  const primaryTargetModule = primaryAction?.targetModule || nextStepGuidance?.recommendedTargetModule;
  const nextStepTargetPath = resolveTargetModulePath(
    primaryTargetModule,
    projectId,
    project?.workflowTemplateId,
  );
  const nextStepTargetLabel = getTargetModuleLabel(primaryTargetModule);
  const nextStepActionButtonLabel = getTargetModuleActionButtonLabel(primaryTargetModule);
  const showProjectCenterHint = primaryTargetModule === 'project-center';

  const timelineItems = useMemo(
    () =>
      activities.map((item: ProjectActivityItem, index) => ({
        key: `${item.activityType || 'activity'}-${index}`,
        color:
          item.activityType === 'approval'
            ? 'blue'
            : item.activityType === 'budget'
              ? 'green'
                : item.activityType === 'clarification'
                ? 'gold'
                : item.activityType === 'intake'
                  ? 'cyan'
                : item.activityType === 'decision'
                ? 'orange'
                : item.activityType === 'meeting'
                  ? 'purple'
                  : item.activityType === 'integration'
                    ? 'geekblue'
                : 'gray',
        children: (
          <Space direction="vertical" size={2}>
            <Space wrap size={[8, 4]}>
              <Typography.Text strong>{item.title || '活动'}</Typography.Text>
              <Typography.Text type="secondary">
                {formatTimelineTime(item.occurredAt)}
              </Typography.Text>
            </Space>
            <Typography.Paragraph
              style={{ marginBottom: 0 }}
              ellipsis={{ rows: 2, tooltip: item.description || undefined }}
              type="secondary"
            >
              {item.description || '-'}
            </Typography.Paragraph>
            {item.externalUrl ? (
              <Typography.Link href={item.externalUrl} rel="noreferrer" target="_blank">
                查看外部对象
              </Typography.Link>
            ) : null}
          </Space>
        ),
      })),
    [activities],
  );

  return (
    <Drawer
      destroyOnHidden
      open={open}
      placement="right"
      title="项目详情"
      width={1120}
      extra={
        <Space>
          <Button
            disabled={!projectId}
            onClick={() => {
              setLinearRepresentationModalOpen(true);
            }}
          >
            Linear 映射
          </Button>
          <Button
            disabled={!projectId}
            onClick={() => {
              setFigmaContextModalOpen(true);
            }}
          >
            Figma 上下文
          </Button>
          <Button
            disabled={!projectId}
            onClick={() => {
              if (projectId) {
                history.push(`/aicoos/requirement-intake?projectId=${projectId}`);
              }
            }}
          >
            需求接收
          </Button>
          <Button
            disabled={!projectId}
            onClick={() => {
              if (projectId) {
                history.push(`/aicoos/clarification-center?projectId=${projectId}`);
              }
            }}
            type="primary"
          >
            澄清中心
          </Button>
          <Button
            disabled={!projectId}
            onClick={() => {
              if (projectId) {
                history.push(`/aicoos/decision-center?projectId=${projectId}`);
              }
            }}
          >
            决策中心
          </Button>
          <Button
            disabled={!projectId}
            onClick={() => {
              if (projectId) {
                history.push(`/aicoos/approval-center?projectId=${projectId}`);
              }
            }}
          >
            审批中心
          </Button>
          <Button
            disabled={!projectId}
            onClick={() => {
              if (projectId) {
                history.push(`/aicoos/budget-center?projectId=${projectId}`);
              }
            }}
          >
            预算中心
          </Button>
          <Button
            disabled={!projectId}
            onClick={() => {
              setMeetingModalOpen(true);
            }}
          >
            会议总结
          </Button>
          <Button
            disabled={!projectId}
            onClick={() => {
              setProductArchitectureModalOpen(true);
            }}
          >
            产品与架构协作
          </Button>
          <Button
            disabled={!project?.workflowTemplateId}
            onClick={() => {
              if (project?.workflowTemplateId) {
                history.push(`/aicoos/workflow-templates?templateId=${project.workflowTemplateId}`);
              }
            }}
          >
            工作流模板
          </Button>
          <Button
            onClick={() => {
              history.push('/aicoos/agent-roles');
            }}
          >
            Agent 角色
          </Button>
        </Space>
      }
      onClose={onClose}
    >
      {loading ? (
        <div
          style={{
            minHeight: 280,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <Spin size="large" />
        </div>
      ) : detail && project ? (
        <Space direction="vertical" size={16} style={{ display: 'flex' }}>
          <ProCard
            title={
              <Space direction="vertical" size={2}>
                <Typography.Title level={4} style={{ margin: 0 }}>
                  {project.projectName || '-'}
                </Typography.Title>
                <Typography.Text type="secondary">
                  {project.projectCode || '-'}
                </Typography.Text>
              </Space>
            }
            extra={
              <Space wrap size={[8, 8]}>
                {renderProjectStatusTag(project.status)}
                {renderGovernanceStatusTag(governance?.governanceStatus)}
                {renderRiskLevelTag(project.riskLevel)}
                {renderBudgetStatusTag(governance?.budgetStatus)}
              </Space>
            }
          >
            <Descriptions
              bordered
              column={{ xs: 1, md: 2 }}
              labelStyle={{ width: 132 }}
              size="small"
            >
              <Descriptions.Item label="项目类型">
                {getOptionLabel(projectTypeOptions, project.projectType)}
              </Descriptions.Item>
              <Descriptions.Item label="负责人">
                {detail.ownerDisplayName || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="当前阶段">
                {governance?.currentStageName ||
                  getOptionLabel(lifecycleStageOptions, project.currentStageCode)}
              </Descriptions.Item>
              <Descriptions.Item label="工作流模板">
                {detail.workflowTemplateName || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="模板阶段">
                {detail.workflowTemplateCurrentStageName
                  || getOptionLabel(lifecycleStageOptions, detail.workflowTemplateCurrentStageCode)}
              </Descriptions.Item>
              <Descriptions.Item label="更新时间">
                {formatDateTime(project.updateTime || project.createTime)}
              </Descriptions.Item>
              <Descriptions.Item label="预算状态">
                {renderBudgetStatusTag(governance?.budgetStatus)}
              </Descriptions.Item>
              <Descriptions.Item label="治理状态">
                <Space direction="vertical" size={4}>
                  {renderGovernanceStatusTag(governance?.governanceStatus)}
                  <Typography.Text type="secondary">
                    {governance?.lastRecomputedAt
                      ? `最近重算：${formatDateTime(governance.lastRecomputedAt)}`
                      : '暂无重算时间'}
                  </Typography.Text>
                </Space>
              </Descriptions.Item>
              <Descriptions.Item label="治理原因" span={2}>
                <Typography.Paragraph
                  style={{ marginBottom: 0 }}
                  ellipsis={{ rows: 2, tooltip: governance?.blockerReasonSummary || undefined }}
                >
                  {governance?.blockerReasonSummary || '治理状态正常'}
                </Typography.Paragraph>
              </Descriptions.Item>
              <Descriptions.Item label="需求摘要" span={2}>
                <Typography.Paragraph
                  style={{ marginBottom: 0 }}
                  ellipsis={{ rows: 3, tooltip: project.intakeSummary || undefined }}
                >
                  {project.intakeSummary || '暂无需求摘要'}
                </Typography.Paragraph>
              </Descriptions.Item>
              <Descriptions.Item label="备注" span={2}>
                <Typography.Paragraph
                  style={{ marginBottom: 0 }}
                  ellipsis={{ rows: 2, tooltip: project.remark || undefined }}
                >
                  {project.remark || '-'}
                </Typography.Paragraph>
              </Descriptions.Item>
            </Descriptions>
          </ProCard>

          <ProCard title="下一步建议">
            <Space direction="vertical" size={16} style={{ display: 'flex' }}>
              <div>
                <Space direction="vertical" size={4} style={{ display: 'flex' }}>
                  <Typography.Title level={5} style={{ margin: 0 }}>
                    {nextStepGuidance?.recommendedNextStep || '继续回看项目治理状态'}
                  </Typography.Title>
                </Space>
              </div>

              <div
                style={{
                  display: 'flex',
                  alignItems: 'flex-start',
                  justifyContent: 'space-between',
                  gap: 12,
                  flexWrap: 'wrap',
                }}
              >
                <Space wrap size={[8, 8]}>
                  {renderNextStepPriorityTag(nextStepGuidance?.recommendedPriority)}
                  <Typography.Text type="secondary">
                    执行角色：{nextStepGuidance?.recommendedActorRole || '-'}
                  </Typography.Text>
                  <Typography.Text type="secondary">目标模块：{nextStepTargetLabel}</Typography.Text>
                </Space>
                {showProjectCenterHint ? (
                  <Typography.Text type="secondary">
                    当前建议可直接结合本页治理信息处理
                  </Typography.Text>
                ) : nextStepTargetPath && nextStepActionButtonLabel ? (
                  <Button
                    onClick={() => {
                      history.push(nextStepTargetPath);
                    }}
                    type="primary"
                  >
                    {nextStepActionButtonLabel}
                  </Button>
                ) : null}
              </div>

              <div>
                <Typography.Text strong>总体判断</Typography.Text>
                <Typography.Paragraph style={{ marginBottom: 0, marginTop: 8 }} type="secondary">
                  {nextStepGuidance?.recommendedReason || '当前暂无额外建议，请继续按项目中心治理视图推进。'}
                </Typography.Paragraph>
              </div>

              <div>
                <Typography.Text strong>当前硬阻塞</Typography.Text>
                {(nextStepGuidance?.currentBlockers || []).length ? (
                  <List
                    dataSource={nextStepGuidance?.currentBlockers || []}
                    renderItem={(item) => (
                      <List.Item style={{ paddingLeft: 0, paddingRight: 0 }}>
                        <Typography.Text>{item}</Typography.Text>
                      </List.Item>
                    )}
                    size="small"
                  />
                ) : (
                  <Typography.Paragraph style={{ marginBottom: 0, marginTop: 8 }} type="secondary">
                    当前无硬阻塞，建议按优先动作推进。
                  </Typography.Paragraph>
                )}
              </div>

              {primaryAction ? (
                <div>
                  <Typography.Text strong>建议先做</Typography.Text>
                  <div
                    style={{
                      border: '1px solid #f0f0f0',
                      borderRadius: 8,
                      marginTop: 12,
                      padding: 16,
                    }}
                  >
                    <Space direction="vertical" size={12} style={{ display: 'flex' }}>
                      <div
                        style={{
                          display: 'flex',
                          alignItems: 'flex-start',
                          justifyContent: 'space-between',
                          gap: 12,
                          flexWrap: 'wrap',
                        }}
                      >
                        <Space direction="vertical" size={4} style={{ display: 'flex', flex: 1, minWidth: 220 }}>
                          <Typography.Text strong>
                            {`${primaryAction.actionOrder || 1}. ${primaryAction.actionLabel || '-'}`}
                          </Typography.Text>
                          <Typography.Text type="secondary">
                            目标模块：{getTargetModuleLabel(primaryAction.targetModule)}
                          </Typography.Text>
                        </Space>
                        {primaryAction.targetModule === 'project-center' ? (
                          <Typography.Text type="secondary">
                            当前动作可直接结合本页治理信息处理
                          </Typography.Text>
                        ) : resolveTargetModulePath(
                          primaryAction.targetModule,
                          projectId,
                          project?.workflowTemplateId,
                        ) && getTargetModuleActionButtonLabel(primaryAction.targetModule) ? (
                          <Button
                            onClick={() => {
                              const targetPath = resolveTargetModulePath(
                                primaryAction.targetModule,
                                projectId,
                                project?.workflowTemplateId,
                              );
                              if (targetPath) {
                                history.push(targetPath);
                              }
                            }}
                            type="primary"
                          >
                            {getTargetModuleActionButtonLabel(primaryAction.targetModule)}
                          </Button>
                        ) : null}
                      </div>

                      <div>
                        <Typography.Text strong>为什么现在做</Typography.Text>
                        <Typography.Paragraph style={{ marginBottom: 0, marginTop: 8 }} type="secondary">
                          {primaryAction.recommendedReason || '当前治理状态优先建议先处理这个动作。'}
                        </Typography.Paragraph>
                      </div>

                      <div>
                        <Typography.Text strong>主要消解</Typography.Text>
                        <Typography.Paragraph style={{ marginBottom: 0, marginTop: 8 }} type="secondary">
                          {primaryAction.addressedRisk || '当前动作主要用于降低项目推进中的治理风险。'}
                        </Typography.Paragraph>
                      </div>

                      <div>
                        <Typography.Text strong>完成后预期</Typography.Text>
                        <Typography.Paragraph style={{ marginBottom: 0, marginTop: 8 }} type="secondary">
                          {primaryAction.expectedChange || '项目治理状态会更清晰，后续动作会更容易收敛。'}
                        </Typography.Paragraph>
                      </div>
                    </Space>
                  </div>
                </div>
              ) : null}

              {followUpActions.length ? (
                <div>
                  <Typography.Text strong>随后跟进</Typography.Text>
                  <List<ProjectNextStepActionItem>
                    dataSource={followUpActions}
                    renderItem={(item) => {
                      const targetPath = resolveTargetModulePath(
                        item.targetModule,
                        projectId,
                        project?.workflowTemplateId,
                      );
                      const targetButtonLabel = getTargetModuleActionButtonLabel(item.targetModule);
                      return (
                        <List.Item style={{ paddingLeft: 0, paddingRight: 0 }}>
                          <Space direction="vertical" size={6} style={{ width: '100%' }}>
                            <div
                              style={{
                                display: 'flex',
                                alignItems: 'flex-start',
                                justifyContent: 'space-between',
                                gap: 12,
                                flexWrap: 'wrap',
                              }}
                            >
                              <Space direction="vertical" size={2} style={{ display: 'flex', flex: 1, minWidth: 220 }}>
                                <Typography.Text>{`${item.actionOrder || '-'}. ${item.actionLabel || '-'}`}</Typography.Text>
                                <Typography.Text type="secondary">
                                  目标模块：{getTargetModuleLabel(item.targetModule)}
                                </Typography.Text>
                              </Space>
                              {item.targetModule === 'project-center' ? (
                                <Typography.Text type="secondary">
                                  可结合本页治理信息继续处理
                                </Typography.Text>
                              ) : targetPath && targetButtonLabel ? (
                                <Button
                                  onClick={() => {
                                    history.push(targetPath);
                                  }}
                                  size="small"
                                >
                                  {targetButtonLabel}
                                </Button>
                              ) : null}
                            </div>
                            <Typography.Paragraph style={{ marginBottom: 0 }} type="secondary">
                              {`跟进理由：${item.recommendedReason || '该动作有助于继续收敛当前治理链路。'}`}
                            </Typography.Paragraph>
                            <Typography.Text type="secondary">
                              主要消解：{item.addressedRisk || '-'}
                            </Typography.Text>
                            <Typography.Text type="secondary">
                              完成后预期：{item.expectedChange || '-'}
                            </Typography.Text>
                          </Space>
                        </List.Item>
                      );
                    }}
                    size="small"
                  />
                </div>
              ) : null}
            </Space>
          </ProCard>

          <ProCard gutter={16} wrap>
            <StatisticCard
              colSpan={{ xs: 24, md: 8, lg: 4 }}
              statistic={{
                title: '需求完整度',
                value: requirementSummary?.completenessScore || 0,
                suffix: '%',
              }}
            />
            <StatisticCard
              colSpan={{ xs: 24, md: 8, lg: 4 }}
              statistic={{
                title: '阻塞澄清项',
                value: requirementSummary?.blockerCount || 0,
              }}
            />
            <StatisticCard
              colSpan={{ xs: 24, md: 8, lg: 4 }}
              statistic={{
                title: '澄清项总数',
                value: requirementSummary?.clarificationCount || 0,
              }}
            />
            <StatisticCard
              colSpan={{ xs: 24, md: 8, lg: 4 }}
              statistic={{
                title: '待处理决策',
                value: governance?.pendingDecisionItemCount || 0,
              }}
            />
            <StatisticCard
              colSpan={{ xs: 24, md: 8, lg: 4 }}
              statistic={{
                title: '阻塞决策',
                value: governance?.blockerDecisionCount || 0,
              }}
            />
            <StatisticCard
              colSpan={{ xs: 24, md: 8, lg: 4 }}
              statistic={{
                title: '待处理审批',
                value: governance?.pendingApprovalCount || 0,
              }}
            />
            <StatisticCard
              colSpan={{ xs: 24, md: 8, lg: 4 }}
              statistic={{
                title: '阻塞审批',
                value: governance?.blockerApprovalCount || 0,
              }}
            />
            <StatisticCard
              colSpan={{ xs: 24, md: 8, lg: 4 }}
              statistic={{
                title: '失败门禁条件',
                value: governance?.failedGateConditionCount ?? governance?.blockingGateConditionCount ?? 0,
              }}
            />
            <StatisticCard
              colSpan={{ xs: 24, md: 8, lg: 4 }}
              statistic={{
                title: '缺失关键角色',
                value: governance?.missingCriticalRoleCount || 0,
              }}
            />
            <StatisticCard
              colSpan={{ xs: 24, md: 8, lg: 4 }}
              statistic={{
                title: '阻塞门禁条件',
                value: governance?.blockingGateConditionCount || 0,
              }}
            />
            <StatisticCard
              colSpan={{ xs: 24, md: 8, lg: 4 }}
              statistic={{
                title: '总预算',
                value: budget?.totalBudgetAmount || 0,
                suffix: budget?.currencyCode || 'TOKEN',
              }}
            />
            <StatisticCard
              colSpan={{ xs: 24, md: 8, lg: 4 }}
              statistic={{
                title: '锁定预算',
                value: budget?.lockedAmount || 0,
                suffix: budget?.currencyCode || 'TOKEN',
              }}
            />
            <StatisticCard
              colSpan={{ xs: 24, md: 8, lg: 4 }}
              statistic={{
                title: '已消耗预算',
                value: budget?.consumedAmount || 0,
                suffix: budget?.currencyCode || 'TOKEN',
              }}
            />
            <StatisticCard
              colSpan={{ xs: 24, md: 8, lg: 4 }}
              statistic={{
                title: '待增补预算',
                value: budget?.pendingIncreaseAmount || 0,
                suffix: budget?.currencyCode || 'TOKEN',
              }}
            />
          </ProCard>

          <ProCard gutter={16} wrap>
            <ProCard title="需求接收概览" colSpan={{ xs: 24, lg: 12 }}>
              <Space direction="vertical" size={12} style={{ display: 'flex' }}>
                <div>
                  <Space align="center" size={12} style={{ width: '100%' }}>
                    <Typography.Text strong>完整度</Typography.Text>
                    <Progress
                      percent={requirementSummary?.completenessScore || 0}
                      size="small"
                      status={
                        (requirementSummary?.completenessScore || 0) >= 80
                          ? 'success'
                          : 'active'
                      }
                      style={{ flex: 1, marginBottom: 0 }}
                    />
                  </Space>
                  <Typography.Text type="secondary">
                    已填写 {requirementSummary?.completedCoreFieldCount || 0} /{' '}
                    {requirementSummary?.totalCoreFieldCount || 0} 个核心字段
                  </Typography.Text>
                </div>
                <Descriptions bordered column={1} size="small">
                  <Descriptions.Item label="业务目标">
                    <Typography.Paragraph style={{ marginBottom: 0 }}>
                      {requirementIntake?.businessGoal || '暂无'}
                    </Typography.Paragraph>
                  </Descriptions.Item>
                  <Descriptions.Item label="功能摘要">
                    <Typography.Paragraph style={{ marginBottom: 0 }}>
                      {requirementIntake?.featureSummary || '暂无'}
                    </Typography.Paragraph>
                  </Descriptions.Item>
                  <Descriptions.Item label="参考产品 / 链接">
                    <Typography.Paragraph style={{ marginBottom: 0 }}>
                      {requirementIntake?.referenceProducts || '暂无'}
                    </Typography.Paragraph>
                  </Descriptions.Item>
                  <Descriptions.Item label="时间预期">
                    {requirementIntake?.timelineExpectation || '暂无'}
                  </Descriptions.Item>
                  <Descriptions.Item label="预算范围">
                    {requirementIntake?.budgetRange || '暂无'}
                  </Descriptions.Item>
                  <Descriptions.Item label="技术约束">
                    <Typography.Paragraph style={{ marginBottom: 0 }}>
                      {requirementIntake?.technicalConstraints || '暂无'}
                    </Typography.Paragraph>
                  </Descriptions.Item>
                  <Descriptions.Item label="缺失核心字段">
                    {requirementSummary?.missingCoreFields?.length
                      ? requirementSummary.missingCoreFields.join('、')
                      : '无'}
                  </Descriptions.Item>
                </Descriptions>
              </Space>
            </ProCard>

            <ProCard title="澄清中心概览" colSpan={{ xs: 24, lg: 12 }}>
              <Space wrap size={[12, 8]} style={{ marginBottom: 12 }}>
                <Typography.Text>总数 {requirementSummary?.clarificationCount || 0}</Typography.Text>
                <Typography.Text>待处理 {requirementSummary?.openClarificationCount || 0}</Typography.Text>
                <Typography.Text>阻塞 {requirementSummary?.blockerCount || 0}</Typography.Text>
              </Space>
              <List
                dataSource={detail.recentClarificationItems || []}
                locale={{
                  emptyText: (
                    <Empty
                      description="暂无澄清项"
                      image={Empty.PRESENTED_IMAGE_SIMPLE}
                    />
                  ),
                }}
              renderItem={(item) => (
                <List.Item>
                  <List.Item.Meta
                    title={
                      <Space wrap size={[8, 4]}>
                        <Typography.Text strong>{item.title || '-'}</Typography.Text>
                        {renderClarificationSeverityTag(item.severity)}
                        {renderClarificationStatusTag(item.status)}
                        {item.remark?.includes('来源：多角色澄清评审') ? <Typography.Text type="secondary">协作来源</Typography.Text> : null}
                      </Space>
                    }
                      description={
                        <Space direction="vertical" size={4} style={{ width: '100%' }}>
                          <Typography.Paragraph
                            ellipsis={{ rows: 2, tooltip: item.question || undefined }}
                            style={{ marginBottom: 0 }}
                            type="secondary"
                          >
                            {item.question || '-'}
                          </Typography.Paragraph>
                          <Typography.Text type="secondary">
                            {getOptionLabel(clarificationCategoryOptions, item.category)} ·{' '}
                            {formatDateTime(item.updateTime || item.createTime)}
                          </Typography.Text>
                          {item.remark ? (
                            <Typography.Text type="secondary">
                              {item.remark}
                            </Typography.Text>
                          ) : null}
                        </Space>
                      }
                    />
                  </List.Item>
                )}
              />
            </ProCard>
          </ProCard>

          <ProCard title="协作治理联动">
            <ProCard gutter={16} wrap>
              <StatisticCard
                colSpan={{ xs: 24, md: 8, lg: 4 }}
                statistic={{
                  title: '协作落地总数',
                  value: collaborationSummary?.appliedCollaborationCount || 0,
                }}
              />
              <StatisticCard
                colSpan={{ xs: 24, md: 8, lg: 4 }}
                statistic={{
                  title: '协作澄清记录',
                  value: requirementCollaboration?.appliedClarificationCount || 0,
                }}
              />
              <StatisticCard
                colSpan={{ xs: 24, md: 8, lg: 4 }}
                statistic={{
                  title: '升级决策建议',
                  value: requirementCollaboration?.decisionEscalationSuggestionCount || 0,
                }}
              />
              <StatisticCard
                colSpan={{ xs: 24, md: 8, lg: 4 }}
                statistic={{
                  title: '协作决策评审',
                  value: decisionBudgetCollaboration?.appliedDecisionCount || 0,
                }}
              />
              <StatisticCard
                colSpan={{ xs: 24, md: 8, lg: 4 }}
                statistic={{
                  title: '预算确认建议',
                  value: decisionBudgetCollaboration?.budgetConfirmationSuggestedCount || 0,
                }}
              />
              <StatisticCard
                colSpan={{ xs: 24, md: 8, lg: 4 }}
                statistic={{
                  title: '协作方案简报',
                  value: productArchitectureCollaboration?.briefCount || 0,
                }}
              />
            </ProCard>

            {(collaborationSummary?.governanceHighlights || []).length ? (
              <List
                dataSource={collaborationSummary?.governanceHighlights || []}
                renderItem={(item) => <List.Item>{item}</List.Item>}
                size="small"
                style={{ marginTop: 12 }}
              />
            ) : (
              <Typography.Text type="secondary">
                当前项目还没有需要额外关注的协作治理提示。
              </Typography.Text>
            )}

            <ProCard gutter={16} style={{ marginTop: 16 }} wrap>
              <ProCard title="需求澄清协作" colSpan={{ xs: 24, lg: 8 }}>
                <Descriptions column={1} size="small">
                  <Descriptions.Item label="最近应用">
                    {formatDateTime(requirementCollaboration?.latestAppliedAt)}
                  </Descriptions.Item>
                  <Descriptions.Item label="待处理协作澄清">
                    {requirementCollaboration?.openClarificationCount || 0}
                  </Descriptions.Item>
                  <Descriptions.Item label="阻塞级协作澄清">
                    {requirementCollaboration?.blockerClarificationCount || 0}
                  </Descriptions.Item>
                  <Descriptions.Item label="建议跟进模块">
                    {(requirementCollaboration?.followUpModules || []).join('、') || '暂无'}
                  </Descriptions.Item>
                </Descriptions>
              </ProCard>

              <ProCard title="决策与预算协作" colSpan={{ xs: 24, lg: 8 }}>
                <Descriptions column={1} size="small">
                  <Descriptions.Item label="最近应用">
                    {formatDateTime(decisionBudgetCollaboration?.latestAppliedAt)}
                  </Descriptions.Item>
                  <Descriptions.Item label="最近决策">
                    {decisionBudgetCollaboration?.latestDecisionTitle || '暂无'}
                  </Descriptions.Item>
                  <Descriptions.Item label="推荐选项">
                    {decisionBudgetCollaboration?.latestRecommendedOption || '暂无'}
                  </Descriptions.Item>
                  <Descriptions.Item label="仍待推进">
                    {decisionBudgetCollaboration?.openDecisionCount || 0}，阻塞{' '}
                    {decisionBudgetCollaboration?.blockerDecisionCount || 0}
                  </Descriptions.Item>
                </Descriptions>
              </ProCard>

              <ProCard title="产品与架构协作" colSpan={{ xs: 24, lg: 8 }}>
                <Descriptions column={1} size="small">
                  <Descriptions.Item label="最近保存">
                    {formatDateTime(productArchitectureCollaboration?.latestSavedAt)}
                  </Descriptions.Item>
                  <Descriptions.Item label="最新简报">
                    {productArchitectureCollaboration?.latestBriefTitle || '暂无'}
                  </Descriptions.Item>
                  <Descriptions.Item label="开放问题 / 行动项">
                    {(productArchitectureCollaboration?.latestOpenQuestionCount || 0)} /{' '}
                    {(productArchitectureCollaboration?.latestActionItemCount || 0)}
                  </Descriptions.Item>
                  <Descriptions.Item label="决策候选">
                    {productArchitectureCollaboration?.latestDecisionCandidateCount || 0}
                  </Descriptions.Item>
                </Descriptions>
                {productArchitectureCollaboration?.latestBriefSummary ? (
                  <Typography.Paragraph
                    ellipsis={{
                      rows: 3,
                      tooltip: productArchitectureCollaboration.latestBriefSummary,
                    }}
                    style={{ marginBottom: 0, marginTop: 12 }}
                    type="secondary"
                  >
                    {productArchitectureCollaboration.latestBriefSummary}
                  </Typography.Paragraph>
                ) : null}
              </ProCard>
            </ProCard>
          </ProCard>

          <ProCard title="阶段概览">
            {stages.length ? (
              <Steps
                current={getCurrentStageIndex(stages, governance?.currentStageCode)}
                direction="vertical"
                items={stages.map((stage) => ({
                  title:
                    stage.stageName ||
                    getOptionLabel(lifecycleStageOptions, stage.stageCode),
                  description: buildStageDescription(stage),
                  status:
                    stage.stageStatus === 'completed'
                      ? 'finish'
                      : stage.stageStatus === 'active'
                        ? 'process'
                        : stage.stageStatus === 'blocked'
                          ? 'error'
                          : 'wait',
                }))}
              />
            ) : (
              <Empty description="当前项目还没有阶段记录" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </ProCard>

          <ProCard title="当前门禁条件">
            <List<ProjectGateConditionItem>
              dataSource={gateConditions}
              locale={{
                emptyText: (
                  <Empty
                    description="当前工作流模板未配置门禁条件"
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                  />
                ),
              }}
              renderItem={(item) => (
                <List.Item>
                  <List.Item.Meta
                    title={
                      <Space wrap size={[8, 4]}>
                        <Typography.Text strong>{item.title || '-'}</Typography.Text>
                        {renderGateConditionStatusTag(item.status)}
                      </Space>
                    }
                    description={
                      <Typography.Paragraph style={{ marginBottom: 0 }} type="secondary">
                        {item.summary || '-'}
                      </Typography.Paragraph>
                    }
                  />
                </List.Item>
              )}
            />
          </ProCard>

          <ProCard gutter={16} wrap>
            <ProCard title="关联 Agent 角色" colSpan={{ xs: 24, lg: 12 }}>
              <List<ProjectAgentRoleSummaryItem>
                dataSource={linkedAgentRoles}
                locale={{
                  emptyText: (
                    <Empty
                      description="当前项目还没有匹配的 Agent 角色"
                      image={Empty.PRESENTED_IMAGE_SIMPLE}
                    />
                  ),
                }}
                renderItem={(item) => (
                  <List.Item>
                    <List.Item.Meta
                      title={
                        <Space wrap size={[8, 4]}>
                          <Typography.Text strong>{item.roleName || '-'}</Typography.Text>
                          {renderAgentRoleStatusTag(item.status)}
                          {item.defaultFlag === 1 ? <Typography.Text type="secondary">默认</Typography.Text> : null}
                        </Space>
                      }
                      description={
                        <Space direction="vertical" size={4} style={{ width: '100%' }}>
                          <Typography.Text type="secondary">
                            {getOptionLabel(agentRoleTypeOptions, item.roleCategory)} · 预算系数{' '}
                            {item.budgetFactor ?? 1}
                          </Typography.Text>
                          <Typography.Text type="secondary">
                            审批协作：{item.approvalCollaborationFlag === 1 ? '是' : '否'}
                          </Typography.Text>
                        </Space>
                      }
                    />
                  </List.Item>
                )}
              />
            </ProCard>

            <ProCard title="当前阶段推荐角色" colSpan={{ xs: 24, lg: 12 }}>
              <List<ProjectAgentRoleSummaryItem>
                dataSource={currentStageRecommendedRoles}
                locale={{
                  emptyText: (
                    <Empty
                      description="当前阶段没有推荐 Agent 角色"
                      image={Empty.PRESENTED_IMAGE_SIMPLE}
                    />
                  ),
                }}
                renderItem={(item) => (
                  <List.Item>
                    <List.Item.Meta
                      title={
                        <Space wrap size={[8, 4]}>
                          <Typography.Text strong>{item.roleName || '-'}</Typography.Text>
                          {renderAgentStageParticipationTag(item.currentStageParticipationType)}
                        </Space>
                      }
                      description={
                        <Space direction="vertical" size={4} style={{ width: '100%' }}>
                          <Typography.Text type="secondary">
                            角色类型：{getOptionLabel(agentRoleTypeOptions, item.roleCategory)}
                          </Typography.Text>
                          <Typography.Text type="secondary">
                            允许动作：
                            {(item.allowedActionCodes || [])
                              .map((code) => getOptionLabel(agentAllowedActionOptions, code))
                              .join('、') || '暂无'}
                          </Typography.Text>
                        </Space>
                      }
                    />
                  </List.Item>
                )}
              />

              <Descriptions column={1} size="small" style={{ marginTop: 12 }}>
                <Descriptions.Item label="缺失关键角色">
                  {missingCriticalRoles.length ? missingCriticalRoles.join('；') : '无'}
                </Descriptions.Item>
              </Descriptions>
            </ProCard>
          </ProCard>

          <ProCard gutter={16} wrap>
            <ProCard title="决策概览" colSpan={{ xs: 24, lg: 8 }}>
              <Space wrap size={[8, 8]} style={{ marginBottom: 12 }}>
                <Typography.Text>待处理 {detail.decisionSummary?.pendingCount || 0}</Typography.Text>
                <Typography.Text>开放 {detail.decisionSummary?.openCount || 0}</Typography.Text>
                <Typography.Text>阻塞 {detail.decisionSummary?.blockerCount || 0}</Typography.Text>
                <Typography.Text>已解决 {detail.decisionSummary?.resolvedCount || 0}</Typography.Text>
              </Space>
              <List
                dataSource={detail.recentDecisionItems || []}
                locale={{
                  emptyText: (
                    <Empty
                      description="暂无决策事项"
                      image={Empty.PRESENTED_IMAGE_SIMPLE}
                    />
                  ),
                }}
              renderItem={(item) => (
                <List.Item>
                  <List.Item.Meta
                    title={
                      <Space wrap size={[8, 4]}>
                        <Typography.Text strong>{item.title || '-'}</Typography.Text>
                        {renderDecisionStatusTag(item.status)}
                        {item.remark?.includes('已应用多角色协作评审建议') ? <Typography.Text type="secondary">协作评审</Typography.Text> : null}
                      </Space>
                    }
                    description={
                      <Space direction="vertical" size={4} style={{ width: '100%' }}>
                        <Typography.Text type="secondary">
                          截止时间：{formatDateTime(item.dueAt)}
                        </Typography.Text>
                        {item.remark ? (
                          <Typography.Text type="secondary">
                            {item.remark}
                          </Typography.Text>
                        ) : null}
                      </Space>
                    }
                  />
                </List.Item>
              )}
              />
            </ProCard>

            <ProCard title="审批概览" colSpan={{ xs: 24, lg: 8 }}>
              <Space wrap size={[8, 8]} style={{ marginBottom: 12 }}>
                <Typography.Text>待处理 {detail.approvalSummary?.pendingCount || 0}</Typography.Text>
                <Typography.Text>阻塞 {detail.approvalSummary?.blockerCount || 0}</Typography.Text>
                <Typography.Text>已批准 {detail.approvalSummary?.approvedCount || 0}</Typography.Text>
                <Typography.Text>已拒绝 {detail.approvalSummary?.rejectedCount || 0}</Typography.Text>
              </Space>
              <List
                dataSource={detail.recentApprovalRecords || []}
                locale={{
                  emptyText: (
                    <Empty
                      description="暂无审批记录"
                      image={Empty.PRESENTED_IMAGE_SIMPLE}
                    />
                  ),
                }}
                renderItem={(item) => (
                  <List.Item>
                    <List.Item.Meta
                      title={
                        <Space wrap size={[8, 4]}>
                          <Typography.Text strong>
                            {item.title ||
                              getOptionLabel(
                                [
                                  { label: '需求审批', value: 'requirement' },
                                  { label: '决策审批', value: 'decision' },
                                  { label: '预算审批', value: 'budget' },
                                  { label: '预算变更审批', value: 'budget_change' },
                                  { label: '风险操作审批', value: 'risk_operation' },
                                  { label: '关卡审批', value: 'gate' },
                                  { label: '发布审批', value: 'release' },
                                ],
                                item.approvalType,
                              )}
                          </Typography.Text>
                          {renderApprovalStatusTag(item.approvalStatus)}
                          {renderApprovalBlockerTag(item.blockerFlag)}
                        </Space>
                      }
                      description={
                        <Space direction="vertical" size={4} style={{ width: '100%' }}>
                          <Typography.Text type="secondary">
                            时间：{formatDateTime(item.decidedAt || item.submittedAt || item.createTime)}
                          </Typography.Text>
                          {item.recommendedAction ? (
                            <Typography.Text type="secondary">
                              推荐动作：{item.recommendedAction}
                            </Typography.Text>
                          ) : null}
                        </Space>
                      }
                    />
                  </List.Item>
                )}
              />
            </ProCard>

            <ProCard title="预算概览" colSpan={{ xs: 24, lg: 8 }}>
              <Descriptions column={1} size="small" style={{ marginBottom: 12 }}>
                <Descriptions.Item label="预算状态">
                  {renderBudgetStatusTag(budget?.status)}
                </Descriptions.Item>
                <Descriptions.Item label="预算计划">
                  {budget?.planName || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="总预算">
                  {formatBudgetAmount(budget?.totalBudgetAmount, budget?.currencyCode || 'TOKEN')}
                </Descriptions.Item>
                <Descriptions.Item label="锁定预算">
                  {formatBudgetAmount(budget?.lockedAmount, budget?.currencyCode || 'TOKEN')}
                </Descriptions.Item>
                <Descriptions.Item label="已消耗金额">
                  {formatBudgetAmount(budget?.consumedAmount, budget?.currencyCode || 'TOKEN')}
                </Descriptions.Item>
                <Descriptions.Item label="待增补预算">
                  {formatBudgetAmount(budget?.pendingIncreaseAmount, budget?.currencyCode || 'TOKEN')}
                </Descriptions.Item>
                <Descriptions.Item label="最后更新时间">
                  {formatDateTime(budget?.lastUpdatedAt)}
                </Descriptions.Item>
              </Descriptions>
              <List
                dataSource={detail.recentBudgetLedgerEntries || []}
                locale={{
                  emptyText: (
                    <Empty
                      description="暂无预算流水"
                      image={Empty.PRESENTED_IMAGE_SIMPLE}
                    />
                  ),
                }}
                renderItem={(item) => (
                  <List.Item>
                    <List.Item.Meta
                      title={
                        <Space wrap size={[8, 4]}>
                          {renderBudgetLedgerEntryTag(item.entryType)}
                          <Typography.Text strong>{item.description || '-'}</Typography.Text>
                        </Space>
                      }
                      description={
                        <Space direction="vertical" size={4} style={{ width: '100%' }}>
                          <Typography.Text type="secondary">
                            {formatBudgetAmount(item.amount, budget?.currencyCode || 'TOKEN')} ·{' '}
                            {formatDateTime(item.occurredAt || item.createTime)}
                          </Typography.Text>
                          {item.referenceDisplayName ? (
                            <Typography.Text type="secondary">
                              关联对象：{item.referenceDisplayName}
                            </Typography.Text>
                          ) : null}
                        </Space>
                      }
                    />
                  </List.Item>
                )}
              />
            </ProCard>
          </ProCard>

          <ProCard title="会议记录概览">
            <List<MeetingRecordItem>
              dataSource={meetingRecords}
              locale={{
                emptyText: (
                  <Empty
                    description="暂无会议记录"
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                  />
                ),
              }}
              renderItem={(item) => (
                <List.Item>
                  <List.Item.Meta
                      title={
                        <Space wrap size={[8, 4]}>
                          <Typography.Text strong>{item.meetingTitle || '-'}</Typography.Text>
                          <Typography.Text type="secondary">
                            {formatDateTime(item.generatedAt || item.createTime)}
                          </Typography.Text>
                          {item.sourceType === 'product_architecture_brief' ? <Typography.Text type="secondary">协作简报</Typography.Text> : null}
                        </Space>
                      }
                      description={
                        <Space direction="vertical" size={4} style={{ width: '100%' }}>
                        <Typography.Paragraph
                          ellipsis={{ rows: 2, tooltip: item.summary || undefined }}
                          style={{ marginBottom: 0 }}
                          type="secondary"
                        >
                          {item.summary || '-'}
                        </Typography.Paragraph>
                        {(item.actionItems || []).length ? (
                          <Typography.Text type="secondary">
                            行动项：{(item.actionItems || []).slice(0, 2).join('；')}
                          </Typography.Text>
                        ) : null}
                        {item.remark ? (
                          <Typography.Text type="secondary">
                            {item.remark}
                          </Typography.Text>
                        ) : null}
                      </Space>
                    }
                  />
                </List.Item>
              )}
            />
          </ProCard>

          <ProCard gutter={16} wrap>
            <ProCard
              title="外部工具上下文"
              colSpan={{ xs: 24, lg: 12 }}
              extra={
                <Space>
                  <Button
                    disabled={!projectId}
                    size="small"
                    onClick={() => {
                      setLinearRepresentationModalOpen(true);
                    }}
                  >
                    Linear 项目映射
                  </Button>
                  <Button
                    disabled={!projectId}
                    size="small"
                    onClick={() => {
                      setFigmaContextModalOpen(true);
                    }}
                  >
                    Figma 上下文
                  </Button>
                </Space>
              }
            >
              <List<ProjectToolBindingItem>
                dataSource={toolBindings}
                locale={{
                  emptyText: (
                    <Empty
                      description="当前项目暂无外部工具绑定"
                      image={Empty.PRESENTED_IMAGE_SIMPLE}
                    />
                  ),
                }}
                renderItem={(item) => (
                  <List.Item>
                    <List.Item.Meta
                      title={
                        <Space wrap size={[8, 4]}>
                          <Typography.Text strong>{item.externalName || item.externalKey || '-'}</Typography.Text>
                          {renderToolTypeTag(item.toolType)}
                          {renderToolBindingStatusTag(item.bindingStatus)}
                          <Typography.Text type="secondary">
                            {getOptionLabel(toolBindingTypeOptions, item.bindingType)}
                          </Typography.Text>
                          {item.defaultFlag === 1 ? <Typography.Text type="secondary">默认</Typography.Text> : null}
                        </Space>
                      }
                      description={
                        <Space direction="vertical" size={4} style={{ width: '100%' }}>
                          <Typography.Text type="secondary">
                            外部标识：{item.externalKey || item.externalId || '-'}
                          </Typography.Text>
                          <Typography.Text type="secondary">
                            更新时间：{formatDateTime(item.updateTime || item.createTime)}
                          </Typography.Text>
                          {item.externalUrl ? (
                            <Typography.Link href={item.externalUrl} rel="noreferrer" target="_blank">
                              打开外部链接
                            </Typography.Link>
                          ) : null}
                          {item.remark ? (
                            <Typography.Paragraph
                              ellipsis={{ rows: 2, tooltip: item.remark }}
                              style={{ marginBottom: 0 }}
                              type="secondary"
                            >
                              {item.remark}
                            </Typography.Paragraph>
                          ) : null}
                        </Space>
                      }
                    />
                  </List.Item>
                )}
              />
            </ProCard>

            <ProCard title="最近集成动作" colSpan={{ xs: 24, lg: 12 }}>
              <List<ToolIntegrationAuditItem>
                dataSource={integrationAudits}
                locale={{
                  emptyText: (
                    <Empty
                      description="暂无工具集成动作"
                      image={Empty.PRESENTED_IMAGE_SIMPLE}
                    />
                  ),
                }}
                renderItem={(item) => (
                  <List.Item>
                    <List.Item.Meta
                      title={
                        <Space wrap size={[8, 4]}>
                          <Typography.Text strong>
                            {getOptionLabel(toolIntegrationActionOptions, item.actionType)}
                          </Typography.Text>
                          {renderToolTypeTag(item.toolType)}
                          {renderToolIntegrationAuditStatusTag(item.auditStatus)}
                          <Typography.Text type="secondary">
                            {item.previewFlag === 1 ? '预览' : '确认写入'}
                          </Typography.Text>
                        </Space>
                      }
                      description={
                        <Space direction="vertical" size={4} style={{ width: '100%' }}>
                          <Typography.Text type="secondary">
                            来源：
                            {integrationSourceTypeLabels[item.sourceObjectType || '']
                              || item.sourceObjectType
                              || '-'}
                            {item.sourceObjectId ? ` · ${item.sourceObjectId}` : ''}
                          </Typography.Text>
                          <Typography.Text type="secondary">
                            操作人：{item.operatorUserId || '-'} · {formatDateTime(item.createTime)}
                          </Typography.Text>
                          {item.errorMessage ? (
                            <Typography.Text type="danger">{item.errorMessage}</Typography.Text>
                          ) : null}
                          {item.externalObjectUrl ? (
                            <Typography.Link href={item.externalObjectUrl} rel="noreferrer" target="_blank">
                              打开外部对象
                            </Typography.Link>
                          ) : null}
                        </Space>
                      }
                    />
                  </List.Item>
                )}
              />
            </ProCard>
          </ProCard>

          <ProCard title="最近活动">
            {timelineItems.length ? (
              <Timeline items={timelineItems} />
            ) : (
              <Empty description="暂无最近活动" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </ProCard>
        </Space>
      ) : (
        <Empty description="未获取到项目详情" image={Empty.PRESENTED_IMAGE_SIMPLE} />
      )}

      <LinearProjectRepresentationModal
        open={linearRepresentationModalOpen}
        projectName={project?.projectName}
        onClose={() => {
          setLinearRepresentationModalOpen(false);
        }}
        onApplied={() => {
          if (projectId) {
            void loadDetail(projectId);
          }
        }}
        previewRequest={async (values) => {
          if (!projectId) {
            throw new Error('missing project id');
          }
          const response = await previewLinearProjectRepresentation(projectId, values);
          return response.result;
        }}
        applyRequest={async (values) => {
          if (!projectId) {
            throw new Error('missing project id');
          }
          const response = await applyLinearProjectRepresentation(projectId, values);
          return response.result;
        }}
      />

      <FigmaContextModal
        open={figmaContextModalOpen}
        onClose={() => {
          setFigmaContextModalOpen(false);
        }}
        onApplied={() => {
          if (projectId) {
            void loadDetail(projectId);
          }
        }}
        previewRequest={async (values) => {
          if (!projectId) {
            throw new Error('missing project id');
          }
          const response = await previewFigmaContext(projectId, values);
          return response.result;
        }}
        applyRequest={async (values) => {
          if (!projectId) {
            throw new Error('missing project id');
          }
          const response = await applyFigmaContext(projectId, values);
          return response.result;
        }}
      />

      <MeetingSummaryModal
        open={meetingModalOpen}
        projectId={projectId}
        projectName={project?.projectName}
        onClose={() => {
          setMeetingModalOpen(false);
        }}
        onSaved={() => {
          if (projectId) {
            void loadDetail(projectId);
          }
        }}
      />

      <ProductArchitectureBriefModal
        open={productArchitectureModalOpen}
        projectId={projectId}
        projectName={project?.projectName}
        onClose={() => {
          setProductArchitectureModalOpen(false);
        }}
        onSaved={() => {
          if (projectId) {
            void loadDetail(projectId);
          }
        }}
      />
    </Drawer>
  );
};
