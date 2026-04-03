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
  agentAllowedActionOptions,
  agentRoleStatusOptions,
  agentRoleTypeOptions,
  agentStageParticipationOptions,
  getOptionLabel,
  lifecycleStageOptions,
  renderAgentRoleStatusTag,
  renderAgentStageParticipationTag,
} from '@/features/aicoos/projectCenter';
import { formatDateTime } from '@/features/workflow/utils';
import {
  createAgentRoleCenter,
  getAgentRoleCenterDetail,
  getAgentRoleCenterPage,
  updateAgentRoleCenter,
} from '@/services/backend/aicoos';
import type {
  AgentRoleAllowedActionItem,
  AgentRoleCenterDetail,
  AgentRoleCenterListItem,
  AgentRoleStageParticipationItem,
} from '@/services/backend/types';
import { AgentRoleDetailDrawer } from './AgentRoleDetailDrawer';

interface AgentRoleStageFormItem {
  stageCode?: string;
  stageName?: string;
  participationType?: string;
  note?: string;
}

interface AgentRoleActionFormItem {
  actionCode?: string;
  actionName?: string;
  allowedFlag?: number;
  approvalRequiredFlag?: number;
  note?: string;
}

interface AgentRoleFormValues {
  roleCode?: string;
  roleName?: string;
  roleCategory?: string;
  description?: string;
  capabilitySummary?: string;
  status?: string;
  defaultFlag?: number;
  budgetFactor?: number;
  approvalCollaborationFlag?: number;
  approvalRequired?: number;
  maxConcurrency?: number;
  remark?: string;
  stageParticipations?: AgentRoleStageFormItem[];
  allowedActions?: AgentRoleActionFormItem[];
}

const yesNoOptions = [
  { label: '是', value: 1 },
  { label: '否', value: 0 },
];

const defaultStageParticipations: AgentRoleStageFormItem[] = lifecycleStageOptions.map((item) => ({
  stageCode: item.value,
  stageName: item.label,
  participationType: 'not_involved',
}));

const defaultAllowedActions: AgentRoleActionFormItem[] = agentAllowedActionOptions.map((item) => ({
  actionCode: item.value,
  actionName: item.label,
  allowedFlag: 0,
  approvalRequiredFlag: item.value === 'high_risk_action' ? 1 : 0,
}));

const buildFormValues = (detail?: AgentRoleCenterDetail): AgentRoleFormValues => ({
  roleCode: detail?.roleCode || undefined,
  roleName: detail?.roleName || undefined,
  roleCategory: detail?.roleCategory || 'delivery',
  description: detail?.description || undefined,
  capabilitySummary: detail?.capabilitySummary || undefined,
  status: detail?.status || 'active',
  defaultFlag: detail?.defaultFlag ?? 0,
  budgetFactor: detail?.budgetFactor ?? 1,
  approvalCollaborationFlag: detail?.approvalCollaborationFlag ?? 1,
  approvalRequired: detail?.approvalRequired ?? 1,
  maxConcurrency: detail?.maxConcurrency ?? 1,
  remark: detail?.remark || undefined,
  stageParticipations:
    detail?.stageParticipations?.map((item: AgentRoleStageParticipationItem) => ({
      stageCode: item.stageCode || undefined,
      stageName: item.stageName || undefined,
      participationType: item.participationType || undefined,
      note: item.note || undefined,
    })) || defaultStageParticipations,
  allowedActions:
    detail?.allowedActions?.map((item: AgentRoleAllowedActionItem) => ({
      actionCode: item.actionCode || undefined,
      actionName: item.actionName || undefined,
      allowedFlag: item.allowedFlag ?? 0,
      approvalRequiredFlag: item.approvalRequiredFlag ?? 0,
      note: item.note || undefined,
    })) || defaultAllowedActions,
});

const serializePayload = (values: AgentRoleFormValues) => ({
  roleCode: values.roleCode,
  roleName: values.roleName,
  roleCategory: values.roleCategory,
  description: values.description,
  capabilitySummary: values.capabilitySummary,
  status: values.status,
  defaultFlag: values.defaultFlag,
  budgetFactor: values.budgetFactor,
  approvalCollaborationFlag: values.approvalCollaborationFlag,
  approvalRequired: values.approvalRequired,
  maxConcurrency: values.maxConcurrency,
  remark: values.remark,
  stageParticipations: (values.stageParticipations || []).map((item) => ({
    stageCode: item.stageCode,
    stageName: item.stageName,
    participationType: item.participationType,
    note: item.note,
  })),
  allowedActions: (values.allowedActions || []).map((item) => ({
    actionCode: item.actionCode,
    actionName: item.actionName,
    allowedFlag: item.allowedFlag,
    approvalRequiredFlag: item.approvalRequiredFlag,
    note: item.note,
  })),
});

