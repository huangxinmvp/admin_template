import { history } from '@umijs/max';
import type { ProFormInstance } from '@ant-design/pro-components';
import { ModalForm, ProCard, ProFormTextArea } from '@ant-design/pro-components';
import { App, Button, Descriptions, Drawer, Empty, List, Modal, Space, Spin, Tag, Timeline, Typography } from 'antd';
import React, { useEffect, useMemo, useRef, useState } from 'react';
import {
  actOnDecision,
  applyDecisionLinearWrite,
  applyDecisionBudgetImpactSuggestion,
  createApprovalFromDecision,
  generateDecisionBudgetImpactSuggestion,
  getApprovalsBySource,
  getDecisionActionHistory,
  previewDecisionLinearWrite,
} from '@/services/backend/aicoos';
import { getResourceDetail } from '@/services/backend/resources';
import type {
  ApprovalRecordItem,
  BudgetImpactSuggestion,
  DecisionActionLogItem,
  DecisionItemRecord,
} from '@/services/backend/types';
import { LinearWriteModal } from '../components/LinearWriteModal';
import { DecisionBudgetCollaborationModal } from '../components/DecisionBudgetCollaborationModal';
import {
  approvalTypeOptions,
  renderApprovalBlockerTag,
  renderApprovalStatusTag,
  decisionSourceOptions,
  decisionStatusOptions,
  decisionTypeOptions,
  getOptionLabel,
  renderDecisionBlockerTag,
  renderDecisionStatusTag,
} from '@/features/aicoos/projectCenter';
import { formatDateTime } from '@/features/workflow/utils';

const DECISION_RESOURCE_PATH = '/api/aicoos/decisionItem';

interface DecisionActionFormValues {
  comment?: string;
}

interface DecisionDetailDrawerProps {
  open: boolean;
  decisionId?: string;
  projectLabelMap: Record<string, string>;
  onClose: () => void;
  onChanged: () => void;
}

const actionLabelMap: Record<string, string> = {
  promoted: '由澄清项提升',
  confirm: '确认',
  reject: '拒绝',
  defer: '暂缓',
};

