import type { ProFormInstance } from '@ant-design/pro-components';
import {
  PageContainer,
  ProCard,
  ProForm,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
} from '@ant-design/pro-components';
import { history, useSearchParams } from '@umijs/max';
import { Alert, App, Button, Checkbox, Empty, Modal, Space, Tag, Typography } from 'antd';
import React, { useEffect, useMemo, useRef, useState } from 'react';
import { projectTypeOptions } from '@/features/aicoos/projectCenter';
import { INLINE_FORM_LABEL_COL, INLINE_FORM_WRAPPER_COL } from '@/constants/formLayout';
import {
  applyRequirementClarificationSuggestions,
  generateRequirementClarificationSuggestions,
  getProjectRequirementIntake,
  saveProjectRequirementIntake,
} from '@/services/backend/aicoos';
import { getResourceDetail } from '@/services/backend/resources';
import { queryPageOptions } from '@/services/backend/system';
import type {
  ClarificationSuggestionItem,
  OptionItem,
  ProjectRecord,
  RequirementIntakeRecord,
} from '@/services/backend/types';
import { RequirementClarificationCollaborationModal } from '../components/RequirementClarificationCollaborationModal';

interface RequirementIntakeFormValues {
  projectId?: string;
  projectName?: string;
  projectType?: string;
  businessGoal?: string;
  featureSummary?: string;
  referenceProducts?: string;
  timelineExpectation?: string;
  budgetRange?: string;
  technicalConstraints?: string;
  notes?: string;
  attachmentPlaceholders?: string;
}

const PROJECT_RESOURCE_PATH = '/api/aicoos/project';