const AgentRoleManagementPage: React.FC = () => {
  const { message } = App.useApp();
  const actionRef = useRef<ActionType | null>(null);
  const formRef = useRef<ProFormInstance<AgentRoleFormValues> | null>(null);
  const [searchParams, setSearchParams] = useSearchParams();
  const [modalOpen, setModalOpen] = useState(false);
  const [editingRoleId, setEditingRoleId] = useState<string>();
  const [detailRoleId, setDetailRoleId] = useState<string | undefined>(
    searchParams.get('roleId') || undefined,
  );

  const updateParams = (nextRoleId?: string) => {
    const nextParams = new URLSearchParams();
    if (nextRoleId) {
      nextParams.set('roleId', nextRoleId);
    }
    setSearchParams(nextParams);
  };

  const syncDetailRoleId = (roleId?: string) => {
    setDetailRoleId(roleId);
    updateParams(roleId);
  };

  const openCreateModal = () => {
    setEditingRoleId(undefined);
    setModalOpen(true);
    requestAnimationFrame(() => {
      formRef.current?.resetFields();
      formRef.current?.setFieldsValue(buildFormValues());
    });
  };

  const openEditModal = async (roleId: string) => {
    setEditingRoleId(roleId);
    setModalOpen(true);
    requestAnimationFrame(() => {
      formRef.current?.resetFields();
    });
    try {
      const response = await getAgentRoleCenterDetail(roleId);
      formRef.current?.setFieldsValue(buildFormValues(response.result));
    } catch (_error) {
      message.error('加载角色详情失败');
    }
  };

  const roleTypeValueEnum = useMemo(
    () => Object.fromEntries(agentRoleTypeOptions.map((item) => [item.value, { text: item.label }])),
    [],
  );

  const roleStatusValueEnum = useMemo(
    () => Object.fromEntries(agentRoleStatusOptions.map((item) => [item.value, { text: item.label }])),
    [],
  );

  const columns: ProColumns<AgentRoleCenterListItem>[] = [
    {
      title: '角色',
      dataIndex: 'roleName',
      search: false,
      width: 320,
      render: (_, record) => (
        <Space direction="vertical" size={4}>
          <Space wrap size={[8, 4]}>
            <Typography.Text strong>{record.roleName || '-'}</Typography.Text>
            {renderAgentRoleStatusTag(record.status)}
            {record.defaultFlag === 1 ? <Tag color="blue">默认角色</Tag> : null}
          </Space>
          <Typography.Text type="secondary">{record.roleCode || '-'}</Typography.Text>
          <Typography.Paragraph
            ellipsis={{ rows: 2, tooltip: record.description || undefined }}
            style={{ marginBottom: 0 }}
            type="secondary"
          >
            {record.description || '暂无角色描述'}
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
      title: '角色类型',
      dataIndex: 'roleCategory',
      valueType: 'select',
      width: 140,
      valueEnum: roleTypeValueEnum,
      render: (value) => getOptionLabel(agentRoleTypeOptions, value as string),
    },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      width: 120,
      valueEnum: roleStatusValueEnum,
      render: (_, record) => renderAgentRoleStatusTag(record.status),
    },
    {
      title: '默认角色',
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
      title: '预算系数',
      dataIndex: 'budgetFactor',
      search: false,
      width: 108,
    },
    {
      title: '审批协作',
      dataIndex: 'approvalCollaborationFlag',
      search: false,
      width: 108,
      render: (_, record) => (record.approvalCollaborationFlag === 1 ? '是' : '否'),
    },
    {
      title: '阶段参与',
      dataIndex: 'involvedStageCount',
      search: false,
      width: 110,
    },
    {
      title: '允许动作',
      dataIndex: 'allowedActionCount',
      search: false,
      width: 110,
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
            syncDetailRoleId(record.id);
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
      title="Agent 角色管理"
      subTitle="把 AI 代理变成明确的业务角色，配置阶段参与、允许动作和治理约束。"
      extra={[
        <Button icon={<PlusOutlined />} key="create" onClick={openCreateModal} type="primary">
          新建角色
        </Button>,
      ]}
    >
      <ProTable<AgentRoleCenterListItem>
        actionRef={actionRef}
        columns={columns}
        rowKey="id"
        request={async (params) => {
          const response = await getAgentRoleCenterPage({
            current: params.current,
            pageSize: params.pageSize,
            keyword: params.keyword as string | undefined,
            roleCategory: params.roleCategory as string | undefined,
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
        scroll={{ x: 1280 }}
        toolBarRender={false}
      />

      <AgentRoleDetailDrawer
        open={!!detailRoleId}
        roleId={detailRoleId}
        onClose={() => {
          syncDetailRoleId(undefined);
        }}
        onEdit={(detail) => {
          syncDetailRoleId(undefined);
          void openEditModal(detail.id);
        }}
      />

      <ModalForm<AgentRoleFormValues>
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
            if (editingRoleId) {
              await updateAgentRoleCenter(editingRoleId, payload);
              message.success('Agent 角色已更新');
            } else {
              await createAgentRoleCenter(payload);
              message.success('Agent 角色已创建');
            }
            setModalOpen(false);
            setEditingRoleId(undefined);
            actionRef.current?.reload();
            return true;
          } catch (_error) {
            message.error(editingRoleId ? '更新角色失败' : '创建角色失败');
            return false;
          }
        }}
        open={modalOpen}
        submitter={{
          searchConfig: {
            resetText: '重置',
            submitText: editingRoleId ? '保存修改' : '创建角色',
          },
        }}
        title={editingRoleId ? '编辑 Agent 角色' : '新建 Agent 角色'}
        width={980}
        wrapperCol={INLINE_FORM_WRAPPER_COL}
        onOpenChange={(nextOpen) => {
          setModalOpen(nextOpen);
          if (!nextOpen) {
            setEditingRoleId(undefined);
          }
        }}
      >
        <ProFormText
          colProps={{ md: 12 }}
          name="roleCode"
          placeholder="如 product-owner-agent"
          rules={[{ required: true, message: '请输入角色编码' }]}
          label="角色编码"
        />
        <ProFormText
          colProps={{ md: 12 }}
          name="roleName"
          placeholder="请输入角色名称"
          rules={[{ required: true, message: '请输入角色名称' }]}
          label="角色名称"
        />
        <ProFormSelect
          colProps={{ md: 12 }}
          name="roleCategory"
          options={agentRoleTypeOptions}
          rules={[{ required: true, message: '请选择角色类型' }]}
          label="角色类型"
        />
        <ProFormSelect
          colProps={{ md: 12 }}
          name="status"
          options={agentRoleStatusOptions}
          rules={[{ required: true, message: '请选择状态' }]}
          label="状态"
        />
        <ProFormDigit
          colProps={{ md: 12 }}
          fieldProps={{ min: 0, precision: 2 }}
          name="budgetFactor"
          label="预算系数"
        />
        <ProFormSelect
          colProps={{ md: 12 }}
          name="defaultFlag"
          options={yesNoOptions}
          label="默认角色"
        />
        <ProFormSelect
          colProps={{ md: 12 }}
          name="approvalCollaborationFlag"
          options={yesNoOptions}
          label="审批协作"
        />
        <ProFormSelect
          colProps={{ md: 12 }}
          name="approvalRequired"
          options={yesNoOptions}
          label="需要审批"
        />
        <ProFormDigit
          colProps={{ md: 12 }}
          fieldProps={{ min: 1, precision: 0 }}
          name="maxConcurrency"
          label="最大并发"
        />

        <ProFormTextArea
          colProps={{ md: 24 }}
          fieldProps={{ rows: 2 }}
          name="description"
          label="角色描述"
        />
        <ProFormTextArea
          colProps={{ md: 24 }}
          fieldProps={{ rows: 2 }}
          name="capabilitySummary"
          label="职责摘要"
        />

        <Typography.Title level={5} style={{ marginBottom: 8 }}>
          阶段参与配置
        </Typography.Title>
        <ProFormList
          colProps={{ span: 24 }}
          copyIconProps={false}
          creatorButtonProps={false}
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
          name="stageParticipations"
          label="阶段参与配置"
        >
          <ProFormGroup>
            <ProFormSelect
              colProps={{ md: 8 }}
              name="stageCode"
              options={lifecycleStageOptions}
              rules={[{ required: true, message: '请选择阶段编码' }]}
              label="阶段编码"
            />
            <ProFormText
              colProps={{ md: 8 }}
              name="stageName"
              rules={[{ required: true, message: '请输入阶段名称' }]}
              label="阶段名称"
            />
            <ProFormSelect
              colProps={{ md: 8 }}
              name="participationType"
              options={agentStageParticipationOptions}
              rules={[{ required: true, message: '请选择参与类型' }]}
              label="参与类型"
            />
          </ProFormGroup>
          <ProFormTextArea
            colProps={{ md: 24 }}
            fieldProps={{ rows: 2 }}
            name="note"
            label="备注"
          />
        </ProFormList>

        <Typography.Title level={5} style={{ marginBottom: 8 }}>
          允许动作配置
        </Typography.Title>
        <ProFormList
          colProps={{ span: 24 }}
          copyIconProps={false}
          creatorButtonProps={false}
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
          name="allowedActions"
          label="允许动作配置"
        >
          <ProFormGroup>
            <ProFormSelect
              colProps={{ md: 8 }}
              name="actionCode"
              options={agentAllowedActionOptions}
              rules={[{ required: true, message: '请选择动作编码' }]}
              label="动作编码"
            />
            <ProFormText
              colProps={{ md: 8 }}
              name="actionName"
              rules={[{ required: true, message: '请输入动作名称' }]}
              label="动作名称"
            />
            <ProFormSelect
              colProps={{ md: 8 }}
              name="allowedFlag"
              options={yesNoOptions}
              label="是否允许"
            />
          </ProFormGroup>
          <ProFormGroup>
            <ProFormSelect
              colProps={{ md: 12 }}
              name="approvalRequiredFlag"
              options={yesNoOptions}
              label="需要审批"
            />
          </ProFormGroup>
          <ProFormTextArea
            colProps={{ md: 24 }}
            fieldProps={{ rows: 2 }}
            name="note"
            label="备注"
          />
        </ProFormList>

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

export default AgentRoleManagementPage;
