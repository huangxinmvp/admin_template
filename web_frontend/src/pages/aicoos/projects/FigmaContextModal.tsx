import { Alert, App, Button, Col, Descriptions, Empty, Form, Input, Modal, Row, Typography } from 'antd';
import React, { useEffect, useState } from 'react';
import { INLINE_FORM_LABEL_COL, INLINE_FORM_WRAPPER_COL } from '@/constants/formLayout';
import { getOptionLabel, toolBindingTypeOptions } from '@/features/aicoos/projectCenter';
import { formatDateTime } from '@/features/workflow/utils';
import type { FigmaContextPreview, ProjectToolBindingItem } from '@/services/backend/types';
import { getRequestErrorMessage } from '@/utils/requestError';

interface FigmaContextFormValues {
  figmaUrl?: string;
  fileKey?: string;
  nodeId?: string;
  bindingName?: string;
  remark?: string;
}

interface FigmaContextModalProps {
  open: boolean;
  onClose: () => void;
  onApplied: (result: ProjectToolBindingItem) => void | Promise<void>;
  previewRequest: (values: FigmaContextFormValues) => Promise<FigmaContextPreview>;
  applyRequest: (values: FigmaContextFormValues) => Promise<ProjectToolBindingItem>;
}

export const FigmaContextModal: React.FC<FigmaContextModalProps> = ({
  open,
  onClose,
  onApplied,
  previewRequest,
  applyRequest,
}) => {
  const { message } = App.useApp();
  const [form] = Form.useForm<FigmaContextFormValues>();
  const [previewLoading, setPreviewLoading] = useState(false);
  const [applyLoading, setApplyLoading] = useState(false);
  const [preview, setPreview] = useState<FigmaContextPreview>();

  useEffect(() => {
    if (!open) {
      form.resetFields();
      setPreview(undefined);
      return;
    }
    form.setFieldsValue({});
    setPreview(undefined);
  }, [form, open]);

  const handlePreview = async () => {
    const values = await form.validateFields();
    setPreviewLoading(true);
    try {
      const result = await previewRequest(values);
      setPreview(result);
      message.success('已读取 Figma 上下文预览');
    } catch (error) {
      message.error(getRequestErrorMessage(error, '读取 Figma 上下文失败'));
    } finally {
      setPreviewLoading(false);
    }
  };

  const handleApply = async () => {
    const values = await form.validateFields();
    setApplyLoading(true);
    try {
      const result = await applyRequest(values);
      message.success('Figma 上下文已关联到项目');
      await onApplied(result);
      onClose();
    } catch (error) {
      message.error(getRequestErrorMessage(error, '关联 Figma 上下文失败'));
    } finally {
      setApplyLoading(false);
    }
  };

  return (
    <Modal
      destroyOnClose
      open={open}
      title="Figma 上下文"
      width={960}
      footer={[
        <Button key="cancel" onClick={onClose}>
          取消
        </Button>,
        <Button key="preview" loading={previewLoading} onClick={() => void handlePreview()}>
          读取预览
        </Button>,
        <Button
          disabled={!preview}
          key="apply"
          loading={applyLoading}
          onClick={() => void handleApply()}
          type="primary"
        >
          确认关联
        </Button>,
      ]}
      onCancel={onClose}
    >
      <Alert
        message="当前版本只读取并关联 Figma 上下文，不会向 Figma 写回任何内容。"
        description="优先使用完整 Figma 链接；如果只填 File Key / Node ID，也可以做最小上下文绑定。Node 读取失败时会明确提示文件权限或节点不存在。"
        showIcon
        style={{ marginBottom: 16 }}
        type="info"
      />
      <Form<FigmaContextFormValues>
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
            <Form.Item label="Figma 链接" name="figmaUrl">
              <Input placeholder="https://www.figma.com/design/..." />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="File Key" name="fileKey">
              <Input placeholder="可直接填写 file key" />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="Node ID" name="nodeId">
              <Input placeholder="例如 1234-5678" />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="绑定名称" name="bindingName">
              <Input placeholder="可留空以使用文件名或节点名" />
            </Form.Item>
          </Col>
          <Col span={24}>
            <Form.Item label="备注" name="remark">
              <Input.TextArea maxLength={500} placeholder="记录这次上下文关联的用途" rows={3} showCount />
            </Form.Item>
          </Col>
        </Row>
      </Form>

      <div style={{ marginTop: 16 }}>
        {preview ? (
          <Descriptions bordered column={1} size="small" title="Figma 上下文预览">
            <Descriptions.Item label="关联方式">
              {getOptionLabel(toolBindingTypeOptions, preview.bindingType || undefined)}
            </Descriptions.Item>
            <Descriptions.Item label="绑定名称">
              {preview.bindingName || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="文件">
              {preview.fileName || preview.fileKey || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="节点">
              {preview.nodeName || preview.nodeId || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="最后修改">
              {formatDateTime(preview.lastModifiedAt)}
            </Descriptions.Item>
            <Descriptions.Item label="操作说明">
              {preview.operationSummary || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="外部链接">
              {preview.externalUrl ? (
                <Typography.Link href={preview.externalUrl} rel="noreferrer" target="_blank">
                  打开 Figma 上下文
                </Typography.Link>
              ) : (
                '-'
              )}
            </Descriptions.Item>
          </Descriptions>
        ) : (
          <Empty
            description="请先读取 Figma 上下文预览，再确认关联"
            image={Empty.PRESENTED_IMAGE_SIMPLE}
          />
        )}
      </div>
    </Modal>
  );
};
