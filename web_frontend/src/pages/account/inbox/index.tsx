import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { PageContainer, ProTable } from '@ant-design/pro-components';
import { useModel } from '@umijs/max';
import {
  Button,
  Descriptions,
  Drawer,
  Space,
  Switch,
  Tag,
  Typography,
  message,
} from 'antd';
import dayjs from 'dayjs';
import React, { useRef, useState } from 'react';
import { INLINE_FORM_LABEL_WIDTH } from '@/constants/formLayout';
import {
  getAnnouncementDetail,
  markAnnouncementRead,
  queryAnnouncementInboxPage,
} from '@/services/backend/system';
import type { GenericRecord } from '@/services/backend/types';

const priorityMap: Record<number, { label: string; color: string }> = {
  1: { label: '低', color: 'default' },
  2: { label: '中', color: 'processing' },
  3: { label: '高', color: 'error' },
};

const messageTypeMap: Record<number, string> = {
  1: '通知公告',
  2: '系统消息',
};

const categoryMap: Record<number, string> = {
  1: '通知',
  2: '系统消息',
};

const renderDateTime = (value?: string | null) =>
  value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-';

const InboxPage: React.FC = () => {
  const actionRef = useRef<ActionType | null>(null);
  const [messageApi, contextHolder] = message.useMessage();
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailRecord, setDetailRecord] = useState<GenericRecord>();
  const [unreadOnly, setUnreadOnly] = useState(false);
  const { initialState, setInitialState } = useModel('@@initialState');

  const refreshCurrentUser = async () => {
    if (!initialState?.fetchUserInfo) {
      return;
    }

    const currentUser = await initialState.fetchUserInfo();
    setInitialState((state: any) => ({
      ...state,
      currentUser,
    }));
  };

  const openDetail = async (record: GenericRecord) => {
    const shouldAutoRead = Number(record.readFlag) === 0;
    setDetailLoading(true);
    try {
      const response = await getAnnouncementDetail(record.id, shouldAutoRead);
      setDetailRecord(response.result);
      setDetailOpen(true);

      if (shouldAutoRead) {
        actionRef.current?.reload();
        await refreshCurrentUser();
      }
    } finally {
      setDetailLoading(false);
    }
  };

  const handleMarkRead = async (record: GenericRecord) => {
    await markAnnouncementRead(record.id);
    messageApi.success('已标记为已读');
    actionRef.current?.reload();
    await refreshCurrentUser();
  };

  const columns: ProColumns<GenericRecord>[] = [
    {
      title: '标题',
      dataIndex: 'title',
      width: 260,
      ellipsis: true,
    },
    {
      title: '发布人',
      dataIndex: 'sender',
      width: 120,
      search: false,
    },
    {
      title: '优先级',
      dataIndex: 'priority',
      width: 100,
      search: false,
      render: (_, record) => {
        const current = priorityMap[Number(record.priority)] || {
          label: String(record.priority ?? '-'),
          color: 'default',
        };
        return <Tag color={current.color}>{current.label}</Tag>;
      },
    },
    {
      title: '类别',
      dataIndex: 'msgCategory',
      width: 110,
      search: false,
      render: (_, record) => categoryMap[Number(record.msgCategory)] || '-',
    },
    {
      title: '类型',
      dataIndex: 'msgType',
      width: 120,
      search: false,
      render: (_, record) => messageTypeMap[Number(record.msgType)] || '-',
    },
    {
      title: '发布时间',
      dataIndex: 'sendTime',
      valueType: 'dateTime',
      width: 180,
      search: false,
    },
    {
      title: '阅读状态',
      dataIndex: 'readFlag',
      width: 110,
      search: false,
      render: (_, record) =>
        Number(record.readFlag) === 1 ? (
          <Tag color="success">已读</Tag>
        ) : (
          <Tag color="warning">未读</Tag>
        ),
    },
    {
      title: '阅读时间',
      dataIndex: 'readTime',
      valueType: 'dateTime',
      width: 180,
      search: false,
    },
    {
      title: '操作',
      key: 'option',
      valueType: 'option',
      width: 160,
      fixed: 'right',
      render: (_, record) => [
        <a
          key="view"
          onClick={() => {
            void openDetail(record);
          }}
        >
          查看
        </a>,
        Number(record.readFlag) === 0 ? (
          <a
            key="read"
            onClick={() => {
              void handleMarkRead(record);
            }}
          >
            标记已读
          </a>
        ) : null,
      ],
    },
  ];

  return (
    <PageContainer
      className="saas-page-container saas-inbox-page"
      title="公告中心"
      content="这里汇总当前账号收到的系统公告与消息，打开详情时会自动完成已读同步。"
      extra={[
        <Tag key="unread-count" color="processing">
          未读 {initialState?.currentUser?.unreadCount ?? 0}
        </Tag>,
      ]}
    >
      {contextHolder}
      <ProTable<GenericRecord>
        actionRef={actionRef}
        rowKey="id"
        cardBordered
        columns={columns}
        search={false}
        options={{
          density: false,
          fullScreen: true,
          reload: true,
          setting: true,
        }}
        pagination={{
          showQuickJumper: true,
          showSizeChanger: true,
          defaultPageSize: 10,
          showTotal: (total) => `共 ${total} 条`,
        }}
        scroll={{ x: 1220 }}
        request={async (params) => {
          const response = await queryAnnouncementInboxPage({
            pageNo: Number(params.current || 1),
            pageSize: Number(params.pageSize || 10),
            unreadOnly,
          });

          return {
            data: response.result.records || [],
            success: response.success,
            total: response.result.total || 0,
          };
        }}
        toolBarRender={() => [
          <Space key="filter" size={12}>
            <span>仅看未读</span>
            <Switch
              checked={unreadOnly}
              onChange={(checked) => {
                setUnreadOnly(checked);
                actionRef.current?.reload();
              }}
            />
          </Space>,
          <Button
            key="refresh"
            onClick={() => {
              actionRef.current?.reload();
            }}
          >
            刷新列表
          </Button>,
        ]}
      />

      <Drawer
        destroyOnHidden
        extra={
          detailRecord && Number(detailRecord.readFlag) === 0 ? (
            <Button
              type="primary"
              onClick={() => {
                void handleMarkRead(detailRecord);
              }}
            >
              标记已读
            </Button>
          ) : null
        }
        loading={detailLoading}
        open={detailOpen}
        title={detailRecord?.title || '公告详情'}
        width={760}
        onClose={() => {
          setDetailOpen(false);
          setDetailRecord(undefined);
        }}
      >
        <Descriptions
          bordered
          column={2}
          labelStyle={{ width: INLINE_FORM_LABEL_WIDTH }}
          size="middle"
        >
          <Descriptions.Item label="标题" span={2}>
            {detailRecord?.title || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="发布人">
            {detailRecord?.sender || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="优先级">
            {detailRecord ? (
              <Tag color={priorityMap[Number(detailRecord.priority)]?.color || 'default'}>
                {priorityMap[Number(detailRecord.priority)]?.label || detailRecord.priority || '-'}
              </Tag>
            ) : (
              '-'
            )}
          </Descriptions.Item>
          <Descriptions.Item label="类别">
            {categoryMap[Number(detailRecord?.msgCategory)] || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="类型">
            {messageTypeMap[Number(detailRecord?.msgType)] || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="发布时间">
            {renderDateTime(detailRecord?.sendTime)}
          </Descriptions.Item>
          <Descriptions.Item label="阅读时间">
            {renderDateTime(detailRecord?.readTime)}
          </Descriptions.Item>
          <Descriptions.Item label="状态">
            {Number(detailRecord?.readFlag) === 1 ? (
              <Tag color="success">已读</Tag>
            ) : (
              <Tag color="warning">未读</Tag>
            )}
          </Descriptions.Item>
          <Descriptions.Item label="内容" span={2}>
            <Typography.Paragraph
              style={{
                marginBottom: 0,
                whiteSpace: 'pre-wrap',
              }}
            >
              {detailRecord?.msgContent || '-'}
            </Typography.Paragraph>
          </Descriptions.Item>
        </Descriptions>
      </Drawer>
    </PageContainer>
  );
};

export default InboxPage;
