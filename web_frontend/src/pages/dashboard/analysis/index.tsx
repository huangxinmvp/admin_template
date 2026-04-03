import { PageContainer, ProCard, StatisticCard } from '@ant-design/pro-components';
import { history, useModel, useRequest } from '@umijs/max';
import {
  Button,
  Col,
  Descriptions,
  Empty,
  List,
  Progress,
  Row,
  Space,
  Table,
  type TableColumnsType,
  Tag,
  Timeline,
  Typography,
} from 'antd';
import React from 'react';
import {
  renderBudgetStatusTag,
  renderRiskLevelTag,
} from '@/features/aicoos/projectCenter';
import { formatDateTime } from '@/features/workflow/utils';
import { getDashboardStats } from '@/services/backend/system';
import type {
  DashboardAttentionProjectItem,
  DashboardRecentActivityItem,
  DashboardStats,
} from '@/services/backend/types';

const overviewCardConfig = [
  { key: 'totalProjectCount', title: '项目总数' },
  { key: 'activeProjectCount', title: '进行中项目' },
  { key: 'pendingDecisionCount', title: '待处理决策' },
  { key: 'pendingApprovalCount', title: '待处理审批' },
  { key: 'blockedProjectCount', title: '阻塞项目' },
  { key: 'highRiskProjectCount', title: '高风险项目' },
  { key: 'budgetWarningProjectCount', title: '预算预警项目' },
  { key: 'budgetExceededProjectCount', title: '预算超支项目' },
] as const;

const stageColorMap: Record<string, string> = {
  intake: '#1677ff',
  clarification: '#13c2c2',
  feasibility: '#2f54eb',
  estimation: '#722ed1',
  approval: '#faad14',
  planning: '#52c41a',
  design: '#eb2f96',
  development: '#1890ff',
  testing: '#fa8c16',
  release_approval: '#f5222d',
  release: '#52c41a',
  retrospective: '#8c8c8c',
};

const activityColor = (activityType?: string | null) => {
  const colorMap: Record<string, string> = {
    project: 'blue',
    intake: 'cyan',
    clarification: 'gold',
    decision: 'orange',
    approval: 'purple',
    budget: 'green',
    stage: 'gray',
  };
  return colorMap[activityType || ''] || 'blue';
};

