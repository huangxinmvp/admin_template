import { ProCard } from '@ant-design/pro-components';
import { App, Descriptions, Drawer, Empty, List, Space, Spin, Tag, Typography } from 'antd';
import React, { useEffect, useState } from 'react';
import {
  agentAllowedActionOptions,
  agentRoleTypeOptions,
  getOptionLabel,
  renderAgentRoleStatusTag,
  renderAgentStageParticipationTag,
} from '@/features/aicoos/projectCenter';
import { formatDateTime } from '@/features/workflow/utils';
import { getAgentRoleCenterDetail } from '@/services/backend/aicoos';
import type {
  AgentRoleAllowedActionItem,
  AgentRoleCenterDetail,
  AgentRoleStageParticipationItem,
} from '@/services/backend/types';

interface AgentRoleDetailDrawerProps {
  open: boolean;
  roleId?: string;
  onClose: () => void;
  onEdit?: (detail: AgentRoleCenterDetail) => void;
}

export const AgentRoleDetailDrawer: React.FC<AgentRoleDetailDrawerProps> = ({
  open,
  roleId,
  onClose,
  onEdit,
}) => {
  const { message } = App.useApp();
  const [loading, setLoading] = useState(false);
  const [detail, setDetail] = useState<AgentRoleCenterDetail>();

  useEffect(() => {
    if (!open || !roleId) {
      if (!open) {
        setDetail(undefined);
      }
      return;
    }

    let active = true;
    const loadDetail = async () => {
      setLoading(true);
      try {
        const response = await getAgentRoleCenterDetail(roleId);
        if (active) {
          setDetail(response.result);
        }
      } catch (_error) {
        if (active) {
          message.error('加载角色详情失败');
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
  }, [message, open, roleId]);

  return (
    <Drawer
      destroyOnHidden
      open={open}
      placement="right"
      title="Agent 角色详情"
      width={920}
      extra={
        onEdit && detail ? (
          <a
            onClick={() => {
              onEdit(detail);
            }}
          >
            编辑
          </a>
        ) : null
      }
      onClose={onClose}
    >
      {loading ? (
        <div
          style={{
            minHeight: 260,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <Spin size="large" />
        </div>
      ) : detail ? (
        <Space direction="vertical" size={16} style={{ display: 'flex' }}>
          <ProCard
            title={
              <Space direction="vertical" size={2}>
                <Typography.Title level={4} style={{ margin: 0 }}>
                  {detail.roleName || '-'}
                </Typography.Title>
                <Typography.Text type="secondary">{detail.roleCode || '-'}</Typography.Text>
              </Space>
            }
            extra={
              <Space wrap size={[8, 8]}>
                {renderAgentRoleStatusTag(detail.status)}
                {detail.defaultFlag === 1 ? <Tag color="blue">默认角色</Tag> : null}
              </Space>
            }
          >
            <Descriptions
              bordered
              column={{ xs: 1, md: 2 }}
              labelStyle={{ width: 132 }}
              size="small"
            >
              <Descriptions.Item label="角色类型">
                {getOptionLabel(agentRoleTypeOptions, detail.roleCategory)}
              </Descriptions.Item>
              <Descriptions.Item label="预算系数">
                {detail.budgetFactor ?? 1}
              </Descriptions.Item>
              <Descriptions.Item label="审批协作">
                {detail.approvalCollaborationFlag === 1 ? '是' : '否'}
              </Descriptions.Item>
              <Descriptions.Item label="需要审批">
                {detail.approvalRequired === 1 ? '是' : '否'}
              </Descriptions.Item>
              <Descriptions.Item label="最大并发">
                {detail.maxConcurrency ?? 1}
              </Descriptions.Item>
              <Descriptions.Item label="更新时间">
                {formatDateTime(detail.updateTime || detail.createTime)}
              </Descriptions.Item>
              <Descriptions.Item label="角色描述" span={2}>
                <Typography.Paragraph style={{ marginBottom: 0 }}>
                  {detail.description || '暂无角色描述'}
                </Typography.Paragraph>
              </Descriptions.Item>
              <Descriptions.Item label="职责摘要" span={2}>
                <Typography.Paragraph style={{ marginBottom: 0 }}>
                  {detail.capabilitySummary || '暂无职责摘要'}
                </Typography.Paragraph>
              </Descriptions.Item>
              <Descriptions.Item label="备注" span={2}>
                <Typography.Paragraph style={{ marginBottom: 0 }}>
                  {detail.remark || '-'}
                </Typography.Paragraph>
              </Descriptions.Item>
            </Descriptions>
          </ProCard>

          <ProCard title="阶段参与配置">
            <List<AgentRoleStageParticipationItem>
              dataSource={detail.stageParticipations || []}
              locale={{
                emptyText: (
                  <Empty
                    description="当前角色还没有阶段参与配置"
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                  />
                ),
              }}
              renderItem={(item) => (
                <List.Item>
                  <List.Item.Meta
                    title={
                      <Space wrap size={[8, 4]}>
                        <Typography.Text strong>{item.stageName || item.stageCode || '-'}</Typography.Text>
                        {renderAgentStageParticipationTag(item.participationType)}
                      </Space>
                    }
                    description={
                      <Space direction="vertical" size={4} style={{ width: '100%' }}>
                        <Typography.Text type="secondary">
                          阶段编码：{item.stageCode || '-'}
                        </Typography.Text>
                        {item.note ? (
                          <Typography.Text type="secondary">备注：{item.note}</Typography.Text>
                        ) : null}
                      </Space>
                    }
                  />
                </List.Item>
              )}
            />
          </ProCard>

          <ProCard title="允许动作配置">
            <List<AgentRoleAllowedActionItem>
              dataSource={detail.allowedActions || []}
              locale={{
                emptyText: (
                  <Empty
                    description="当前角色还没有允许动作配置"
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
                          {getOptionLabel(agentAllowedActionOptions, item.actionCode)}
                        </Typography.Text>
                        {item.allowedFlag === 1 ? <Tag color="success">允许</Tag> : <Tag>禁用</Tag>}
                        {item.approvalRequiredFlag === 1 ? <Tag color="orange">需审批</Tag> : null}
                      </Space>
                    }
                    description={
                      item.note ? (
                        <Typography.Text type="secondary">{item.note}</Typography.Text>
                      ) : (
                        <Typography.Text type="secondary">
                          动作编码：{item.actionCode || '-'}
                        </Typography.Text>
                      )
                    }
                  />
                </List.Item>
              )}
            />
          </ProCard>
        </Space>
      ) : (
        <Empty description="暂无角色详情" image={Empty.PRESENTED_IMAGE_SIMPLE} />
      )}
    </Drawer>
  );
};
