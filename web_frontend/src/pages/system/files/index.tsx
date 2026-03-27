import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { FooterToolbar, PageContainer, ProTable } from '@ant-design/pro-components';
import {
  DeleteOutlined,
  DownloadOutlined,
  EyeOutlined,
  ImportOutlined,
  InboxOutlined,
  ReloadOutlined,
  UploadOutlined,
} from '@ant-design/icons';
import {
  App,
  Button,
  Card,
  Descriptions,
  Empty,
  Form,
  Input,
  Modal,
  Popconfirm,
  Space,
  Statistic,
  Tag,
  Typography,
} from 'antd';
import React, { useEffect, useMemo, useRef, useState } from 'react';
import {
  deleteBatchResource,
  deleteResource,
  downloadProtectedResource,
  exportResourceExcel,
  importResourceExcel,
  pickLocalFile,
  pickLocalFiles,
  previewProtectedResource,
  queryResourcePage,
  uploadResourceBinary,
} from '@/services/backend/resources';
import { getFileCenterStats } from '@/services/backend/fileCenter';
import type { FileCenterStats, GenericRecord, PageResult } from '@/services/backend/types';

const formatBytes = (value?: number | null) => {
  if (!value) {
    return '0 B';
  }
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  let next = value;
  let unitIndex = 0;
  while (next >= 1024 && unitIndex < units.length - 1) {
    next /= 1024;
    unitIndex += 1;
  }
  return `${next.toFixed(next >= 100 || unitIndex === 0 ? 0 : 1)} ${units[unitIndex]}`;
};

const getFileExtension = (fileName?: string) => {
  const match = fileName?.match(/\.([^.]+)$/);
  return match?.[1]?.toUpperCase() || 'FILE';
};

const isImageFile = (record: GenericRecord) =>
  String(record.contentType || '').toLowerCase().startsWith('image/');