const AnalysisDashboard: React.FC = () => {
  const { initialState } = useModel('@@initialState');
  const currentUser = initialState?.currentUser;
  const { data, loading } = useRequest(async () => {
    try {
      return await getDashboardStats();
    } catch {
      return undefined;
    }
  });

  const stats = (data?.result ?? data) as DashboardStats | undefined;
  const overview = stats?.overview;
  const stageDistribution = stats?.stageDistribution || [];
  const bottlenecks = stats?.bottleneckSummary;
  const budgetHealth = stats?.budgetHealth;
  const attentionProjects = stats?.attentionProjects || [];
  const recentActivities = stats?.recentGovernanceActivities || [];
  const totalProjects = overview?.totalProjectCount || 0;

  const attentionColumns: TableColumnsType<DashboardAttentionProjectItem> = [
    {
      title: '项目',
      dataIndex: 'projectName',
      width: 240,
      render: (_, record) => (
        <Space direction="vertical" size={2}>
          <Typography.Text strong>{record.projectName || '-'}</Typography.Text>
          <Typography.Text type="secondary">{record.projectCode || '-'}</Typography.Text>
        </Space>
      ),
    },
    {
      title: '当前阶段',
      dataIndex: 'currentStageName',
      width: 140,
    },
    {
      title: '风险等级',
      dataIndex: 'riskLevel',
      width: 120,
      render: (_, record) => renderRiskLevelTag(record.riskLevel),
    },
    {
      title: '阻塞原因',
      dataIndex: 'blockingReason',
      ellipsis: true,
      width: 260,
    },
    {
      title: '预算状态',
      dataIndex: 'budgetStatus',
      width: 120,
      render: (_, record) => renderBudgetStatusTag(record.budgetStatus),
    },
    {
      title: '待审批',
      dataIndex: 'pendingApprovalCount',
      width: 90,
    },
    {
      title: '待决策',
      dataIndex: 'pendingDecisionCount',
      width: 90,
    },
    {
      title: '最后更新',
      dataIndex: 'updateTime',
      width: 170,
      render: (value) => formatDateTime(value as string),
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, record) => (
        <a
          onClick={() => {
            if (record.projectId) {
              history.push(`/aicoos/projects?projectId=${record.projectId}`);
            }
          }}
        >
          查看项目
        </a>
      ),
    },
  ];

  return (
    <PageContainer
      className="saas-page-container"
      title="运营驾驶舱"
      content="从公司级视角查看项目健康、治理瓶颈、预算压力、角色覆盖和最近治理活动。"
      extra={[
        <Button
          key="projects"
          onClick={() => {
            history.push('/aicoos/projects');
          }}
        >
          项目中心
        </Button>,
        <Button
          key="approvals"
          onClick={() => {
            history.push('/aicoos/approval-center');
          }}
        >
          审批中心
        </Button>,
        <Button
          key="budgets"
          onClick={() => {
            history.push('/aicoos/budget-center');
          }}
          type="primary"
        >
          预算中心
        </Button>,
      ]}
    >
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <ProCard gutter={16} wrap>
            <ProCard colSpan={{ xs: 24, xl: 10 }} title="当前用户" loading={loading}>
              <Descriptions column={{ xs: 1, sm: 2 }} size="small">
                <Descriptions.Item label="用户名">
                  {currentUser?.username || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="姓名">
                  {currentUser?.realname || currentUser?.name || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="租户">
                  {currentUser?.tenantName || currentUser?.tenantCode || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="未读公告">
                  {currentUser?.unreadCount ?? 0}
                </Descriptions.Item>
                <Descriptions.Item label="角色" span={2}>
                  <Space wrap>
                    {(currentUser?.roles || []).length ? (
                      (currentUser?.roles || []).map((role: string) => (
                        <Tag key={role} color="blue">
                          {role}
                        </Tag>
                      ))
                    ) : (
                      <span>-</span>
                    )}
                  </Space>
                </Descriptions.Item>
              </Descriptions>
            </ProCard>

            <ProCard colSpan={{ xs: 24, xl: 14 }} title="平台快照" loading={loading}>
              <Row gutter={[12, 12]}>
                <Col md={6} xs={12}>
                  <StatisticCard statistic={{ title: '租户', value: stats?.tenantCount || 0 }} />
                </Col>
                <Col md={6} xs={12}>
                  <StatisticCard statistic={{ title: '用户', value: stats?.userCount || 0 }} />
                </Col>
                <Col md={6} xs={12}>
                  <StatisticCard statistic={{ title: '角色', value: stats?.roleCount || 0 }} />
                </Col>
                <Col md={6} xs={12}>
                  <StatisticCard statistic={{ title: '定时任务', value: stats?.quartzJobCount || 0 }} />
                </Col>
              </Row>
            </ProCard>
          </ProCard>
        </Col>

        <Col span={24}>
          <ProCard title="运营总览" loading={loading}>
            {overview ? (
              <Row gutter={[16, 16]}>
                {overviewCardConfig.map((item) => (
                  <Col key={item.key} lg={6} md={8} sm={12} xs={24}>
                    <StatisticCard
                      statistic={{
                        title: item.title,
                        value: overview[item.key] || 0,
                      }}
                    />
                  </Col>
                ))}
              </Row>
            ) : (
              <Empty description="暂无项目运营数据" />
            )}
          </ProCard>
        </Col>

        <Col xl={10} xs={24}>
          <ProCard title="阶段分布" loading={loading}>
            {stageDistribution.length ? (
              <List
                dataSource={stageDistribution}
                renderItem={(item) => {
                  const percent =
                    totalProjects > 0 ? Math.round(((item.projectCount || 0) * 100) / totalProjects) : 0;
                  return (
                    <List.Item>
                      <div style={{ width: '100%' }}>
                        <Space
                          align="center"
                          style={{ justifyContent: 'space-between', width: '100%', marginBottom: 8 }}
                        >
                          <Space size={8}>
                            <Tag color={stageColorMap[item.stageCode || ''] || 'blue'}>
                              {item.stageName || item.stageCode || '-'}
                            </Tag>
                          </Space>
                          <Typography.Text>{item.projectCount || 0} 个</Typography.Text>
                        </Space>
                        <Progress percent={percent} showInfo={false} strokeColor={stageColorMap[item.stageCode || ''] || '#1677ff'} />
                      </div>
                    </List.Item>
                  );
                }}
              />
            ) : (
              <Empty description="暂无阶段分布数据" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </ProCard>
        </Col>

        <Col xl={14} xs={24}>
          <ProCard title="治理瓶颈" loading={loading}>
            {bottlenecks ? (
              <Row gutter={[16, 16]}>
                <Col md={8} xs={12}>
                  <StatisticCard statistic={{ title: '阻塞澄清项目', value: bottlenecks.blockerClarificationProjectCount }} />
                </Col>
                <Col md={8} xs={12}>
                  <StatisticCard statistic={{ title: '待决策项目', value: bottlenecks.pendingDecisionProjectCount }} />
                </Col>
                <Col md={8} xs={12}>
                  <StatisticCard statistic={{ title: '阻塞审批项目', value: bottlenecks.blockingApprovalProjectCount }} />
                </Col>
                <Col md={8} xs={12}>
                  <StatisticCard statistic={{ title: '预算预警/超支', value: bottlenecks.budgetWarningOrExceededProjectCount }} />
                </Col>
                <Col md={8} xs={12}>
                  <StatisticCard statistic={{ title: '缺失关键角色', value: bottlenecks.missingCriticalRoleProjectCount }} />
                </Col>
                <Col md={8} xs={12}>
                  <StatisticCard statistic={{ title: '失败门禁条件', value: bottlenecks.failedGateConditionProjectCount }} />
                </Col>
              </Row>
            ) : (
              <Empty description="暂无治理瓶颈数据" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </ProCard>
        </Col>

        <Col xl={10} xs={24}>
          <ProCard title="预算健康概览" loading={loading}>
            {budgetHealth ? (
              <Space direction="vertical" size={12} style={{ display: 'flex' }}>
                <Descriptions column={1} size="small">
                  <Descriptions.Item label="未规划项目">{budgetHealth.unplannedProjectCount}</Descriptions.Item>
                  <Descriptions.Item label="待审批项目">{budgetHealth.pendingProjectCount}</Descriptions.Item>
                  <Descriptions.Item label="健康项目">{budgetHealth.healthyProjectCount}</Descriptions.Item>
                  <Descriptions.Item label="预警项目">{budgetHealth.warningProjectCount}</Descriptions.Item>
                  <Descriptions.Item label="超支项目">{budgetHealth.overrunProjectCount}</Descriptions.Item>
                </Descriptions>
                <Row gutter={[12, 12]}>
                  <Col span={12}>
                    <StatisticCard statistic={{ title: '预算健康', value: budgetHealth.healthyProjectCount }} />
                  </Col>
                  <Col span={12}>
                    <StatisticCard statistic={{ title: '预算风险', value: budgetHealth.warningProjectCount + budgetHealth.overrunProjectCount }} />
                  </Col>
                </Row>
              </Space>
            ) : (
              <Empty description="暂无预算健康数据" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </ProCard>
        </Col>

        <Col xl={14} xs={24}>
          <ProCard title="最近治理活动" loading={loading}>
            {recentActivities.length ? (
              <Timeline
                items={recentActivities.map((item: DashboardRecentActivityItem, index) => ({
                  key: `${item.projectId || 'project'}-${index}`,
                  color: activityColor(item.activityType),
                  children: (
                    <Space direction="vertical" size={2}>
                      <Space wrap size={[8, 4]}>
                        <Typography.Text strong>{item.title || '治理活动'}</Typography.Text>
                        <Tag>{item.projectName || '-'}</Tag>
                        <Typography.Text type="secondary">
                          {formatDateTime(item.occurredAt)}
                        </Typography.Text>
                      </Space>
                      <Typography.Paragraph style={{ marginBottom: 0 }} type="secondary">
                        {item.description || '-'}
                      </Typography.Paragraph>
                    </Space>
                  ),
                }))}
              />
            ) : (
              <Empty description="暂无治理活动" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </ProCard>
        </Col>

        <Col span={24}>
          <ProCard title="需要关注的项目" loading={loading}>
            <Table<DashboardAttentionProjectItem>
              columns={attentionColumns}
              dataSource={attentionProjects}
              locale={{ emptyText: '暂无需要关注的项目' }}
              pagination={false}
              rowKey="projectId"
              scroll={{ x: 1200 }}
              size="small"
            />
          </ProCard>
        </Col>
      </Row>
    </PageContainer>
  );
};

export default AnalysisDashboard;
