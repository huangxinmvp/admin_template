import { PageContainer, ProCard, StatisticCard } from '@ant-design/pro-components';
import { useModel, useRequest } from '@umijs/max';
import { Col, Descriptions, Empty, Row, Space, Tag } from 'antd';
import React from 'react';
import { getDashboardStats } from '@/services/backend/system';

const statItems = [
  { key: 'tenantCount', title: '租户数' },
  { key: 'userCount', title: '用户数' },
  { key: 'enabledUserCount', title: '启用用户' },
  { key: 'roleCount', title: '角色数' },
  { key: 'permissionCount', title: '权限数' },
  { key: 'departCount', title: '部门数' },
  { key: 'announcementCount', title: '公告数' },
  { key: 'quartzJobCount', title: '定时任务数' },
] as const;

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
  const stats = (data?.result ?? data) as
    | Record<string, number | undefined>
    | undefined;

  return (
    <PageContainer
      className="saas-page-container"
      title="数据概览"
      content="仪表盘已改为对接后端 /api/system/dashboard，并结合当前登录用户资料展示。"
    >
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <ProCard title="当前用户" loading={loading}>
            <Descriptions column={{ xs: 1, sm: 2 }} size="middle">
              <Descriptions.Item label="用户名">
                {currentUser?.username || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="姓名">
                {currentUser?.realname || currentUser?.name || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="租户">
                {currentUser?.tenantName || currentUser?.tenantCode || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="邮箱">
                {currentUser?.email || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="手机号">
                {currentUser?.phone || '-'}
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
        </Col>

        {stats ? (
          statItems.map((item) => (
            <Col key={item.key} lg={6} md={8} sm={12} xs={24}>
              <StatisticCard
                loading={loading}
                statistic={{
                  title: item.title,
                  value: stats[item.key],
                }}
              />
            </Col>
          ))
        ) : (
          <Col span={24}>
            <Empty description="当前账号无法访问仪表盘统计，已保留个人资料与其余业务页面入口。" />
          </Col>
        )}
      </Row>
    </PageContainer>
  );
};

export default AnalysisDashboard;
