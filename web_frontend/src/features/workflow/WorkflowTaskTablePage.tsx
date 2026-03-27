import { CheckCircleOutlined, EyeOutlined, ReloadOutlined } from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { App, Button, Card, Empty, Space, Table, Tag, Typography } from 'antd';
import type { TableColumnsType } from 'antd';
import React, { useEffect, useState } from 'react';
import type { BackendResponse, WorkflowTask } from '@/services/backend/types';
import { formatDateTime, renderWorkflowStatusTag } from './utils';
import { WorkflowInstanceDetailDrawer } from './WorkflowInstanceDetailDrawer';
import { WorkflowTaskCompleteModal } from './WorkflowTaskCompleteModal';

interface WorkflowTaskTablePageProps {
  title: string;
  description: string;
  endpointLabel: string;
  emptyDescription: string;
  allowComplete?: boolean;
  loadTasks: () => Promise<BackendResponse<WorkflowTask[]>>;
}

export const WorkflowTaskTablePage: React.FC<WorkflowTaskTablePageProps> = ({
  title,
  description,
  endpointLabel,
  emptyDescription,
  allowComplete = false,
  loadTasks,
}) => {
  const { message } = App.useApp();
  const [loading, setLoading] = useState(false);
  const [tasks, setTasks] = useState<WorkflowTask[]>([]);
  const [detailProcessInstanceId, setDetailProcessInstanceId] = useState<string>();
  const [completeTask, setCompleteTask] = useState<WorkflowTask>();

  const loadData = async () => {
    setLoading(true);
    try {
      const response = await loadTasks();
      setTasks(response.result || []);
    } catch (_error) {
      message.error('加载流程任务失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadData();
  }, []);

  const columns: TableColumnsType<WorkflowTask> = [
    {
      title: '任务名称',
      dataIndex: 'name',
      width: 180,
      ellipsis: true,
      render: (value) => <Typography.Text strong>{value || '-'}</Typography.Text>,
    },
    {
      title: '流程名称',
      dataIndex: 'processDefinitionName',
      width: 160,
      ellipsis: true,
      render: (value) => value || '-',
    },
    {
      title: '业务Key',
      dataIndex: 'businessKey',
      width: 180,
      ellipsis: true,
      render: (value) => value || '-',
    },
    {
      title: '办理人',
      dataIndex: 'assignee',
      width: 120,
      render: (value) => value || '-',
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      width: 168,
      render: (value) => formatDateTime(value),
    },
    {
      title: '结束时间',
      dataIndex: 'endTime',
      width: 168,
      render: (value) => formatDateTime(value),
    },
    {
      title: '状态',
      key: 'status',
      width: 110,
      render: (_, record) =>
        renderWorkflowStatusTag(record.finished, record.suspended),
    },
    {
      title: '说明',
      dataIndex: 'description',
      width: 260,
      ellipsis: true,
      render: (value) => value || '-',
    },
    {
      title: '操作',
      key: 'action',
      width: allowComplete ? 180 : 96,
      fixed: 'right',
      render: (_, record) => (
        <Space split={<span style={{ color: '#d9d9d9' }}>|</span>}>
          <Button
            type="link"
            icon={<EyeOutlined />}
            style={{ paddingInline: 0 }}
            onClick={() => {
              setDetailProcessInstanceId(record.processInstanceId);
            }}
          >
            详情
          </Button>
          {allowComplete ? (
            <Button
              type="link"
              icon={<CheckCircleOutlined />}
              style={{ paddingInline: 0 }}
              onClick={() => {
                setCompleteTask(record);
              }}
            >
              办理
            </Button>
          ) : null}
        </Space>
      ),
    },
  ];

  return (
    <PageContainer
      className="saas-page-container saas-workflow-page"
      content={description}
      extra={[
        <Tag key="endpoint" color="blue">
          {endpointLabel}
        </Tag>,
      ]}
      title={title}
    >
      <Card variant="borderless">
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            gap: 16,
            marginBottom: 16,
            flexWrap: 'wrap',
          }}
        >
          <Space wrap>
            <Typography.Text type="secondary">
              当前共 {tasks.length} 条任务记录
            </Typography.Text>
          </Space>
          <Button
            icon={<ReloadOutlined />}
            onClick={() => {
              void loadData();
            }}
          >
            刷新
          </Button>
        </div>

        <Table<WorkflowTask>
          columns={columns}
          dataSource={tasks}
          loading={loading}
          locale={{
            emptyText: (
              <Empty
                description={emptyDescription}
                image={Empty.PRESENTED_IMAGE_SIMPLE}
              />
            ),
          }}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
          rowKey="taskId"
          scroll={{ x: 1380 }}
          size="middle"
          tableLayout="fixed"
        />
      </Card>

      <WorkflowInstanceDetailDrawer
        open={Boolean(detailProcessInstanceId)}
        processInstanceId={detailProcessInstanceId}
        onClose={() => {
          setDetailProcessInstanceId(undefined);
        }}
      />

      <WorkflowTaskCompleteModal
        open={Boolean(completeTask)}
        task={completeTask}
        onCancel={() => {
          setCompleteTask(undefined);
        }}
        onSuccess={() => {
          setDetailProcessInstanceId(completeTask?.processInstanceId);
          setCompleteTask(undefined);
          void loadData();
        }}
      />
    </PageContainer>
  );
};
