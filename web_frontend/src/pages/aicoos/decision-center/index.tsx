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
import { history, useSearchParams } from '@umijs/max';
import { App, Button, Select, Space, Tag, Typography } from 'antd';
import React, { useEffect, useMemo, useRef, useState } from 'react';
import { DecisionDetailDrawer } from './DecisionDetailDrawer';
import {
  decisionSourceOptions,
  decisionStatusOptions,
  decisionTypeOptions,
  getOptionLabel,
  renderDecisionBlockerTag,
  renderDecisionStatusTag,
} from '@/features/aicoos/projectCenter';
import { INLINE_FORM_LABEL_COL, INLINE_FORM_WRAPPER_COL } from '@/constants/formLayout';
import { formatDateTime } from '@/features/workflow/utils';
import { createResource, getResourceDetail, queryResourcePage, updateResource } from '@/services/backend/resources';
import { queryPageOptions } from '@/services/backend/system';
import type { DecisionItemRecord, OptionItem } from '@/services/backend/types';

interface DecisionFormValues {
  projectId?: string;
  title?: string;
  itemType?: string;
  sourceType?: string;
  description?: string;
  impactSummary?: string;
  suggestedOptions?: string;
  recommendedOption?: string;
  budgetImpactSummary?: string;
  projectImpactSummary?: string;
  blockerFlag?: number;
  priority?: string;
  status?: string;
  requestedByUserId?: string;
  assigneeUserId?: string;
  dueAt?: string;
  remark?: string;
}

const DECISION_RESOURCE_PATH = '/api/aicoos/decisionItem';
const PROJECT_RESOURCE_PATH = '/api/aicoos/project';

const blockerFilterOptions = [
  { label: '全部', value: '' },
  { label: '阻塞', value: 1 },
  { label: '非阻塞', value: 0 },
];

const priorityOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '关键', value: 'critical' },
];

