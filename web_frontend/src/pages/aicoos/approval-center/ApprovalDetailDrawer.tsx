import type { ProFormInstance } from '@ant-design/pro-components';
import { ModalForm, ProCard, ProFormTextArea } from '@ant-design/pro-components';
import { history } from '@umijs/max';
import { App, Button, Descriptions, Drawer, Empty, Space, Spin, Timeline, Typography } from 'antd';
import React, { useEffect, useMemo, useRef, useState } from 'react';
import {
  actOnApproval,
  getApprovalActionHistory,
} from '@/services/backend/aicoos';
import { getResourceDetail } from '@/services/backend/resources';
import type {
  ApprovalActionLogItem,
  ApprovalRecordItem,
} from '@/services/backend/types';
import {
  approvalSourceObjectTypeOptions,
  approvalStatusOptions,
  approvalTypeOptions,
  getOptionLabel,
  renderApprovalBlockerTag,
  renderApprovalStatusTag,
} from '@/features/aicoos/projectCenter';
import { formatDateTime } from '@/features/workflow/utils';

const APPROVAL_RESOURCE_PATH = '/api/aicoos/approvalRecord';

interface ApprovalActionFormValues {
  comment?: string;
}

interface ApprovalDetailDrawerProps {
  open: boolean;
  approvalId?: string;
  projectLabelMap: Record<string, string>;
  userLabelMap: Record<string, string>;
  onClose: () => void;
  onChanged: () => void;
}

const actionLabelMap: Record<string, string> = {
  created: '发起审批',
  approve: '批准',
  reject: '拒绝',
  request_changes: '要求修改',
  defer: '暂缓',
};

