import { Alert, App, Button, Col, Descriptions, Empty, Form, Input, Modal, Row, Select, Space, Typography } from 'antd';
import React, { useEffect, useState } from 'react';
import { INLINE_FORM_LABEL_COL, INLINE_FORM_WRAPPER_COL } from '@/constants/formLayout';
import { getOptionLabel } from '@/features/aicoos/projectCenter';
import type { LinearRepresentationPreview, ProjectToolBindingItem } from '@/services/backend/types';
import { getRequestErrorMessage } from '@/utils/requestError';

interface LinearProjectRepresentationFormValues {
  mode?: string;
  teamId?: string;
  representationTitle?: string;
  representationDescription?: string;
  existingIssueId?: string;
  existingIssueIdentifier?: string;
  existingIssueUrl?: string;
  remark?: string;
}

interface LinearProjectRepresentationModalProps {
  open: boolean;
  projectName?: string | null;
  onClose: () => void;
  onApplied: (result: ProjectToolBindingItem) => void | Promise<void>;
  previewRequest: (
    values: LinearProjectRepresentationFormValues,
  ) => Promise<LinearRepresentationPreview>;
  applyRequest: (
    values: LinearProjectRepresentationFormValues,
  ) => Promise<ProjectToolBindingItem>;
}

const representationModeOptions = [
  { label: '创建 Linear 主工作项', value: 'create' },
  { label: '关联已有 Linear Issue', value: 'link_existing' },
];

export const LinearProjectRepresentationModal: React.FC<LinearProjectRepresentationModalProps> = ({
  open,
  projectName,
  onClose,
  onApplied,
  previewRequest,
  applyRequest,
}) => {
  const { message } = App.useApp();
  const [form] = Form.useForm<LinearProjectRepresentationFormValues>();
  const [previewLoading, setPreviewLoading] = useState(false);
  const [applyLoading, setApplyLoading] = useState(false);
  const [preview, setPreview] = useState<LinearRepresentationPreview>();

  const mode = Form.useWatch('mode', form);

  useEffect(() => {
    if (!open) {
      form.resetFields();
      setPreview(undefined);
      return;
    }
    form.setFieldsValue({
      mode: 'create',
      representationTitle: projectName ? `[AICoOS 项目] ${projectName}` : undefined,
    });
    setPreview(undefined);
  }, [form, open, projectName]);

  const handlePreview = async () => {
    const values = await form.validateFields();
    setPreviewLoading(true);
    try {
      const result = await previewRequest(values);
      setPreview(result);
      message.success('已生成项目映射预览');
    } catch (error) {
      message.error(getRequestErrorMessage(error, '生成项目映射预览失败'));
    } finally {
      setPreviewLoading(false);
    }
  };

  const handleApply = async () => {
    const values = await form.validateFields();
    setApplyLoading(true);
    try {
      const result = await applyRequest(values);
      message.success('Linear 项目映射已确认');
      await onApplied(result);
      onClose();
    } catch (error) {
      message.error(getRequestErrorMessage(error, '写入 Linear 项目映射失败'));
    } finally {
      setApplyLoading(false);
    }
  };

  return (
    <Modal
      destroyOnClose
      open={open}
      title="Linear 项目映射"
      width={960}
      footer={[
        <Button key="cancel" onClick={onClose}>
          取消
        </Button>,
        <Button key="preview" loading={previewLoading} onClick={() => void handlePreview()}>
          预览映射
        </Button>,
        <Button
          disabled={!preview}
          key="apply"
          loading={applyLoading}
          onClick={() => void handleApply()}
          type="primary"
        >
          确认映射
        </Button>,
      ]}
      onCancel={onClose}
    >
      <Alert
        message="项目映射是后续澄清项/决策项写入 Linear Comment 的默认落点。"
        description="创建模式会在 Linear 新建主工作项；关联模式不会改写 Linear 内容，只会把已有 Issue 绑定到当前项目。"
        showIcon
        style={{ marginBottom: 16 }}
        type="info"
      />
      <Form<LinearProjectRepresentationFormValues>
        className="saas-inline-form"
        form={form}
        labelAlign="right"
        labelCol={INLINE_FORM_LABEL_COL}
        layout="horizontal"
        wrapperCol={INLINE_FORM_WRAPPER_COL}
        onValuesChange={() => {
          setPreview(undefined);
        }}
      >
        <Row gutter={16}>
          <Col span={12}>
            <Form.Item
              label="映射方式"
              name="mode"
              rules={[{ required: true, message: '请选择映射方式' }]}
            >
              <Select options={representationModeOptions} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="Linear Team ID" name="teamId">
              <Input placeholder="创建模式可选填写团队 ID" />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="映射标题" name="representationTitle">
              <Input placeholder="可留空以使用系统建议标题" />
            </Form.Item>
          </Col>
          {mode === 'link_existing' ? (
            <>
              <Col span={12}>
                <Form.Item label="已有 Issue ID" name="existingIssueId">
                  <Input placeholder="例如 issue UUID" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="已有编号" name="existingIssueIdentifier">
                  <Input placeholder="例如 AIC-100" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="已有链接" name="existingIssueUrl">
                  <Input placeholder="https://linear.app/..." />
                </Form.Item>
              </Col>
            </>
          ) : null}
          <Col span={24}>
            <Form.Item label="描述草稿" name="representationDescription">
              <Input.TextArea
                maxLength={4000}
                placeholder="可留空以使用系统根据项目上下文生成的内容"
                rows={7}
                showCount
              />
            </Form.Item>
          </Col>
          <Col span={24}>
            <Form.Item label="备注" name="remark">
              <Input.TextArea maxLength={500} placeholder="记录这次映射的说明" rows={3} showCount />
            </Form.Item>
          </Col>
        </Row>
      </Form>

      <div style={{ marginTop: 16 }}>
        {preview ? (
          <Descriptions bordered column={1} size="small" title="映射预览">
            <Descriptions.Item label="操作说明">
              {preview.operationSummary || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="映射方式">
              {getOptionLabel(representationModeOptions, preview.mode || undefined)}
            </Descriptions.Item>
            <Descriptions.Item label="映射标题">
              {preview.representationTitle || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="描述">
              <Typography.Paragraph style={{ marginBottom: 0, whiteSpace: 'pre-wrap' }}>
                {preview.representationDescription || '-'}
              </Typography.Paragraph>
            </Descriptions.Item>
            {preview.existingIssueIdentifier || preview.existingIssueId ? (
              <Descriptions.Item label="已有 Issue">
                <Space direction="vertical" size={2} style={{ width: '100%' }}>
                  <Typography.Text>
                    {preview.existingIssueIdentifier || preview.existingIssueId || '-'}
                  </Typography.Text>
                  {preview.existingIssueUrl ? (
                    <Typography.Link href={preview.existingIssueUrl} rel="noreferrer" target="_blank">
                      打开外部链接
                    </Typography.Link>
                  ) : null}
                </Space>
              </Descriptions.Item>
            ) : null}
          </Descriptions>
        ) : (
          <Empty
            description="请先生成预览，再确认 Linear 项目映射"
            image={Empty.PRESENTED_IMAGE_SIMPLE}
          />
        )}
      </div>
    </Modal>
  );
};
