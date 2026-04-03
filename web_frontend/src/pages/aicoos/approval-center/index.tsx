import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns, ProFormInstance } from '@ant-design/pro-components';
import {
  ModalForm,
  PageContainer,
  ProFormDateTimePicker,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
  ProTable,
} from '@ant-design/pro-components';
import { useSearchParams } from '@umijs/max';
import { App, Button, Select, Space, Typography } from 'antd';
import dayjs from 'dayjs';
import React, { useEffect, useMemo, useRef, useState } from 'react';
import { ApprovalDetailDrawer } from './ApprovalDetailDrawer';
import {
  approvalSourceObjectTypeOptions,
  approvalStatusOptions,
  approvalTypeOptions,
  getOptionLabel,
  renderApprovalBlockerTag,
  renderApprovalStatusTag,
} from '@/features/aicoos/projectCenter';
import { INLINE_FORM_LABEL_COL, INLINE_FORM_WRAPPER_COL } from '@/constants/formLayout';
import { formatDateTime } from '@/features/workflow/utils';
import { createResource, getResourceDetail, queryResourcePage, updateResource } from '@/services/backend/resources';
import { queryPageOptions } from '@/services/backend/system';
import type { ApprovalRecordItem, OptionItem } from '@/services/backend/types';

interface ApprovalFormValues {
  projectId?: string;
  title?: string;
  decisionItemId?: string;
  approvalType?: string;
  sourceObjectType?: string;
  sourceObjectId?: string;
  requesterUserId?: string;
  approverUserId?: string;
  description?: string;
  riskSummary?: string;
  budgetImpactSummary?: string;
  recommendedAction?: string;
  blockerFlag?: number;
  approvalStatus?: string;
  submittedAt?: dayjs.Dayjs;
  remark?: string;
}

const APPROVAL_RESOURCE_PATH = '/api/aicoos/approvalRecord';
const PROJECT_RESOURCE_PATH = '/api/aicoos/project';
const USER_RESOURCE_PATH = '/api/user';

const blockerFilterOptions = [
  { label: '全部', value: '' },
  { label: '阻塞', value: 1 },
  { label: '非阻塞', value: 0 },
];

const buildApprovalFormValues = (record?: ApprovalRecordItem): ApprovalFormValues => ({
  projectId: record?.projectId || undefined,
  title: record?.title || undefined,
  decisionItemId: record?.decisionItemId || undefined,
  approvalType: record?.approvalType || undefined,
  sourceObjectType: record?.sourceObjectType || undefined,
  sourceObjectId: record?.sourceObjectId || undefined,
  requesterUserId: record?.requesterUserId || undefined,
  approverUserId: record?.approverUserId || undefined,
  description: record?.description || undefined,
  riskSummary: record?.riskSummary || undefined,
  budgetImpactSummary: record?.budgetImpactSummary || undefined,
  recommendedAction: record?.recommendedAction || undefined,
  blockerFlag: record?.blockerFlag ?? 0,
  approvalStatus: record?.approvalStatus || 'submitted',
  submittedAt: record?.submittedAt ? dayjs(record.submittedAt) : undefined,
  remark: record?.remark || undefined,
});

const serializeApprovalPayload = (values: ApprovalFormValues) => ({
  projectId: values.projectId,
  title: values.title,
  decisionItemId: values.decisionItemId,
  approvalType: values.approvalType,
  sourceObjectType: values.sourceObjectType,
  sourceObjectId: values.sourceObjectId,
  requesterUserId: values.requesterUserId,
  approverUserId: values.approverUserId,
  description: values.description,
  riskSummary: values.riskSummary,
  budgetImpactSummary: values.budgetImpactSummary,
  recommendedAction: values.recommendedAction,
  blockerFlag: values.blockerFlag,
  approvalStatus: values.approvalStatus,
  submittedAt: values.submittedAt?.format('YYYY-MM-DDTHH:mm:ss'),
  remark: values.remark,
});

