import { Drawer, Empty, Space, Spin, Table, Tag, Typography, Descriptions } from 'antd';
import type { TableColumnsType } from 'antd';
import React, { useEffect, useState } from 'react';
import { getWorkflowInstanceDetail } from '@/services/backend/workflow';
import type {
  WorkflowProcessInstanceDetail,
  WorkflowTask,
} from '@/services/backend/types';
import {
  formatDateTime,
  renderWorkflowStatusTag,
  stringifyWorkflowVariables,
} from './utils';

const taskColumns: TableColumnsType<WorkflowTask> = [
  {
    title: '任务名称',
    dataIndex: 'name',
    width: 180,
    ellipsis: true,
  },
  {
    title: '任务定义Key',
    dataIndex: 'taskDefinitionKey',
    width: 180,
    ellipsis: true,
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
    width: 96,
    render: (_, record) =>
      renderWorkflowStatusTag(record.finished, record.suspended),
  },
];

interface WorkflowInstanceDetailDrawerProps {
  open: boolean;
  processInstanceId?: string;
  onClose: () => void;
}

export const WorkflowInstanceDetailDrawer: React.FC<
  WorkflowInstanceDetailDrawerProps
> = ({ open, processInstanceId, onClose }) => {
  const [loading, setLoading] = useState(false);
  const [detail, setDetail] = useState<WorkflowProcessInstanceDetail>();

  useEffect(() => {
    if (!open || !processInstanceId) {
      if (!open) {
        setDetail(undefined);
      }
      return;
    }

    let active = true;
    const loadDetail = async () => {
      setLoading(true);
      try {
        const response = await getWorkflowInstanceDetail(processInstanceId);
        if (active) {
          setDetail(response.result);
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    void loadDetail();
    return () => {
      active = false;
    };
  }, [open, processInstanceId]);

  const instance = detail?.instance;

  return (
    <Drawer
      destroyOnHidden
      open={open}
      placement="right"
      title="流程实例详情"
      width={980}
      onClose={onClose}
    >
      {loading ? (
        <div
          style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            minHeight: 260,
          }}
        >
          <Spin size="large" />
        </div>
      ) : detail && instance ? (
        <Space direction="vertical" size={16} style={{ display: 'flex' }}>
          <Descriptions
            bordered
            column={{ xs: 1, md: 2 }}
            labelStyle={{ width: 132 }}
            size="middle"
            title="基本信息"
          >
            <Descriptions.Item label="流程名称">
              {instance.processDefinitionName || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="定义Key">
              {instance.processDefinitionKey || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="业务Key">
              {instance.businessKey || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="发起人ID">
              {instance.startUserId || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="发起时间">
              {formatDateTime(instance.startTime)}
            </Descriptions.Item>
            <Descriptions.Item label="结束时间">
              {formatDateTime(instance.endTime)}
            </Descriptions.Item>
            <Descriptions.Item label="当前任务">
              {instance.currentTaskName ? (
                <Tag color="processing">{instance.currentTaskName}</Tag>
              ) : (
                '-'
              )}
            </Descriptions.Item>
            <Descriptions.Item label="流程状态">
              {renderWorkflowStatusTag(instance.finished, instance.suspended)}
            </Descriptions.Item>
            <Descriptions.Item label="实例ID" span={2}>
              {instance.processInstanceId}
            </Descriptions.Item>
          </Descriptions>

          <div>
            <Typography.Title level={5} style={{ marginBottom: 12 }}>
              当前任务
            </Typography.Title>
            <Table<WorkflowTask>
              columns={taskColumns}
              dataSource={detail.currentTasks || []}
              locale={{
                emptyText: (
                  <Empty
                    description="当前没有活动任务"
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                  />
                ),
              }}
              pagination={false}
              rowKey="taskId"
              scroll={{ x: 900 }}
              size="middle"
              tableLayout="fixed"
            />
          </div>

          <div>
            <Typography.Title level={5} style={{ marginBottom: 12 }}>
              历史任务
            </Typography.Title>
            <Table<WorkflowTask>
              columns={taskColumns}
              dataSource={detail.historyTasks || []}
              locale={{
                emptyText: (
                  <Empty
                    description="当前没有历史任务"
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                  />
                ),
              }}
              pagination={false}
              rowKey="taskId"
              scroll={{ x: 900 }}
              size="middle"
              tableLayout="fixed"
            />
          </div>

          <div>
            <Typography.Title level={5} style={{ marginBottom: 12 }}>
              流程变量
            </Typography.Title>
            <pre
              className="saas-json-block"
              style={{
                margin: 0,
                padding: '16px 18px',
                borderRadius: 16,
                background: '#0f172a',
                color: '#e2e8f0',
                overflowX: 'auto',
                whiteSpace: 'pre-wrap',
                wordBreak: 'break-word',
                fontSize: 13,
                lineHeight: 1.7,
              }}
            >
              {stringifyWorkflowVariables(detail.variables)}
            </pre>
          </div>
        </Space>
      ) : (
        <Empty description="未获取到流程实例详情" image={Empty.PRESENTED_IMAGE_SIMPLE} />
      )}
    </Drawer>
  );
};
