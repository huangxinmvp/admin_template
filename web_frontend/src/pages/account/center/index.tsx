import { PageContainer, ProCard } from '@ant-design/pro-components';
import { history, useModel } from '@umijs/max';
import { Button, Col, Descriptions, Empty, Row, Space, Tag, Typography } from 'antd';
import React from 'react';

const quickLinks = [
  { path: '/account/inbox', label: '公告中心' },
  { path: '/account/sessions', label: '会话管理' },
  { path: '/system/users', label: '用户管理' },
  { path: '/system/roles', label: '角色管理' },
  { path: '/system/permissions', label: '权限菜单' },
  { path: '/system/message-center', label: '消息中心' },
  { path: '/system/announcements', label: '公告管理' },
  { path: '/system/announcement-sends', label: '公告送达' },
  { path: '/system/data-rules', label: '数据权限' },
  { path: '/system/tenants', label: '租户管理' },
  { path: '/system/files', label: '文件管理' },
];

const AccountCenter: React.FC = () => {
  const { initialState } = useModel('@@initialState');
  const currentUser = initialState?.currentUser;

  return (
    <PageContainer
      className="saas-page-container"
      title="个人中心"
      content="这里已不再使用示例文章/项目数据，而是展示当前登录用户的真实后台资料。"
    >
      {!currentUser ? (
        <Empty description="未获取到当前用户信息" />
      ) : (
        <Row gutter={[16, 16]}>
          <Col lg={14} xs={24}>
            <ProCard title="基础资料">
              <Descriptions column={{ xs: 1, sm: 2 }} size="middle">
                <Descriptions.Item label="用户名">
                  {currentUser.username || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="姓名">
                  {currentUser.realname || currentUser.name || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="租户">
                  {currentUser.tenantName || currentUser.tenantCode || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="邮箱">
                  {currentUser.email || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="手机号">
                  {currentUser.phone || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="未读公告">
                  {currentUser.unreadCount ?? 0}
                </Descriptions.Item>
              </Descriptions>
            </ProCard>
          </Col>
          <Col lg={10} xs={24}>
            <ProCard title="权限摘要">
              <Typography.Paragraph>
                角色数量：{currentUser.roles?.length || 0}
              </Typography.Paragraph>
              <Typography.Paragraph>
                权限数量：{currentUser.permissions?.length || 0}
              </Typography.Paragraph>
              <Space wrap>
                {(currentUser.roles || []).map((role: string) => (
                  <Tag key={role} color="blue">
                    {role}
                  </Tag>
                ))}
              </Space>
            </ProCard>
          </Col>
          <Col span={24}>
            <ProCard title="快捷入口">
              <Space wrap>
                {quickLinks.map((item) => (
                  <Button
                    key={item.path}
                    onClick={() => {
                      history.push(item.path);
                    }}
                  >
                    {item.label}
                  </Button>
                ))}
              </Space>
            </ProCard>
          </Col>
        </Row>
      )}
    </PageContainer>
  );
};

export default AccountCenter;