const DecisionCenterPage: React.FC = () => {
  const { message } = App.useApp();
  const actionRef = useRef<ActionType | null>(null);
  const formRef = useRef<ProFormInstance<DecisionFormValues> | null>(null);
  const [searchParams, setSearchParams] = useSearchParams();
  const [projectOptions, setProjectOptions] = useState<OptionItem[]>([]);
  const [selectedProjectId, setSelectedProjectId] = useState<string | undefined>(
    searchParams.get('projectId') || undefined,
  );
  const [modalOpen, setModalOpen] = useState(false);
  const [editingDecisionId, setEditingDecisionId] = useState<string>();
  const [detailDecisionId, setDetailDecisionId] = useState<string>();

  const projectLabelMap = useMemo(
    () =>
      Object.fromEntries(
        projectOptions.map((item) => [String(item.value), item.label]),
      ) as Record<string, string>,
    [projectOptions],
  );

  const selectedProjectLabel = useMemo(
    () => projectOptions.find((item) => String(item.value) === selectedProjectId)?.label,
    [projectOptions, selectedProjectId],
  );

  const syncProjectId = (projectId?: string) => {
    setSelectedProjectId(projectId);
    const nextParams = new URLSearchParams();
    if (projectId) {
      nextParams.set('projectId', projectId);
    }
    setSearchParams(nextParams);
    actionRef.current?.reload();
  };

  useEffect(() => {
    let active = true;
    const loadProjects = async () => {
      try {
        const options = await queryPageOptions(PROJECT_RESOURCE_PATH, 'projectName');
        if (active) {
          setProjectOptions(options);
        }
      } catch (_error) {
        if (active) {
          message.error('加载项目选项失败');
        }
      }
    };
    void loadProjects();
    return () => {
      active = false;
    };
  }, [message]);

  const openCreateModal = () => {
    setEditingDecisionId(undefined);
    setModalOpen(true);
    requestAnimationFrame(() => {
      formRef.current?.setFieldsValue({
        projectId: selectedProjectId,
        itemType: 'clarification',
        sourceType: 'manual',
        blockerFlag: 0,
        priority: 'medium',
        status: 'open',
      });
    });
  };

  const openEditModal = async (record: DecisionItemRecord) => {
    setEditingDecisionId(record.id);
    setModalOpen(true);
    requestAnimationFrame(() => {
      formRef.current?.resetFields();
    });
    try {
      const response = await getResourceDetail(DECISION_RESOURCE_PATH, record.id);
      formRef.current?.setFieldsValue(response.result as DecisionFormValues);
    } catch (_error) {
      message.error('加载决策详情失败');
    }
  };

  const columns: ProColumns<DecisionItemRecord>[] = [
    {
      title: '决策事项',
      dataIndex: 'title',
      search: false,
      width: 360,
      render: (_, record) => (
        <Space direction="vertical" size={4}>
          <Space wrap size={[8, 4]}>
            <Typography.Text strong>{record.title || '-'}</Typography.Text>
            {renderDecisionStatusTag(record.status)}
            {renderDecisionBlockerTag(record.blockerFlag)}
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
      title: '类型',
      dataIndex: 'itemType',
      valueType: 'select',
      valueEnum: Object.fromEntries(
        decisionTypeOptions.map((item) => [item.value, { text: item.label }]),
      ),
      width: 140,
      render: (value) => getOptionLabel(decisionTypeOptions, value as string),
    },
    {
      title: '来源',
      dataIndex: 'sourceType',
      valueType: 'select',
      valueEnum: Object.fromEntries(
        decisionSourceOptions.map((item) => [item.value, { text: item.label }]),
      ),
      width: 136,
      render: (value) => getOptionLabel(decisionSourceOptions, value as string),
    },
    {
      title: '推荐选项',
      dataIndex: 'recommendedOption',
      search: false,
      width: 180,
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
      title: '截止时间',
      dataIndex: 'dueAt',
      search: false,
      width: 168,
      valueType: 'dateTime',
      render: (value) => formatDateTime(value as string),
    },
    {
      title: '最后更新',
      dataIndex: 'updateTime',
      search: false,
      width: 180,
      render: (_, record) => (
        <Space direction="vertical" size={2}>
          <Typography.Text>{formatDateTime(record.updateTime || record.createTime)}</Typography.Text>
          <Typography.Text type="secondary">{record.updateBy || record.createBy || '-'}</Typography.Text>
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
            setDetailDecisionId(record.id);
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
      title="决策中心"
      content="把重要澄清结果和治理不确定项转成正式决策事项，用显式确认、拒绝、暂缓动作来建立平台的确认层。"
      extra={[
        selectedProjectLabel ? (
          <Tag key="project" color="blue">
            {selectedProjectLabel}
          </Tag>
        ) : null,
        <Tag key="endpoint" color="gold">
          /api/aicoos/decisionItem
        </Tag>,
      ].filter(Boolean)}
    >
      <ProTable<DecisionItemRecord>
        actionRef={actionRef}
        rowKey="id"
        cardBordered
        columns={columns}
        pagination={{
          showSizeChanger: true,
          showQuickJumper: true,
          defaultPageSize: 10,
          showTotal: (total) => `共 ${total} 条决策事项`,
        }}
        request={async (params) => {
          const response = await queryResourcePage(DECISION_RESOURCE_PATH, {
            pageNo: params.current,
            pageSize: params.pageSize,
            projectId: selectedProjectId,
            itemType: params.itemType,
            sourceType: params.sourceType,
            status: params.status,
            blockerFlag:
              params.blockerFlag === '' || params.blockerFlag === undefined
                ? undefined
                : Number(params.blockerFlag),
          });
          return {
            data: (response.result.records || []) as DecisionItemRecord[],
            success: response.success,
            total: response.result.total || 0,
          };
        }}
        search={{
          labelWidth: 88,
        }}
        toolBarRender={() => [
          <Select
            key="project"
            allowClear
            options={projectOptions}
            placeholder="选择项目"
            showSearch
            style={{ width: 260 }}
            value={selectedProjectId}
            onChange={(value) => {
              syncProjectId(value ? String(value) : undefined);
            }}
          />,
          <Button
            key="projects"
            onClick={() => history.push('/aicoos/projects')}
          >
            项目中心
          </Button>,
          <Button
            key="clarifications"
            disabled={!selectedProjectId}
            onClick={() => {
              if (selectedProjectId) {
                history.push(`/aicoos/clarification-center?projectId=${selectedProjectId}`);
              }
            }}
          >
            澄清中心
          </Button>,
          <Button
            key="create"
            icon={<PlusOutlined />}
            onClick={openCreateModal}
            type="primary"
          >
            新建决策
          </Button>,
        ]}
      />

      <ModalForm<DecisionFormValues>
        formRef={formRef}
        className="saas-form-modal saas-inline-form"
        grid
        layout="horizontal"
        labelAlign="right"
        labelCol={INLINE_FORM_LABEL_COL}
        open={modalOpen}
        rowProps={{ gutter: [16, 0] }}
        title={editingDecisionId ? '编辑决策事项' : '新建决策事项'}
        width={960}
        wrapperCol={INLINE_FORM_WRAPPER_COL}
        modalProps={{
          destroyOnHidden: true,
          maskClosable: false,
          onCancel: () => {
            setModalOpen(false);
            setEditingDecisionId(undefined);
          },
        }}
        onFinish={async (values) => {
          const payload: DecisionFormValues = {
            projectId: values.projectId,
            title: values.title,
            itemType: values.itemType,
            sourceType: values.sourceType,
            description: values.description,
            impactSummary: values.impactSummary,
            suggestedOptions: values.suggestedOptions,
            recommendedOption: values.recommendedOption,
            budgetImpactSummary: values.budgetImpactSummary,
            projectImpactSummary: values.projectImpactSummary,
            blockerFlag: values.blockerFlag,
            priority: values.priority,
            status: values.status,
            requestedByUserId: values.requestedByUserId,
            assigneeUserId: values.assigneeUserId,
            dueAt: values.dueAt,
            remark: values.remark,
          };

          if (editingDecisionId) {
            await updateResource(DECISION_RESOURCE_PATH, editingDecisionId, payload as Record<string, unknown>);
            message.success('决策事项已更新');
          } else {
            await createResource(DECISION_RESOURCE_PATH, payload as Record<string, unknown>);
            message.success('决策事项已创建');
          }
          setModalOpen(false);
          setEditingDecisionId(undefined);
          actionRef.current?.reload();
          return true;
        }}
      >
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          label="关联项目"
          name="projectId"
          options={projectOptions}
          rules={[{ required: true, message: '请选择项目' }]}
          showSearch
        />
        <ProFormText
          colProps={{ xs: 24, md: 12 }}
          label="标题"
          name="title"
          rules={[{ required: true, message: '请输入决策标题' }]}
        />
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          label="决策类型"
          name="itemType"
          options={decisionTypeOptions}
        />
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          label="来源"
          name="sourceType"
          options={decisionSourceOptions}
        />
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          label="阻塞标记"
          name="blockerFlag"
          options={blockerFilterOptions.filter((item) => item.value !== '')}
        />
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          label="优先级"
          name="priority"
          options={priorityOptions}
        />
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          label="状态"
          name="status"
          options={decisionStatusOptions}
        />
        <ProFormDateTimePicker
          colProps={{ xs: 24, md: 12 }}
          label="截止时间"
          name="dueAt"
        />
        <ProFormText
          colProps={{ xs: 24, md: 12 }}
          label="发起人用户ID"
          name="requestedByUserId"
        />
        <ProFormText
          colProps={{ xs: 24, md: 12 }}
          label="处理人用户ID"
          name="assigneeUserId"
        />
        <ProFormTextArea
          colProps={{ xs: 24, md: 24 }}
          fieldProps={{ rows: 4, showCount: true, maxLength: 1200 }}
          label="描述"
          name="description"
          rules={[{ required: true, message: '请输入描述' }]}
        />
        <ProFormTextArea
          colProps={{ xs: 24, md: 24 }}
          fieldProps={{ rows: 3, showCount: true, maxLength: 1200 }}
          label="影响摘要"
          name="impactSummary"
        />
        <ProFormTextArea
          colProps={{ xs: 24, md: 24 }}
          fieldProps={{ rows: 4, showCount: true, maxLength: 1200 }}
          label="建议选项"
          name="suggestedOptions"
        />
        <ProFormTextArea
          colProps={{ xs: 24, md: 24 }}
          fieldProps={{ rows: 4, showCount: true, maxLength: 800 }}
          label="推荐选项"
          name="recommendedOption"
        />
        <ProFormTextArea
          colProps={{ xs: 24, md: 24 }}
          fieldProps={{ rows: 4, showCount: true, maxLength: 1200 }}
          label="预算影响摘要"
          name="budgetImpactSummary"
        />
        <ProFormTextArea
          colProps={{ xs: 24, md: 24 }}
          fieldProps={{ rows: 4, showCount: true, maxLength: 1200 }}
          label="项目影响摘要"
          name="projectImpactSummary"
        />
        <ProFormTextArea
          colProps={{ xs: 24, md: 24 }}
          fieldProps={{ rows: 3, showCount: true, maxLength: 500 }}
          label="备注"
          name="remark"
        />
      </ModalForm>

      <DecisionDetailDrawer
        open={Boolean(detailDecisionId)}
        decisionId={detailDecisionId}
        projectLabelMap={projectLabelMap}
        onClose={() => {
          setDetailDecisionId(undefined);
        }}
        onChanged={() => {
          actionRef.current?.reload();
        }}
      />
    </PageContainer>
  );
};

export default DecisionCenterPage;
