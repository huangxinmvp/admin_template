import {
  DeleteOutlined,
  DownloadOutlined,
  EditOutlined,
  PlusOutlined,
  ReloadOutlined,
  UploadOutlined,
} from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { App, Button, Card, Col, Empty, Input, List, Popconfirm, Row, Space, Statistic, Tag, Typography } from 'antd';
import { useNavigate } from '@umijs/max';
import React, { useEffect, useMemo, useRef, useState } from 'react';
import { formatDateTime } from '@/features/workflow/utils';
import { deleteWorkflowDraft, getWorkflowDrafts, saveWorkflowDraft } from '@/services/backend/workflow';
import type { WorkflowDraft, WorkflowSaveDraftPayload } from '@/services/backend/types';

const stripFileExtension = (fileName: string) => fileName.replace(/\.json$/i, '');

const downloadTextFile = (content: string, fileName: string, type: string) => {
  const blob = new Blob([content], { type });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  link.click();
  window.setTimeout(() => {
    URL.revokeObjectURL(url);
  }, 1000);
};

const WorkflowDraftsPage: React.FC = () => {
  const navigate = useNavigate();
  const { message } = App.useApp();
  const importInputRef = useRef<HTMLInputElement>(null);
  const [loading, setLoading] = useState(false);
  const [importing, setImporting] = useState(false);
  const [keyword, setKeyword] = useState('');
  const [appliedKeyword, setAppliedKeyword] = useState<string>();
  const [drafts, setDrafts] = useState<WorkflowDraft[]>([]);

  const loadDrafts = async (nextKeyword?: string) => {
    setLoading(true);
    try {
      const response = await getWorkflowDrafts({ keyword: nextKeyword });
      setDrafts(response.result || []);
    } catch (_error) {
      message.error('加载流程草稿失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadDrafts();
  }, []);

  const latestUpdateTime = useMemo(() => {
    if (!drafts.length) {
      return '-';
    }
    return formatDateTime(drafts[0]?.updateTime);
  }, [drafts]);

  const handleExportDraft = (draft: WorkflowDraft) => {
    const payload = {
      draftName: draft.draftName,
      processDefinitionKey: draft.processDefinitionKey || undefined,
      processDefinitionName: draft.processDefinitionName || undefined,
      sourceDefinitionId: draft.sourceDefinitionId || undefined,
      sourceDefinitionKey: draft.sourceDefinitionKey || undefined,
      category: draft.category || undefined,
      bpmnXml: draft.bpmnXml || '',
      remark: draft.remark || undefined,
    };

    downloadTextFile(
      JSON.stringify(payload, null, 2),
      `${stripFileExtension(draft.draftName || draft.processDefinitionKey || 'workflow-draft')}.json`,
      'application/json;charset=utf-8',
    );
    message.success(`已导出流程草稿：${draft.draftName}`);
  };

  const handleImportDraft = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) {
      return;
    }

    setImporting(true);
    try {
      const raw = await file.text();
      const parsed = JSON.parse(raw) as Partial<WorkflowSaveDraftPayload>;
      if (!parsed.bpmnXml?.trim()) {
        throw new Error('导入文件缺少 bpmnXml 内容');
      }

      const payload: WorkflowSaveDraftPayload = {
        draftName: parsed.draftName?.trim() || stripFileExtension(file.name) || '导入流程草稿',
        processDefinitionKey: parsed.processDefinitionKey?.trim() || undefined,
        processDefinitionName: parsed.processDefinitionName?.trim() || undefined,
        sourceDefinitionId: parsed.sourceDefinitionId?.trim() || undefined,
        sourceDefinitionKey: parsed.sourceDefinitionKey?.trim() || undefined,
        category: parsed.category?.trim() || undefined,
        bpmnXml: parsed.bpmnXml,
        remark: parsed.remark?.trim() || undefined,
      };

      await saveWorkflowDraft(payload);
      message.success(`已导入流程草稿：${payload.draftName}`);
      await loadDrafts(appliedKeyword);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '导入流程草稿失败');
    } finally {
      event.target.value = '';
      setImporting(false);
    }
  };

  return (
    <PageContainer
      className="saas-page-container saas-workflow-page"
      content="草稿箱用于集中管理当前账号保存到后端的流程草稿，适合继续编辑、整理命名和清理历史版本。"
      extra={[
        <Button
          key="designer"
          icon={<PlusOutlined />}
          type="primary"
          onClick={() => {
            navigate('/workflow/designer');
          }}
        >
          新建流程
        </Button>,
        <Button
          key="import"
          icon={<UploadOutlined />}
          loading={importing}
          onClick={() => {
            importInputRef.current?.click();
          }}
        >
          导入草稿
        </Button>,
      ]}
      title="流程草稿"
    >
      <Space direction="vertical" size={16} style={{ display: 'flex' }}>
        <Row gutter={[16, 16]}>
          <Col lg={8} md={8} sm={24} xs={24}>
            <Card className="saas-workflow-halo-card" variant="borderless">
              <Statistic title="当前草稿数" value={drafts.length} />
            </Card>
          </Col>
          <Col lg={8} md={8} sm={24} xs={24}>
            <Card className="saas-workflow-halo-card" variant="borderless">
              <Statistic title="最近更新时间" value={latestUpdateTime} />
            </Card>
          </Col>
          <Col lg={8} md={8} sm={24} xs={24}>
            <Card className="saas-workflow-halo-card" variant="borderless">
              <Space direction="vertical" size={10}>
                <Typography.Text strong>快捷入口</Typography.Text>
                <Space wrap>
                  <Button
                    onClick={() => {
                      navigate('/workflow/designer');
                    }}
                  >
                    打开设计器
                  </Button>
                  <Button
                    onClick={() => {
                      navigate('/workflow/definitions');
                    }}
                  >
                    查看定义库
                  </Button>
                </Space>
              </Space>
            </Card>
          </Col>
        </Row>

        <Card className="saas-workflow-panel-card" variant="borderless">
          <div className="saas-workflow-studio__toolbar">
            <div className="saas-workflow-studio__filters saas-workflow-drafts__filters">
              <Input
                allowClear
                placeholder="按草稿名称、流程名称或流程 Key 搜索"
                value={keyword}
                onChange={(event) => {
                  setKeyword(event.target.value);
                }}
              />
            </div>
            <Space wrap>
              <Button
                type="primary"
                onClick={() => {
                  const nextKeyword = keyword.trim() || undefined;
                  setAppliedKeyword(nextKeyword);
                  void loadDrafts(nextKeyword);
                }}
              >
                查询
              </Button>
              <Button
                icon={<ReloadOutlined />}
                onClick={() => {
                  setKeyword('');
                  setAppliedKeyword(undefined);
                  void loadDrafts();
                }}
              >
                重置
              </Button>
            </Space>
          </div>

          <List<WorkflowDraft>
            className="saas-workflow-draft-grid"
            dataSource={drafts}
            loading={loading}
            locale={{
              emptyText: (
                <Empty
                  description={
                    appliedKeyword
                      ? '没有找到符合条件的流程草稿'
                      : '当前还没有已保存的流程草稿'
                  }
                  image={Empty.PRESENTED_IMAGE_SIMPLE}
                />
              ),
            }}
            renderItem={(draft) => (
              <List.Item className="saas-workflow-draft-grid__item">
                <Card className="saas-workflow-draft-card" hoverable variant="borderless">
                  <Space direction="vertical" size={14} style={{ display: 'flex' }}>
                    <div className="saas-workflow-draft-card__header">
                      <div>
                        <Typography.Title level={5} style={{ marginBottom: 4 }}>
                          {draft.draftName}
                        </Typography.Title>
                        <Typography.Text type="secondary">
                          {draft.processDefinitionName || draft.processDefinitionKey || '未识别流程元信息'}
                        </Typography.Text>
                      </div>
                      {draft.category ? <Tag color="cyan">{draft.category}</Tag> : null}
                    </div>

                    <div className="saas-workflow-draft-card__meta">
                      <div>
                        <span>流程 Key</span>
                        <strong>{draft.processDefinitionKey || '-'}</strong>
                      </div>
                      <div>
                        <span>更新时间</span>
                        <strong>{formatDateTime(draft.updateTime)}</strong>
                      </div>
                      <div>
                        <span>来源定义</span>
                        <strong>{draft.sourceDefinitionKey || '-'}</strong>
                      </div>
                    </div>

                    <Typography.Paragraph
                      className="saas-workflow-draft-card__remark"
                      ellipsis={{ rows: 2 }}
                      type="secondary"
                    >
                      {draft.remark || '暂无备注说明'}
                    </Typography.Paragraph>

                    <Space wrap>
                      <Button
                        icon={<EditOutlined />}
                        type="primary"
                        onClick={() => {
                          navigate(`/workflow/designer?draftId=${encodeURIComponent(draft.id)}`);
                        }}
                      >
                        编辑流程
                      </Button>
                      <Button
                        icon={<DownloadOutlined />}
                        onClick={() => {
                          handleExportDraft(draft);
                        }}
                      >
                        导出 JSON
                      </Button>
                      <Popconfirm
                        cancelText="取消"
                        okText="删除"
                        title="删除草稿"
                        onConfirm={async () => {
                          await deleteWorkflowDraft(draft.id);
                          message.success('流程草稿已删除');
                          await loadDrafts(appliedKeyword);
                        }}
                      >
                        <Button danger icon={<DeleteOutlined />}>
                          删除
                        </Button>
                      </Popconfirm>
                    </Space>
                  </Space>
                </Card>
              </List.Item>
            )}
          />
        </Card>
      </Space>

      <input
        accept=".json"
        hidden
        ref={importInputRef}
        type="file"
        onChange={handleImportDraft}
      />
    </PageContainer>
  );
};

export default WorkflowDraftsPage;
