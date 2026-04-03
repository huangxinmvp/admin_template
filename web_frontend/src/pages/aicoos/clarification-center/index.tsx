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
import { App, Button, Checkbox, Descriptions, Drawer, Empty, Modal, Select, Space, Tag, Typography } from 'antd';
import React, { useEffect, useMemo, useRef, useState } from 'react';
import {
  clarificationCategoryOptions,
  clarificationSeverityOptions,
  clarificationStatusOptions,
  getOptionLabel,
  renderClarificationSeverityTag,
  renderClarificationStatusTag,
} from '@/features/aicoos/projectCenter';
import { INLINE_FORM_LABEL_COL, INLINE_FORM_WRAPPER_COL } from '@/constants/formLayout';
import { formatDateTime } from '@/features/workflow/utils';
import {
  applyProjectDecisionSuggestions,
  applyClarificationLinearWrite,
  generateProjectClarificationItems,
  generateProjectDecisionSuggestions,
  promoteClarificationToDecision,
  previewClarificationLinearWrite,
} from '@/services/backend/aicoos';
import { createResource, getResourceDetail, queryResourcePage, updateResource } from '@/services/backend/resources';
import { queryPageOptions } from '@/services/backend/system';
import type { ClarificationItemRecord, DecisionPromotionSuggestionItem, OptionItem } from '@/services/backend/types';
import { LinearWriteModal } from '../components/LinearWriteModal';
import { RequirementClarificationCollaborationModal } from '../components/RequirementClarificationCollaborationModal';

interface ClarificationFormValues {
  projectId?: string;
  title?: string;
  question?: string;
  category?: string;
  severity?: string;
  suggestedOptions?: string;
  userResponse?: string;
  status?: string;
  remark?: string;
}

const CLARIFICATION_RESOURCE_PATH = '/api/aicoos/clarificationItem';
const PROJECT_RESOURCE_PATH = '/api/aicoos/project';

