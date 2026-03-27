import { App, Form, Input, Modal, Radio } from 'antd';
import React, { useEffect, useState } from 'react';
import { INLINE_FORM_LABEL_COL, INLINE_FORM_WRAPPER_COL } from '@/constants/formLayout';
import { completeWorkflowTask } from '@/services/backend/workflow';
import type { WorkflowTask } from '@/services/backend/types';
import { parseWorkflowVariables } from './utils';

const { TextArea } = Input;

interface WorkflowCompleteFormValues {
  approved?: boolean;
  comment?: string;
  variablesJson?: string;
}

interface WorkflowTaskCompleteModalProps {
  open: boolean;
  task?: WorkflowTask;
  onCancel: () => void;
  onSuccess: () => void;
}

export const WorkflowTaskCompleteModal: React.FC<
  WorkflowTaskCompleteModalProps
> = ({ open, task, onCancel, onSuccess }) => {
  const { message } = App.useApp();
  const [form] = Form.useForm<WorkflowCompleteFormValues>();
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (open) {
      form.resetFields();
    }
  }, [form, open]);

  const handleSubmit = async () => {
    if (!task?.taskId) {
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
      await completeWorkflowTask(task.taskId, {
        approved: values.approved,
        comment: values.comment?.trim() || undefined,
        variables,
      });
      message.success('任务办理成功');
      form.resetFields();
      onSuccess();
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      destroyOnHidden
      confirmLoading={submitting}
      open={open}
      title="办理任务"
      width={760}
      onCancel={onCancel}
      onOk={() => void handleSubmit()}
    >
      <Form<WorkflowCompleteFormValues>
        form={form}
        className="saas-inline-form"
        layout="horizontal"
        labelAlign="right"
        labelCol={INLINE_FORM_LABEL_COL}
        wrapperCol={INLINE_FORM_WRAPPER_COL}
      >
        <Form.Item label="任务名称">
          <Input disabled value={task?.name || '-'} />
        </Form.Item>
        <Form.Item label="业务Key">
          <Input disabled value={task?.businessKey || '-'} />
        </Form.Item>
        <Form.Item extra="示例流程 leaveApproval 会读取 approved=true/false，其他流程可留空。" label="审批结果" name="approved">
          <Radio.Group
            optionType="button"
            options={[
              { label: '同意', value: true },
              { label: '驳回', value: false },
            ]}
          />
        </Form.Item>
        <Form.Item label="办理意见" name="comment">
          <TextArea maxLength={500} rows={4} showCount />
        </Form.Item>
        <Form.Item extra="选填，必须是 JSON 对象。" label="附加变量" name="variablesJson">
          <TextArea placeholder='{"nextApprover": "admin"}' rows={5} />
        </Form.Item>
      </Form>
    </Modal>
  );
};
