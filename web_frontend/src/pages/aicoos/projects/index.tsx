import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns, ProFormInstance } from '@ant-design/pro-components';
import {
  ModalForm,
  PageContainer,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
  ProTable,
} from '@ant-design/pro-components';
import { history, useSearchParams } from '@umijs/max';
import { App, Button, Space, Tag, Typography } from 'antd';
import React, { useEffect, useRef, useState } from 'react';
import { ProjectCenterDetailDrawer } from './ProjectCenterDetailDrawer';
import {
  lifecycleStageOptions,
  projectStatusOptions,
  projectTypeOptions,
  renderBudgetStatusTag,
  renderGovernanceStatusTag,
  renderGateStatusTag,
  renderNextStepPriorityTag,
  renderProjectStatusTag,
  renderRiskLevelTag,
  renderStageStatusTag,
  riskLevelOptions,
  formatBudgetAmount,
} from '@/features/aicoos/projectCenter';
import { formatDateTime } from '@/features/workflow/utils';
import { INLINE_FORM_LABEL_COL, INLINE_FORM_WRAPPER_COL } from '@/constants/formLayout';
import { getProjectCenterPage } from '@/services/backend/aicoos';
import { createResource, getResourceDetail, updateResource } from '@/services/backend/resources';
import { queryPageOptions } from '@/services/backend/system';
import type { OptionItem, ProjectCenterListItem } from '@/services/backend/types';

interface ProjectFormValues {
  projectCode?: string;
  projectName?: string;
  projectType?: string;
  intakeSummary?: string;
  currentStageCode?: string;
  status?: string;
  riskLevel?: string;
  ownerUserId?: string;
  workflowTemplateId?: string;
  remark?: string;
}

const PROJECT_RESOURCE_PATH = '/api/aicoos/project';

