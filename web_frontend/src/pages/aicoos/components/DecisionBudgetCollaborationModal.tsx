import { App, Button, Descriptions, Empty, List, Modal, Space, Tag, Typography } from 'antd';
import React, { useEffect, useState } from 'react';
import {
  applyDecisionBudgetReview,
  generateDecisionBudgetReview,
} from '@/services/backend/aicoos';
import type { DecisionBudgetReview } from '@/services/backend/types';

const decisionRiskFlagLabels: Record<string, string> = {
  scope_drift: '范围漂移',
  budget_confirmation: '待补预算确认',
  timeline_pressure: '排期压力',
};

interface DecisionBudgetCollaborationModalProps {
  open: boolean;
  decisionId?: string;
  onClose: () => void;
  onApplied: () => void;
}

export const DecisionBudgetCollaborationModal: React.FC<
  DecisionBudgetCollaborationModalProps
> = ({ open, decisionId, onClose, onApplied }) => {
  const { message } = App.useApp();
  const [loading, setLoading] = useState(false);
  const [applying, setApplying] = useState(false);
  const [preview, setPreview] = useState<DecisionBudgetReview>();

  const loadPreview = async () => {
    if (!decisionId) {
      return;
    }
    setLoading(true);
    try {
      const response = await generateDecisionBudgetReview(decisionId);
      setPreview(response.result);
    } catch (_error) {
      message.error('生成协作评审失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!open) {
      setPreview(undefined);
      return;
    }
    void loadPreview();
  }, [open, decisionId]);

  return (
    <Modal
      destroyOnClose
      open={open}
      title="决策与预算协作评审"
      width={880}
      footer={[
        <Button key="close" onClick={onClose}>
          关闭
        </Button>,
        <Button key="reload" loading={loading} onClick={() => void loadPreview()}>
          重新生成
        </Button>,
        <Button
          disabled={!preview}
          key="apply"
          loading={applying}
          onClick={async () => {
            if (!decisionId || !preview) {
              return;
            }
            setApplying(true);
            try {
              await applyDecisionBudgetReview(decisionId, preview);
              message.success('协作评审结果已应用');
              onApplied();
              onClose();
            } catch (_error) {
              message.error('应用协作评审结果失败');
            } finally {
              setApplying(false);
            }
          }}
          type="primary"
        >
          应用到当前决策
        </Button>,
      ]}
      onCancel={onClose}
    >
      {preview ? (
        <Space direction="vertical" size={16} style={{ display: 'flex' }}>
          <Typography.Title level={5} style={{ marginBottom: 0 }}>
            {preview.scenarioLabel || '决策与预算协作评审'}
          </Typography.Title>
          <Space wrap size={[8, 8]}>
            {(preview.participants || []).map((participant) => (
              <Tag key={participant} color="blue">
                {participant}
              </Tag>
            ))}
            <Tag color={preview.budgetConfirmationAdvised ? 'orange' : 'green'}>
              {preview.budgetConfirmationAdvised ? '建议预算确认' : '可直接留档'}
            </Tag>
            {(preview.riskFlags || []).map((flag) => (
              <Tag key={flag}>{decisionRiskFlagLabels[flag] || flag}</Tag>
            ))}
          </Space>

          {(preview.collaborationSummary ||
            preview.recommendedOperatorAction ||
            preview.blockerAssessment ||
            preview.recommendedOption) ? (
            <Descriptions bordered column={1} size="small" title="协作结论">
              <Descriptions.Item label="结论摘要">
                {preview.collaborationSummary || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="推荐操作">
                {preview.recommendedOperatorAction || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="推荐选项">
                {preview.recommendedOption || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="阻塞判断">
                {preview.blockerAssessment || '-'}
              </Descriptions.Item>
            </Descriptions>
          ) : null}

          {preview.governanceLinkage ? (
            <Descriptions bordered column={1} size="small" title="治理联动">
              <Descriptions.Item label="当前阶段">
                {String(preview.governanceLinkage?.currentStageCode || '-')}
              </Descriptions.Item>
              <Descriptions.Item label="项目治理状态">
                {String(preview.governanceLinkage?.projectGovernanceStatus || '-')}
              </Descriptions.Item>
              <Descriptions.Item label="当前预算状态">
                {String(preview.governanceLinkage?.currentBudgetStatus || '-')}
              </Descriptions.Item>
              <Descriptions.Item label="当前决策状态">
                {String(preview.governanceLinkage?.currentDecisionStatus || '-')}
              </Descriptions.Item>
              <Descriptions.Item label="需要预算确认">
                {String(preview.governanceLinkage?.requiresBudgetConfirmation ?? '-')}
              </Descriptions.Item>
              <Descriptions.Item label="待处理决策 / 审批">
                {String(preview.governanceLinkage?.pendingDecisionItemCount ?? '-')} /{' '}
                {String(preview.governanceLinkage?.pendingApprovalCount ?? '-')}
              </Descriptions.Item>
              <Descriptions.Item label="阻塞审批数">
                {String(preview.governanceLinkage?.blockerApprovalCount ?? '-')}
              </Descriptions.Item>
              <Descriptions.Item label="建议人工判断是否发起审批">
                {String(preview.governanceLinkage?.shouldOpenApproval ?? '-')}
              </Descriptions.Item>
              <Descriptions.Item label="应用后写入">
                仅会更新当前 `DecisionItem`，不会自动发起审批或阶段流转
              </Descriptions.Item>
            </Descriptions>
          ) : null}

          {preview.roleInsights ? (
            <Descriptions bordered column={1} size="small" title="角色视角">
              <Descriptions.Item label="Product Manager">
                <Typography.Paragraph style={{ marginBottom: 0 }}>
                  {preview.roleInsights.productManagerView || '-'}
                </Typography.Paragraph>
              </Descriptions.Item>
              <Descriptions.Item label="Budget Analyst">
                <Typography.Paragraph style={{ marginBottom: 0 }}>
                  {preview.roleInsights.budgetAnalystView || '-'}
                </Typography.Paragraph>
              </Descriptions.Item>
            </Descriptions>
          ) : null}

          {(preview.decisionLinkageSummary || preview.budgetLinkageSummary || (preview.followUpHints || []).length) ? (
            <Descriptions bordered column={1} size="small" title="治理联动说明">
              <Descriptions.Item label="决策联动">
                {preview.decisionLinkageSummary || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="预算联动">
                {preview.budgetLinkageSummary || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="后续治理建议">
                {(preview.followUpHints || []).length ? (preview.followUpHints || []).join('；') : '-'}
              </Descriptions.Item>
            </Descriptions>
          ) : null}

          <Descriptions bordered column={1} size="small">
            <Descriptions.Item label="决策建议">
              <Typography.Paragraph style={{ marginBottom: 0 }}>
                {preview.decisionRecommendation || '-'}
              </Typography.Paragraph>
            </Descriptions.Item>
            <Descriptions.Item label="推荐选项">
              {preview.recommendedOption || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="预算影响说明">
              <Typography.Paragraph style={{ marginBottom: 0 }}>
                {preview.budgetImpactNote || '-'}
              </Typography.Paragraph>
            </Descriptions.Item>
            <Descriptions.Item label="项目影响说明">
              <Typography.Paragraph style={{ marginBottom: 0 }}>
                {preview.projectImpactNote || '-'}
              </Typography.Paragraph>
            </Descriptions.Item>
            <Descriptions.Item label="阻塞评估">
              <Typography.Paragraph style={{ marginBottom: 0 }}>
                {preview.blockerAssessment || '-'}
              </Typography.Paragraph>
            </Descriptions.Item>
          </Descriptions>

          <div>
            <Typography.Text strong>下一步建议</Typography.Text>
            <List
              dataSource={preview.nextSteps || []}
              locale={{
                emptyText: <Empty description="暂无下一步建议" image={Empty.PRESENTED_IMAGE_SIMPLE} />,
              }}
              renderItem={(item) => <List.Item>{item}</List.Item>}
              size="small"
            />
          </div>

          {(preview.followUpHints || []).length ? (
            <div>
              <Typography.Text strong>治理跟进</Typography.Text>
              <List
                dataSource={preview.followUpHints || []}
                locale={{
                  emptyText: <Empty description="暂无治理跟进建议" image={Empty.PRESENTED_IMAGE_SIMPLE} />,
                }}
                renderItem={(item) => <List.Item>{item}</List.Item>}
                size="small"
              />
            </div>
          ) : null}
        </Space>
      ) : (
        <Empty description={loading ? '正在生成协作评审结果' : '暂无协作评审结果'} image={Empty.PRESENTED_IMAGE_SIMPLE} />
      )}
    </Modal>
  );
};
