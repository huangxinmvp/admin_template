import type { ActionType, ProColumns } from '@ant-design/pro-components';
import {
  ModalForm,
  PageContainer,
  ProCard,
  ProFormDateTimePicker,
  ProFormText,
  ProTable,
  StatisticCard,
} from '@ant-design/pro-components';
import { history, useModel } from '@umijs/max';
import dayjs from 'dayjs';
import { App, Button, Col, Popconfirm, Row, Space, Tag, Typography } from 'antd';
import React, { useEffect, useMemo, useRef, useState } from 'react';
import {
  forceLogoutUserSessions,
  getUserSessionCounts,
} from '@/services/backend/auth';
import { clearAuthSession } from '@/utils/auth';
import { queryResourcePage } from '@/services/backend/resources';
import {
  getSystemConfigGroups,
  lockUser,
  resetUserPassword,
  unlockUser,
} from '@/services/backend/system';
import type { GenericRecord, SystemConfigGroup } from '@/services/backend/types';

const getConfigValue = (
  groups: SystemConfigGroup[],
  configKey: string,
  fallback = '-',
) => {
  const item = groups.flatMap((group) => group.items).find((current) => current.configKey === configKey);
  return item?.configValue ?? item?.defaultValue ?? fallback;
};

const SecurityCenterPage: React.FC = () => {
  const actionRef = useRef<ActionType | null>(null);
  const { message } = App.useApp();
  const { initialState, setInitialState } = useModel('@@initialState');
  const [configGroups, setConfigGroups] = useState<SystemConfigGroup[]>([]);
  const [resetTarget, setResetTarget] = useState<GenericRecord>();
  const [lockTarget, setLockTarget] = useState<GenericRecord>();

  useEffect(() => {
    getSystemConfigGroups()
      .then((response) => {
        setConfigGroups(response.result || []);
      })
      .catch(() => {
        setConfigGroups([]);
      });
  }, []);

  const policySummary = useMemo(() => {
    const rules: string[] = [];
    if (getConfigValue(configGroups, 'security.password.requireUppercase', 'true') === 'true') {
      rules.push('大写字母');
    }
    if (getConfigValue(configGroups, 'security.password.requireLowercase', 'true') === 'true') {
      rules.push('小写字母');
    }
    if (getConfigValue(configGroups, 'security.password.requireDigit', 'true') === 'true') {
      rules.push('数字');
    }
    if (getConfigValue(configGroups, 'security.password.requireSpecial', 'true') === 'true') {
      rules.push('特殊字符');
    }
    return rules.length ? rules.join('、') : '无额外复杂度要求';
  }, [configGroups]);

  const columns: ProColumns<GenericRecord>[] = [
    {
      title: '用户名',
      dataIndex: 'username',
      width: 150,
    },
    {
      title: '姓名',
      dataIndex: 'realname',
      width: 130,
      search: false,
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      width: 140,
      search: false,
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      width: 220,
      search: false,
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      valueType: 'select',
      valueEnum: {
        0: { text: '冻结' },
        1: { text: '正常' },
      },
      render: (_, record) =>
        Number(record.status) === 1 ? <Tag color="success">正常</Tag> : <Tag color="error">冻结</Tag>,
    },
    {
      title: '活跃会话',
      dataIndex: 'activeSessionCount',
      width: 110,
      search: false,
      render: (_, record) => (
        <Tag color={Number(record.activeSessionCount) > 0 ? 'processing' : 'default'}>
          {record.activeSessionCount ?? 0}
        </Tag>
      ),
    },
    {
      title: '锁定到',
      dataIndex: 'lockUntil',
      valueType: 'dateTime',
      width: 180,
      search: false,
      render: (_, record) =>
        record.lockUntil ? dayjs(record.lockUntil).format('YYYY-MM-DD HH:mm:ss') : '-',
    },
    {
      title: '角色',
      dataIndex: 'roleSummary',
      width: 220,
      search: false,
      ellipsis: true,
    },
    {
      title: '操作',
      key: 'option',
      valueType: 'option',
      width: 220,
      fixed: 'right',
      render: (_, record) => [
        <a
          key="reset-password"
          onClick={() => {
            setResetTarget(record);
          }}
        >
          重置密码
        </a>,
        <a
          key="lock"
          onClick={() => {
            setLockTarget(record);
          }}
        >
          锁定
        </a>,
        Number(record.lockUntil) || record.lockUntil ? (
          <Popconfirm
            key="unlock"
            title="确认解除该用户的锁定状态吗？"
            onConfirm={async () => {
              await unlockUser(record.id);
              message.success('已解除锁定');
              actionRef.current?.reload();
            }}
          >
            <a>解锁</a>
          </Popconfirm>
        ) : null,
        <Popconfirm
          key="force-offline"
          title="确认强制下线该用户的全部活跃会话吗？"
          onConfirm={async () => {
            await forceLogoutUserSessions(record.id);
            message.success('已强制下线该用户的活跃会话');
            if (record.id === initialState?.currentUser?.userId) {
              clearAuthSession();
              setInitialState((state: any) => ({ ...state, currentUser: undefined }));
              history.replace('/user/login');
              return;
            }
            actionRef.current?.reload();
          }}
        >
          <a>强制下线</a>
        </Popconfirm>,
      ],
    },
  ];

  return (
    <PageContainer
      className="saas-page-container saas-resource-page"
      title="安全中心"
      content="这里集中处理密码策略、令牌策略，以及管理员对用户进行重置密码、锁定和解锁的操作。"
    >
      <Row gutter={[16, 16]}>
        <Col lg={8} xs={24}>
          <StatisticCard
            statistic={{
              title: '密码长度要求',
              value: `${getConfigValue(configGroups, 'security.password.minLength', '8')} - ${getConfigValue(configGroups, 'security.password.maxLength', '32')} 位`,
            }}
          />
        </Col>
        <Col lg={8} xs={24}>
          <StatisticCard
            statistic={{
              title: '密码复杂度',
              value: policySummary,
            }}
          />
        </Col>
        <Col lg={8} xs={24}>
          <StatisticCard
            statistic={{
              title: '访问/刷新令牌',
              value: `${getConfigValue(configGroups, 'security.jwt.expSeconds', '3600')}s / ${getConfigValue(configGroups, 'security.jwt.refreshExpSeconds', '1209600')}s`,
            }}
          />
        </Col>
        <Col span={24}>
          <ProCard>
            <Typography.Paragraph type="secondary">
              密码策略由系统配置中心维护。这里的用户安全操作会直接调用后端真实接口，并立即影响登录态与账户锁定状态。
            </Typography.Paragraph>
            <ProTable<GenericRecord>
              actionRef={actionRef}
              cardBordered
              columns={columns}
              pagination={{
                defaultPageSize: 10,
                showQuickJumper: true,
                showSizeChanger: true,
              }}
              rowKey="id"
              scroll={{ x: 1200 }}
              search={{
                labelWidth: 100,
              }}
              request={async (params) => {
                const response = await queryResourcePage('/api/user', {
                  pageNo: params.current || 1,
                  pageSize: params.pageSize || 10,
                  username: params.username,
                  status: params.status,
                });
                const records = response.result.records || [];
                const userIds = records
                  .map((item) => String(item.id || ''))
                  .filter(Boolean);
                const countResponse = userIds.length
                  ? await getUserSessionCounts(userIds)
                  : undefined;
                const countMap = countResponse?.result || {};
                return {
                  data: records.map((item) => ({
                    ...item,
                    activeSessionCount: countMap[String(item.id)] ?? 0,
                  })),
                  success: response.success,
                  total: response.result.total || 0,
                };
              }}
              toolBarRender={() => [
                <Button
                  key="config"
                  onClick={() => {
                    history.push('/system/config-center');
                  }}
                >
                  前往系统配置
                </Button>,
                <Button
                  key="my-sessions"
                  onClick={() => {
                    history.push('/account/sessions');
                  }}
                >
                  查看我的会话
                </Button>,
              ]}
            />
          </ProCard>
        </Col>
      </Row>

      <ModalForm<{
        password: string;
        confirmPassword: string;
      }>
        open={Boolean(resetTarget)}
        title={resetTarget ? `重置密码 - ${resetTarget.username}` : '重置密码'}
        onOpenChange={(open) => {
          if (!open) {
            setResetTarget(undefined);
          }
        }}
        onFinish={async (values) => {
          if (!resetTarget) {
            return false;
          }
          if (values.password !== values.confirmPassword) {
            message.error('两次输入的新密码不一致');
            return false;
          }
          await resetUserPassword(resetTarget.id, values.password);
          message.success('密码已重置');
          setResetTarget(undefined);
          actionRef.current?.reload();
          return true;
        }}
      >
        <ProFormText.Password
          name="password"
          label="新密码"
          rules={[{ required: true, message: '请输入新密码' }]}
        />
        <ProFormText.Password
          name="confirmPassword"
          label="确认密码"
          rules={[{ required: true, message: '请再次输入新密码' }]}
        />
      </ModalForm>

      <ModalForm<{
        lockUntil: dayjs.Dayjs;
      }>
        initialValues={{
          lockUntil: dayjs().add(1, 'day'),
        }}
        open={Boolean(lockTarget)}
        title={lockTarget ? `锁定账户 - ${lockTarget.username}` : '锁定账户'}
        onOpenChange={(open) => {
          if (!open) {
            setLockTarget(undefined);
          }
        }}
        onFinish={async (values) => {
          if (!lockTarget) {
            return false;
          }
          await lockUser(lockTarget.id, values.lockUntil.format('YYYY-MM-DDTHH:mm:ss'));
          message.success('用户已锁定');
          setLockTarget(undefined);
          actionRef.current?.reload();
          return true;
        }}
      >
        <ProFormDateTimePicker
          name="lockUntil"
          label="锁定到"
          rules={[{ required: true, message: '请选择锁定截止时间' }]}
        />
      </ModalForm>
    </PageContainer>
  );
};

export default SecurityCenterPage;
