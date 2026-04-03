import { App, Button, Empty, Form, Input, List, Modal, Select, Space, Tag, Typography } from 'antd';
import React, { useEffect, useMemo, useState } from 'react';
import {
  generateProjectMeetingSummary,
  saveProjectMeetingRecord,
} from '@/services/backend/aicoos';
import type { MeetingSummarySuggestion } from '@/services/backend/types';
import { decisionTypeOptions, getOptionLabel, meetingSourceTypeOptions } from '@/features/aicoos/projectCenter';

interface MeetingSummaryModalProps {
  open: boolean;
  projectId?: string;
  projectName?: string | null;
  onClose: () => void;
  onSaved: () => void;
}

interface MeetingSummaryFormValues {
  meetingTitle?: string;
  rawNotes?: string;
  sourceType?: string;
  sourceObjectId?: string;
}

export const MeetingSummaryModal: React.FC<MeetingSummaryModalProps> = ({
  open,
  projectId,
  projectName,
  onClose,
  onSaved,
}) => {
  const { message } = App.useApp();
  const [form] = Form.useForm<MeetingSummaryFormValues>();
  const [generating, setGenerating] = useState(false);
  const [saving, setSaving] = useState(false);
  const [preview, setPreview] = useState<MeetingSummarySuggestion>();

  useEffect(() => {
    if (!open) {
      form.resetFields();
      setPreview(undefined);
      return;
    }
    form.setFieldsValue({
      meetingTitle: projectName ? `${projectName} 讨论纪要` : undefined,
      sourceType: 'project_center',
    });
  }, [form, open, projectName]);

  const decisionCandidateTags = useMemo(
    () =>
      (preview?.decisionCandidates || []).map((item, index) => (
        <Space key={`${item.title || 'candidate'}-${index}`} size={[8, 4]} wrap>
          <Typography.Text strong>{item.title || '-'}</Typography.Text>
          <Tag color={item.blockerFlag ? 'red' : 'blue'}>
            {item.blockerFlag ? '阻塞' : '建议'}
          </Tag>
          <Tag>{getOptionLabel(decisionTypeOptions, item.decisionType || undefined)}</Tag>
        </Space>
      )),
    [preview?.decisionCandidates],
  );

  const handleGenerate = async () => {
    if (!projectId) {
      message.warning('请选择项目后再生成会议总结');
      return;
    }
    const values = await form.validateFields();
    setGenerating(true);
    try {
      const response = await generateProjectMeetingSummary(projectId, {
        meetingTitle: values.meetingTitle,
        rawNotes: values.rawNotes || '',
        sourceType: values.sourceType,
        sourceObjectId: values.sourceObjectId,
      });
      setPreview(response.result);
      message.success('会议总结建议已生成');
    } catch (_error) {
      message.error('生成会议总结失败');
    } finally {
      setGenerating(false);
    }
  };

  const handleSave = async () => {
    if (!projectId || !preview) {
      return;
    }
    const values = await form.validateFields();
    setSaving(true);
    try {
      await saveProjectMeetingRecord(projectId, {
        meetingTitle: preview.meetingTitle || values.meetingTitle,
        rawNotes: values.rawNotes,
        summary: preview.summary || undefined,
        actionItems: preview.actionItems || undefined,
        openQuestions: preview.openQuestions || undefined,
        decisionCandidates: preview.decisionCandidates || undefined,
        sourceType: values.sourceType,
        sourceObjectId: values.sourceObjectId,
      });
      message.success('会议记录已保存');
      onSaved();
      onClose();
    } catch (_error) {
      message.error('保存会议记录失败');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal
      destroyOnClose
      open={open}
      title="会议总结工具"
      width={920}
      footer={[
        <Button key="cancel" onClick={onClose}>
          关闭
        </Button>,
        <Button key="generate" loading={generating} onClick={() => void handleGenerate()}>
          生成总结建议
        </Button>,
        <Button
          disabled={!preview}
          key="save"
          loading={saving}
          onClick={() => void handleSave()}
          type="primary"
        >
          保存会议记录
        </Button>,
      ]}
      onCancel={onClose}
    >
      <Form<MeetingSummaryFormValues> form={form} layout="vertical">
        <Form.Item label="会议标题" name="meetingTitle">
          <Input placeholder="例如：需求澄清会 / 方案评审会 / 项目例会" />
        </Form.Item>
        <Form.Item label="来源类型" name="sourceType">
          <Select
            allowClear
            options={meetingSourceTypeOptions}
            placeholder="请选择来源类型"
          />
        </Form.Item>
        <Form.Item label="来源对象ID" name="sourceObjectId">
          <Input placeholder="可选填写关联对象 ID" />
        </Form.Item>
        <Form.Item
          label="会议 / 讨论原始笔记"
          name="rawNotes"
          rules={[{ required: true, message: '请输入会议或讨论原始笔记' }]}
        >
          <Input.TextArea
            placeholder="粘贴会议纪要、讨论记录、访谈笔记或即时沟通要点"
            rows={8}
            showCount
            maxLength={4000}
          />
        </Form.Item>
      </Form>

      {preview ? (
        <Space direction="vertical" size={16} style={{ display: 'flex' }}>
          <div>
            <Typography.Title level={5} style={{ marginBottom: 8 }}>
              {preview.meetingTitle || '会议总结'}
            </Typography.Title>
            <Typography.Paragraph style={{ marginBottom: 0 }}>
              {preview.summary || '-'}
            </Typography.Paragraph>
          </div>

          <div>
            <Typography.Text strong>行动项</Typography.Text>
            <List
              dataSource={preview.actionItems || []}
              locale={{
                emptyText: <Empty description="暂无行动项建议" image={Empty.PRESENTED_IMAGE_SIMPLE} />,
              }}
              renderItem={(item) => <List.Item>{item}</List.Item>}
              size="small"
            />
          </div>

          <div>
            <Typography.Text strong>开放问题</Typography.Text>
            <List
              dataSource={preview.openQuestions || []}
              locale={{
                emptyText: <Empty description="暂无开放问题" image={Empty.PRESENTED_IMAGE_SIMPLE} />,
              }}
              renderItem={(item) => <List.Item>{item}</List.Item>}
              size="small"
            />
          </div>

          <div>
            <Typography.Text strong>决策候选</Typography.Text>
            {(preview.decisionCandidates || []).length ? (
              <Space direction="vertical" size={8} style={{ display: 'flex', marginTop: 8 }}>
                {decisionCandidateTags}
                {(preview.decisionCandidates || []).map((item, index) => (
                  <Typography.Paragraph key={`${item.title || 'detail'}-${index}`} style={{ marginBottom: 0 }} type="secondary">
                    推荐选项：{item.recommendedOption || '-'}
                  </Typography.Paragraph>
                ))}
              </Space>
            ) : (
              <Empty description="暂无决策候选" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </div>
        </Space>
      ) : null}
    </Modal>
  );
};
