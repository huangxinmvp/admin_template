import { EyeOutlined, ReloadOutlined } from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { App, Button, Card, Empty, Space, Table, Tag, Typography } from 'antd';
import type { TableColumnsType } from 'antd';
import React, { useEffect, useState } from 'react';
import { WorkflowInstanceDetailDrawer } from '@/features/workflow/WorkflowInstanceDetailDrawer';
import {
  formatDateTime,
  renderWorkflowStatusTag,
} from '@/features/workflow/utils';
import { getMyWorkflowStartedInstances } from '@/services/backend/workflow';
import type { WorkflowInstance } from '@/services/backend/types';

const WorkflowInstancesPage: React.FC = () => {
  const { message } = App.useApp();
  const [loading, setLoading] = useState(false);
  const [instances, setInstances] = useState<WorkflowInstance[]>([]);
  const [detailProcessInstanceId, setDetailProcessInstanceId] = useState<string>();

  const loadInstances = async () => {
    setLoading(true);
    try {
      const response = await getMyWorkflowStartedInstances();
      setInstances(response.result || []);
    } catch (_error) {
      message.error('加载我发起的流程失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadInstances();
  }, []);

  const columns: TableColumnsType<WorkflowInstance> = [
    {
      title: '流程名称',
      dataIndex: 'processDefinitionName',
      width: 180,
      ellipsis: true,
      render: (value) => <Typography.Text strong>{value || '-'}</Typography.Text>,
    },
    {
      title: '定义Key',
      dataIndex: 'processDefinitionKey',
      width: 160,
      ellipsis: true,
    },
    {
      title: '业务Key',
      dataIndex: 'businessKey',
      width: 200,
      ellipsis: true,
      render: (value) => value || '-',
    },
    {
      title: '发起人ID',
      dataIndex: 'startUserId',
      width: 160,
      ellipsis: true,
      render: (value) => value || '-',
    },
    {
      title: '发起时间',
      dataIndex: 'startTime',
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
      title: '当前任务',
      dataIndex: 'currentTaskName',
      width: 160,
      render: (value) =>
        value ? <Tag color="processing">{value}</Tag> : <Tag color="default">已结束</Tag>,
    },
    {
      title: '状态',
      key: 'status',
      width: 96,
      render: (_, record) =>
        renderWorkflowStatusTag(record.finished, record.suspended),
    },
    {
      title: '操作',
      key: 'action',
      width: 96,
      fixed: 'right',
      render: (_, record) => (
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
      ),
    },
  ];

  return (
    <PageContainer
      className="saas-page-container saas-workflow-page"
      content="对接 /api/workflow/instances/my-started，展示当前账号发起过的流程实例，并支持查看完整审批轨迹。"
      extra={[
        <Tag key="endpoint" color="blue">
          /api/workflow/instances/my-started
        </Tag>,
      ]}
      title="我发起的流程"
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
          <Typography.Text type="secondary">
            当前共 {instances.length} 条流程实例
          </Typography.Text>
          <Button
            icon={<ReloadOutlined />}
            onClick={() => {
              void loadInstances();
            }}
          >
            刷新
          </Button>
        </div>

        <Table<WorkflowInstance>
          columns={columns}
          dataSource={instances}
          loading={loading}
          locale={{
            emptyText: (
              <Empty
                description="当前还没有发起过流程"
                image={Empty.PRESENTED_IMAGE_SIMPLE}
              />
            ),
          }}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
          rowKey="processInstanceId"
          scroll={{ x: 1320 }}
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
    </PageContainer>
  );
};

export default WorkflowInstancesPage;