const ClarificationCenterPage: React.FC = () => {
  const { message } = App.useApp();
  const actionRef = useRef<ActionType | null>(null);
  const formRef = useRef<ProFormInstance<ClarificationFormValues> | null>(null);
  const [searchParams, setSearchParams] = useSearchParams();
  const [projectOptions, setProjectOptions] = useState<OptionItem[]>([]);
  const [selectedProjectId, setSelectedProjectId] = useState<string | undefined>(
    searchParams.get('projectId') || undefined,
  );
  const [modalOpen, setModalOpen] = useState(false);
  const [editingItemId, setEditingItemId] = useState<string>();
  const [detailItem, setDetailItem] = useState<ClarificationItemRecord>();
  const [decisionSuggestionLoading, setDecisionSuggestionLoading] = useState(false);
  const [decisionSuggestionModalOpen, setDecisionSuggestionModalOpen] = useState(false);
  const [collaborationModalOpen, setCollaborationModalOpen] = useState(false);
  const [decisionSuggestions, setDecisionSuggestions] = useState<DecisionPromotionSuggestionItem[]>([]);
  const [selectedDecisionSuggestionIndexes, setSelectedDecisionSuggestionIndexes] = useState<number[]>([]);
  const [linearTargetItem, setLinearTargetItem] = useState<ClarificationItemRecord>();

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
    setEditingItemId(undefined);
    setModalOpen(true);
    requestAnimationFrame(() => {
      formRef.current?.setFieldsValue({
        projectId: selectedProjectId,
        severity: 'medium',
        status: 'open',
      });
    });
  };

  const openEditModal = async (record: ClarificationItemRecord) => {
    setEditingItemId(record.id);
    setModalOpen(true);
    requestAnimationFrame(() => {
      formRef.current?.resetFields();
    });
    try {
      const response = await getResourceDetail(CLARIFICATION_RESOURCE_PATH, record.id);
      formRef.current?.setFieldsValue(response.result as ClarificationFormValues);
    } catch (_error) {
      message.error('加载澄清项详情失败');
    }
  };

  const columns: ProColumns<ClarificationItemRecord>[] = [
    {
      title: '标题 / 问题',
      dataIndex: 'title',
      search: false,
      width: 360,
      render: (_, record) => (
        <Space direction="vertical" size={4}>
          <Space wrap size={[8, 4]}>
            <Typography.Text strong>{record.title || '-'}</Typography.Text>
            {record.generatedFlag ? <Tag color="blue">示例生成</Tag> : null}
          </Space>
          <Typography.Paragraph
            ellipsis={{ rows: 2, tooltip: record.question || undefined }}
            style={{ marginBottom: 0 }}
            type="secondary"
          >
            {record.question || '-'}
          </Typography.Paragraph>
        </Space>
      ),
    },
    {
      title: '分类',
      dataIndex: 'category',
      valueType: 'select',
      valueEnum: Object.fromEntries(
        clarificationCategoryOptions.map((item) => [item.value, { text: item.label }]),
      ),
      width: 128,
    },
    {
      title: '严重等级',
      dataIndex: 'severity',
      valueType: 'select',
      valueEnum: Object.fromEntries(
        clarificationSeverityOptions.map((item) => [item.value, { text: item.label }]),
      ),
      width: 108,
      render: (_, record) => renderClarificationSeverityTag(record.severity),
    },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: Object.fromEntries(
        clarificationStatusOptions.map((item) => [item.value, { text: item.label }]),
      ),
      width: 132,
      render: (_, record) => renderClarificationStatusTag(record.status),
    },
    {
      title: '建议选项',
      dataIndex: 'suggestedOptions',
      search: false,
      width: 220,
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
      title: '用户回复',
      dataIndex: 'userResponse',
      search: false,
      width: 240,
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
      title: '更新时间',
      dataIndex: 'updateTime',
      search: false,
      width: 168,
      valueType: 'dateTime',
      render: (value) => formatDateTime(value as string),
    },
    {
      title: '操作',
      key: 'action',
      valueType: 'option',
      width: 220,
      render: (_, record) => [
        <a
          key="detail"
          onClick={() => {
            setDetailItem(record);
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
        <a
          key="linear"
          onClick={() => {
            setLinearTargetItem(record);
          }}
        >
          Linear
        </a>,
        <a
          key="promote"
          onClick={async () => {
            if (record.promotedDecisionItemId) {
              history.push(`/aicoos/decision-center?projectId=${record.projectId || ''}`);
              return;
            }
            await promoteClarificationToDecision(record.id);
            message.success('已提升为正式决策事项');
            actionRef.current?.reload();
          }}
        >
          {record.promotedDecisionItemId ? '决策中心' : '转决策'}
        </a>,
      ],
    },
  ];

  return (
    <PageContainer
      className="saas-page-container"
      title="澄清中心"
      content="把模糊或缺失的信息拆成可追踪的澄清问题，记录建议答案、用户回复和阻塞级别。"
      extra={[
        selectedProjectLabel ? (
          <Tag key="project" color="blue">
            {selectedProjectLabel}
          </Tag>
        ) : null,
        <Tag key="endpoint" color="purple">
          /api/aicoos/clarificationItem
        </Tag>,
      ].filter(Boolean)}
    >
      <ProTable<ClarificationItemRecord>
        actionRef={actionRef}
        rowKey="id"
        cardBordered
        columns={columns}
        pagination={{
          showSizeChanger: true,
          showQuickJumper: true,
          defaultPageSize: 10,
          showTotal: (total) => `共 ${total} 条澄清项`,
        }}
        request={async (params) => {
          if (!selectedProjectId) {
            return {
              data: [],
              success: true,
              total: 0,
            };
          }
          const response = await queryResourcePage(CLARIFICATION_RESOURCE_PATH, {
            pageNo: params.current,
            pageSize: params.pageSize,
            projectId: selectedProjectId,
            category: params.category,
            severity: params.severity,
            status: params.status,
          });
          return {
            data: (response.result.records || []) as ClarificationItemRecord[],
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
            key="intake"
            disabled={!selectedProjectId}
            onClick={() => {
              if (selectedProjectId) {
                history.push(`/aicoos/requirement-intake?projectId=${selectedProjectId}`);
              }
            }}
          >
            需求接收
          </Button>,
          <Button
            key="decisions"
            disabled={!selectedProjectId}
            onClick={() => {
              if (selectedProjectId) {
                history.push(`/aicoos/decision-center?projectId=${selectedProjectId}`);
              }
            }}
          >
            决策中心
          </Button>,
          <Button
            key="mock"
            disabled={!selectedProjectId}
            onClick={async () => {
              if (!selectedProjectId) {
                return;
              }
              await generateProjectClarificationItems(selectedProjectId);
              message.success('已生成示例澄清项');
              actionRef.current?.reload();
            }}
          >
            生成示例项
          </Button>,
          <Button
            key="collaboration"
            disabled={!selectedProjectId}
            onClick={() => {
              setCollaborationModalOpen(true);
            }}
          >
            多角色澄清评审
          </Button>,
          <Button
            key="decision-suggestions"
            disabled={!selectedProjectId}
            loading={decisionSuggestionLoading}
            onClick={async () => {
              if (!selectedProjectId) {
                return;
              }
              setDecisionSuggestionLoading(true);
              try {
                const response = await generateProjectDecisionSuggestions(selectedProjectId);
                const suggestions = response.result || [];
                setDecisionSuggestions(suggestions);
                setSelectedDecisionSuggestionIndexes(suggestions.map((_, index) => index));
                setDecisionSuggestionModalOpen(true);
              } catch (_error) {
                message.error('分析决策建议失败');
              } finally {
                setDecisionSuggestionLoading(false);
              }
            }}
          >
            AI 分析转决策
          </Button>,
          <Button
            key="create"
            icon={<PlusOutlined />}
            onClick={openCreateModal}
            type="primary"
          >
            新建澄清项
          </Button>,
        ]}
        tableAlertRender={false}
        locale={{
          emptyText: selectedProjectId ? (
            <Empty description="当前项目暂无澄清项" image={Empty.PRESENTED_IMAGE_SIMPLE} />
          ) : (
            <Empty description="请选择项目后查看澄清项" image={Empty.PRESENTED_IMAGE_SIMPLE} />
          ),
        }}
      />

      <ModalForm<ClarificationFormValues>
        formRef={formRef}
        className="saas-form-modal saas-inline-form"
        grid
        layout="horizontal"
        labelAlign="right"
        labelCol={INLINE_FORM_LABEL_COL}
        open={modalOpen}
        rowProps={{ gutter: [16, 0] }}
        title={editingItemId ? '编辑澄清项' : '新建澄清项'}
        width={900}
        wrapperCol={INLINE_FORM_WRAPPER_COL}
        modalProps={{
          destroyOnHidden: true,
          maskClosable: false,
          onCancel: () => {
            setModalOpen(false);
            setEditingItemId(undefined);
          },
        }}
        onFinish={async (values) => {
          const payload: ClarificationFormValues = {
            projectId: values.projectId,
            title: values.title,
            question: values.question,
            category: values.category,
            severity: values.severity,
            suggestedOptions: values.suggestedOptions,
            userResponse: values.userResponse,
            status: values.status,
            remark: values.remark,
          };
          if (editingItemId) {
            await updateResource(
              CLARIFICATION_RESOURCE_PATH,
              editingItemId,
              payload as Record<string, unknown>,
            );
            message.success('澄清项已更新');
          } else {
            await createResource(CLARIFICATION_RESOURCE_PATH, payload as Record<string, unknown>);
            message.success('澄清项已创建');
          }
          if (values.projectId) {
            syncProjectId(values.projectId);
          }
          setModalOpen(false);
          setEditingItemId(undefined);
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
          rules={[{ required: true, message: '请输入澄清标题' }]}
        />
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          label="分类"
          name="category"
          options={clarificationCategoryOptions}
          rules={[{ required: true, message: '请选择分类' }]}
        />
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          label="严重等级"
          name="severity"
          options={clarificationSeverityOptions}
        />
        <ProFormSelect
          colProps={{ xs: 24, md: 12 }}
          label="状态"
          name="status"
          options={clarificationStatusOptions}
        />
        <ProFormTextArea
          colProps={{ xs: 24, md: 24 }}
          fieldProps={{ rows: 4, showCount: true, maxLength: 1200 }}
          label="问题"
          name="question"
          rules={[{ required: true, message: '请输入澄清问题' }]}
        />
        <ProFormTextArea
          colProps={{ xs: 24, md: 24 }}
          fieldProps={{ rows: 4, showCount: true, maxLength: 1200 }}
          label="建议选项"
          name="suggestedOptions"
          placeholder="可填写建议方向、回答模板或可选方案"
        />
        <ProFormTextArea
          colProps={{ xs: 24, md: 24 }}
          fieldProps={{ rows: 4, showCount: true, maxLength: 1200 }}
          label="用户回复"
          name="userResponse"
          placeholder="记录来自客户或业务方的回复"
        />
        <ProFormTextArea
          colProps={{ xs: 24, md: 24 }}
          fieldProps={{ rows: 3, showCount: true, maxLength: 500 }}
          label="备注"
          name="remark"
        />
      </ModalForm>

      <Drawer
        destroyOnHidden
        open={Boolean(detailItem)}
        placement="right"
        title="澄清项详情"
        width={720}
        onClose={() => {
          setDetailItem(undefined);
        }}
      >
        {detailItem ? (
          <Descriptions bordered column={1} size="small">
            <Descriptions.Item label="标题">{detailItem.title || '-'}</Descriptions.Item>
            <Descriptions.Item label="分类">
              {getOptionLabel(clarificationCategoryOptions, detailItem.category)}
            </Descriptions.Item>
            <Descriptions.Item label="严重等级">
              {renderClarificationSeverityTag(detailItem.severity)}
            </Descriptions.Item>
            <Descriptions.Item label="状态">
              {renderClarificationStatusTag(detailItem.status)}
            </Descriptions.Item>
            <Descriptions.Item label="问题">
              <Typography.Paragraph style={{ marginBottom: 0 }}>
                {detailItem.question || '-'}
              </Typography.Paragraph>
            </Descriptions.Item>
            <Descriptions.Item label="建议选项">
              <Typography.Paragraph style={{ marginBottom: 0 }}>
                {detailItem.suggestedOptions || '-'}
              </Typography.Paragraph>
            </Descriptions.Item>
            <Descriptions.Item label="用户回复">
              <Typography.Paragraph style={{ marginBottom: 0 }}>
                {detailItem.userResponse || '-'}
              </Typography.Paragraph>
            </Descriptions.Item>
            <Descriptions.Item label="备注">
              <Typography.Paragraph style={{ marginBottom: 0 }}>
                {detailItem.remark || '-'}
              </Typography.Paragraph>
            </Descriptions.Item>
            <Descriptions.Item label="更新时间">
              {formatDateTime(detailItem.updateTime || detailItem.createTime)}
            </Descriptions.Item>
          </Descriptions>
        ) : (
          <Empty description="未获取到澄清项详情" image={Empty.PRESENTED_IMAGE_SIMPLE} />
        )}
      </Drawer>

      <Modal
        open={decisionSuggestionModalOpen}
        title="AI 决策提升建议"
        width={920}
        okButtonProps={{
          disabled: !selectedDecisionSuggestionIndexes.length,
        }}
        okText="应用选中建议"
        onCancel={() => {
          setDecisionSuggestionModalOpen(false);
        }}
        onOk={async () => {
          if (!selectedProjectId) {
            return;
          }
          const selectedSuggestions = selectedDecisionSuggestionIndexes
            .map((index) => decisionSuggestions[index])
            .filter(Boolean);
          if (!selectedSuggestions.length) {
            message.warning('请至少选择一条建议');
            return;
          }
          await applyProjectDecisionSuggestions(selectedProjectId, selectedSuggestions);
          message.success('已生成正式决策事项');
          setDecisionSuggestionModalOpen(false);
          actionRef.current?.reload();
        }}
      >
        {decisionSuggestions.length ? (
          <Checkbox.Group
            style={{ width: '100%' }}
            value={selectedDecisionSuggestionIndexes}
            onChange={(values) => {
              setSelectedDecisionSuggestionIndexes((values as Array<string | number>).map((value) => Number(value)));
            }}
          >
            <Space direction="vertical" size={12} style={{ display: 'flex' }}>
              {decisionSuggestions.map((item, index) => (
                <div
                  key={`${item.clarificationId || item.suggestedTitle || 'decision'}-${index}`}
                  style={{ border: '1px solid #f0f0f0', borderRadius: 8, padding: 12 }}
                >
                  <Space align="start" size={12}>
                    <Checkbox value={index} />
                    <Space direction="vertical" size={4} style={{ width: '100%' }}>
                      <Space wrap size={[8, 4]}>
                        <Typography.Text strong>{item.suggestedTitle || '-'}</Typography.Text>
                        <Tag>{item.type || '-'}</Tag>
                        <Tag color={item.blockerFlag ? 'red' : 'blue'}>
                          {item.blockerFlag ? '阻塞' : '建议'}
                        </Tag>
                      </Space>
                      <Typography.Text type="secondary">
                        来源澄清项：{item.clarificationId || '-'}
                      </Typography.Text>
                      <Typography.Paragraph style={{ marginBottom: 0 }}>
                        {item.impactSummary || '-'}
                      </Typography.Paragraph>
                      {item.suggestedOptions ? (
                        <Typography.Text type="secondary">
                          建议选项：{item.suggestedOptions}
                        </Typography.Text>
                      ) : null}
                      {item.recommendedOption ? (
                        <Typography.Text type="secondary">
                          推荐选项：{item.recommendedOption}
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
        ) : (
          <Empty description="当前没有可提升的决策建议" image={Empty.PRESENTED_IMAGE_SIMPLE} />
        )}
      </Modal>

      <LinearWriteModal
        open={Boolean(linearTargetItem)}
        title="澄清项导出到 Linear"
        sourceLabel={linearTargetItem?.title || linearTargetItem?.question || linearTargetItem?.id}
        onClose={() => {
          setLinearTargetItem(undefined);
        }}
        onApplied={() => {
          actionRef.current?.reload();
        }}
        previewRequest={async (values) => {
          if (!linearTargetItem?.id) {
            throw new Error('missing clarification id');
          }
          const response = await previewClarificationLinearWrite(linearTargetItem.id, values);
          return response.result;
        }}
        applyRequest={async (values) => {
          if (!linearTargetItem?.id) {
            throw new Error('missing clarification id');
          }
          const response = await applyClarificationLinearWrite(linearTargetItem.id, values);
          return response.result;
        }}
      />

      <RequirementClarificationCollaborationModal
        open={collaborationModalOpen}
        projectId={selectedProjectId}
        onApplied={() => {
          actionRef.current?.reload();
        }}
        onClose={() => {
          setCollaborationModalOpen(false);
        }}
      />
    </PageContainer>
  );
};

export default ClarificationCenterPage;
