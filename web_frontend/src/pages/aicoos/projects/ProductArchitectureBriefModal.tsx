import { App, Button, Descriptions, Empty, Form, Input, List, Modal, Space, Tag, Typography } from 'antd';
import React, { useEffect, useState } from 'react';
import {
  generateProductArchitectureBrief,
  saveProjectMeetingRecord,
} from '@/services/backend/aicoos';
import type { ProductArchitectureBrief } from '@/services/backend/types';

interface ProductArchitectureBriefModalProps {
  open: boolean;
  projectId?: string;
  projectName?: string | null;
  onClose: () => void;
  onSaved: () => void;
}

interface ProductArchitectureBriefFormValues {
  focusNotes?: string;
}

export const ProductArchitectureBriefModal: React.FC<ProductArchitectureBriefModalProps> = ({
  open,
  projectId,
  projectName,
  onClose,
  onSaved,
}) => {
  const { message } = App.useApp();
  const [form] = Form.useForm<ProductArchitectureBriefFormValues>();
  const [generating, setGenerating] = useState(false);
  const [saving, setSaving] = useState(false);
  const [preview, setPreview] = useState<ProductArchitectureBrief>();

  useEffect(() => {
    if (!open) {
      form.resetFields();
      setPreview(undefined);
    }
  }, [form, open]);

  const handleGenerate = async () => {
    if (!projectId) {
      message.warning('请先选择项目');
      return;
    }
    const values = await form.validateFields();
    setGenerating(true);
    try {
      const response = await generateProductArchitectureBrief(projectId, {
        focusNotes: values.focusNotes,
      });
      setPreview(response.result);
      message.success('产品与架构协作简报已生成');
    } catch (_error) {
      message.error('生成产品与架构协作简报失败');
    } finally {
      setGenerating(false);
    }
  };

  const handleSave = async () => {
    if (!projectId || !preview) {
      return;
    }
    const values = await form.validateFields();
    const remarks = [
      (preview.participants || []).length ? `协作角色：${(preview.participants || []).join(' / ')}` : null,
      (preview.risks || []).length ? `风险：${(preview.risks || []).join(' / ')}` : null,
      preview.projectGovernanceLinkageSummary ? `治理联动：${preview.projectGovernanceLinkageSummary}` : null,
      (preview.followUpHints || []).length ? `后续治理建议：${(preview.followUpHints || []).join(' / ')}` : null,
    ]
      .filter(Boolean)
      .join(' | ');
    setSaving(true);
    try {
      await saveProjectMeetingRecord(projectId, {
        meetingTitle: preview.briefTitle || `${projectName || '项目'} 产品与架构协作简报`,
        rawNotes: values.focusNotes || preview.solutionBrief || '',
        summary: preview.solutionBrief || undefined,
        actionItems: preview.nextSteps || undefined,
        openQuestions: preview.openQuestions || undefined,
        sourceType: 'product_architecture_brief',
        sourceObjectId: projectId,
        remark: remarks || undefined,
      });
      message.success('协作简报已保存为项目记录');
      onSaved();
      onClose();
    } catch (_error) {
      message.error('保存协作简报失败');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal
      destroyOnClose
      open={open}
      title="产品与架构协作简报"
      width={920}
      footer={[
        <Button key="close" onClick={onClose}>
          关闭
        </Button>,
        <Button key="generate" loading={generating} onClick={() => void handleGenerate()}>
          生成协作简报
        </Button>,
        <Button disabled={!preview} key="save" loading={saving} onClick={() => void handleSave()} type="primary">
          保存为项目记录
        </Button>,
      ]}
      onCancel={onClose}
    >
      <Form<ProductArchitectureBriefFormValues> form={form} layout="vertical">
        <Form.Item label="操作员关注点" name="focusNotes">
          <Input.TextArea
            placeholder="可填写希望 Product Manager 与 Architect 重点审阅的范围、约束或风险"
            rows={5}
            showCount
            maxLength={2000}
          />
        </Form.Item>
      </Form>

      {preview ? (
        <Space direction="vertical" size={16} style={{ display: 'flex' }}>
          <div>
            <Space wrap size={[8, 8]} style={{ marginBottom: 8 }}>
              {(preview.participants || []).map((participant) => (
                <Tag key={participant} color="blue">
                  {participant}
                </Tag>
              ))}
            </Space>
            <Typography.Title level={5} style={{ marginBottom: 8 }}>
              {preview.scenarioLabel || preview.briefTitle || '产品与架构协作简报'}
            </Typography.Title>
            <Typography.Paragraph style={{ marginBottom: 0 }}>
              {preview.solutionBrief || '-'}
            </Typography.Paragraph>
          </div>

          {(preview.collaborationSummary ||
            preview.recommendedOperatorAction ||
            preview.projectGovernanceLinkageSummary) ? (
            <Descriptions bordered column={1} size="small" title="协作结论">
              <Descriptions.Item label="结论摘要">
                {preview.collaborationSummary || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="推荐操作">
                {preview.recommendedOperatorAction || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="治理联动">
                {preview.projectGovernanceLinkageSummary || '-'}
              </Descriptions.Item>
            </Descriptions>
          ) : null}

          {preview.governanceLinkage ? (
            <Descriptions bordered column={1} size="small" title="治理上下文">
              <Descriptions.Item label="目标模块">
                {String(preview.governanceLinkage.targetModule || '-')}
              </Descriptions.Item>
              <Descriptions.Item label="当前阶段">
                {String(preview.governanceLinkage.currentStageCode || '-')}
              </Descriptions.Item>
              <Descriptions.Item label="项目治理状态">
                {String(preview.governanceLinkage.projectGovernanceStatus || '-')}
              </Descriptions.Item>
              <Descriptions.Item label="阻塞 / 风险标记">
                {String(preview.governanceLinkage.blockedFlag ?? '-')} /{' '}
                {String(preview.governanceLinkage.atRiskFlag ?? '-')}
              </Descriptions.Item>
              <Descriptions.Item label="待处理决策 / 审批">
                {String(preview.governanceLinkage.pendingDecisionCount ?? '-')} /{' '}
                {String(preview.governanceLinkage.pendingApprovalCount ?? '-')}
              </Descriptions.Item>
              <Descriptions.Item label="失败门禁 / 缺失关键角色">
                {String(preview.governanceLinkage.failedGateConditionCount ?? '-')} /{' '}
                {String(preview.governanceLinkage.missingCriticalRoleCount ?? '-')}
              </Descriptions.Item>
              <Descriptions.Item label="治理原因摘要">
                {String(preview.governanceLinkage.blockerReasonSummary || '-')}
              </Descriptions.Item>
              <Descriptions.Item label="保存后写入">
                仅会保存为项目 `MeetingRecord`，不会自动创建决策、审批或阶段流转
              </Descriptions.Item>
            </Descriptions>
          ) : null}

          {preview.roleInsights ? (
            <Descriptions bordered column={1} size="small" title="角色视角">
              <Descriptions.Item label="Product Manager">
                {preview.roleInsights.productManagerView || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="Architect">
                {preview.roleInsights.architectView || '-'}
              </Descriptions.Item>
            </Descriptions>
          ) : null}

          {(preview.architectureFocusAreas || []).length ? (
            <div>
              <Typography.Text strong>架构关注点</Typography.Text>
              <List
                dataSource={preview.architectureFocusAreas || []}
                locale={{
                  emptyText: <Empty description="暂无架构关注点" image={Empty.PRESENTED_IMAGE_SIMPLE} />,
                }}
                renderItem={(item) => <List.Item>{item}</List.Item>}
                size="small"
              />
            </div>
          ) : null}

          {(preview.deliveryImplications || []).length ? (
            <div>
              <Typography.Text strong>交付影响</Typography.Text>
              <List
                dataSource={preview.deliveryImplications || []}
                locale={{
                  emptyText: <Empty description="暂无交付影响" image={Empty.PRESENTED_IMAGE_SIMPLE} />,
                }}
                renderItem={(item) => <List.Item>{item}</List.Item>}
                size="small"
              />
            </div>
          ) : null}

          {(preview.followUpHints || []).length ? (
            <div>
              <Typography.Text strong>治理跟进</Typography.Text>
              <List
                dataSource={preview.followUpHints || []}
                locale={{
                  emptyText: <Empty description="暂无后续治理建议" image={Empty.PRESENTED_IMAGE_SIMPLE} />,
                }}
                renderItem={(item) => <List.Item>{item}</List.Item>}
                size="small"
              />
            </div>
          ) : null}

          <div>
            <Typography.Text strong>关键风险</Typography.Text>
            <List
              dataSource={preview.risks || []}
              locale={{
                emptyText: <Empty description="暂无关键风险" image={Empty.PRESENTED_IMAGE_SIMPLE} />,
              }}
              renderItem={(item) => <List.Item>{item}</List.Item>}
              size="small"
            />
          </div>

          <div>
            <Typography.Text strong>开放问题</Typography.Text>
            <List
              dataSource={preview.openQuestions || []}
              locale={{
                emptyText: <Empty description="暂无开放问题" image={Empty.PRESENTED_IMAGE_SIMPLE} />,
              }}
              renderItem={(item) => <List.Item>{item}</List.Item>}
              size="small"
            />
          </div>

          <div>
            <Typography.Text strong>推荐下一步</Typography.Text>
            <List
              dataSource={preview.nextSteps || []}
              locale={{
                emptyText: <Empty description="暂无下一步建议" image={Empty.PRESENTED_IMAGE_SIMPLE} />,
              }}
              renderItem={(item) => <List.Item>{item}</List.Item>}
              size="small"
            />
          </div>
        </Space>
      ) : null}
    </Modal>
  );
};
