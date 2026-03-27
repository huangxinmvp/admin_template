import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { PageContainer, ProCard, ProTable, StatisticCard } from '@ant-design/pro-components';
import { history, useModel } from '@umijs/max';
import dayjs from 'dayjs';
import { App, Button, Popconfirm, Row, Col, Space, Tag, Typography } from 'antd';
import React, { useMemo, useRef, useState } from 'react';
import { getCurrentUserSessions, logout, revokeUserSession } from '@/services/backend/auth';
import type { UserSession } from '@/services/backend/types';
import { getSessionId } from '@/utils/auth';

const AccountSessionsPage: React.FC = () => {
  const { message } = App.useApp();
  const actionRef = useRef<ActionType | null>(null);
  const { initialState, setInitialState } = useModel('@@initialState');
  const currentSessionId = getSessionId();
  const [dataSource, setDataSource] = useState<UserSession[]>([]);

  const currentSession = useMemo(
    () => dataSource.find((item) => item.id === currentSessionId),
    [currentSessionId, dataSource],
  );

  const handleOffline = async (record: UserSession) => {
    if (record.id === currentSessionId) {
      setInitialState((state: any) => ({ ...state, currentUser: undefined }));
      await logout();
      message.success('当前会话已退出');
      history.replace('/user/login');
      return;
    }
    await revokeUserSession(record.id);
    message.success('会话已下线');
    actionRef.current?.reload();
  };

  const columns: ProColumns<UserSession>[] = [
    {
      title: '设备',
      dataIndex: 'deviceName',
      width: 180,
      render: (_, record) => (
        <Space>
          <span>{record.deviceName || '未知设备'}</span>
          {record.id === currentSessionId ? <Tag color="processing">当前设备</Tag> : null}
        </Space>
      ),
    },
    {
      title: 'IP',
      dataIndex: 'clientIp',
      width: 140,
    },
    {
      title: '登录时间',
      dataIndex: 'loginTime',
      width: 180,
      render: (_, record) => (record.loginTime ? dayjs(record.loginTime).format('YYYY-MM-DD HH:mm:ss') : '-'),
    },
    {
      title: '最后活跃',
      dataIndex: 'lastActiveTime',
      width: 180,
      render: (_, record) => (record.lastActiveTime ? dayjs(record.lastActiveTime).format('YYYY-MM-DD HH:mm:ss') : '-'),
    },
    {
      title: '访问令牌到期',
      dataIndex: 'accessExpiresAt',
      width: 180,
      render: (_, record) => (record.accessExpiresAt ? dayjs(record.accessExpiresAt).format('YYYY-MM-DD HH:mm:ss') : '-'),
    },
    {
      title: '刷新令牌到期',
      dataIndex: 'refreshExpiresAt',
      width: 180,
      render: (_, record) => (record.refreshExpiresAt ? dayjs(record.refreshExpiresAt).format('YYYY-MM-DD HH:mm:ss') : '-'),
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 110,
      render: (_, record) =>
        Number(record.status) === 1 ? <Tag color="success">在线</Tag> : <Tag color="default">离线</Tag>,
    },
    {
      title: '操作',
      key: 'option',
      width: 160,
      fixed: 'right',
      render: (_, record) => [
        <Popconfirm
          key="offline"
          title={record.id === currentSessionId ? '确认退出当前登录吗？' : '确认下线这个会话吗？'}
          onConfirm={() => handleOffline(record)}
        >
          <a>{record.id === currentSessionId ? '退出登录' : '下线会话'}</a>
        </Popconfirm>,
      ],
    },
  ];

  return (
    <PageContainer
      className="saas-page-container saas-resource-page"
      title="会话管理"
      content="这里展示当前账号的登录设备与活跃会话。你可以主动下线其他设备，也可以直接退出当前登录。"
    >
      <Row gutter={[16, 16]}>
        <Col lg={8} xs={24}>
          <StatisticCard statistic={{ title: '活跃会话数', value: dataSource.filter((item) => Number(item.status) === 1).length }} />
        </Col>
        <Col lg={8} xs={24}>
          <StatisticCard statistic={{ title: '当前设备', value: currentSession?.deviceName || '-' }} />
        </Col>
        <Col lg={8} xs={24}>
          <StatisticCard statistic={{ title: '当前租户', value: initialState?.currentUser?.tenantName || initialState?.currentUser?.tenantCode || '-' }} />
        </Col>
      </Row>

      <ProCard style={{ marginTop: 16 }}>
        <Typography.Paragraph type="secondary">
          当前会话改为服务端可管理模式，管理员强制下线或本人退出后，该设备上的 token 会立即失效。
        </Typography.Paragraph>
        <ProTable<UserSession>
          actionRef={actionRef}
          cardBordered
          columns={columns}
          dataSource={dataSource}
          pagination={false}
          rowKey="id"
          scroll={{ x: 1200 }}
          search={false}
          request={async () => {
            const response = await getCurrentUserSessions();
            const sessions = response.result || [];
            setDataSource(sessions);
            return {
              data: sessions,
              success: response.success,
              total: sessions.length,
            };
          }}
          toolBarRender={() => [
            <Button key="reload" onClick={() => actionRef.current?.reload()}>
              刷新会话
            </Button>,
          ]}
        />
      </ProCard>
    </PageContainer>
  );
};

export default AccountSessionsPage;