const ProjectCenterPage: React.FC = () => {
  const { message } = App.useApp();
  const actionRef = useRef<ActionType | null>(null);
  const formRef = useRef<ProFormInstance<ProjectFormValues> | null>(null);
  const [searchParams, setSearchParams] = useSearchParams();
  const [modalOpen, setModalOpen] = useState(false);
  const [editingProjectId, setEditingProjectId] = useState<string>();
  const [detailProjectId, setDetailProjectId] = useState<string | undefined>(
    searchParams.get('projectId') || undefined,
  );
  const [ownerOptions, setOwnerOptions] = useState<OptionItem[]>([]);
  const [workflowTemplateOptions, setWorkflowTemplateOptions] = useState<OptionItem[]>([]);

  const syncDetailProjectId = (projectId?: string) => {
    setDetailProjectId(projectId);
    const nextParams = new URLSearchParams();
    if (projectId) {
      nextParams.set('projectId', projectId);
    }
    setSearchParams(nextParams);
  };

  useEffect(() => {
    let active = true;
    const loadOptions = async () => {
      try {
        const [users, templates] = await Promise.all([
          queryPageOptions('/api/user', 'username'),
          queryPageOptions('/api/aicoos/workflowTemplate', 'templateName'),
        ]);
        if (!active) {
          return;
        }
        setOwnerOptions(users);
        setWorkflowTemplateOptions(templates);
      } catch (_error) {
        if (active) {
          message.warning('部分项目表单选项加载失败');
        }
      }
    };
    void loadOptions();
    return () => {
      active = false;
    };
  }, [message]);

  const openCreateModal = () => {
    setEditingProjectId(undefined);
    setModalOpen(true);
    requestAnimationFrame(() => {
      formRef.current?.setFieldsValue({
        projectType: 'delivery',
        currentStageCode: 'intake',
        status: 'draft',
        riskLevel: 'medium',
      });
    });
  };

  const openEditModal = async (record: ProjectCenterListItem) => {
    setEditingProjectId(record.id);
    setModalOpen(true);
    requestAnimationFrame(() => {
      formRef.current?.resetFields();
    });
    try {
      const response = await getResourceDetail(PROJECT_RESOURCE_PATH, record.id);
      formRef.current?.setFieldsValue(response.result as ProjectFormValues);
    } catch (_error) {
      message.error('加载项目详情失败');
    }
  };

  const columns: ProColumns<ProjectCenterListItem>[] = [
    {
      title: '项目',
      dataIndex: 'projectName',
      fixed: 'left',
      width: 260,
      render: (_, record) => (
        <Space direction="vertical" size={2}>
          <Typography.Text strong>{record.projectName || '-'}</Typography.Text>
          <Space wrap size={[8, 4]}>
            <Typography.Text type="secondary">
              {record.projectCode || '-'}
            </Typography.Text>
            {renderProjectStatusTag(record.status)}
            {renderGovernanceStatusTag(record.governanceStatus)}
          </Space>
          {record.blockerReasonSummary ? (
            <Typography.Paragraph
              ellipsis={{ rows: 1, tooltip: record.blockerReasonSummary }}
              style={{ marginBottom: 0 }}
              type="secondary"
            >
              {record.blockerReasonSummary}
            </Typography.Paragraph>
          ) : null}
        </Space>
      ),
      search: false,
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
      width: 132,
      valueEnum: Object.fromEntries(
        projectTypeOptions.map((item) => [item.value, { text: item.label }]),
      ),
    },
    {
      title: '当前阶段',
      dataIndex: 'currentStageCode',
      valueType: 'select',
      width: 220,
      valueEnum: Object.fromEntries(
        lifecycleStageOptions.map((item) => [item.value, { text: item.label }]),
      ),
      render: (_, record) => (
        <Space direction="vertical" size={4}>
          <Typography.Text>{record.currentStageName || '-'}</Typography.Text>
          <Space wrap size={[8, 4]}>
            {renderStageStatusTag(record.currentStageStatus)}
            {renderGateStatusTag(record.currentGateStatus)}
          </Space>
        </Space>
      ),
    },
    {
      title: '风险等级',
      dataIndex: 'riskLevel',
      valueType: 'select',
      width: 108,
      valueEnum: Object.fromEntries(
        riskLevelOptions.map((item) => [item.value, { text: item.label }]),
      ),
      render: (_, record) => renderRiskLevelTag(record.riskLevel),
    },
    {
      title: '预算状态',
      dataIndex: 'budgetStatus',
      hideInSearch: true,
      width: 210,
      render: (_, record) => (
        <Space direction="vertical" size={4}>
          {renderBudgetStatusTag(record.budgetStatus)}
          <Typography.Text type="secondary">
            {formatBudgetAmount(record.budgetConsumedAmount)} / {formatBudgetAmount(record.budgetApprovedAmount)}
          </Typography.Text>
        </Space>
      ),
    },
    {
      title: '待处理决策',
      dataIndex: 'pendingDecisionItemCount',
      hideInSearch: true,
      width: 146,
      render: (_, record) => (
        <Space direction="vertical" size={2}>
          <Typography.Text>{record.pendingDecisionItemCount ?? 0}</Typography.Text>
          <Typography.Text type="secondary">
            阻塞 {record.blockerDecisionItemCount ?? 0}
          </Typography.Text>
        </Space>
      ),
    },
    {
      title: '下一步',
      dataIndex: 'recommendedNextStep',
      hideInSearch: true,
      width: 230,
      render: (_, record) => (
        <Space direction="vertical" size={2} style={{ width: '100%' }}>
          <Typography.Paragraph
            ellipsis={{ rows: 2, tooltip: record.recommendedNextStep || undefined }}
            style={{ marginBottom: 0 }}
          >
            {record.recommendedNextStep || '-'}
          </Typography.Paragraph>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, minWidth: 0 }}>
            {renderNextStepPriorityTag(record.recommendedPriority)}
            {record.recommendedActorRole ? (
              <Typography.Text
                ellipsis={{ tooltip: record.recommendedActorRole }}
                style={{ display: 'block', flex: 1, minWidth: 0 }}
                type="secondary"
              >
                {record.recommendedActorRole}
              </Typography.Text>
            ) : null}
          </div>
        </Space>
      ),
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      hideInSearch: true,
      width: 168,
      valueType: 'dateTime',
      render: (value) => formatDateTime(value as string),
    },
    {
      title: '操作',
      key: 'action',
      valueType: 'option',
      fixed: 'right',
      width: 250,
      render: (_, record) => [
        <a
          key="detail"
          onClick={() => {
            syncDetailProjectId(record.id);
          }}
        >
          详情
        </a>,
        <a
          key="intake"
          onClick={() => {
            history.push(`/aicoos/requirement-intake?projectId=${record.id}`);
          }}
        >
          需求
        </a>,
        <a
          key="clarification"
          onClick={() => {
            history.push(`/aicoos/clarification-center?projectId=${record.id}`);
          }}
        >
          澄清
        </a>,
        <a
          key="decision"
          onClick={() => {
            history.push(`/aicoos/decision-center?projectId=${record.id}`);
          }}
        >
          决策
        </a>,
        <a
          key="budget"
          onClick={() => {
            history.push(`/aicoos/budget-center?projectId=${record.id}`);
          }}
        >
          预算
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
      title="项目中心"
      content="项目中心将项目、需求接收、澄清、阶段、预算、决策与审批聚合为统一的治理视图，让每个项目都能被当作受控交付计划来运营。"
      extra={[
        <Tag key="endpoint" color="blue">
          /api/aicoos/project/center/page
        </Tag>,
      ]}
    >
      <ProTable<ProjectCenterListItem>
        actionRef={actionRef}
        rowKey="id"
        cardBordered
        columns={columns}
        pagination={{
          showSizeChanger: true,
          showQuickJumper: true,
          defaultPageSize: 10,
          showTotal: (total) => `共 ${total} 个项目`,
        }}
        scroll={{ x: 1380 }}
        request={async (params) => {
          const response = await getProjectCenterPage({
            current: params.current,
            pageSize: params.pageSize,
            keyword: params.keyword as string | undefined,
            projectType: params.projectType as string | undefined,
            currentStageCode: params.currentStageCode as string | undefined,
            status: params.status as string | undefined,
            riskLevel: params.riskLevel as string | undefined,
          });

          return {
            data: response.result.records || [],
            success: response.success,
            total: response.result.total || 0,
          };
        }}
        search={{
          labelWidth: 88,
        }}
        options={{
          density: false,
          reload: true,
          setting: true,
          fullScreen: true,
        }}
        toolBarRender={() => [
          <Button
            key="create"
            icon={<PlusOutlined />}
            onClick={openCreateModal}
            type="primary"
          >
            新建项目
          </Button>,
        ]}
      />

      <ModalForm<ProjectFormValues>
        formRef={formRef}
        className="saas-form-modal saas-inline-form"
        grid
        layout="horizontal"
        labelAlign="right"
        labelCol={INLINE_FORM_LABEL_COL}
        open={modalOpen}
        rowProps={{
          gutter: [16, 0],
        }}
        title={editingProjectId ? '编辑项目' : '新建项目'}
        width={860}
        wrapperCol={INLINE_FORM_WRAPPER_COL}
        modalProps={{
          destroyOnHidden: true,
          maskClosable: false,
          onCancel: () => {
            setModalOpen(false);
            setEditingProjectId(undefined);
          },
        }}
        onFinish={async (values) => {
          const payload: ProjectFormValues = {
            projectCode: values.projectCode,
            projectName: values.projectName,
            projectType: values.projectType,
            intakeSummary: values.intakeSummary,
            currentStageCode: values.currentStageCode,
            status: values.status,
            riskLevel: values.riskLevel,
            ownerUserId: values.ownerUserId,
            workflowTemplateId: values.workflowTemplateId,
            remark: values.remark,
          };

          if (editingProjectId) {
            await updateResource(PROJECT_RESOURCE_PATH, editingProjectId, payload);
            message.success('项目已更新');
          } else {
            await createResource(PROJECT_RESOURCE_PATH, payload as Record<string, unknown>);
            message.success('项目已创建');
          }

          setModalOpen(false);
          setEditingProjectId(undefined);
          actionRef.current?.reload();
          return true;
        }}
      >
        <ProFormText
          colProps={{ xs: 24, md: 12 }}
          label="项目编码"
          name="projectCode"
          rules={[{ required: true, message: '请输入项目编码' }]}
        />
        <ProFormText
          colProps={{ xs: 24, md: 12 }}
          label="项目名称"
          name="projectName"
          rules={[{ required: true, message: '请输入项目名称' }]}
        />
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          label="项目类型"
          name="projectType"
          options={projectTypeOptions}
          rules={[{ required: true, message: '请选择项目类型' }]}
        />
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          label="当前阶段"
          name="currentStageCode"
          options={lifecycleStageOptions}
        />
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          label="项目状态"
          name="status"
          options={projectStatusOptions}
        />
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          label="风险等级"
          name="riskLevel"
          options={riskLevelOptions}
        />
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          label="项目负责人"
          name="ownerUserId"
          options={ownerOptions}
          showSearch
        />
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          label="工作流模板"
          name="workflowTemplateId"
          options={workflowTemplateOptions}
          showSearch
        />
        <ProFormTextArea
          colProps={{ xs: 24, md: 24 }}
          label="需求摘要"
          name="intakeSummary"
          fieldProps={{ rows: 4, showCount: true, maxLength: 500 }}
        />
        <ProFormTextArea
          colProps={{ xs: 24, md: 24 }}
          label="备注"
          name="remark"
          fieldProps={{ rows: 3, showCount: true, maxLength: 500 }}
        />
      </ModalForm>

      <ProjectCenterDetailDrawer
        open={Boolean(detailProjectId)}
        projectId={detailProjectId}
        onClose={() => {
          syncDetailProjectId(undefined);
        }}
      />
    </PageContainer>
  );
};

export default ProjectCenterPage;