const ApprovalCenterPage: React.FC = () => {
  const { message } = App.useApp();
  const actionRef = useRef<ActionType | null>(null);
  const formRef = useRef<ProFormInstance<ApprovalFormValues> | null>(null);
  const [searchParams, setSearchParams] = useSearchParams();
  const [projectOptions, setProjectOptions] = useState<OptionItem[]>([]);
  const [userOptions, setUserOptions] = useState<OptionItem[]>([]);
  const [selectedProjectId, setSelectedProjectId] = useState<string | undefined>(
    searchParams.get('projectId') || undefined,
  );
  const [modalOpen, setModalOpen] = useState(false);
  const [editingApprovalId, setEditingApprovalId] = useState<string>();
  const [detailApprovalId, setDetailApprovalId] = useState<string | undefined>(
    searchParams.get('approvalId') || undefined,
  );

  const projectLabelMap = useMemo(
    () =>
      Object.fromEntries(projectOptions.map((item) => [String(item.value), item.label])) as Record<string, string>,
    [projectOptions],
  );

  const userLabelMap = useMemo(
    () =>
      Object.fromEntries(userOptions.map((item) => [String(item.value), item.label])) as Record<string, string>,
    [userOptions],
  );

  const selectedProjectLabel = useMemo(
    () => projectOptions.find((item) => String(item.value) === selectedProjectId)?.label,
    [projectOptions, selectedProjectId],
  );

  const updateParams = (nextProjectId?: string, nextApprovalId?: string) => {
    const nextParams = new URLSearchParams();
    if (nextProjectId) {
      nextParams.set('projectId', nextProjectId);
    }
    if (nextApprovalId) {
      nextParams.set('approvalId', nextApprovalId);
    }
    setSearchParams(nextParams);
  };

  const syncProjectId = (projectId?: string) => {
    setSelectedProjectId(projectId);
    updateParams(projectId, detailApprovalId);
    actionRef.current?.reload();
  };

  const syncDetailApprovalId = (approvalId?: string) => {
    setDetailApprovalId(approvalId);
    updateParams(selectedProjectId, approvalId);
  };

  useEffect(() => {
    let active = true;
    const loadOptions = async () => {
      try {
        const [projects, users] = await Promise.all([
          queryPageOptions(PROJECT_RESOURCE_PATH, 'projectName'),
          queryPageOptions(USER_RESOURCE_PATH, 'username'),
        ]);
        if (!active) {
          return;
        }
        setProjectOptions(projects);
        setUserOptions(users);
      } catch (_error) {
        if (active) {
          message.error('加载审批中心选项失败');
        }
      }
    };
    void loadOptions();
    return () => {
      active = false;
    };
  }, [message]);

  const openCreateModal = () => {
    setEditingApprovalId(undefined);
    setModalOpen(true);
    requestAnimationFrame(() => {
      formRef.current?.resetFields();
      formRef.current?.setFieldsValue({
        projectId: selectedProjectId,
        approvalType: 'decision',
        sourceObjectType: 'manual',
        blockerFlag: 0,
        approvalStatus: 'submitted',
      });
    });
  };

  const openEditModal = async (record: ApprovalRecordItem) => {
    setEditingApprovalId(record.id);
    setModalOpen(true);
    requestAnimationFrame(() => {
      formRef.current?.resetFields();
    });
    try {
      const response = await getResourceDetail(APPROVAL_RESOURCE_PATH, record.id);
      formRef.current?.setFieldsValue(buildApprovalFormValues(response.result as ApprovalRecordItem));
    } catch (_error) {
      message.error('加载审批详情失败');
    }
  };

  const columns: ProColumns<ApprovalRecordItem>[] = [
    {
      title: '审批事项',
      dataIndex: 'title',
      search: false,
      width: 360,
      render: (_, record) => (
        <Space direction="vertical" size={4}>
          <Space wrap size={[8, 4]}>
            <Typography.Text strong>{record.title || '-'}</Typography.Text>
            {renderApprovalStatusTag(record.approvalStatus)}
            {renderApprovalBlockerTag(record.blockerFlag)}
          </Space>
          <Typography.Text type="secondary">
            {projectLabelMap[record.projectId || ''] || record.projectId || '-'}
          </Typography.Text>
          <Typography.Paragraph
            ellipsis={{ rows: 2, tooltip: record.description || undefined }}
            style={{ marginBottom: 0 }}
            type="secondary"
          >
            {record.description || '-'}
          </Typography.Paragraph>
        </Space>
      ),
    },
    {
      title: '审批类型',
      dataIndex: 'approvalType',
      valueType: 'select',
      width: 150,
      valueEnum: Object.fromEntries(
        approvalTypeOptions.map((item) => [item.value, { text: item.label }]),
      ),
      render: (value) => getOptionLabel(approvalTypeOptions, value as string),
    },
    {
      title: '来源对象',
      dataIndex: 'sourceObjectType',
      valueType: 'select',
      width: 160,
      valueEnum: Object.fromEntries(
        approvalSourceObjectTypeOptions.map((item) => [item.value, { text: item.label }]),
      ),
      render: (_, record) => (
        <Space direction="vertical" size={2}>
          <Typography.Text>{getOptionLabel(approvalSourceObjectTypeOptions, record.sourceObjectType)}</Typography.Text>
          <Typography.Text type="secondary">{record.sourceObjectId || '-'}</Typography.Text>
        </Space>
      ),
    },
    {
      title: '发起人',
      dataIndex: 'requesterUserId',
      valueType: 'select',
      width: 150,
      valueEnum: Object.fromEntries(
        userOptions.map((item) => [item.value, { text: item.label }]),
      ),
      render: (_, record) => userLabelMap[record.requesterUserId || ''] || record.requesterUserId || '-',
    },
    {
      title: '推荐动作',
      dataIndex: 'recommendedAction',
      search: false,
      width: 200,
      render: (value) => (
        <Typography.Paragraph
          ellipsis={{ rows: 2, tooltip: value as string | undefined }}
          style={{ marginBottom: 0 }}
          type="secondary"
        >
          {(value as string) || '-'}
        </Typography.Paragraph>
      ),
    },
    {
      title: '最后更新',
      dataIndex: 'updateTime',
      search: false,
      width: 188,
      render: (_, record) => (
        <Space direction="vertical" size={2}>
          <Typography.Text>{formatDateTime(record.updateTime || record.createTime)}</Typography.Text>
          <Typography.Text type="secondary">
            {userLabelMap[record.operatorUserId || '']
              || userLabelMap[record.approverUserId || '']
              || record.operatorUserId
              || record.updateBy
              || '-'}
          </Typography.Text>
        </Space>
      ),
    },
    {
      title: '操作',
      key: 'action',
      valueType: 'option',
      width: 120,
      render: (_, record) => [
        <a
          key="detail"
          onClick={() => {
            syncDetailApprovalId(record.id);
          }}
        >
          详情
        </a>,
        <a
          key="edit"
          onClick={() => {
            void openEditModal(record);
          }}
        >
          编辑
        </a>,
      ],
    },
  ];

  return (
    <PageContainer
      className="saas-page-container"
      title="审批中心"
      subTitle={
        selectedProjectLabel
          ? `当前聚焦项目：${selectedProjectLabel}`
          : '集中管理高影响决策、预算变化与风险敏感操作的正式审批。'
      }
      extra={[
        <Select
          key="project-filter"
          allowClear
          options={projectOptions}
          placeholder="按项目筛选"
          style={{ width: 240 }}
          value={selectedProjectId}
          onChange={(value) => {
            syncProjectId(value ? String(value) : undefined);
          }}
        />,
      ]}
    >
      <ProTable<ApprovalRecordItem>
        actionRef={actionRef}
        columns={columns}
        rowKey="id"
        request={async (params) => {
          const response = await queryResourcePage(APPROVAL_RESOURCE_PATH, {
            pageNo: params.current,
            pageSize: params.pageSize,
            projectId: selectedProjectId,
            approvalType: params.approvalType,
            sourceObjectType: params.sourceObjectType,
            requesterUserId: params.requesterUserId,
            approvalStatus: params.approvalStatus,
            blockerFlag: params.blockerFlag,
          });
          return {
            data: (response.result.records || []) as ApprovalRecordItem[],
            success: response.success,
            total: response.result.total || 0,
          };
        }}
        search={{
          labelWidth: 96,
          defaultCollapsed: false,
        }}
        scroll={{ x: 1320 }}
        toolBarRender={() => [
          <Button
            key="clear-filter"
            disabled={!selectedProjectId}
            onClick={() => {
              syncProjectId(undefined);
            }}
          >
            清空项目筛选
          </Button>,
          <Button
            key="create"
            icon={<PlusOutlined />}
            onClick={openCreateModal}
            type="primary"
          >
            新建审批
          </Button>,
        ]}
      />

      <ModalForm<ApprovalFormValues>
        formRef={formRef}
        className="saas-form-modal saas-inline-form"
        grid
        layout="horizontal"
        labelAlign="right"
        labelCol={INLINE_FORM_LABEL_COL}
        open={modalOpen}
        rowProps={{ gutter: [16, 0] }}
        title={editingApprovalId ? '编辑审批' : '新建审批'}
        width={820}
        wrapperCol={INLINE_FORM_WRAPPER_COL}
        modalProps={{
          destroyOnHidden: true,
          maskClosable: false,
          onCancel: () => {
            setModalOpen(false);
            setEditingApprovalId(undefined);
          },
        }}
        onFinish={async (values) => {
          const payload = serializeApprovalPayload(values);

          if (editingApprovalId) {
            await updateResource(APPROVAL_RESOURCE_PATH, editingApprovalId, payload);
            message.success('审批已更新');
          } else {
            await createResource(APPROVAL_RESOURCE_PATH, payload as Record<string, unknown>);
            message.success('审批已创建');
          }

          setModalOpen(false);
          setEditingApprovalId(undefined);
          actionRef.current?.reload();
          return true;
        }}
      >
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          label="所属项目"
          name="projectId"
          options={projectOptions}
          rules={[{ required: true, message: '请选择所属项目' }]}
          showSearch
        />
        <ProFormText
          colProps={{ xs: 24, md: 12 }}
          label="审批标题"
          name="title"
          rules={[{ required: true, message: '请输入审批标题' }]}
        />
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          label="审批类型"
          name="approvalType"
          options={approvalTypeOptions}
        />
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          label="来源对象类型"
          name="sourceObjectType"
          options={approvalSourceObjectTypeOptions}
        />
        <ProFormText colProps={{ xs: 24, md: 12 }} label="来源对象ID" name="sourceObjectId" />
        <ProFormText colProps={{ xs: 24, md: 12 }} label="关联决策ID" name="decisionItemId" />
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          label="发起人"
          name="requesterUserId"
          options={userOptions}
          showSearch
        />
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          label="审批人"
          name="approverUserId"
          options={userOptions}
          showSearch
        />
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          label="是否阻塞"
          name="blockerFlag"
          options={blockerFilterOptions.filter((item) => item.value !== '')}
        />
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          label="当前状态"
          name="approvalStatus"
          options={approvalStatusOptions}
        />
        <ProFormDateTimePicker colProps={{ xs: 24, md: 12 }} label="提交时间" name="submittedAt" />
        <ProFormTextArea
          colProps={{ xs: 24, md: 24 }}
          label="背景说明"
          name="description"
          fieldProps={{ rows: 3 }}
        />
        <ProFormTextArea
          colProps={{ xs: 24, md: 24 }}
          label="风险摘要"
          name="riskSummary"
          fieldProps={{ rows: 3 }}
        />
        <ProFormTextArea
          colProps={{ xs: 24, md: 24 }}
          label="预算影响摘要"
          name="budgetImpactSummary"
          fieldProps={{ rows: 3 }}
        />
        <ProFormTextArea
          colProps={{ xs: 24, md: 24 }}
          label="推荐动作"
          name="recommendedAction"
          fieldProps={{ rows: 3 }}
        />
        <ProFormTextArea colProps={{ xs: 24, md: 24 }} label="备注" name="remark" fieldProps={{ rows: 3 }} />
      </ModalForm>

      <ApprovalDetailDrawer
        approvalId={detailApprovalId}
        open={!!detailApprovalId}
        projectLabelMap={projectLabelMap}
        userLabelMap={userLabelMap}
        onChanged={() => {
          actionRef.current?.reload();
        }}
        onClose={() => {
          syncDetailApprovalId(undefined);
        }}
      />
    </PageContainer>
  );
};

export default ApprovalCenterPage;
