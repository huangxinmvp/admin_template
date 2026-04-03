import { Alert, App, Button, Col, Descriptions, Empty, Form, Input, Modal, Row, Select, Space, Typography } from 'antd';
import React, { useEffect, useState } from 'react';
import { INLINE_FORM_LABEL_COL, INLINE_FORM_WRAPPER_COL } from '@/constants/formLayout';
import { getOptionLabel } from '@/features/aicoos/projectCenter';
import type { LinearWritePreview, ToolIntegrationAuditItem } from '@/services/backend/types';
import { getRequestErrorMessage } from '@/utils/requestError';

interface LinearWriteFormValues {
  writeMode?: string;
  teamId?: string;
  title?: string;
  body?: string;
  targetIssueId?: string;
  targetIssueIdentifier?: string;
  targetIssueUrl?: string;
  remark?: string;
}

interface LinearWriteModalProps {
  open: boolean;
  title: string;
  sourceLabel?: string;
  defaultValues?: Partial<LinearWriteFormValues>;
  onClose: () => void;
  onApplied: (result: ToolIntegrationAuditItem) => void | Promise<void>;
  previewRequest: (values: LinearWriteFormValues) => Promise<LinearWritePreview>;
  applyRequest: (values: LinearWriteFormValues) => Promise<ToolIntegrationAuditItem>;
}

const writeModeOptions = [
  { label: '写入 Linear Issue', value: 'issue' },
  { label: '写入 Linear Comment', value: 'comment' },
];

export const LinearWriteModal: React.FC<LinearWriteModalProps> = ({
  open,
  title,
  sourceLabel,
  defaultValues,
  onClose,
  onApplied,
  previewRequest,
  applyRequest,
}) => {
  const { message } = App.useApp();
  const [form] = Form.useForm<LinearWriteFormValues>();
  const [previewLoading, setPreviewLoading] = useState(false);
  const [applyLoading, setApplyLoading] = useState(false);
  const [preview, setPreview] = useState<LinearWritePreview>();

  const writeMode = Form.useWatch('writeMode', form);

  useEffect(() => {
    if (!open) {
      form.resetFields();
      setPreview(undefined);
      return;
    }
    form.setFieldsValue({
      writeMode: 'issue',
      ...defaultValues,
    });
    setPreview(undefined);
  }, [defaultValues, form, open]);

  const handlePreview = async () => {
    const values = await form.validateFields();
    setPreviewLoading(true);
    try {
      const result = await previewRequest(values);
      setPreview(result);
      message.success('已生成 Linear 写入预览');
    } catch (error) {
      message.error(getRequestErrorMessage(error, '生成 Linear 写入预览失败'));
    } finally {
      setPreviewLoading(false);
    }
  };

  const handleApply = async () => {
    const values = await form.validateFields();
    setApplyLoading(true);
    try {
      const result = await applyRequest(values);
      message.success('已确认写入 Linear');
      await onApplied(result);
      onClose();
    } catch (error) {
      message.error(getRequestErrorMessage(error, '写入 Linear 失败'));
    } finally {
      setApplyLoading(false);
    }
  };

  return (
    <Modal
      destroyOnClose
      open={open}
      title={title}
      width={960}
      footer={[
        <Button key="cancel" onClick={onClose}>
          取消
        </Button>,
        <Button key="preview" loading={previewLoading} onClick={() => void handlePreview()}>
          预览写入
        </Button>,
        <Button
          disabled={!preview}
          key="apply"
          loading={applyLoading}
          onClick={() => void handleApply()}
          type="primary"
        >
          确认写入
        </Button>,
      ]}
      onCancel={onClose}
    >
      <Alert
        message="所有外部写入都必须先预览再确认，确认后会写入审计记录。"
        description="Comment 模式默认要求项目已经建立 Linear 主工作项映射；如果当前项目尚未建立映射，请先在项目中心做项目映射，或直接切换到 Issue 模式。"
        showIcon
        style={{ marginBottom: 16 }}
        type="info"
      />
      <Form<LinearWriteFormValues>
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
        {sourceLabel ? (
          <Typography.Paragraph style={{ marginBottom: 16 }} type="secondary">
            来源对象：{sourceLabel}
          </Typography.Paragraph>
        ) : null}
        <Row gutter={16}>
          <Col span={12}>
            <Form.Item
              label="写入方式"
              name="writeMode"
              rules={[{ required: true, message: '请选择写入方式' }]}
            >
              <Select options={writeModeOptions} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="Linear Team ID" name="teamId">
              <Input placeholder="Issue 模式可选填写团队 ID" />
            </Form.Item>
          </Col>
          {writeMode === 'comment' ? (
            <>
              <Col span={12}>
                <Form.Item label="目标 Issue ID" name="targetIssueId">
                  <Input placeholder="可留空以使用项目主工作项" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="目标编号" name="targetIssueIdentifier">
                  <Input placeholder="例如 AIC-123" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="目标链接" name="targetIssueUrl">
                  <Input placeholder="可选填写 Issue URL" />
                </Form.Item>
              </Col>
            </>
          ) : (
            <Col span={12}>
              <Form.Item label="Issue 标题" name="title">
                <Input placeholder="可留空以使用系统建议标题" />
              </Form.Item>
            </Col>
          )}
          <Col span={24}>
            <Form.Item label="内容草稿" name="body">
              <Input.TextArea
                maxLength={4000}
                placeholder="可留空以使用系统根据当前对象生成的内容"
                rows={7}
                showCount
              />
            </Form.Item>
          </Col>
          <Col span={24}>
            <Form.Item label="备注" name="remark">
              <Input.TextArea maxLength={500} placeholder="记录这次外部写入的说明" rows={3} showCount />
            </Form.Item>
          </Col>
        </Row>
      </Form>

      <div style={{ marginTop: 16 }}>
        {preview ? (
          <Descriptions bordered column={1} size="small" title="写入预览">
            <Descriptions.Item label="操作说明">
              {preview.operationSummary || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="写入方式">
              {getOptionLabel(writeModeOptions, preview.writeMode || undefined)}
            </Descriptions.Item>
            {preview.title ? (
              <Descriptions.Item label="Issue 标题">
                {preview.title}
              </Descriptions.Item>
            ) : null}
            {preview.targetIssueIdentifier || preview.targetIssueId ? (
              <Descriptions.Item label="目标 Issue">
                <Space direction="vertical" size={2} style={{ width: '100%' }}>
                  <Typography.Text>
                    {preview.targetIssueIdentifier || preview.targetIssueId || '-'}
                  </Typography.Text>
                  {preview.targetIssueUrl ? (
                    <Typography.Link href={preview.targetIssueUrl} rel="noreferrer" target="_blank">
                      打开外部链接
                    </Typography.Link>
                  ) : null}
                </Space>
              </Descriptions.Item>
            ) : null}
            <Descriptions.Item label="内容">
              <Typography.Paragraph style={{ marginBottom: 0, whiteSpace: 'pre-wrap' }}>
                {preview.body || '-'}
              </Typography.Paragraph>
            </Descriptions.Item>
          </Descriptions>
        ) : (
          <Empty
            description="请先生成预览，再确认外部写入"
            image={Empty.PRESENTED_IMAGE_SIMPLE}
          />
        )}
      </div>
    </Modal>
  );
};