const RequirementIntakePage: React.FC = () => {
  const { message } = App.useApp();
  const formRef = useRef<ProFormInstance<RequirementIntakeFormValues> | null>(null);
  const [searchParams, setSearchParams] = useSearchParams();
  const [projectOptions, setProjectOptions] = useState<OptionItem[]>([]);
  const [selectedProjectId, setSelectedProjectId] = useState<string | undefined>(
    searchParams.get('projectId') || undefined,
  );
  const [loading, setLoading] = useState(false);
  const [suggestionLoading, setSuggestionLoading] = useState(false);
  const [suggestionModalOpen, setSuggestionModalOpen] = useState(false);
  const [collaborationModalOpen, setCollaborationModalOpen] = useState(false);
  const [clarificationSuggestions, setClarificationSuggestions] = useState<ClarificationSuggestionItem[]>([]);
  const [selectedSuggestionIndexes, setSelectedSuggestionIndexes] = useState<number[]>([]);

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

  useEffect(() => {
    let active = true;
    const loadProjectIntake = async (projectId: string) => {
      setLoading(true);
      try {
        const [intakeResponse, projectResponse] = await Promise.all([
          getProjectRequirementIntake(projectId),
          getResourceDetail(PROJECT_RESOURCE_PATH, projectId),
        ]);
        if (!active) {
          return;
        }
        const intake = intakeResponse.result as RequirementIntakeRecord | null;
        const project = projectResponse.result as ProjectRecord;
        formRef.current?.setFieldsValue({
          projectId,
          projectName: intake?.projectName || project.projectName || '',
          projectType: intake?.projectType || project.projectType || 'delivery',
          businessGoal: intake?.businessGoal || '',
          featureSummary: intake?.featureSummary || '',
          referenceProducts: intake?.referenceProducts || '',
          timelineExpectation: intake?.timelineExpectation || '',
          budgetRange: intake?.budgetRange || '',
          technicalConstraints: intake?.technicalConstraints || '',
          notes: intake?.notes || '',
          attachmentPlaceholders: intake?.attachmentPlaceholders || '',
        });
      } catch (_error) {
        if (active) {
          message.error('加载需求接收失败');
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    if (!selectedProjectId) {
      formRef.current?.resetFields();
      return () => {
        active = false;
      };
    }

    void loadProjectIntake(selectedProjectId);
    return () => {
      active = false;
    };
  }, [message, selectedProjectId]);

  return (
    <PageContainer
      className="saas-page-container"
      title="需求接收"
      content="把模糊项目请求整理成可继续澄清和估算的结构化输入，并同步回项目中心。"
      extra={[
        selectedProjectLabel ? (
          <Tag key="project" color="blue">
            {selectedProjectLabel}
          </Tag>
        ) : null,
        <Tag key="endpoint" color="green">
          /api/aicoos/requirementIntake/project/{`{projectId}`}
        </Tag>,
      ].filter(Boolean)}
    >
      <ProCard
        loading={loading}
        title="项目需求接收表"
        extra={
          <Space>
            <Button onClick={() => history.push('/aicoos/projects')}>返回项目中心</Button>
            <Button
              disabled={!selectedProjectId}
              loading={suggestionLoading}
              onClick={async () => {
                if (!selectedProjectId) {
                  message.warning('请先选择项目');
                  return;
                }
                setSuggestionLoading(true);
                try {
                  const response = await generateRequirementClarificationSuggestions(selectedProjectId);
                  const suggestions = response.result || [];
                  setClarificationSuggestions(suggestions);
                  setSelectedSuggestionIndexes(suggestions.map((_, index) => index));
                  setSuggestionModalOpen(true);
                } catch (_error) {
                  message.error('生成澄清建议失败');
                } finally {
                  setSuggestionLoading(false);
                }
              }}
            >
              生成澄清建议
            </Button>
            <Button
              disabled={!selectedProjectId}
              onClick={() => {
                setCollaborationModalOpen(true);
              }}
            >
              多角色澄清评审
            </Button>
            <Button
              disabled={!selectedProjectId}
              onClick={() => {
                if (selectedProjectId) {
                  history.push(`/aicoos/clarification-center?projectId=${selectedProjectId}`);
                }
              }}
              type="primary"
            >
              进入澄清中心
            </Button>
          </Space>
        }
      >
        <Alert
          showIcon
          style={{ marginBottom: 16 }}
          type="info"
          message="当前阶段只提供附件占位字段。正式文件上传和智能分析将在后续里程碑接入。"
        />

        <ProForm<RequirementIntakeFormValues>
          className="saas-inline-form"
          formRef={formRef}
          grid
          layout="horizontal"
          labelAlign="right"
          labelCol={INLINE_FORM_LABEL_COL}
          rowProps={{ gutter: [16, 0] }}
          submitter={{
            searchConfig: {
              submitText: '保存需求接收',
              resetText: '重置',
            },
          }}
          wrapperCol={INLINE_FORM_WRAPPER_COL}
          onValuesChange={(changedValues) => {
            const nextProjectId = changedValues.projectId as string | undefined;
            if (nextProjectId && nextProjectId !== selectedProjectId) {
              syncProjectId(nextProjectId);
            }
          }}
          onFinish={async (values) => {
            const projectId = values.projectId || selectedProjectId;
            if (!projectId) {
              message.warning('请先选择项目');
              return false;
            }
            await saveProjectRequirementIntake(projectId, {
              projectName: values.projectName,
              projectType: values.projectType,
              businessGoal: values.businessGoal,
              featureSummary: values.featureSummary,
              referenceProducts: values.referenceProducts,
              timelineExpectation: values.timelineExpectation,
              budgetRange: values.budgetRange,
              technicalConstraints: values.technicalConstraints,
              notes: values.notes,
              attachmentPlaceholders: values.attachmentPlaceholders,
            });
            syncProjectId(projectId);
            message.success('需求接收已保存');
            return true;
          }}
        >
          <ProFormSelect
            colProps={{ xs: 24, md: 12 }}
            label="关联项目"
            name="projectId"
            options={projectOptions}
            placeholder="请选择项目"
            rules={[{ required: true, message: '请选择项目' }]}
            showSearch
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
          <ProFormText
            colProps={{ xs: 24, md: 12 }}
            label="时间预期"
            name="timelineExpectation"
            placeholder="例如：4 周内上线 MVP"
          />
          <ProFormText
            colProps={{ xs: 24, md: 12 }}
            label="预算范围"
            name="budgetRange"
            placeholder="例如：20k TOKEN / 10-20 万人民币"
          />
          <ProFormTextArea
            colProps={{ xs: 24, md: 24 }}
            fieldProps={{ rows: 3, showCount: true, maxLength: 800 }}
            label="业务目标"
            name="businessGoal"
            placeholder="说明这个项目最终要解决什么业务问题"
            rules={[{ required: true, message: '请输入业务目标' }]}
          />
          <ProFormTextArea
            colProps={{ xs: 24, md: 24 }}
            fieldProps={{ rows: 4, showCount: true, maxLength: 1200 }}
            label="功能摘要"
            name="featureSummary"
            placeholder="概述首期希望交付的功能范围"
            rules={[{ required: true, message: '请输入功能摘要' }]}
          />
          <ProFormTextArea
            colProps={{ xs: 24, md: 24 }}
            fieldProps={{ rows: 3, showCount: true, maxLength: 1200 }}
            label="参考产品 / 链接"
            name="referenceProducts"
            placeholder="填写竞品、参考产品或相关链接"
          />
          <ProFormTextArea
            colProps={{ xs: 24, md: 24 }}
            fieldProps={{ rows: 4, showCount: true, maxLength: 800 }}
            label="技术约束"
            name="technicalConstraints"
            placeholder="例如：必须私有化部署 / 必须接入现有 CRM"
          />
          <ProFormTextArea
            colProps={{ xs: 24, md: 24 }}
            fieldProps={{ rows: 4, showCount: true, maxLength: 1200 }}
            label="补充说明"
            name="notes"
            placeholder="记录当前已知但未结构化的背景信息"
          />
          <ProFormTextArea
            colProps={{ xs: 24, md: 24 }}
            fieldProps={{ rows: 4, showCount: true, maxLength: 1200 }}
            label="附件占位"
            name="attachmentPlaceholders"
            placeholder="例如：需求文档、原型图、会议纪要、招标文件"
          />
        </ProForm>

        {!selectedProjectId ? (
          <Empty
            description="请选择一个项目开始录入需求接收信息"
            image={Empty.PRESENTED_IMAGE_SIMPLE}
          />
        ) : (
          <Typography.Paragraph style={{ marginTop: 16, marginBottom: 0 }} type="secondary">
            需求接收保存后会同步更新项目名称、项目类型和项目摘要，让项目中心中的详情视图能够直接看到最新的需求背景。
          </Typography.Paragraph>
        )}
      </ProCard>

      <Modal
        open={suggestionModalOpen}
        title="AI 澄清建议预览"
        width={920}
        okButtonProps={{
          disabled: !selectedSuggestionIndexes.length,
        }}
        okText="应用选中建议"
        onCancel={() => {
          setSuggestionModalOpen(false);
        }}
        onOk={async () => {
          if (!selectedProjectId) {
            return;
          }
          const selectedSuggestions = selectedSuggestionIndexes
            .map((index) => clarificationSuggestions[index])
            .filter(Boolean);
          if (!selectedSuggestions.length) {
            message.warning('请至少选择一条建议');
            return;
          }
          await applyRequirementClarificationSuggestions(selectedProjectId, selectedSuggestions);
          message.success('已创建澄清项');
          setSuggestionModalOpen(false);
        }}
      >
        {clarificationSuggestions.length ? (
          <Checkbox.Group
            style={{ width: '100%' }}
            value={selectedSuggestionIndexes}
            onChange={(values) => {
              setSelectedSuggestionIndexes((values as Array<string | number>).map((value) => Number(value)));
            }}
          >
            <Space direction="vertical" size={12} style={{ display: 'flex' }}>
              {clarificationSuggestions.map((item, index) => (
                <div
                  key={`${item.title || item.question || 'suggestion'}-${index}`}
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
                      </Space>
                      <Typography.Paragraph style={{ marginBottom: 0 }}>
                        {item.question || '-'}
                      </Typography.Paragraph>
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
        ) : (
          <Empty description="当前没有可生成的澄清建议" image={Empty.PRESENTED_IMAGE_SIMPLE} />
        )}
      </Modal>

      <RequirementClarificationCollaborationModal
        open={collaborationModalOpen}
        projectId={selectedProjectId}
        onApplied={() => {}}
        onClose={() => {
          setCollaborationModalOpen(false);
        }}
      />
    </PageContainer>
  );
};

export default RequirementIntakePage;