const FileCenterPage: React.FC = () => {
  const actionRef = useRef<ActionType | null>(null);
  const lastQueryParamsRef = useRef<Record<string, any>>({});
  const { message } = App.useApp();
  const [statsLoading, setStatsLoading] = useState(false);
  const [stats, setStats] = useState<FileCenterStats>();
  const [selectedRows, setSelectedRows] = useState<GenericRecord[]>([]);
  const [uploadModalOpen, setUploadModalOpen] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [transferLoading, setTransferLoading] = useState<'import' | 'export'>();
  const [pendingFiles, setPendingFiles] = useState<File[]>([]);
  const [uploadForm] = Form.useForm<{ bizType?: string }>();

  const bizTypeOptions = useMemo(
    () =>
      (stats?.bizTypeMetrics || []).map((item) => ({
        label: `${item.label} (${item.count})`,
        value: item.label,
      })),
    [stats],
  );

  const providerOptions = useMemo(
    () =>
      (stats?.providerMetrics || []).map((item) => ({
        label: `${item.label} (${item.count})`,
        value: item.label,
      })),
    [stats],
  );

  const loadStats = async () => {
    setStatsLoading(true);
    try {
      const response = await getFileCenterStats();
      setStats(response.result);
    } finally {
      setStatsLoading(false);
    }
  };

  useEffect(() => {
    void loadStats();
  }, []);

  const reloadEverything = () => {
    void loadStats();
    actionRef.current?.reload();
  };

  const handleOpenUpload = async () => {
    const accept = stats?.allowedTypes?.length ? stats.allowedTypes.join(',') : '*';
    const files = await pickLocalFiles(accept);
    if (!files.length) {
      return;
    }
    setPendingFiles(files);
    uploadForm.setFieldsValue({
      bizType: stats?.defaultBizType || 'system',
    });
    setUploadModalOpen(true);
  };

  const handleBatchUpload = async () => {
    const values = await uploadForm.validateFields();
    setUploading(true);
    let successCount = 0;
    const failedFiles: string[] = [];

    try {
      for (const file of pendingFiles) {
        try {
          await uploadResourceBinary('/api/file/upload', file, {
            bizType: values.bizType?.trim() || undefined,
          });
          successCount += 1;
        } catch (_error) {
          failedFiles.push(file.name);
        }
      }

      if (successCount) {
        message.success(`已上传 ${successCount} 个文件`);
      }
      if (failedFiles.length) {
        message.warning(`以下文件上传失败：${failedFiles.join('、')}`);
      }
      setUploadModalOpen(false);
      setPendingFiles([]);
      uploadForm.resetFields();
      reloadEverything();
    } finally {
      setUploading(false);
    }
  };

  const handleImport = async () => {
    const file = await pickLocalFile('.xlsx,.xls');
    if (!file) {
      return;
    }
    setTransferLoading('import');
    try {
      const response = await importResourceExcel('/api/file', file);
      message.success(response.message || response.result || '文件元数据导入成功');
      reloadEverything();
    } finally {
      setTransferLoading(undefined);
    }
  };

  const handleExport = async () => {
    const params = { ...lastQueryParamsRef.current };
    if (selectedRows.length) {
      params.selections = selectedRows.map((item) => item.id).join(',');
    }
    setTransferLoading('export');
    try {
      await exportResourceExcel('/api/file', params, '文件中心');
      message.success('文件中心导出成功');
    } finally {
      setTransferLoading(undefined);
    }
  };

  const handleDelete = async (record: GenericRecord) => {
    await deleteResource('/api/file', record.id);
    message.success('文件已删除');
    reloadEverything();
  };

  const handleBatchDelete = async () => {
    if (!selectedRows.length) {
      message.warning('请先选择要删除的文件');
      return;
    }
    await deleteBatchResource(
      '/api/file',
      selectedRows.map((item) => item.id),
    );
    setSelectedRows([]);
    message.success('已批量删除选中文件');
    reloadEverything();
  };

  const columns: ProColumns<GenericRecord>[] = [
    {
      title: '文件',
      dataIndex: 'fileName',
      width: 280,
      ellipsis: true,
      render: (_, record) => (
        <Space align="start" size={12}>
          <div className="saas-file-center__file-badge">
            {isImageFile(record) ? 'IMG' : getFileExtension(record.fileName)}
          </div>
          <div style={{ minWidth: 0 }}>
            <Typography.Text ellipsis={{ tooltip: record.fileName }} strong>
              {record.fileName}
            </Typography.Text>
            <div>
              <Typography.Text type="secondary">
                {record.contentType || '未知类型'} · {formatBytes(record.fileSize)}
              </Typography.Text>
            </div>
          </div>
        </Space>
      ),
    },
    {
      title: '业务类型',
      dataIndex: 'bizType',
      width: 140,
      valueType: 'select',
      fieldProps: {
        options: bizTypeOptions,
      },
      render: (_, record) =>
        record.bizType ? <Tag color="processing">{record.bizType}</Tag> : '-',
    },
    {
      title: '存储提供方',
      dataIndex: 'storageProvider',
      width: 120,
      valueType: 'select',
      fieldProps: {
        options: providerOptions,
      },
      render: (_, record) =>
        record.storageProvider ? <Tag>{record.storageProvider}</Tag> : '-',
    },
    {
      title: '目录',
      dataIndex: 'bucketName',
      width: 130,
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 90,
      valueType: 'select',
      fieldProps: {
        options: [
          { label: '启用', value: 1 },
          { label: '禁用', value: 0 },
        ],
      },
      render: (_, record) =>
        Number(record.status) === 1 ? (
          <Tag color="success">启用</Tag>
        ) : (
          <Tag color="default">禁用</Tag>
        ),
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      valueType: 'dateTime',
      width: 180,
      hideInSearch: true,
    },
    {
      title: '操作',
      key: 'option',
      valueType: 'option',
      fixed: 'right',
      width: 220,
      render: (_, record) => [
        <a
          key="preview"
          onClick={async () => {
            await previewProtectedResource(`/api/file/preview/${record.id}`, record.fileName || 'preview');
          }}
        >
          <EyeOutlined /> 预览
        </a>,
        <a
          key="download"
          onClick={async () => {
            await downloadProtectedResource(`/api/file/download/${record.id}`, record.fileName || 'download');
          }}
        >
          <DownloadOutlined /> 下载
        </a>,
        <Popconfirm
          key="delete"
          title="确认删除这个文件吗？"
          onConfirm={async () => {
            await handleDelete(record);
          }}
        >
          <a>删除</a>
        </Popconfirm>,
      ],
    },
  ];

  return (
    <PageContainer
      className="saas-page-container saas-file-center"
      title="文件管理"
      content="集中管理上传文件、业务分类、存储策略和文件元数据，支持批量上传、预览、下载、导入导出。"
      extra={[
        <Tag key="endpoint" color="blue">
          /api/file
        </Tag>,
      ]}
    >
      <Space direction="vertical" size={16} style={{ display: 'flex' }}>
        <div className="saas-file-center__overview">
          <Card loading={statsLoading} variant="borderless">
            <Statistic title="文件总数" value={stats?.totalCount || 0} />
          </Card>
          <Card loading={statsLoading} variant="borderless">
            <Statistic title="累计存储" value={formatBytes(stats?.totalSize)} />
          </Card>
          <Card loading={statsLoading} variant="borderless">
            <Statistic title="今日上传" value={stats?.todayUploadCount || 0} suffix="个" />
          </Card>
          <Card loading={statsLoading} variant="borderless">
            <Statistic title="今日流入" value={formatBytes(stats?.todayUploadSize)} />
          </Card>
        </div>

        <div className="saas-file-center__side-grid">
          <Card className="saas-file-center__policy" title="上传策略" variant="borderless">
            <Descriptions column={1} size="small">
              <Descriptions.Item label="默认业务类型">
                {stats?.defaultBizType || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="默认存储提供方">
                {stats?.storageProvider || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="大小上限">
                {stats?.maxSizeMb ? `${stats.maxSizeMb} MB` : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="允许扩展名">
                <Space size={[6, 6]} wrap>
                  {(stats?.allowedTypes || []).length ? (
                    stats?.allowedTypes?.map((item) => <Tag key={item}>{item}</Tag>)
                  ) : (
                    <Typography.Text type="secondary">未配置</Typography.Text>
                  )}
                </Space>
              </Descriptions.Item>
            </Descriptions>
          </Card>

          <Card className="saas-file-center__metrics" title="业务分布" variant="borderless">
            {(stats?.bizTypeMetrics || []).length ? (
              <Space size={[8, 8]} wrap>
                {stats?.bizTypeMetrics?.map((item) => (
                  <Tag key={item.label} color="processing">
                    {item.label} {item.count}
                  </Tag>
                ))}
              </Space>
            ) : (
              <Empty description="暂无业务分类数据" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </Card>

          <Card className="saas-file-center__metrics" title="存储分布" variant="borderless">
            {(stats?.providerMetrics || []).length ? (
              <Space size={[8, 8]} wrap>
                {stats?.providerMetrics?.map((item) => (
                  <Tag key={item.label}>
                    {item.label} {item.count}
                  </Tag>
                ))}
              </Space>
            ) : (
              <Empty description="暂无存储分布数据" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </Card>
        </div>

        <ProTable<GenericRecord>
          actionRef={actionRef}
          cardBordered
          columns={columns}
          rowKey="id"
          rowSelection={{
            onChange: (_, rows) => {
              setSelectedRows(rows);
            },
          }}
          request={async (params) => {
            const queryParams = {
              pageNo: params.current,
              pageSize: params.pageSize,
              fileName: params.fileName,
              bizType: params.bizType,
              storageProvider: params.storageProvider,
              status: params.status,
            };
            lastQueryParamsRef.current = queryParams;
            const response = await queryResourcePage('/api/file', queryParams);
            const result = response.result as PageResult<GenericRecord>;
            return {
              data: result.records,
              success: response.success,
              total: result.total,
            };
          }}
          search={{
            labelWidth: 120,
            defaultCollapsed: false,
            span: 8,
          }}
          options={false}
          pagination={{
            showSizeChanger: true,
            pageSize: 10,
          }}
          scroll={{ x: 1180 }}
          tableLayout="fixed"
          toolbar={{
            actions: [
              <Button
                key="upload"
                icon={<UploadOutlined />}
                type="primary"
                onClick={() => {
                  void handleOpenUpload();
                }}
              >
                批量上传
              </Button>,
              <Button
                key="import"
                icon={<ImportOutlined />}
                loading={transferLoading === 'import'}
                onClick={() => {
                  void handleImport();
                }}
              >
                导入元数据
              </Button>,
              <Button
                key="export"
                icon={<DownloadOutlined />}
                loading={transferLoading === 'export'}
                onClick={() => {
                  void handleExport();
                }}
              >
                导出元数据
              </Button>,
              <Button
                key="refresh"
                icon={<ReloadOutlined />}
                onClick={reloadEverything}
              >
                刷新
              </Button>,
            ],
          }}
        />

        {selectedRows.length ? (
          <FooterToolbar>
            <Space>
              <span>已选择 {selectedRows.length} 个文件</span>
              <Button
                danger
                icon={<DeleteOutlined />}
                onClick={() => {
                  void handleBatchDelete();
                }}
              >
                批量删除
              </Button>
            </Space>
          </FooterToolbar>
        ) : null}
      </Space>

      <Modal
        destroyOnHidden
        open={uploadModalOpen}
        title="批量上传文件"
        width={760}
        okText="开始上传"
        confirmLoading={uploading}
        onOk={() => {
          void handleBatchUpload();
        }}
        onCancel={() => {
          setUploadModalOpen(false);
          setPendingFiles([]);
          uploadForm.resetFields();
        }}
      >
        <Space direction="vertical" size={16} style={{ display: 'flex' }}>
          <Form form={uploadForm} layout="vertical">
            <Form.Item
              label="业务类型"
              name="bizType"
              rules={[{ required: true, message: '请输入业务类型' }]}
            >
              <Input placeholder="请输入业务类型，例如 system、invoice、contract" />
            </Form.Item>
          </Form>

          <Card variant="borderless">
            <Typography.Text strong>待上传文件</Typography.Text>
            <div className="saas-file-center__pending-list">
              {pendingFiles.map((file) => (
                <div key={`${file.name}-${file.size}`} className="saas-file-center__pending-item">
                  <Space align="start" size={12}>
                    <div className="saas-file-center__file-badge">
                      {getFileExtension(file.name)}
                    </div>
                    <div>
                      <Typography.Text>{file.name}</Typography.Text>
                      <div>
                        <Typography.Text type="secondary">
                          {formatBytes(file.size)}
                        </Typography.Text>
                      </div>
                    </div>
                  </Space>
                </div>
              ))}
            </div>
          </Card>
        </Space>
      </Modal>
    </PageContainer>
  );
};

export default FileCenterPage;
