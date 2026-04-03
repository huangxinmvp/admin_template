import type { ProFormInstance } from '@ant-design/pro-components';
import {
  ModalForm,
  ProCard,
  ProFormDateTimePicker,
  ProFormDigit,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
  StatisticCard,
} from '@ant-design/pro-components';
import { history } from '@umijs/max';
import {
  App,
  Button,
  Descriptions,
  Drawer,
  Empty,
  List,
  Progress,
  Space,
  Spin,
  Typography,
} from 'antd';
import dayjs from 'dayjs';
import React, { useEffect, useRef, useState } from 'react';
import {
  approvalTypeOptions,
  budgetLedgerEntryTypeOptions,
  budgetPlanStatusOptions,
  formatBudgetAmount,
  getOptionLabel,
  projectTypeOptions,
  renderApprovalBlockerTag,
  renderApprovalStatusTag,
  renderBudgetLedgerEntryTag,
  renderBudgetStatusTag,
} from '@/features/aicoos/projectCenter';
import { INLINE_FORM_LABEL_COL, INLINE_FORM_WRAPPER_COL } from '@/constants/formLayout';
import { formatDateTime } from '@/features/workflow/utils';
import {
  createApprovalFromBudgetPlan,
  getApprovalsBySource,
  getBudgetCenterDetail,
} from '@/services/backend/aicoos';
import { createResource, updateResource } from '@/services/backend/resources';
import type {
  ApprovalRecordItem,
  BudgetCenterDetail,
  BudgetLedgerItem,
  BudgetPlanRecord,
  BudgetRoleAllocationItem,
} from '@/services/backend/types';

const BUDGET_PLAN_RESOURCE_PATH = '/api/aicoos/budgetPlan';
const BUDGET_LEDGER_RESOURCE_PATH = '/api/aicoos/budgetLedger';

interface BudgetPlanFormValues {
  planName?: string;
  currencyCode?: string;
  proposedAmount?: number;
  approvedAmount?: number;
  reservedAmount?: number;
  consumedAmount?: number;
  roleAllocationsJson?: string;
  status?: string;
  effectiveAt?: dayjs.Dayjs;
  remark?: string;
}

interface BudgetLedgerFormValues {
  entryType?: string;
  amount?: number;
  balanceAfter?: number;
  referenceType?: string;
  referenceId?: string;
  occurredAt?: dayjs.Dayjs;
  description?: string;
  remark?: string;
}

interface BudgetCenterDetailDrawerProps {
  open: boolean;
  projectId?: string;
  onClose: () => void;
  onChanged: () => void;
}

const serializePayload = (values: Record<string, any>) => {
  const next: Record<string, any> = {};
  Object.entries(values || {}).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return;
    }
    if (dayjs.isDayjs(value)) {
      next[key] = value.format('YYYY-MM-DDTHH:mm:ss');
      return;
    }
    next[key] = value;
  });
  return next;
};

const buildPlanFormValues = (latestPlan?: BudgetPlanRecord | null): BudgetPlanFormValues => ({
  planName: latestPlan?.planName || undefined,
  currencyCode: latestPlan?.currencyCode || 'TOKEN',
  proposedAmount: latestPlan?.proposedAmount || 0,
  approvedAmount: latestPlan?.approvedAmount || 0,
  reservedAmount: latestPlan?.reservedAmount || 0,
  consumedAmount: latestPlan?.consumedAmount || 0,
  roleAllocationsJson: latestPlan?.roleAllocationsJson || '',
  status: latestPlan?.status || 'draft',
  effectiveAt: latestPlan?.effectiveAt ? dayjs(latestPlan.effectiveAt) : undefined,
  remark: latestPlan?.remark || undefined,
});

const buildLedgerFormValues = (
  latestPlan?: BudgetPlanRecord | null,
  detail?: BudgetCenterDetail,
): BudgetLedgerFormValues => ({
  entryType: latestPlan ? 'adjust' : 'manual_correction',
  amount: 0,
  balanceAfter: detail?.budgetSummary?.remainingAmount || 0,
  occurredAt: dayjs(),
});