export const DecisionDetailDrawer: React.FC<DecisionDetailDrawerProps> = ({
  open,
  decisionId,
  projectLabelMap,
  onClose,
  onChanged,
}) => {
  const { message } = App.useApp();
  const actionFormRef = useRef<ProFormInstance<DecisionActionFormValues> | null>(null);
  const [loading, setLoading] = useState(false);
  const [detail, setDetail] = useState<DecisionItemRecord>();
  const [historyItems, setHistoryItems] = useState<DecisionActionLogItem[]>([]);
  const [linkedApprovals, setLinkedApprovals] = useState<ApprovalRecordItem[]>([]);
  const [actionModalOpen, setActionModalOpen] = useState(false);
  const [pendingActionType, setPendingActionType] = useState<string>();
  const [budgetSuggestionOpen, setBudgetSuggestionOpen] = useState(false);
  const [budgetSuggestionLoading, setBudgetSuggestionLoading] = useState(false);
  const [budgetSuggestion, setBudgetSuggestion] = useState<BudgetImpactSuggestion>();
  const [linearWriteOpen, setLinearWriteOpen] = useState(false);
  const [collaborationOpen, setCollaborationOpen] = useState(false);

  useEffect(() => {
    if (!open || !decisionId) {
      if (!open) {
        setDetail(undefined);
        setHistoryItems([]);
        setLinkedApprovals([]);
      }
      return;
    }
    void reloadDecisionBundle(decisionId);
  }, [decisionId, message, open]);

  const reloadDecisionBundle = async (id: string) => {
    setLoading(true);
    try {
      const [detailResponse, historyResponse, approvalResponse] = await Promise.all([
        getResourceDetail(DECISION_RESOURCE_PATH, id),
        getDecisionActionHistory(id),
        getApprovalsBySource({
          sourceObjectType: 'decision_item',
          sourceObjectId: id,
        }),
      ]);
      setDetail(detailResponse.result as DecisionItemRecord);
      setHistoryItems(historyResponse.result || []);
      setLinkedApprovals(approvalResponse.result || []);
    } catch (_error) {
      message.error('加载决策详情失败');
    } finally {
      setLoading(false);
    }
  };

  const allowFurtherAction = useMemo(
    () => detail?.status !== 'confirmed' && detail?.status !== 'rejected',
    [detail?.status],
  );

  const timelineItems = useMemo(
    () =>
      historyItems.map((item, index) => ({
        key: `${item.id || index}`,
        color:
          item.actionType === 'confirm'
            ? 'green'
            : item.actionType === 'reject'
              ? 'red'
              : item.actionType === 'defer'
                ? 'gray'
                : 'blue',
        children: (
          <Space direction="vertical" size={2}>
            <Space wrap size={[8, 4]}>
              <Typography.Text strong>
                {actionLabelMap[item.actionType || ''] || item.actionType || '动作'}
              </Typography.Text>
              <Typography.Text type="secondary">
                {formatDateTime(item.operatedAt || item.createTime)}
              </Typography.Text>
            </Space>
            <Typography.Text type="secondary">
              状态：{getOptionLabel(decisionStatusOptions, item.previousStatus)} {'->'}{' '}
              {getOptionLabel(decisionStatusOptions, item.nextStatus)}
            </Typography.Text>
            <Typography.Text type="secondary">
              操作人：{item.operatorUserId || '-'}
            </Typography.Text>
            {item.actionComment ? (
              <Typography.Paragraph style={{ marginBottom: 0 }} type="secondary">
                {item.actionComment}
              </Typography.Paragraph>
            ) : null}
          </Space>
        ),
      })),
    [historyItems],
  );

  return (
    <>
      <Drawer
        destroyOnHidden
        open={open}
        placement="right"
        title="决策详情"
        width={920}
        extra={
          detail?.id ? (
            <Space>
              <Button
                onClick={() => {
                  setLinearWriteOpen(true);
                }}
              >
                Linear 导出
              </Button>
              <Button
                onClick={async () => {
                  if (!detail.id) {
                    return;
                  }
                  setBudgetSuggestionLoading(true);
                  try {
                    const response = await generateDecisionBudgetImpactSuggestion(detail.id);
                    setBudgetSuggestion(response.result);
                    setBudgetSuggestionOpen(true);
                  } catch (_error) {
                    message.error('生成预算影响建议失败');
                  } finally {
                    setBudgetSuggestionLoading(false);
                  }
                }}
                loading={budgetSuggestionLoading}
              >
                预算影响建议
              </Button>
              <Button
                onClick={() => {
                  setCollaborationOpen(true);
                }}
              >
                多角色预算评审
              </Button>
              <Button
                onClick={async () => {
                  if (!detail.id) {
                    return;
                  }
                  const response = await createApprovalFromDecision(detail.id);
                  message.success('已创建或打开关联审批');
                  onChanged();
                  history.push(
                    `/aicoos/approval-center?projectId=${detail.projectId || ''}&approvalId=${response.result.id}`,
                  );
                }}
              >
                发起审批
              </Button>
              <Button
                disabled={!detail.projectId}
                onClick={() => {
                  if (detail.projectId) {
                    history.push(`/aicoos/approval-center?projectId=${detail.projectId}`);
                  }
                }}
              >
                审批中心
              </Button>
              {allowFurtherAction ? (
                <>
              <Button
                    onClick={() => {
                      setPendingActionType('defer');
                      setActionModalOpen(true);
                    }}
                  >
                    暂缓
                  </Button>
                  <Button
                    danger
                    onClick={() => {
                      setPendingActionType('reject');
                      setActionModalOpen(true);
                    }}
                  >
                    拒绝
                  </Button>
                  <Button
                    onClick={() => {
                      setPendingActionType('confirm');
                      setActionModalOpen(true);
                    }}
                    type="primary"
                  >
                    确认
                  </Button>
                </>
              ) : null}
            </Space>
          ) : null
        }
        onClose={onClose}
      >
        {loading ? (
          <div
            style={{
              minHeight: 240,
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
                    {detail.title || '-'}
                  </Typography.Title>
                  <Space wrap size={[8, 4]}>
                    {renderDecisionStatusTag(detail.status)}
                    {renderDecisionBlockerTag(detail.blockerFlag)}
                  </Space>
                </Space>
              }
            >
              <Descriptions bordered column={1} size="small">
                <Descriptions.Item label="关联项目">
                  {projectLabelMap[detail.projectId || ''] || detail.projectId || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="决策类型">
                  {getOptionLabel(decisionTypeOptions, detail.itemType)}
                </Descriptions.Item>
                <Descriptions.Item label="来源">
                  {getOptionLabel(decisionSourceOptions, detail.sourceType)}
                  {detail.sourceId ? ` · ${detail.sourceId}` : ''}
                </Descriptions.Item>
                <Descriptions.Item label="描述">
                  <Typography.Paragraph style={{ marginBottom: 0 }}>
                    {detail.description || '-'}
                  </Typography.Paragraph>
                </Descriptions.Item>
                <Descriptions.Item label="影响摘要">
                  <Typography.Paragraph style={{ marginBottom: 0 }}>
                    {detail.impactSummary || '-'}
                  </Typography.Paragraph>
                </Descriptions.Item>
                <Descriptions.Item label="建议选项">
                  <Typography.Paragraph style={{ marginBottom: 0 }}>
                    {detail.suggestedOptions || '-'}
                  </Typography.Paragraph>
                </Descriptions.Item>
                <Descriptions.Item label="推荐选项">
                  {detail.recommendedOption || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="预算影响摘要">
                  <Typography.Paragraph style={{ marginBottom: 0 }}>
                    {detail.budgetImpactSummary || '-'}
                  </Typography.Paragraph>
                </Descriptions.Item>
                <Descriptions.Item label="项目影响摘要">
                  <Typography.Paragraph style={{ marginBottom: 0 }}>
                    {detail.projectImpactSummary || '-'}
                  </Typography.Paragraph>
                </Descriptions.Item>
                <Descriptions.Item label="截止时间">
                  {formatDateTime(detail.dueAt)}
                </Descriptions.Item>
                <Descriptions.Item label="发起人">
                  {detail.requestedByUserId || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="处理人">
                  {detail.assigneeUserId || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="最后更新">
                  {detail.updateBy || '-'} · {formatDateTime(detail.updateTime || detail.createTime)}
                </Descriptions.Item>
                <Descriptions.Item label="备注">
                  <Typography.Paragraph style={{ marginBottom: 0 }}>
                    {detail.remark || '-'}
                  </Typography.Paragraph>
                </Descriptions.Item>
              </Descriptions>
            </ProCard>

            <ProCard title="关联审批">
              <List
                dataSource={linkedApprovals}
                locale={{
                  emptyText: (
                    <Empty description="暂无关联审批" image={Empty.PRESENTED_IMAGE_SIMPLE} />
                  ),
                }}
                renderItem={(item) => (
                  <List.Item
                    actions={[
                      <a
                        key="open"
                        onClick={() => {
                          history.push(`/aicoos/approval-center?projectId=${item.projectId || ''}&approvalId=${item.id}`);
                        }}
                      >
                        查看
                      </a>,
                    ]}
                  >
                    <List.Item.Meta
                      title={
                        <Space wrap size={[8, 4]}>
                          <Typography.Text strong>{item.title || '-'}</Typography.Text>
                          {renderApprovalStatusTag(item.approvalStatus)}
                          {renderApprovalBlockerTag(item.blockerFlag)}
                        </Space>
                      }
                      description={
                        <Space direction="vertical" size={4} style={{ width: '100%' }}>
                          <Typography.Text type="secondary">
                            {getOptionLabel(approvalTypeOptions, item.approvalType)} ·{' '}
                            {formatDateTime(item.submittedAt || item.createTime)}
                          </Typography.Text>
                          <Typography.Paragraph
                            ellipsis={{ rows: 2, tooltip: item.recommendedAction || undefined }}
                            style={{ marginBottom: 0 }}
                            type="secondary"
                          >
                            {item.recommendedAction || item.description || '-'}
                          </Typography.Paragraph>
                        </Space>
                      }
                    />
                  </List.Item>
                )}
              />
            </ProCard>

            <ProCard title="动作历史">
              {timelineItems.length ? (
                <Timeline items={timelineItems} />
              ) : (
                <Empty description="暂无动作历史" image={Empty.PRESENTED_IMAGE_SIMPLE} />
              )}
            </ProCard>
          </Space>
        ) : (
          <Empty description="未获取到决策详情" image={Empty.PRESENTED_IMAGE_SIMPLE} />
        )}
      </Drawer>

      <ModalForm<DecisionActionFormValues>
        formRef={actionFormRef}
        open={actionModalOpen}
        title={`${actionLabelMap[pendingActionType || ''] || '执行动作'}说明`}
        modalProps={{
          destroyOnHidden: true,
          maskClosable: false,
          onCancel: () => {
            setActionModalOpen(false);
            setPendingActionType(undefined);
          },
        }}
        onFinish={async (values) => {
          if (!decisionId || !pendingActionType) {
            return false;
          }
          await actOnDecision(decisionId, {
            actionType: pendingActionType,
            comment: values.comment,
          });
          message.success(`决策已${actionLabelMap[pendingActionType] || '更新'}`);
          setActionModalOpen(false);
          setPendingActionType(undefined);
          await reloadDecisionBundle(decisionId);
          onChanged();
          return true;
        }}
      >
        <ProFormTextArea
          fieldProps={{ rows: 4, showCount: true, maxLength: 1000 }}
          label="说明"
          name="comment"
          placeholder="可选填写本次确认、拒绝或暂缓的原因"
        />
      </ModalForm>

      <Modal
        open={budgetSuggestionOpen}
        title="预算影响建议"
        okText="应用到当前决策"
        onCancel={() => {
          setBudgetSuggestionOpen(false);
        }}
        onOk={async () => {
          if (!detail?.id || !budgetSuggestion) {
            return;
          }
          await applyDecisionBudgetImpactSuggestion(detail.id, budgetSuggestion);
          message.success('预算影响建议已应用');
          setBudgetSuggestionOpen(false);
          await reloadDecisionBundle(detail.id);
          onChanged();
        }}
      >
        {budgetSuggestion ? (
          <Space direction="vertical" size={12} style={{ display: 'flex' }}>
            <Descriptions bordered column={1} size="small">
              <Descriptions.Item label="预算影响摘要">
                <Typography.Paragraph style={{ marginBottom: 0 }}>
                  {budgetSuggestion.budgetImpactSummary || '-'}
                </Typography.Paragraph>
              </Descriptions.Item>
              <Descriptions.Item label="项目影响摘要">
                <Typography.Paragraph style={{ marginBottom: 0 }}>
                  {budgetSuggestion.projectImpactSummary || '-'}
                </Typography.Paragraph>
              </Descriptions.Item>
              <Descriptions.Item label="预算变化范围">
                {budgetSuggestion.deltaRange || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="可信度">
                {budgetSuggestion.confidence || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="受影响角色">
                {(budgetSuggestion.affectedRoles || []).length ? (
                  <Space wrap size={[8, 4]}>
                    {(budgetSuggestion.affectedRoles || []).map((role) => (
                      <Tag key={role}>{role}</Tag>
                    ))}
                  </Space>
                ) : (
                  '-'
                )}
              </Descriptions.Item>
            </Descriptions>
          </Space>
        ) : (
          <Empty description="暂无预算影响建议" image={Empty.PRESENTED_IMAGE_SIMPLE} />
        )}
      </Modal>

      <LinearWriteModal
        open={linearWriteOpen}
        title="决策事项导出到 Linear"
        sourceLabel={detail?.title || detail?.id}
        onClose={() => {
          setLinearWriteOpen(false);
        }}
        onApplied={async () => {
          if (decisionId) {
            await reloadDecisionBundle(decisionId);
          }
          onChanged();
        }}
        previewRequest={async (values) => {
          if (!decisionId) {
            throw new Error('missing decision id');
          }
          const response = await previewDecisionLinearWrite(decisionId, values);
          return response.result;
        }}
        applyRequest={async (values) => {
          if (!decisionId) {
            throw new Error('missing decision id');
          }
          const response = await applyDecisionLinearWrite(decisionId, values);
          return response.result;
        }}
      />

      <DecisionBudgetCollaborationModal
        open={collaborationOpen}
        decisionId={detail?.id}
        onApplied={async () => {
          if (decisionId) {
            await reloadDecisionBundle(decisionId);
          }
          onChanged();
        }}
        onClose={() => {
          setCollaborationOpen(false);
        }}
      />
    </>
  );
};
