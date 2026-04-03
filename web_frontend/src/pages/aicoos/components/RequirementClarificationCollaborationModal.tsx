import { App, Button, Checkbox, Descriptions, Empty, Modal, Space, Spin, Tag, Typography } from 'antd';
import React, { useEffect, useState } from 'react';
import {
  applyRequirementClarificationSuggestions,
  generateRequirementClarificationCollaboration,
} from '@/services/backend/aicoos';
import type { RequirementClarificationReview } from '@/services/backend/types';

interface RequirementClarificationCollaborationModalProps {
  open: boolean;
  projectId?: string;
  onClose: () => void;
  onApplied: () => void;
}

export const RequirementClarificationCollaborationModal: React.FC<
  RequirementClarificationCollaborationModalProps
> = ({ open, projectId, onClose, onApplied }) => {
  const { message } = App.useApp();
  const [loading, setLoading] = useState(false);
  const [applying, setApplying] = useState(false);
  const [preview, setPreview] = useState<RequirementClarificationReview>();
  const [selectedSuggestionIndexes, setSelectedSuggestionIndexes] = useState<number[]>([]);

  const loadPreview = async () => {
    if (!projectId) {
      return;
    }
    setLoading(true);
    try {
      const response = await generateRequirementClarificationCollaboration(projectId);
      const result = response.result;
      setPreview(result);
      setSelectedSuggestionIndexes((result?.suggestions || []).map((_, index) => index));
    } catch (_error) {
      message.error('生成协作澄清审阅失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!open) {
      setPreview(undefined);
      setSelectedSuggestionIndexes([]);
      return;
    }
    void loadPreview();
  }, [open, projectId]);

  const selectedSuggestions = selectedSuggestionIndexes
    .map((index) => preview?.suggestions?.[index])
    .filter((item): item is NonNullable<typeof item> => Boolean(item));

  return (
    <Modal
      destroyOnClose
      open={open}
      title="需求澄清协作审阅"
      width={960}
      footer={[
        <Button key="close" onClick={onClose}>
          关闭
        </Button>,
        <Button key="reload" loading={loading} onClick={() => void loadPreview()}>
          重新生成
        </Button>,
        <Button
          disabled={!selectedSuggestions.length}
          key="apply"
          loading={applying}
          onClick={async () => {
            if (!projectId || !selectedSuggestions.length) {
              return;
            }
            setApplying(true);
            try {
              await applyRequirementClarificationSuggestions(
                projectId,
                selectedSuggestions,
                'requirement_clarification_collaboration',
              );
              message.success('已应用协作澄清建议');
              onApplied();
              onClose();
            } catch (_error) {
              message.error('应用协作澄清建议失败');
            } finally {
              setApplying(false);
            }
          }}
          type="primary"
        >
          应用选中建议
        </Button>,
      ]}
      onCancel={onClose}
    >
      {loading ? (
        <div style={{ minHeight: 240, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Spin size="large" />
        </div>
      ) : preview ? (
        <Space direction="vertical" size={16} style={{ display: 'flex' }}>
          <div>
            <Typography.Title level={5} style={{ marginBottom: 8 }}>
              {preview.scenarioLabel || '需求澄清协作审阅'}
            </Typography.Title>
            <Space wrap size={[8, 8]} style={{ marginBottom: 8 }}>
              {(preview.participants || []).map((participant) => (
                <Tag key={participant} color="blue">
                  {participant}
                </Tag>
              ))}
              <Tag color={preview.decisionEscalationAdvised ? 'orange' : 'green'}>
                {preview.decisionEscalationAdvised ? '含升级决策建议' : '以澄清留档为主'}
              </Tag>
            </Space>
          </div>

          {(preview.collaborationSummary ||
            preview.blockerAssessment ||
            preview.recommendedOperatorAction ||
            preview.selectionGuidance) ? (
            <Descriptions bordered column={1} size="small" title="协作结论">
              <Descriptions.Item label="结论摘要">
                {preview.collaborationSummary || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="阻塞判断">
                {preview.blockerAssessment || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="推荐操作">
                {preview.recommendedOperatorAction || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="优先应用建议">
                {preview.selectionGuidance || '-'}
              </Descriptions.Item>
            </Descriptions>
          ) : null}

          {(preview.governanceInterpretation || preview.decisionEscalationAdvised !== undefined) ? (
            <Descriptions bordered column={1} size="small" title="治理判断">
              <Descriptions.Item label="治理解释">
                {preview.governanceInterpretation || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="建议人工判断是否提升决策">
                {String(Boolean(preview.decisionEscalationAdvised))}
              </Descriptions.Item>
            </Descriptions>
          ) : null}

          {(preview.roleInsights || []).length ? (
            <Descriptions bordered column={1} size="small" title="角色视角">
              {(preview.roleInsights || []).map((insight) => (
                <Descriptions.Item key={insight} label="角色判断">
                  {insight}
                </Descriptions.Item>
              ))}
            </Descriptions>
          ) : null}

          {preview.governanceLinkage ? (
            <Descriptions bordered column={1} size="small" title="治理联动">
              <Descriptions.Item label="目标模块">
                {String(preview.governanceLinkage.targetModule || '-')}
              </Descriptions.Item>
              <Descriptions.Item label="当前阶段">
                {String(preview.governanceLinkage.currentStageCode || '-')}
              </Descriptions.Item>
              <Descriptions.Item label="项目治理状态">
                {String(preview.governanceLinkage.projectGovernanceStatus || '-')}
              </Descriptions.Item>
              <Descriptions.Item label="已有澄清数">
                {String(preview.governanceLinkage.existingClarificationCount ?? '-')}
              </Descriptions.Item>
              <Descriptions.Item label="待处理澄清数">
                {String(preview.governanceLinkage.openClarificationCount ?? '-')}
              </Descriptions.Item>
              <Descriptions.Item label="既有阻塞澄清数">
                {String(preview.governanceLinkage.existingBlockerClarificationCount ?? '-')}
              </Descriptions.Item>
              <Descriptions.Item label="阻塞级建议数">
                {String(preview.governanceLinkage.blockerSuggestionCount ?? '-')}
              </Descriptions.Item>
              <Descriptions.Item label="建议升级决策数">
                {String(preview.governanceLinkage.decisionEscalationSuggestionCount ?? '-')}
              </Descriptions.Item>
              <Descriptions.Item label="待处理决策 / 审批">
                {String(preview.governanceLinkage.pendingDecisionItemCount ?? '-')} /{' '}
                {String(preview.governanceLinkage.pendingApprovalCount ?? '-')}
              </Descriptions.Item>
              <Descriptions.Item label="建议跟进模块">
                {Array.isArray(preview.governanceLinkage.followUpModules)
                  ? preview.governanceLinkage.followUpModules.join('、') || '-'
                  : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="治理原因摘要">
                {String(preview.governanceLinkage.blockerReasonSummary || '-')}
              </Descriptions.Item>
              <Descriptions.Item label="应用后写入">
                仅会在主系统中创建选中的 `ClarificationItem`
              </Descriptions.Item>
            </Descriptions>
          ) : null}

          {(preview.followUpHints || []).length ? (
            <div>
              <Typography.Text strong>后续治理建议</Typography.Text>
              <Space direction="vertical" size={8} style={{ display: 'flex', marginTop: 8 }}>
                {(preview.followUpHints || []).map((hint) => (
                  <Typography.Paragraph key={hint} style={{ marginBottom: 0 }} type="secondary">
                    {hint}
                  </Typography.Paragraph>
                ))}
              </Space>
            </div>
          ) : null}

          <div>
            <Typography.Text strong>下一轮关键问题</Typography.Text>
            {(preview.nextQuestions || []).length ? (
              <Space direction="vertical" size={8} style={{ display: 'flex', marginTop: 8 }}>
                {(preview.nextQuestions || []).map((question) => (
                  <Typography.Paragraph key={question} style={{ marginBottom: 0 }} type="secondary">
                    {question}
                  </Typography.Paragraph>
                ))}
              </Space>
            ) : (
              <Empty description="暂无下一轮问题" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </div>

          {(preview.suggestions || []).length ? (
            <div>
              <Typography.Text strong>
                建议清单
              </Typography.Text>
              <Typography.Paragraph style={{ marginBottom: 12, marginTop: 8 }} type="secondary">
                已选 {selectedSuggestionIndexes.length} / {(preview.suggestions || []).length} 条。应用后仅会创建选中的{' '}
                <Typography.Text code>ClarificationItem</Typography.Text>。
              </Typography.Paragraph>
              <Checkbox.Group
                style={{ width: '100%' }}
                value={selectedSuggestionIndexes}
                onChange={(values) => {
                  setSelectedSuggestionIndexes(
                    (values as Array<string | number>).map((value) => Number(value)),
                  );
                }}
              >
                <Space direction="vertical" size={12} style={{ display: 'flex' }}>
                  {(preview.suggestions || []).map((item, index) => (
                    <div
                      key={`${item.title || item.question || 'suggestion'}-${item.category || 'category'}-${index}`}
                      style={{ border: '1px solid #f0f0f0', borderRadius: 8, padding: 12 }}
                    >
                      <Space align="start" size={12}>
                        <Checkbox value={index} />
                        <Space direction="vertical" size={4} style={{ width: '100%' }}>
                          <Space wrap size={[8, 4]}>
                            <Typography.Text strong>{item.title || '-'}</Typography.Text>
                            <Tag>{item.category || '-'}</Tag>
                            <Tag color={item.blockerFlag ? 'red' : 'blue'}>
                              {item.blockerFlag ? '阻塞' : '建议'}
                            </Tag>
                            {item.followUpModule ? <Tag color="gold">{item.followUpModule}</Tag> : null}
                            {item.escalationRecommended ? <Tag color="orange">建议升级决策</Tag> : null}
                          </Space>
                          <Typography.Paragraph style={{ marginBottom: 0 }}>
                            {item.question || '-'}
                          </Typography.Paragraph>
                          {item.governanceReason ? (
                            <Typography.Text type="secondary">
                              治理解释：{item.governanceReason}
                            </Typography.Text>
                          ) : null}
                          {item.suggestedOptions ? (
                            <Typography.Text type="secondary">
                              建议选项：{item.suggestedOptions}
                            </Typography.Text>
                          ) : null}
                          {item.reason ? (
                            <Typography.Text type="secondary">
                              原因：{item.reason}
                            </Typography.Text>
                          ) : null}
                        </Space>
                      </Space>
                    </div>
                  ))}
                </Space>
              </Checkbox.Group>
            </div>
          ) : (
            <Empty description="当前没有可用的协作澄清建议" image={Empty.PRESENTED_IMAGE_SIMPLE} />
          )}
        </Space>
      ) : (
        <Empty description="暂无协作审阅结果" image={Empty.PRESENTED_IMAGE_SIMPLE} />
      )}
    </Modal>
  );
};