export const BudgetCenterDetailDrawer: React.FC<BudgetCenterDetailDrawerProps> = ({
  open,
  projectId,
  onClose,
  onChanged,
}) => {
  const { message } = App.useApp();
  const planFormRef = useRef<ProFormInstance<BudgetPlanFormValues> | null>(null);
  const ledgerFormRef = useRef<ProFormInstance<BudgetLedgerFormValues> | null>(null);
  const [loading, setLoading] = useState(false);
  const [detail, setDetail] = useState<BudgetCenterDetail>();
  const [linkedApprovals, setLinkedApprovals] = useState<ApprovalRecordItem[]>([]);
  const [planModalOpen, setPlanModalOpen] = useState(false);
  const [ledgerModalOpen, setLedgerModalOpen] = useState(false);

  const reloadDetail = async (currentProjectId: string) => {
    setLoading(true);
    try {
      const response = await getBudgetCenterDetail(currentProjectId);
      setDetail(response.result);
      const latestPlanId = response.result?.latestBudgetPlan?.id;
      if (latestPlanId) {
        const approvalResponse = await getApprovalsBySource({
          sourceObjectType: 'budget_plan',
          sourceObjectId: latestPlanId,
        });
        setLinkedApprovals(approvalResponse.result || []);
      } else {
        setLinkedApprovals([]);
      }
    } catch (_error) {
      message.error('加载预算详情失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!open || !projectId) {
      if (!open) {
        setDetail(undefined);
        setLinkedApprovals([]);
      }
      return;
    }
    void reloadDetail(projectId);
  }, [message, open, projectId]);

  const openPlanModal = () => {
    setPlanModalOpen(true);
    requestAnimationFrame(() => {
      planFormRef.current?.resetFields();
      planFormRef.current?.setFieldsValue(buildPlanFormValues(detail?.latestBudgetPlan));
    });
  };

  const openLedgerModal = () => {
    setLedgerModalOpen(true);
    requestAnimationFrame(() => {
      ledgerFormRef.current?.resetFields();
      ledgerFormRef.current?.setFieldsValue(buildLedgerFormValues(detail?.latestBudgetPlan, detail));
    });
  };

  return (
    <>
      <Drawer
        destroyOnHidden
        open={open}
        placement="right"
        title="预算详情"
        width={980}
        extra={
          projectId ? (
            <Space>
              <Button
                onClick={() => {
                  history.push(`/aicoos/projects?projectId=${projectId}`);
                }}
              >
                项目中心
              </Button>
              <Button
                disabled={!detail?.latestBudgetPlan?.id}
                onClick={async () => {
                  if (!detail?.latestBudgetPlan?.id) {
                    return;
                  }
                  const response = await createApprovalFromBudgetPlan(detail.latestBudgetPlan.id);
                  message.success('已创建或打开预算审批');
                  onChanged();
                  history.push(
                    `/aicoos/approval-center?projectId=${projectId || ''}&approvalId=${response.result.id}`,
                  );
                }}
              >
                发起审批
              </Button>
              <Button
                disabled={!projectId}
                onClick={() => {
                  if (projectId) {
                    history.push(`/aicoos/approval-center?projectId=${projectId}`);
                  }
                }}
              >
                审批中心
              </Button>
              <Button onClick={openPlanModal}>
                {detail?.latestBudgetPlan?.id ? '编辑预算计划' : '创建预算计划'}
              </Button>
              <Button onClick={openLedgerModal} type="primary">
                登记预算流水
              </Button>
            </Space>
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
                    {detail.projectName || '-'}
                  </Typography.Title>
                  <Typography.Text type="secondary">
                    {detail.projectCode || '-'} ·{' '}
                    {getOptionLabel(projectTypeOptions, detail.projectType)}
                  </Typography.Text>
                </Space>
              }
              extra={renderBudgetStatusTag(detail.budgetSummary?.status)}
            >
              <Descriptions bordered column={{ xs: 1, md: 2 }} size="small">
                <Descriptions.Item label="预算计划">
                  {detail.budgetSummary?.planName || '未建立'}
                </Descriptions.Item>
                <Descriptions.Item label="最后更新">
                  {formatDateTime(detail.budgetSummary?.lastUpdatedAt)}
                </Descriptions.Item>
                <Descriptions.Item label="币种">
                  {detail.budgetSummary?.currencyCode || 'TOKEN'}
                </Descriptions.Item>
                <Descriptions.Item label="状态">
                  {renderBudgetStatusTag(detail.budgetSummary?.status)}
                </Descriptions.Item>
              </Descriptions>
            </ProCard>

            <ProCard gutter={16} wrap>
              <StatisticCard
                colSpan={{ xs: 24, md: 8, lg: 4 }}
                statistic={{
                  title: '总预算',
                  value: detail.budgetSummary?.totalBudgetAmount || 0,
                  suffix: detail.budgetSummary?.currencyCode || 'TOKEN',
                }}
              />
              <StatisticCard
                colSpan={{ xs: 24, md: 8, lg: 4 }}
                statistic={{
                  title: '锁定预算',
                  value: detail.budgetSummary?.lockedAmount || 0,
                  suffix: detail.budgetSummary?.currencyCode || 'TOKEN',
                }}
              />
              <StatisticCard
                colSpan={{ xs: 24, md: 8, lg: 4 }}
                statistic={{
                  title: '已消耗',
                  value: detail.budgetSummary?.consumedAmount || 0,
                  suffix: detail.budgetSummary?.currencyCode || 'TOKEN',
                }}
              />
              <StatisticCard
                colSpan={{ xs: 24, md: 8, lg: 4 }}
                statistic={{
                  title: '待增补',
                  value: detail.budgetSummary?.pendingIncreaseAmount || 0,
                  suffix: detail.budgetSummary?.currencyCode || 'TOKEN',
                }}
              />
              <StatisticCard
                colSpan={{ xs: 24, md: 8, lg: 4 }}
                statistic={{
                  title: '预算剩余',
                  value: detail.budgetSummary?.remainingAmount || 0,
                  suffix: detail.budgetSummary?.currencyCode || 'TOKEN',
                }}
              />
            </ProCard>

            <ProCard gutter={16} wrap>
              <ProCard title="预算计划摘要" colSpan={{ xs: 24, lg: 10 }}>
                {detail.latestBudgetPlan ? (
                  <Descriptions bordered column={1} size="small">
                    <Descriptions.Item label="计划名称">
                      {detail.latestBudgetPlan.planName || '-'}
                    </Descriptions.Item>
                    <Descriptions.Item label="计划状态">
                      {getOptionLabel(budgetPlanStatusOptions, detail.latestBudgetPlan.status)}
                    </Descriptions.Item>
                    <Descriptions.Item label="提议金额">
                      {formatBudgetAmount(
                        detail.latestBudgetPlan.proposedAmount,
                        detail.latestBudgetPlan.currencyCode || 'TOKEN',
                      )}
                    </Descriptions.Item>
                    <Descriptions.Item label="批准金额">
                      {formatBudgetAmount(
                        detail.latestBudgetPlan.approvedAmount,
                        detail.latestBudgetPlan.currencyCode || 'TOKEN',
                      )}
                    </Descriptions.Item>
                    <Descriptions.Item label="锁定金额">
                      {formatBudgetAmount(
                        detail.latestBudgetPlan.reservedAmount,
                        detail.latestBudgetPlan.currencyCode || 'TOKEN',
                      )}
                    </Descriptions.Item>
                    <Descriptions.Item label="已消耗金额">
                      {formatBudgetAmount(
                        detail.latestBudgetPlan.consumedAmount,
                        detail.latestBudgetPlan.currencyCode || 'TOKEN',
                      )}
                    </Descriptions.Item>
                    <Descriptions.Item label="生效时间">
                      {formatDateTime(detail.latestBudgetPlan.effectiveAt)}
                    </Descriptions.Item>
                    <Descriptions.Item label="备注">
                      <Typography.Paragraph style={{ marginBottom: 0 }}>
                        {detail.latestBudgetPlan.remark || '-'}
                      </Typography.Paragraph>
                    </Descriptions.Item>
                  </Descriptions>
                ) : (
                  <Empty description="当前项目还没有预算计划" image={Empty.PRESENTED_IMAGE_SIMPLE} />
                )}
              </ProCard>

              <ProCard title="角色预算分配" colSpan={{ xs: 24, lg: 14 }}>
                {detail.roleAllocations?.length ? (
                  <Space direction="vertical" size={12} style={{ display: 'flex' }}>
                    {!detail.latestBudgetPlan?.roleAllocationsJson ? (
                      <Typography.Text type="secondary">
                        当前按默认角色配比推导，后续可在预算计划中填写显式角色分配 JSON。
                      </Typography.Text>
                    ) : null}
                    <List
                      dataSource={detail.roleAllocations}
                      renderItem={(item: BudgetRoleAllocationItem) => (
                        <List.Item>
                          <div style={{ width: '100%' }}>
                            <Space
                              align="center"
                              style={{ width: '100%', justifyContent: 'space-between' }}
                            >
                              <Typography.Text strong>{item.roleName || '-'}</Typography.Text>
                              <Typography.Text type="secondary">
                                {formatBudgetAmount(
                                  item.amount,
                                  detail.budgetSummary?.currencyCode || 'TOKEN',
                                )}
                              </Typography.Text>
                            </Space>
                            <Progress
                              percent={item.sharePercent || 0}
                              size="small"
                              style={{ marginTop: 8, marginBottom: 0 }}
                            />
                          </div>
                        </List.Item>
                      )}
                    />
                  </Space>
                ) : (
                  <Empty description="暂无角色预算分配" image={Empty.PRESENTED_IMAGE_SIMPLE} />
                )}
              </ProCard>
            </ProCard>

            <ProCard title="关联审批">
              <List
                dataSource={linkedApprovals}
                locale={{
                  emptyText: (
                    <Empty description="暂无关联审批" image={Empty.PRESENTED_IMAGE_SIMPLE} />
                  ),
                }}
                renderItem={(item: ApprovalRecordItem) => (
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
                            ellipsis={{ rows: 2, tooltip: item.budgetImpactSummary || undefined }}
                            style={{ marginBottom: 0 }}
                            type="secondary"
                          >
                            {item.budgetImpactSummary || item.recommendedAction || '-'}
                          </Typography.Paragraph>
                        </Space>
                      }
                    />
                  </List.Item>
                )}
              />
            </ProCard>

            <ProCard title="预算台账">
              <List
                dataSource={detail.recentLedgerEntries || []}
                locale={{
                  emptyText: (
                    <Empty description="暂无预算流水" image={Empty.PRESENTED_IMAGE_SIMPLE} />
                  ),
                }}
                renderItem={(item: BudgetLedgerItem) => (
                  <List.Item>
                    <List.Item.Meta
                      title={
                        <Space wrap size={[8, 4]}>
                          {renderBudgetLedgerEntryTag(item.entryType)}
                          <Typography.Text strong>
                            {item.description || item.referenceDisplayName || '-'}
                          </Typography.Text>
                        </Space>
                      }
                      description={
                        <Space direction="vertical" size={4} style={{ width: '100%' }}>
                          <Typography.Text type="secondary">
                            {formatBudgetAmount(
                              item.amount,
                              detail.budgetSummary?.currencyCode || 'TOKEN',
                            )}{' '}
                            · 余额 {formatBudgetAmount(
                              item.balanceAfter,
                              detail.budgetSummary?.currencyCode || 'TOKEN',
                            )}{' '}
                            · {formatDateTime(item.occurredAt || item.createTime)}
                          </Typography.Text>
                          {item.referenceDisplayName ? (
                            <Typography.Text type="secondary">
                              关联对象：{item.referenceDisplayName}
                            </Typography.Text>
                          ) : null}
                          {item.remark ? (
                            <Typography.Paragraph style={{ marginBottom: 0 }} type="secondary">
                              {item.remark}
                            </Typography.Paragraph>
                          ) : null}
                        </Space>
                      }
                    />
                  </List.Item>
                )}
              />
            </ProCard>
          </Space>
        ) : (
          <Empty description="未获取到预算详情" image={Empty.PRESENTED_IMAGE_SIMPLE} />
        )}
      </Drawer>

      <ModalForm<BudgetPlanFormValues>
        formRef={planFormRef}
        className="saas-form-modal saas-inline-form"
        grid
        layout="horizontal"
        labelAlign="right"
        labelCol={INLINE_FORM_LABEL_COL}
        modalProps={{ destroyOnClose: true }}
        open={planModalOpen}
        rowProps={{ gutter: [16, 0] }}
        submitter={{ searchConfig: { submitText: '保存预算计划' } }}
        title={detail?.latestBudgetPlan?.id ? '编辑预算计划' : '创建预算计划'}
        width={760}
        wrapperCol={INLINE_FORM_WRAPPER_COL}
        onOpenChange={setPlanModalOpen}
        onFinish={async (values) => {
          if (!projectId) {
            message.warning('缺少项目上下文');
            return false;
          }
          const payload = serializePayload(values);
          try {
            if (detail?.latestBudgetPlan?.id) {
              await updateResource(BUDGET_PLAN_RESOURCE_PATH, detail.latestBudgetPlan.id, {
                ...payload,
                projectId,
              });
            } else {
              await createResource(BUDGET_PLAN_RESOURCE_PATH, {
                ...payload,
                projectId,
              });
            }
            message.success('预算计划已保存');
            setPlanModalOpen(false);
            await reloadDetail(projectId);
            onChanged();
            return true;
          } catch (_error) {
            message.error('保存预算计划失败');
            return false;
          }
        }}
      >
        <ProFormText
          colProps={{ xs: 24, md: 12 }}
          name="planName"
          label="计划名称"
          placeholder="例如：Phase 1 预算计划"
          rules={[{ required: true, message: '请输入预算计划名称' }]}
        />
        <ProFormText colProps={{ xs: 24, md: 12 }} name="currencyCode" label="币种" placeholder="TOKEN" />
        <ProFormDigit colProps={{ xs: 24, md: 12 }} name="proposedAmount" label="提议金额" min={0} />
        <ProFormDigit colProps={{ xs: 24, md: 12 }} name="approvedAmount" label="批准金额" min={0} />
        <ProFormDigit colProps={{ xs: 24, md: 12 }} name="reservedAmount" label="锁定金额" min={0} />
        <ProFormDigit colProps={{ xs: 24, md: 12 }} name="consumedAmount" label="已消耗金额" min={0} />
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          name="status"
          label="状态"
          options={budgetPlanStatusOptions}
          rules={[{ required: true, message: '请选择预算状态' }]}
        />
        <ProFormDateTimePicker colProps={{ xs: 24, md: 12 }} name="effectiveAt" label="生效时间" />
        <ProFormTextArea
          colProps={{ xs: 24, md: 24 }}
          name="roleAllocationsJson"
          label="角色预算分配"
          fieldProps={{
            rows: 5,
            placeholder:
              '{"frontend": 2000, "backend": 2600}\n或\n[{"roleCode":"product_analysis","amount":600}]',
          }}
        />
        <ProFormTextArea colProps={{ xs: 24, md: 24 }} name="remark" label="备注" fieldProps={{ rows: 3 }} />
      </ModalForm>

      <ModalForm<BudgetLedgerFormValues>
        formRef={ledgerFormRef}
        className="saas-form-modal saas-inline-form"
        grid
        layout="horizontal"
        labelAlign="right"
        labelCol={INLINE_FORM_LABEL_COL}
        modalProps={{ destroyOnClose: true }}
        open={ledgerModalOpen}
        rowProps={{ gutter: [16, 0] }}
        submitter={{ searchConfig: { submitText: '登记流水' } }}
        title="登记预算流水"
        width={720}
        wrapperCol={INLINE_FORM_WRAPPER_COL}
        onOpenChange={setLedgerModalOpen}
        onFinish={async (values) => {
          if (!projectId) {
            message.warning('缺少项目上下文');
            return false;
          }
          try {
            await createResource(BUDGET_LEDGER_RESOURCE_PATH, {
              ...serializePayload(values),
              projectId,
              budgetPlanId: detail?.latestBudgetPlan?.id,
            });
            message.success('预算流水已登记');
            setLedgerModalOpen(false);
            await reloadDetail(projectId);
            onChanged();
            return true;
          } catch (_error) {
            message.error('登记预算流水失败');
            return false;
          }
        }}
      >
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          name="entryType"
          label="流水类型"
          options={budgetLedgerEntryTypeOptions}
          rules={[{ required: true, message: '请选择流水类型' }]}
        />
        <ProFormDigit
          colProps={{ xs: 24, md: 12 }}
          name="amount"
          label="金额"
          min={0}
          rules={[{ required: true, message: '请输入金额' }]}
        />
        <ProFormDigit colProps={{ xs: 24, md: 12 }} name="balanceAfter" label="流水后余额" />
        <ProFormText
          colProps={{ xs: 24, md: 12 }}
          name="referenceType"
          label="关联类型"
          placeholder="例如：decision_item"
        />
        <ProFormText colProps={{ xs: 24, md: 12 }} name="referenceId" label="关联对象ID" />
        <ProFormDateTimePicker colProps={{ xs: 24, md: 12 }} name="occurredAt" label="发生时间" />
        <ProFormTextArea
          colProps={{ xs: 24, md: 24 }}
          name="description"
          label="说明"
          rules={[{ required: true, message: '请输入流水说明' }]}
          fieldProps={{ rows: 3 }}
        />
        <ProFormTextArea colProps={{ xs: 24, md: 24 }} name="remark" label="备注" fieldProps={{ rows: 3 }} />
      </ModalForm>
    </>
  );
};
