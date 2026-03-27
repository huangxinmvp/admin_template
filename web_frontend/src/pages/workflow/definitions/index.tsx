import {
  DeleteOutlined,
  DownloadOutlined,
  EditOutlined,
  PlayCircleOutlined,
  ReloadOutlined,
  UploadOutlined,
} from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { App, Button, Card, Col, Empty, Input, Popconfirm, Row, Space, Statistic, Table, Tag, Typography } from 'antd';
import type { TableColumnsType } from 'antd';
import { useNavigate } from '@umijs/max';
import React, { useEffect, useRef, useState } from 'react';
import { WorkflowInstanceDetailDrawer } from '@/features/workflow/WorkflowInstanceDetailDrawer';
import { WorkflowStartProcessModal } from '@/features/workflow/WorkflowStartProcessModal';
import { getWorkflowResourceName } from '@/features/workflow/utils';
import {
  deleteWorkflowDeployment,
  deployWorkflowDefinition,
  getWorkflowDefinitions,
  getWorkflowDefinitionXml,
} from '@/services/backend/workflow';
import type { WorkflowDefinition, WorkflowInstance } from '@/services/backend/types';

const DEFAULT_CATEGORY = 'https://admin-template/workflow';

const stripFileExtension = (fileName: string) =>
  fileName.replace(/(\.bpmn20\.xml|\.bpmn|\.xml)$/i, '');

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

