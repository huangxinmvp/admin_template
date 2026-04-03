import { ProCard } from '@ant-design/pro-components';
import { App, Descriptions, Drawer, Empty, List, Space, Spin, Tag, Typography } from 'antd';
import React, { useEffect, useState } from 'react';
import {
  getOptionLabel,
  projectTypeOptions,
  renderGateConditionStatusTag,
  renderWorkflowTemplateStatusTag,
} from '@/features/aicoos/projectCenter';
import { formatDateTime } from '@/features/workflow/utils';
import { getWorkflowTemplateCenterDetail } from '@/services/backend/aicoos';
import type { WorkflowTemplateCenterDetail, WorkflowTemplateStageConfigItem } from '@/services/backend/types';

interface WorkflowTemplateDetailDrawerProps {
  open: boolean;
  templateId?: string;
  onClose: () => void;
  onEdit?: (detail: WorkflowTemplateCenterDetail) => void;
}

const renderEnabledTag = (value?: number | null) =>
  value === 1 ? <Tag color="success">启用</Tag> : <Tag>停用</Tag>;

export const WorkflowTemplateDetailDrawer: React.FC<WorkflowTemplateDetailDrawerProps> = ({
  open,
  templateId,
  onClose,
  onEdit,
}) => {
  const { message } = App.useApp();
  const [loading, setLoading] = useState(false);
  const [detail, setDetail] = useState<WorkflowTemplateCenterDetail>();

  useEffect(() => {
    if (!open || !templateId) {
      if (!open) {
        setDetail(undefined);
      }
      return;
    }

    let active = true;
    const loadDetail = async () => {
      setLoading(true);
      try {
        const response = await getWorkflowTemplateCenterDetail(templateId);
        if (active) {
          setDetail(response.result);
        }
      } catch (_error) {
        if (active) {
          message.error('加载模板详情失败');
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
  }, [message, open, templateId]);

  return (
    <Drawer
      destroyOnHidden
      open={open}
      placement="right"
      title="工作流模板详情"
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
                  {detail.templateName || '-'}
                </Typography.Title>
                <Typography.Text type="secondary">
                  {detail.templateCode || '-'}
                </Typography.Text>
              </Space>
            }
            extra={
              <Space wrap size={[8, 8]}>
                {renderWorkflowTemplateStatusTag(detail.status)}
                {detail.defaultFlag === 1 ? <Tag color="blue">默认模板</Tag> : null}
              </Space>
            }
          >
            <Descriptions
              bordered
              column={{ xs: 1, md: 2 }}
              labelStyle={{ width: 132 }}
              size="small"
            >
              <Descriptions.Item label="项目类型">
                {getOptionLabel(projectTypeOptions, detail.projectType)}
              </Descriptions.Item>
              <Descriptions.Item label="版本">
                v{detail.versionNo || 1}
              </Descriptions.Item>
              <Descriptions.Item label="阶段数">
                {detail.stageCount || 0}
              </Descriptions.Item>
              <Descriptions.Item label="更新时间">
                {formatDateTime(detail.updateTime || detail.createTime)}
              </Descriptions.Item>
              <Descriptions.Item label="描述" span={2}>
                <Typography.Paragraph style={{ marginBottom: 0 }}>
                  {detail.description || '暂无模板描述'}
                </Typography.Paragraph>
              </Descriptions.Item>
              <Descriptions.Item label="备注" span={2}>
                <Typography.Paragraph style={{ marginBottom: 0 }}>
                  {detail.remark || '-'}
                </Typography.Paragraph>
              </Descriptions.Item>
            </Descriptions>
          </ProCard>

          <ProCard title="阶段配置">
            <List<WorkflowTemplateStageConfigItem>
              dataSource={detail.stages || []}
              locale={{
                emptyText: (
                  <Empty
                    description="当前模板还没有阶段配置"
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
                          {item.stageOrder || 0}. {item.stageName || item.stageCode || '-'}
                        </Typography.Text>
                        {renderEnabledTag(item.enabledFlag)}
                        {renderGateConditionStatusTag(item.enabledFlag === 1 ? 'configured' : 'warning')}
                      </Space>
                    }
                    description={
                      <Space direction="vertical" size={4} style={{ width: '100%' }}>
                        <Typography.Text type="secondary">
                          阶段编码：{item.stageCode || '-'}
                        </Typography.Text>
                        <Typography.Paragraph style={{ marginBottom: 0 }}>
                          {item.stageDescription || '暂无阶段说明'}
                        </Typography.Paragraph>
                        {item.stageNote ? (
                          <Typography.Text type="secondary">
                            阶段备注：{item.stageNote}
                          </Typography.Text>
                        ) : null}
                      </Space>
                    }
                  />
                </List.Item>
              )}
            />
          </ProCard>

          <ProCard title="治理规则配置" gutter={16} wrap>
            <ProCard title="Gate 检查" colSpan={{ xs: 24, lg: 12 }}>
              <Typography.Paragraph style={{ marginBottom: 0 }}>
                {detail.gateChecksConfig || '未配置'}
              </Typography.Paragraph>
            </ProCard>
            <ProCard title="阻塞决策条件" colSpan={{ xs: 24, lg: 12 }}>
              <Typography.Paragraph style={{ marginBottom: 0 }}>
                {detail.blockingDecisionConfig || '未配置'}
              </Typography.Paragraph>
            </ProCard>
            <ProCard title="阻塞审批条件" colSpan={{ xs: 24, lg: 12 }}>
              <Typography.Paragraph style={{ marginBottom: 0 }}>
                {detail.blockingApprovalConfig || '未配置'}
              </Typography.Paragraph>
            </ProCard>
            <ProCard title="预算阈值条件" colSpan={{ xs: 24, lg: 12 }}>
              <Typography.Paragraph style={{ marginBottom: 0 }}>
                {detail.budgetThresholdConfig || '未配置'}
              </Typography.Paragraph>
            </ProCard>
            <ProCard title="高风险动作审批要求" colSpan={24}>
              <Typography.Paragraph style={{ marginBottom: 0 }}>
                {detail.highRiskApprovalConfig || '未配置'}
              </Typography.Paragraph>
            </ProCard>
          </ProCard>
        </Space>
      ) : (
        <Empty description="暂无模板详情" image={Empty.PRESENTED_IMAGE_SIMPLE} />
      )}
    </Drawer>
  );
};