export const ApprovalDetailDrawer: React.FC<ApprovalDetailDrawerProps> = ({
  open,
  approvalId,
  projectLabelMap,
  userLabelMap,
  onClose,
  onChanged,
}) => {
  const { message } = App.useApp();
  const actionFormRef = useRef<ProFormInstance<ApprovalActionFormValues> | null>(null);
  const [loading, setLoading] = useState(false);
  const [detail, setDetail] = useState<ApprovalRecordItem>();
  const [historyItems, setHistoryItems] = useState<ApprovalActionLogItem[]>([]);
  const [actionModalOpen, setActionModalOpen] = useState(false);
  const [pendingActionType, setPendingActionType] = useState<string>();

  const reloadDetail = async (id: string) => {
    setLoading(true);
    try {
      const [detailResponse, historyResponse] = await Promise.all([
        getResourceDetail(APPROVAL_RESOURCE_PATH, id),
        getApprovalActionHistory(id),
      ]);
      setDetail(detailResponse.result as ApprovalRecordItem);
      setHistoryItems(historyResponse.result || []);
    } catch (_error) {
      message.error('加载审批详情失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!open || !approvalId) {
      if (!open) {
        setDetail(undefined);
        setHistoryItems([]);
      }
      return;
    }
    void reloadDetail(approvalId);
  }, [approvalId, message, open]);

  const allowFurtherAction = useMemo(
    () => detail?.approvalStatus !== 'approved'
      && detail?.approvalStatus !== 'rejected'
      && detail?.approvalStatus !== 'cancelled',
    [detail?.approvalStatus],
  );

  const timelineItems = useMemo(
    () =>
      historyItems.map((item, index) => ({
        key: `${item.id || index}`,
        color:
          item.actionType === 'approve'
            ? 'green'
            : item.actionType === 'reject'
              ? 'red'
              : item.actionType === 'request_changes'
                ? 'gold'
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
              状态：{getOptionLabel(approvalStatusOptions, item.previousStatus)} {'->'}{' '}
              {getOptionLabel(approvalStatusOptions, item.nextStatus)}
            </Typography.Text>
            <Typography.Text type="secondary">
              操作人：{userLabelMap[item.operatorUserId || ''] || item.operatorUserId || '-'}
            </Typography.Text>
            {item.actionComment ? (
              <Typography.Paragraph style={{ marginBottom: 0 }} type="secondary">
                {item.actionComment}
              </Typography.Paragraph>
            ) : null}
          </Space>
        ),
      })),
    [historyItems, userLabelMap],
  );

  const openSourceCenter = () => {
    if (!detail?.projectId) {
      return;
    }
    if (detail.sourceObjectType === 'decision_item') {
      history.push(`/aicoos/decision-center?projectId=${detail.projectId}`);
      return;
    }
    if (detail.sourceObjectType === 'budget_plan' || detail.sourceObjectType === 'budget_ledger') {
      history.push(`/aicoos/budget-center?projectId=${detail.projectId}`);
      return;
    }
    history.push(`/aicoos/projects?projectId=${detail.projectId}`);
  };

  return (
    <>
      <Drawer
        destroyOnHidden
        open={open}
        placement="right"
        title="审批详情"
        width={940}
        extra={
          detail?.id ? (
            <Space>
              <Button disabled={!detail.projectId} onClick={openSourceCenter}>
                查看来源上下文
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
                    onClick={() => {
                      setPendingActionType('request_changes');
                      setActionModalOpen(true);
                    }}
                  >
                    要求修改
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
                      setPendingActionType('approve');
                      setActionModalOpen(true);
                    }}
                    type="primary"
                  >
                    批准
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
                    {renderApprovalStatusTag(detail.approvalStatus)}
                    {renderApprovalBlockerTag(detail.blockerFlag)}
                  </Space>
                </Space>
              }
            >
              <Descriptions bordered column={1} size="small">
                <Descriptions.Item label="关联项目">
                  {projectLabelMap[detail.projectId || ''] || detail.projectId || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="审批类型">
                  {getOptionLabel(approvalTypeOptions, detail.approvalType)}
                </Descriptions.Item>
                <Descriptions.Item label="来源对象">
                  {getOptionLabel(approvalSourceObjectTypeOptions, detail.sourceObjectType)}
                  {detail.sourceObjectId ? ` · ${detail.sourceObjectId}` : ''}
                </Descriptions.Item>
                <Descriptions.Item label="发起人">
                  {userLabelMap[detail.requesterUserId || ''] || detail.requesterUserId || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="审批人">
                  {userLabelMap[detail.approverUserId || ''] || detail.approverUserId || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="背景说明">
                  <Typography.Paragraph style={{ marginBottom: 0 }}>
                    {detail.description || '-'}
                  </Typography.Paragraph>
                </Descriptions.Item>
                <Descriptions.Item label="风险摘要">
                  <Typography.Paragraph style={{ marginBottom: 0 }}>
                    {detail.riskSummary || '-'}
                  </Typography.Paragraph>
                </Descriptions.Item>
                <Descriptions.Item label="预算影响摘要">
                  <Typography.Paragraph style={{ marginBottom: 0 }}>
                    {detail.budgetImpactSummary || '-'}
                  </Typography.Paragraph>
                </Descriptions.Item>
                <Descriptions.Item label="推荐动作">
                  {detail.recommendedAction || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="提交时间">
                  {formatDateTime(detail.submittedAt)}
                </Descriptions.Item>
                <Descriptions.Item label="处理时间">
                  {formatDateTime(detail.decidedAt)}
                </Descriptions.Item>
                <Descriptions.Item label="最近操作人">
                  {userLabelMap[detail.operatorUserId || ''] || detail.operatorUserId || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="最后更新">
                  {detail.updateBy || '-'} · {formatDateTime(detail.updateTime || detail.createTime)}
                </Descriptions.Item>
                <Descriptions.Item label="审批结论">
                  <Typography.Paragraph style={{ marginBottom: 0 }}>
                    {detail.decisionNote || '-'}
                  </Typography.Paragraph>
                </Descriptions.Item>
                <Descriptions.Item label="备注">
                  <Typography.Paragraph style={{ marginBottom: 0 }}>
                    {detail.remark || '-'}
                  </Typography.Paragraph>
                </Descriptions.Item>
              </Descriptions>
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
          <Empty description="未获取到审批详情" image={Empty.PRESENTED_IMAGE_SIMPLE} />
        )}
      </Drawer>

      <ModalForm<ApprovalActionFormValues>
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
          if (!approvalId || !pendingActionType) {
            return false;
          }
          await actOnApproval(approvalId, {
            actionType: pendingActionType,
            comment: values.comment,
          });
          message.success(`审批已${actionLabelMap[pendingActionType] || '更新'}`);
          setActionModalOpen(false);
          setPendingActionType(undefined);
          await reloadDetail(approvalId);
          onChanged();
          return true;
        }}
      >
        <ProFormTextArea
          fieldProps={{ rows: 4, showCount: true, maxLength: 1000 }}
          label="说明"
          name="comment"
          placeholder="可选填写本次批准、拒绝、要求修改或暂缓的原因"
        />
      </ModalForm>
    </>
  );
};