const WorkflowDefinitionsPage: React.FC = () => {
  const navigate = useNavigate();
  const { message } = App.useApp();
  const importInputRef = useRef<HTMLInputElement>(null);
  const [loading, setLoading] = useState(false);
  const [deploying, setDeploying] = useState(false);
  const [definitions, setDefinitions] = useState<WorkflowDefinition[]>([]);
  const [filters, setFilters] = useState({ key: '', name: '' });
  const [appliedFilters, setAppliedFilters] = useState<{ key?: string; name?: string }>({});
  const [selectedDefinition, setSelectedDefinition] = useState<WorkflowDefinition>();
  const [detailProcessInstanceId, setDetailProcessInstanceId] = useState<string>();
  const [exportingDefinitionId, setExportingDefinitionId] = useState<string>();

  const loadDefinitions = async (params?: { key?: string; name?: string }) => {
    setLoading(true);
    try {
      const response = await getWorkflowDefinitions(params);
      setDefinitions(response.result || []);
    } catch (_error) {
      message.error('加载流程定义失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadDefinitions();
  }, []);

  const handleQuickDeploy = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) {
      return;
    }

    setDeploying(true);
    try {
      await deployWorkflowDefinition({
        file,
        category: DEFAULT_CATEGORY,
        name: stripFileExtension(file.name),
      });
      message.success(`已部署流程文件：${file.name}`);
      await loadDefinitions(appliedFilters);
    } catch (_error) {
      message.error('上传部署失败');
    } finally {
      setDeploying(false);
      event.target.value = '';
    }
  };

  const handleExportDefinition = async (record: WorkflowDefinition) => {
    setExportingDefinitionId(record.id);
    try {
      const response = await getWorkflowDefinitionXml(record.id);
      downloadTextFile(
        response.result || '',
        getWorkflowResourceName(record.resourceName) !== '-'
          ? getWorkflowResourceName(record.resourceName)
          : `${record.key}-v${record.version}.bpmn20.xml`,
        'application/xml;charset=utf-8',
      );
      message.success(`已导出流程定义：${record.name || record.key}`);
    } catch (_error) {
      message.error('导出流程定义失败');
    } finally {
      setExportingDefinitionId(undefined);
    }
  };

  const columns: TableColumnsType<WorkflowDefinition> = [
    {
      title: '流程名称',
      dataIndex: 'name',
      width: 220,
      ellipsis: true,
      render: (value, record) => (
        <Space direction="vertical" size={2}>
          <Typography.Text strong>{value || record.key}</Typography.Text>
          <Typography.Text type="secondary">
            {record.key} · v{record.version}
          </Typography.Text>
        </Space>
      ),
    },
    {
      title: '资源文件',
      dataIndex: 'resourceName',
      width: 180,
      ellipsis: true,
      render: (value) => getWorkflowResourceName(value),
    },
    {
      title: '部署ID',
      dataIndex: 'deploymentId',
      width: 200,
      ellipsis: true,
    },
    {
      title: '分类',
      dataIndex: 'category',
      width: 180,
      ellipsis: true,
      render: (value) => value || '-',
    },
    {
      title: '状态',
      dataIndex: 'suspended',
      width: 88,
      align: 'center',
      render: (value) =>
        value ? <Tag color="warning">挂起</Tag> : <Tag color="success">可用</Tag>,
    },
    {
      title: '操作',
      key: 'action',
      width: 230,
      fixed: 'right',
      render: (_, record) => (
        <Space split={<span style={{ color: '#d9d9d9' }}>|</span>}>
          <Button
            icon={<EditOutlined />}
            style={{ paddingInline: 0 }}
            type="link"
            onClick={() => {
              navigate(`/workflow/designer?definitionId=${encodeURIComponent(record.id)}`);
            }}
          >
            编辑流程
          </Button>
          <Button
            icon={<DownloadOutlined />}
            loading={exportingDefinitionId === record.id}
            style={{ paddingInline: 0 }}
            type="link"
            onClick={() => {
              void handleExportDefinition(record);
            }}
          >
            导出 BPMN
          </Button>
          <Button
            icon={<PlayCircleOutlined />}
            style={{ paddingInline: 0 }}
            type="link"
            onClick={() => {
              setSelectedDefinition(record);
            }}
          >
            发起
          </Button>
          <Popconfirm
            cancelText="取消"
            description="删除部署会级联删除该部署下的流程定义与实例数据。"
            okText="删除"
            title="删除部署"
            onConfirm={async () => {
              await deleteWorkflowDeployment(record.deploymentId, true);
              message.success('部署已删除');
              await loadDefinitions(appliedFilters);
            }}
          >
            <Button
              danger
              icon={<DeleteOutlined />}
              style={{ paddingInline: 0 }}
              type="link"
            >
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <PageContainer
      className="saas-page-container saas-workflow-page"
      content="这里用于查看 Flowable 已部署的流程定义，并跳转到独立设计器继续编辑或发起流程。"
      extra={[
        <Button
          key="designer"
          type="primary"
          onClick={() => {
            navigate('/workflow/designer');
          }}
        >
          新建流程
        </Button>,
        <Button
          key="drafts"
          onClick={() => {
            navigate('/workflow/drafts');
          }}
        >
          流程草稿
        </Button>,
        <Button
          key="deploy"
          icon={<UploadOutlined />}
          loading={deploying}
          onClick={() => {
            importInputRef.current?.click();
          }}
        >
          导入 BPMN
        </Button>,
      ]}
      title="流程定义"
    >
      <Space direction="vertical" size={16} style={{ display: 'flex' }}>
        <Row gutter={[16, 16]}>
          <Col lg={8} md={8} sm={24} xs={24}>
            <Card className="saas-workflow-halo-card" variant="borderless">
              <Statistic title="已部署定义数" value={definitions.length} />
            </Card>
          </Col>
          <Col lg={8} md={8} sm={24} xs={24}>
            <Card className="saas-workflow-halo-card" variant="borderless">
              <Statistic title="当前查询条件" value={appliedFilters.name || appliedFilters.key || '全部'} />
            </Card>
          </Col>
          <Col lg={8} md={8} sm={24} xs={24}>
            <Card className="saas-workflow-halo-card" variant="borderless">
              <Space direction="vertical" size={10}>
                <Typography.Text strong>模块入口</Typography.Text>
                <Space wrap>
                  <Button
                    onClick={() => {
                      navigate('/workflow/designer');
                    }}
                  >
                    去设计器
                  </Button>
                  <Button
                    onClick={() => {
                      navigate('/workflow/drafts');
                    }}
                  >
                    去草稿箱
                  </Button>
                </Space>
              </Space>
            </Card>
          </Col>
        </Row>

        <Card className="saas-workflow-panel-card" variant="borderless">
          <Space direction="vertical" size={16} style={{ display: 'flex' }}>
            <div className="saas-workflow-studio__toolbar">
              <div className="saas-workflow-studio__filters saas-workflow-definitions__filters">
                <Input
                  allowClear
                  placeholder="按流程名称搜索"
                  value={filters.name}
                  onChange={(event) => {
                    setFilters((prev) => ({ ...prev, name: event.target.value }));
                  }}
                />
                <Input
                  allowClear
                  placeholder="按定义 Key 搜索"
                  value={filters.key}
                  onChange={(event) => {
                    setFilters((prev) => ({ ...prev, key: event.target.value }));
                  }}
                />
              </div>
              <Space wrap>
                <Button
                  type="primary"
                  onClick={() => {
                    const params = {
                      key: filters.key.trim() || undefined,
                      name: filters.name.trim() || undefined,
                    };
                    setAppliedFilters(params);
                    void loadDefinitions(params);
                  }}
                >
                  查询
                </Button>
                <Button
                  icon={<ReloadOutlined />}
                  onClick={() => {
                    setFilters({ key: '', name: '' });
                    setAppliedFilters({});
                    void loadDefinitions();
                  }}
                >
                  重置
                </Button>
              </Space>
            </div>

            <Table<WorkflowDefinition>
              columns={columns}
              dataSource={definitions}
              loading={loading}
              locale={{
                emptyText: (
                  <Empty
                    description="当前还没有可用的流程定义"
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                  />
                ),
              }}
              pagination={false}
              rowKey="id"
              scroll={{ x: 1040 }}
              size="middle"
              tableLayout="fixed"
            />
          </Space>
        </Card>
      </Space>

      <input
        accept=".bpmn,.bpmn20.xml,.xml"
        hidden
        ref={importInputRef}
        type="file"
        onChange={handleQuickDeploy}
      />

      <WorkflowStartProcessModal
        definition={selectedDefinition}
        open={Boolean(selectedDefinition)}
        onCancel={() => {
          setSelectedDefinition(undefined);
        }}
        onSuccess={(instance: WorkflowInstance) => {
          setSelectedDefinition(undefined);
          setDetailProcessInstanceId(instance.processInstanceId);
        }}
      />

      <WorkflowInstanceDetailDrawer
        open={Boolean(detailProcessInstanceId)}
        processInstanceId={detailProcessInstanceId}
        onClose={() => {
          setDetailProcessInstanceId(undefined);
        }}
      />
    </PageContainer>
  );
};

export default WorkflowDefinitionsPage;
