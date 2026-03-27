import { App, Form, Input, Modal, Typography } from 'antd';
import React, { useEffect, useState } from 'react';
import { INLINE_FORM_LABEL_COL, INLINE_FORM_WRAPPER_COL } from '@/constants/formLayout';
import { startWorkflowInstance } from '@/services/backend/workflow';
import type { WorkflowDefinition, WorkflowInstance } from '@/services/backend/types';
import { parseWorkflowVariables } from './utils';

const { TextArea } = Input;

interface WorkflowStartFormValues {
  businessKey?: string;
  title?: string;
  approver?: string;
  variablesJson?: string;
}

interface WorkflowStartProcessModalProps {
  open: boolean;
  definition?: WorkflowDefinition;
  onCancel: () => void;
  onSuccess: (instance: WorkflowInstance) => void;
}

export const WorkflowStartProcessModal: React.FC<
  WorkflowStartProcessModalProps
> = ({ open, definition, onCancel, onSuccess }) => {
  const { message } = App.useApp();
  const [form] = Form.useForm<WorkflowStartFormValues>();
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (open) {
      form.resetFields();
      form.setFieldsValue({
        title: definition?.name ? `${definition.name}申请` : undefined,
      });
    }
  }, [definition?.name, form, open]);

  const handleSubmit = async () => {
    if (!definition) {
      return;
    }

    const values = await form.validateFields();

    let variables;
    try {
      variables = parseWorkflowVariables(values.variablesJson);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '流程变量格式不正确');
      return;
    }

    setSubmitting(true);
    try {
      const response = await startWorkflowInstance({
        definitionKey: definition.key,
        businessKey: values.businessKey?.trim() || undefined,
        title: values.title?.trim() || undefined,
        approver: values.approver?.trim() || undefined,
        variables,
      });
      message.success('流程发起成功');
      onSuccess(response.result);
      form.resetFields();
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      destroyOnHidden
      confirmLoading={submitting}
      open={open}
      title="发起流程"
      width={760}
      onCancel={onCancel}
      onOk={() => void handleSubmit()}
    >
      <Typography.Paragraph type="secondary">
        当前选择的流程定义：
        <Typography.Text strong>{definition?.name || '-'}</Typography.Text>
        ，定义 Key：
        <Typography.Text code>{definition?.key || '-'}</Typography.Text>
      </Typography.Paragraph>

      <Form<WorkflowStartFormValues>
        form={form}
        className="saas-inline-form"
        layout="horizontal"
        labelAlign="right"
        labelCol={INLINE_FORM_LABEL_COL}
        wrapperCol={INLINE_FORM_WRAPPER_COL}
      >
        <Form.Item label="业务Key" name="businessKey">
          <Input allowClear maxLength={64} placeholder="选填，用于关联业务单据编号" />
        </Form.Item>
        <Form.Item label="流程标题" name="title">
          <Input allowClear maxLength={100} placeholder="选填，建议填写一个易识别的标题" />
        </Form.Item>
        <Form.Item label="审批人" name="approver">
          <Input
            allowClear
            maxLength={50}
            placeholder="示例流程 leaveApproval 需要填写审批人用户名，如 admin"
          />
        </Form.Item>
        <Form.Item extra="选填，必须是 JSON 对象，例如 {&quot;days&quot;: 2, &quot;reason&quot;: &quot;病假&quot;}" label="流程变量" name="variablesJson">
          <TextArea placeholder='{"days": 2, "reason": "病假"}' rows={6} />
        </Form.Item>
      </Form>
    </Modal>
  );
};
