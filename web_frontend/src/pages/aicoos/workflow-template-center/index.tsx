import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns, ProFormInstance } from '@ant-design/pro-components';
import {
  ModalForm,
  PageContainer,
  ProFormDigit,
  ProFormGroup,
  ProFormList,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
  ProTable,
} from '@ant-design/pro-components';
import { useSearchParams } from '@umijs/max';
import { App, Button, Space, Tag, Typography } from 'antd';
import React, { useMemo, useRef, useState } from 'react';
import { INLINE_FORM_LABEL_COL, INLINE_FORM_WRAPPER_COL } from '@/constants/formLayout';
import {
  getOptionLabel,
  lifecycleStageOptions,
  projectTypeOptions,
  renderWorkflowTemplateStatusTag,
  workflowTemplateStatusOptions,
} from '@/features/aicoos/projectCenter';
import { formatDateTime } from '@/features/workflow/utils';
import {
  createWorkflowTemplateCenter,
  getWorkflowTemplateCenterDetail,
  getWorkflowTemplateCenterPage,
  updateWorkflowTemplateCenter,
} from '@/services/backend/aicoos';
import type {
  WorkflowTemplateCenterDetail,
  WorkflowTemplateCenterListItem,
  WorkflowTemplateStageConfigItem,
} from '@/services/backend/types';
import { WorkflowTemplateDetailDrawer } from './WorkflowTemplateDetailDrawer';

interface WorkflowTemplateFormStage {
  stageCode?: string;
  stageName?: string;
  stageOrder?: number;
  enabledFlag?: number;
  stageDescription?: string;
  stageNote?: string;
}

interface WorkflowTemplateFormValues {
  templateCode?: string;
  templateName?: string;
  projectType?: string;
  versionNo?: number;
  status?: string;
  defaultFlag?: number;
  description?: string;
  gateChecksConfig?: string;
  blockingDecisionConfig?: string;
  blockingApprovalConfig?: string;
  budgetThresholdConfig?: string;
  highRiskApprovalConfig?: string;
  remark?: string;
  stages?: WorkflowTemplateFormStage[];
}

const yesNoOptions = [
  { label: '是', value: 1 },
  { label: '否', value: 0 },
];

const defaultStages: WorkflowTemplateFormStage[] = lifecycleStageOptions.map((item, index) => ({
  stageCode: item.value,
  stageName: item.label,
  stageOrder: index + 1,
  enabledFlag: 1,
}));

const buildFormValues = (
  detail?: WorkflowTemplateCenterDetail,
): WorkflowTemplateFormValues => ({
  templateCode: detail?.templateCode || undefined,
  templateName: detail?.templateName || undefined,
  projectType: detail?.projectType || 'delivery',
  versionNo: detail?.versionNo || 1,
  status: detail?.status || 'draft',
  defaultFlag: detail?.defaultFlag ?? 0,
  description: detail?.description || undefined,
  gateChecksConfig: detail?.gateChecksConfig || undefined,
  blockingDecisionConfig: detail?.blockingDecisionConfig || undefined,
  blockingApprovalConfig: detail?.blockingApprovalConfig || undefined,
  budgetThresholdConfig: detail?.budgetThresholdConfig || undefined,
  highRiskApprovalConfig: detail?.highRiskApprovalConfig || undefined,
  remark: detail?.remark || undefined,
  stages:
    detail?.stages?.map((item: WorkflowTemplateStageConfigItem) => ({
      stageCode: item.stageCode || undefined,
      stageName: item.stageName || undefined,
      stageOrder: item.stageOrder || undefined,
      enabledFlag: item.enabledFlag ?? 1,
      stageDescription: item.stageDescription || undefined,
      stageNote: item.stageNote || undefined,
    })) || defaultStages,
});

const serializePayload = (values: WorkflowTemplateFormValues) => ({
  templateCode: values.templateCode,
  templateName: values.templateName,
  projectType: values.projectType,
  versionNo: values.versionNo,
  status: values.status,
  defaultFlag: values.defaultFlag,
  description: values.description,
  gateChecksConfig: values.gateChecksConfig,
  blockingDecisionConfig: values.blockingDecisionConfig,
  blockingApprovalConfig: values.blockingApprovalConfig,
  budgetThresholdConfig: values.budgetThresholdConfig,
  highRiskApprovalConfig: values.highRiskApprovalConfig,
  remark: values.remark,
  stages: (values.stages || []).map((item, index) => ({
    stageCode: item.stageCode,
    stageName: item.stageName,
    stageOrder: item.stageOrder ?? index + 1,
    enabledFlag: item.enabledFlag ?? 1,
    stageDescription: item.stageDescription,
    stageNote: item.stageNote,
  })),
});

const WorkflowTemplateCenterPage: React.FC = () => {
  const { message } = App.useApp();
  const actionRef = useRef<ActionType | null>(null);
  const formRef = useRef<ProFormInstance<WorkflowTemplateFormValues> | null>(null);
  const [searchParams, setSearchParams] = useSearchParams();
  const [modalOpen, setModalOpen] = useState(false);
  const [editingTemplateId, setEditingTemplateId] = useState<string>();
  const [detailTemplateId, setDetailTemplateId] = useState<string | undefined>(
    searchParams.get('templateId') || undefined,
  );

  const updateParams = (nextTemplateId?: string) => {
    const nextParams = new URLSearchParams();
    if (nextTemplateId) {
      nextParams.set('templateId', nextTemplateId);
    }
    setSearchParams(nextParams);
  };

  const syncDetailTemplateId = (templateId?: string) => {
    setDetailTemplateId(templateId);
    updateParams(templateId);
  };

  const openCreateModal = () => {
    setEditingTemplateId(undefined);
    setModalOpen(true);
    requestAnimationFrame(() => {
      formRef.current?.resetFields();
      formRef.current?.setFieldsValue(buildFormValues());
    });
  };

  const openEditModal = async (templateId: string) => {
    setEditingTemplateId(templateId);
    setModalOpen(true);
    requestAnimationFrame(() => {
      formRef.current?.resetFields();
    });
    try {
      const response = await getWorkflowTemplateCenterDetail(templateId);
      formRef.current?.setFieldsValue(buildFormValues(response.result));
    } catch (_error) {
      message.error('加载模板详情失败');
    }
  };

  const projectTypeValueEnum = useMemo(
    () => Object.fromEntries(projectTypeOptions.map((item) => [item.value, { text: item.label }])),
    [],
  );

  const workflowStatusValueEnum = useMemo(
    () =>
      Object.fromEntries(workflowTemplateStatusOptions.map((item) => [item.value, { text: item.label }])),
    [],
  );

  const columns: ProColumns<WorkflowTemplateCenterListItem>[] = [
    {
      title: '模板',
      dataIndex: 'templateName',
      search: false,
      width: 320,
      render: (_, record) => (
        <Space direction="vertical" size={4}>
          <Space wrap size={[8, 4]}>
            <Typography.Text strong>{record.templateName || '-'}</Typography.Text>
            {renderWorkflowTemplateStatusTag(record.status)}
            {record.defaultFlag === 1 ? <Tag color="blue">默认模板</Tag> : null}
          </Space>
          <Typography.Text type="secondary">{record.templateCode || '-'}</Typography.Text>
          <Typography.Paragraph
            ellipsis={{ rows: 2, tooltip: record.description || undefined }}
            style={{ marginBottom: 0 }}
            type="secondary"
          >
            {record.description || '暂无模板说明'}
          </Typography.Paragraph>
        </Space>
      ),
    },
    {
      title: '关键字',
      dataIndex: 'keyword',
      hideInTable: true,
    },
    {
      title: '项目类型',
      dataIndex: 'projectType',
      valueType: 'select',
      width: 140,
      valueEnum: projectTypeValueEnum,
      render: (value) => getOptionLabel(projectTypeOptions, value as string),
    },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      width: 120,
      valueEnum: workflowStatusValueEnum,
      render: (_, record) => renderWorkflowTemplateStatusTag(record.status),
    },
    {
      title: '默认模板',
      dataIndex: 'defaultFlag',
      valueType: 'select',
      width: 120,
      valueEnum: {
        1: { text: '是' },
        0: { text: '否' },
      },
      render: (_, record) => (record.defaultFlag === 1 ? <Tag color="blue">是</Tag> : '否'),
    },
    {
      title: '版本',
      dataIndex: 'versionNo',
      search: false,
      width: 88,
      render: (value) => `v${value || 1}`,
    },
    {
      title: '阶段配置',
      dataIndex: 'enabledStageCount',
      search: false,
      width: 128,
      render: (_, record) => `${record.enabledStageCount || 0} / ${record.stageCount || 0}`,
    },
    {
      title: '最后更新',
      dataIndex: 'updateTime',
      search: false,
      width: 180,
      render: (value) => formatDateTime(value as string),
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
            syncDetailTemplateId(record.id);
          }}
        >
          详情
        </a>,
        <a
          key="edit"
          onClick={() => {
            void openEditModal(record.id);
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
      title="工作流模板中心"
      subTitle="配置不同项目类型的阶段顺序、门禁检查、审批与预算治理要求。"
      extra={[
        <Button icon={<PlusOutlined />} key="create" onClick={openCreateModal} type="primary">
          新建模板
        </Button>,
      ]}
    >
      <ProTable<WorkflowTemplateCenterListItem>
        actionRef={actionRef}
        columns={columns}
        rowKey="id"
        request={async (params) => {
          const response = await getWorkflowTemplateCenterPage({
            current: params.current,
            pageSize: params.pageSize,
            keyword: params.keyword as string | undefined,
            projectType: params.projectType as string | undefined,
            status: params.status as string | undefined,
            defaultFlag:
              params.defaultFlag === undefined || params.defaultFlag === ''
                ? undefined
                : Number(params.defaultFlag),
          });
          const result = response.result;
          return {
            data: result.records || [],
            success: response.success,
            total: result.total || 0,
          };
        }}
        search={{
          labelWidth: 88,
          defaultCollapsed: false,
        }}
        scroll={{ x: 1100 }}
        toolBarRender={false}
      />

      <WorkflowTemplateDetailDrawer
        open={!!detailTemplateId}
        templateId={detailTemplateId}
        onClose={() => {
          syncDetailTemplateId(undefined);
        }}
        onEdit={(detail) => {
          syncDetailTemplateId(undefined);
          void openEditModal(detail.id);
        }}
      />

      <ModalForm<WorkflowTemplateFormValues>
        className="saas-inline-form"
        formRef={formRef}
        grid
        labelCol={INLINE_FORM_LABEL_COL}
        layout="horizontal"
        modalProps={{
          destroyOnClose: true,
          maskClosable: false,
        }}
        onFinish={async (values) => {
          try {
            const payload = serializePayload(values);
            if (editingTemplateId) {
              await updateWorkflowTemplateCenter(editingTemplateId, payload);
              message.success('工作流模板已更新');
            } else {
              await createWorkflowTemplateCenter(payload);
              message.success('工作流模板已创建');
            }
            setModalOpen(false);
            setEditingTemplateId(undefined);
            actionRef.current?.reload();
            return true;
          } catch (_error) {
            message.error(editingTemplateId ? '更新模板失败' : '创建模板失败');
            return false;
          }
        }}
        open={modalOpen}
        submitter={{
          searchConfig: {
            resetText: '重置',
            submitText: editingTemplateId ? '保存修改' : '创建模板',
          },
        }}
        title={editingTemplateId ? '编辑工作流模板' : '新建工作流模板'}
        width={980}
        wrapperCol={INLINE_FORM_WRAPPER_COL}
        onOpenChange={(nextOpen) => {
          setModalOpen(nextOpen);
          if (!nextOpen) {
            setEditingTemplateId(undefined);
          }
        }}
      >
        <ProFormText
          colProps={{ md: 12 }}
          name="templateCode"
          placeholder="如 delivery-standard-v1"
          rules={[{ required: true, message: '请输入模板编码' }]}
          label="模板编码"
        />
        <ProFormText
          colProps={{ md: 12 }}
          name="templateName"
          placeholder="请输入模板名称"
          rules={[{ required: true, message: '请输入模板名称' }]}
          label="模板名称"
        />
        <ProFormSelect
          colProps={{ md: 12 }}
          name="projectType"
          options={projectTypeOptions}
          rules={[{ required: true, message: '请选择项目类型' }]}
          label="项目类型"
        />
        <ProFormSelect
          colProps={{ md: 12 }}
          name="status"
          options={workflowTemplateStatusOptions}
          rules={[{ required: true, message: '请选择状态' }]}
          label="状态"
        />
        <ProFormDigit
          colProps={{ md: 12 }}
          fieldProps={{ min: 1, precision: 0 }}
          name="versionNo"
          label="版本号"
        />
        <ProFormSelect
          colProps={{ md: 12 }}
          name="defaultFlag"
          options={yesNoOptions}
          label="默认模板"
        />

        <Typography.Title level={5} style={{ marginBottom: 8 }}>
          阶段配置
        </Typography.Title>
        <ProFormList
          colProps={{ span: 24 }}
          copyIconProps={false}
          creatorButtonProps={{
            creatorButtonText: '添加阶段',
            type: 'dashed',
          }}
          deleteIconProps={{ tooltipText: '删除阶段' }}
          initialValue={defaultStages}
          itemRender={({ listDom }) => (
            <div
              style={{
                marginBottom: 12,
                border: '1px solid #f0f0f0',
                borderRadius: 8,
                padding: 16,
              }}
            >
              {listDom}
            </div>
          )}
          name="stages"
          label="阶段配置"
        >
          <ProFormGroup>
            <ProFormSelect
              colProps={{ md: 12 }}
              name="stageCode"
              options={lifecycleStageOptions}
              rules={[{ required: true, message: '请选择阶段编码' }]}
              label="阶段编码"
            />
            <ProFormText
              colProps={{ md: 12 }}
              name="stageName"
              rules={[{ required: true, message: '请输入阶段名称' }]}
              label="阶段名称"
            />
            <ProFormDigit
              colProps={{ md: 12 }}
              fieldProps={{ min: 1, precision: 0 }}
              name="stageOrder"
              label="阶段顺序"
            />
            <ProFormSelect
              colProps={{ md: 12 }}
              name="enabledFlag"
              options={yesNoOptions}
              label="启用"
            />
          </ProFormGroup>
          <ProFormTextArea
            colProps={{ md: 24 }}
            fieldProps={{ rows: 2 }}
            name="stageDescription"
            label="阶段说明"
          />
          <ProFormTextArea
            colProps={{ md: 24 }}
            fieldProps={{ rows: 2 }}
            name="stageNote"
            label="阶段备注"
          />
        </ProFormList>

        <ProFormTextArea
          colProps={{ md: 24 }}
          fieldProps={{ rows: 2 }}
          name="description"
          label="模板描述"
        />
        <ProFormTextArea
          colProps={{ md: 24 }}
          fieldProps={{ rows: 3 }}
          name="gateChecksConfig"
          label="Gate 检查"
        />
        <ProFormTextArea
          colProps={{ md: 24 }}
          fieldProps={{ rows: 3 }}
          name="blockingDecisionConfig"
          label="阻塞决策条件"
        />
        <ProFormTextArea
          colProps={{ md: 24 }}
          fieldProps={{ rows: 3 }}
          name="blockingApprovalConfig"
          label="阻塞审批条件"
        />
        <ProFormTextArea
          colProps={{ md: 24 }}
          fieldProps={{ rows: 3 }}
          name="budgetThresholdConfig"
          label="预算阈值条件"
        />
        <ProFormTextArea
          colProps={{ md: 24 }}
          fieldProps={{ rows: 3 }}
          name="highRiskApprovalConfig"
          label="高风险审批要求"
        />
        <ProFormTextArea
          colProps={{ md: 24 }}
          fieldProps={{ rows: 2 }}
          name="remark"
          label="备注"
        />
      </ModalForm>
    </PageContainer>
  );
};

export default WorkflowTemplateCenterPage;
