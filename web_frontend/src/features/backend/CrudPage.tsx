import type { ActionType, ProColumns, ProFormInstance } from '@ant-design/pro-components';
import {
  FooterToolbar,
  ModalForm,
  PageContainer,
  ProFormDatePicker,
  ProFormDateTimePicker,
  ProFormDigit,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
  ProFormTreeSelect,
  ProTable,
} from '@ant-design/pro-components';
import dayjs from 'dayjs';
import { Button, Popconfirm, Result, Space, Tag, Typography, message } from 'antd';
import React, { useEffect, useRef, useState } from 'react';
import {
  createResource,
  deleteBatchResource,
  deleteResource,
  exportResourceExcel,
  getResourceDetail,
  importResourceExcel,
  pickLocalFile,
  queryResourcePage,
  updateResource,
} from '@/services/backend/resources';
import type { GenericRecord, OptionItem, TreeOption } from '@/services/backend/types';
import {
  INLINE_FORM_LABEL_COL,
  INLINE_FORM_LABEL_WIDTH,
  INLINE_FORM_WRAPPER_COL,
} from '@/constants/formLayout';
import type { BackendFieldConfig, ResourceConfig } from './types';

const buildValueEnum = (options?: OptionItem[]) => {
  if (!options?.length) {
    return undefined;
  }

  return Object.fromEntries(
    options.map((item) => [
      item.value,
      {
        text: item.label,
      },
    ]),
  );
};

const serializeValues = (
  values: GenericRecord,
  fields: BackendFieldConfig[],
) => {
  const next: GenericRecord = {};

  fields.forEach((field) => {
    const value = values[field.name];
    if (
      value === undefined ||
      value === null ||
      value === '' ||
      (Array.isArray(value) && value.length === 0)
    ) {
      return;
    }

    if (dayjs.isDayjs(value)) {
      next[field.name] =
        field.type === 'date'
          ? value.format('YYYY-MM-DD')
          : value.format('YYYY-MM-DDTHH:mm:ss');
      return;
    }

    if (Array.isArray(value)) {
      next[field.name] = value.map((item) => String(item));
      return;
    }

    next[field.name] = value;
  });

  return next;
};

const toFormValues = (
  record: GenericRecord,
  fields: BackendFieldConfig[],
) => {
  const next = { ...record };
  fields.forEach((field) => {
    const value = record[field.name];
    if (!value) {
      return;
    }
    if (field.type === 'date' || field.type === 'datetime') {
      next[field.name] = dayjs(value);
    }
  });
  return next;
};

const renderSummaryValue = (value: unknown) => {
  if (Array.isArray(value)) {
    return value.join(', ');
  }
  if (value === null || value === undefined || value === '') {
    return '-';
  }
  return String(value);
};

const isFullRowField = (field: BackendFieldConfig) =>
  field.colSpan === 24 ||
  field.type === 'textarea' ||
  field.type === 'image' ||
  field.type === 'file' ||
  field.type === 'treeMultiselect';

const getFieldColProps = (field: BackendFieldConfig) => {
  const span = isFullRowField(field) ? 24 : field.colSpan || 12;
  return {
    xs: 24,
    md: span,
  };
};

const getColumnWidth = (field: BackendFieldConfig) => {
  if (field.width) {
    return field.width;
  }

  if (field.copyable || field.name === 'id' || field.name.endsWith('Id')) {
    return 180;
  }

  if (
    field.name.includes('Time') ||
    field.type === 'datetime'
  ) {
    return 180;
  }

  if (field.type === 'date') {
    return 140;
  }

  if (field.type === 'number') {
    return 120;
  }

  if (
    field.type === 'select' ||
    field.name.endsWith('Status') ||
    field.name === 'status' ||
    field.name.endsWith('Type') ||
    field.name === 'type'
  ) {
    return 120;
  }

  if (
    ['url', 'requestUrl', 'invokeTarget', 'component', 'perms', 'email'].includes(
      field.name,
    )
  ) {
    return 240;
  }

  if (
    ['description', 'remark', 'msgContent', 'exceptionInfo', 'lastMessage'].includes(
      field.name,
    ) ||
    field.type === 'textarea'
  ) {
    return 260;
  }

  if (field.name.includes('phone')) {
    return 160;
  }

  if (field.name.includes('Name') || field.name === 'name') {
    return 160;
  }

  if (field.name.includes('Code') || field.name === 'code') {
    return 160;
  }

  return 150;
};

const renderFormField = (
  field: BackendFieldConfig,
  mode: 'create' | 'edit',
  optionsMap: Record<string, OptionItem[]>,
  treeOptionsMap: Record<string, TreeOption[]>,
) => {
  const rules =
    field.required || (mode === 'create' && field.requiredOnCreate)
      ? [{ required: true, message: `请输入${field.label}` }]
      : undefined;

  const options = field.options || optionsMap[field.name];
  const treeData = treeOptionsMap[field.name];

  switch (field.type) {
    case 'textarea':
      return (
        <ProFormTextArea
          key={field.name}
          name={field.name}
          label={field.label}
          colProps={getFieldColProps(field)}
          fieldProps={{
            rows: 4,
            showCount: true,
            maxLength: 500,
          }}
          rules={rules}
        />
      );
    case 'number':
      return (
        <ProFormDigit
          key={field.name}
          name={field.name}
          label={field.label}
          colProps={getFieldColProps(field)}
          rules={rules}
        />
      );
    case 'date':
      return (
        <ProFormDatePicker
          key={field.name}
          name={field.name}
          label={field.label}
          colProps={getFieldColProps(field)}
          rules={rules}
        />
      );
    case 'datetime':
      return (
        <ProFormDateTimePicker
          key={field.name}
          name={field.name}
          label={field.label}
          colProps={getFieldColProps(field)}
          rules={rules}
        />
      );
    case 'select':
      return (
        <ProFormSelect
          key={field.name}
          name={field.name}
          label={field.label}
          colProps={getFieldColProps(field)}
          options={options}
          rules={rules}
        />
      );
    case 'multiselect':
      return (
        <ProFormSelect
          key={field.name}
          name={field.name}
          label={field.label}
          colProps={getFieldColProps(field)}
          mode="multiple"
          options={options}
          rules={rules}
        />
      );
    case 'treeSelect':
      return (
        <ProFormTreeSelect
          key={field.name}
          name={field.name}
          label={field.label}
          colProps={getFieldColProps(field)}
          rules={rules}
          fieldProps={{
            treeData,
            showSearch: true,
            treeDefaultExpandAll: true,
          }}
        />
      );
    case 'treeMultiselect':
      return (
        <ProFormTreeSelect
          key={field.name}
          name={field.name}
          label={field.label}
          colProps={getFieldColProps(field)}
          rules={rules}
          fieldProps={{
            treeData,
            showSearch: true,
            multiple: true,
            treeCheckable: true,
            treeDefaultExpandAll: true,
          }}
        />
      );
    case 'password':
      return (
        <ProFormText.Password
          key={field.name}
          name={field.name}
          label={field.label}
          colProps={getFieldColProps(field)}
          rules={rules}
        />
      );
    default:
      return (
        <ProFormText
          key={field.name}
          name={field.name}
          label={field.label}
          colProps={getFieldColProps(field)}
          rules={rules}
        />
      );
  }
};

export const CrudPage: React.FC<{ resource: ResourceConfig }> = ({
  resource,
}) => {
  const actionRef = useRef<ActionType | null>(null);
  const formRef = useRef<ProFormInstance | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState<'create' | 'edit'>('create');
  const [currentRecord, setCurrentRecord] = useState<GenericRecord>();
  const [selectedRows, setSelectedRows] = useState<GenericRecord[]>([]);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [optionMap, setOptionMap] = useState<Record<string, OptionItem[]>>({});
  const [treeOptionMap, setTreeOptionMap] = useState<Record<string, TreeOption[]>>({});
  const [messageApi, contextHolder] = message.useMessage();
  const idField = resource.idField || 'id';
  const lastQueryParamsRef = useRef<GenericRecord>({});

  useEffect(() => {
    let mounted = true;

    const loadFieldOptions = async () => {
      const nextOptions: Record<string, OptionItem[]> = {};
      const nextTreeOptions: Record<string, TreeOption[]> = {};

      await Promise.all(
        resource.fields.map(async (field) => {
          if (field.loadOptions) {
            nextOptions[field.name] = await field.loadOptions();
          }
          if (field.loadTreeOptions) {
            nextTreeOptions[field.name] = await field.loadTreeOptions();
          }
        }),
      );

      if (!mounted) {
        return;
      }

      setOptionMap(nextOptions);
      setTreeOptionMap(nextTreeOptions);
    };

    loadFieldOptions();

    return () => {
      mounted = false;
    };
  }, [resource]);

  const openCreateModal = () => {
    setModalMode('create');
    setCurrentRecord(undefined);
    setModalOpen(true);
    requestAnimationFrame(() => {
      formRef.current?.resetFields();
    });
  };

  const openEditModal = async (record: GenericRecord) => {
    setModalMode('edit');
    setCurrentRecord(record);
    setModalOpen(true);
    setLoadingDetail(true);
    requestAnimationFrame(() => {
      formRef.current?.resetFields();
    });

    try {
      const detail = resource.detailLoader
        ? await resource.detailLoader(record[idField])
        : (
            await getResourceDetail(
              resource.resourcePath,
              record[idField],
              resource.detailParams,
            )
          ).result;
      formRef.current?.setFieldsValue(toFormValues(detail, resource.fields));
    } catch (error) {
      messageApi.error('加载详情失败，请稍后重试');
      throw error;
    } finally {
      setLoadingDetail(false);
    }
  };

  const handleDelete = async (record: GenericRecord) => {
    await deleteResource(resource.resourcePath, record[idField]);
    messageApi.success('删除成功');
    actionRef.current?.reload();
  };

  const handleBatchDelete = async () => {
    if (!selectedRows.length) {
      messageApi.warning('请先选择要删除的数据');
      return;
    }

    await deleteBatchResource(
      resource.resourcePath,
      selectedRows.map((item) => item[idField]),
    );
    setSelectedRows([]);
    messageApi.success('批量删除成功');
    actionRef.current?.reloadAndRest?.();
  };

  const handleExport = async () => {
    const params = { ...lastQueryParamsRef.current };
    if (selectedRows.length) {
      params.selections = selectedRows.map((item) => item[idField]).join(',');
    }
    await exportResourceExcel(
      resource.resourcePath,
      params,
      resource.title.replace(/\s+/g, '_'),
    );
    messageApi.success('导出成功');
  };

  const handleImport = async () => {
    const file = await pickLocalFile('.xlsx,.xls');
    if (!file) {
      return;
    }
    const response = await importResourceExcel(resource.resourcePath, file);
    messageApi.success(response.message || response.result || '导入成功');
    actionRef.current?.reload();
  };

  const columns: ProColumns<GenericRecord>[] = resource.fields
    .filter((field) => !field.hideInTable)
    .map((field) => {
      const options = field.options || optionMap[field.name];
      const width = getColumnWidth(field);

      return {
        title: field.label,
        dataIndex: field.name,
        width,
        copyable: field.copyable,
        ellipsis: !isFullRowField(field),
        valueType:
          field.type === 'datetime'
            ? 'dateTime'
            : field.type === 'date'
              ? 'date'
              : field.type === 'number'
                ? 'digit'
                : field.type === 'select'
                  ? 'select'
                  : 'text',
        hideInSearch: field.hideInSearch ?? !['text', 'number', 'select', 'date', 'datetime'].includes(field.type || 'text'),
        valueEnum: buildValueEnum(options),
        render: (_, record) => {
          const value = record[field.name];
          if (Array.isArray(value)) {
            return value.length ? (
              <Space size={[4, 4]} wrap>
                {value.map((item) => (
                  <Tag key={`${field.name}-${item}`}>{String(item)}</Tag>
                ))}
              </Space>
            ) : (
              '-'
            );
          }
          const text = renderSummaryValue(value);
          if (text === '-') {
            return text;
          }
          if (field.type === 'textarea') {
            return (
              <Typography.Paragraph
                ellipsis={{ rows: 2, tooltip: text }}
                style={{ marginBottom: 0 }}
              >
                {text}
              </Typography.Paragraph>
            );
          }
          return (
            <Typography.Text
              ellipsis={{ tooltip: text }}
              style={{ display: 'block', maxWidth: '100%' }}
            >
              {text}
            </Typography.Text>
          );
        },
      };
    });

  columns.push({
    title: '操作',
    key: 'option',
    valueType: 'option',
    width: 220,
    fixed: 'right',
    render: (_, record) => {
      const actions: React.ReactNode[] = [];

      if (resource.allowEdit !== false) {
        actions.push(
          <a
            key="edit"
            onClick={() => {
              openEditModal(record);
            }}
          >
            编辑
          </a>,
        );
      }

      (resource.rowActions || [])
        .filter((action) => (action.visible ? action.visible(record) : true))
        .forEach((action) => {
          actions.push(
            <a
              key={action.key}
              onClick={async () => {
                await action.onClick(record);
                if (action.successMessage !== false) {
                  messageApi.success(action.successMessage || `${action.label}成功`);
                }
                if (action.reloadAfterAction !== false) {
                  actionRef.current?.reload();
                }
              }}
            >
              {action.label}
            </a>,
          );
        });

      if (resource.allowDelete !== false) {
        actions.push(
          <Popconfirm
            key="delete"
            title="确认删除这条记录吗？"
            onConfirm={async () => {
              await handleDelete(record);
            }}
          >
            <a>删除</a>
          </Popconfirm>,
        );
      }

      return actions;
    },
  });

  const tableScrollX = columns.reduce((sum, column) => {
    return sum + Number(column.width || 150);
  }, 0);

  if (!resource.fields.length) {
    return (
      <Result
        status="warning"
        title="当前资源没有可用字段定义"
        subTitle={resource.title}
      />
    );
  }

  return (
    <PageContainer
      className="saas-page-container saas-resource-page"
      title={resource.title}
      content={resource.description}
      extra={[
        <Tag key="endpoint" color="blue">
          {resource.resourcePath}
        </Tag>,
      ]}
    >
      {contextHolder}
      <ProTable<GenericRecord>
        actionRef={actionRef}
        rowKey={idField}
        columns={columns}
        cardBordered
        search={{
          labelWidth: INLINE_FORM_LABEL_WIDTH,
        }}
        scroll={{ x: Math.max(tableScrollX, 960) }}
        tableLayout="fixed"
        pagination={{
          showSizeChanger: true,
          showQuickJumper: true,
          defaultPageSize: 10,
          showTotal: (total) => `共 ${total} 条`,
        }}
        options={{
          density: false,
          reload: true,
          setting: true,
          fullScreen: true,
        }}
        request={async (params) => {
          const searchParams: GenericRecord = {};

          Object.entries(params).forEach(([key, value]) => {
            if (key === 'current') {
              searchParams.pageNo = value;
              return;
            }
            if (key === 'pageSize') {
              searchParams.pageSize = value;
              return;
            }
            if (
              value === undefined ||
              value === null ||
              value === '' ||
              key === '_timestamp'
            ) {
              return;
            }

            if (dayjs.isDayjs(value)) {
              searchParams[key] = value.format('YYYY-MM-DDTHH:mm:ss');
              return;
            }

            searchParams[key] = value;
          });

          lastQueryParamsRef.current = searchParams;

          const response = await queryResourcePage(
            resource.resourcePath,
            searchParams,
          );
          return {
            data: response.result.records || [],
            success: response.success,
            total: response.result.total || 0,
          };
        }}
        toolBarRender={() => [
          resource.allowExport === false ? null : (
            <Button key="export" onClick={() => void handleExport()}>
              导出
            </Button>
          ),
          resource.allowImport === true ||
          (resource.allowImport !== false && resource.allowCreate !== false) ? (
            <Button key="import" onClick={() => void handleImport()}>
              导入
            </Button>
          ) : null,
          resource.allowCreate === false ? null : (
            <Button key="create" type="primary" onClick={openCreateModal}>
              新建
            </Button>
          ),
          ...(resource.toolbarActions || []).map((action) => (
            <Button
              key={action.key}
              type={action.type}
              onClick={async () => {
                await action.onClick({
                  reload: () => {
                    actionRef.current?.reload();
                  },
                  selectedRows,
                });
                if (action.successMessage !== false) {
                  messageApi.success(action.successMessage || `${action.label}成功`);
                }
                if (action.reloadAfterAction !== false) {
                  actionRef.current?.reload();
                }
              }}
            >
              {action.label}
            </Button>
          )),
        ]}
        rowSelection={
          resource.allowDelete === false || resource.allowBatchDelete === false
            ? false
            : {
                onChange: (_, rows) => {
                  setSelectedRows(rows);
                },
              }
        }
      />

      {selectedRows.length > 0 &&
        resource.allowDelete !== false &&
        resource.allowBatchDelete !== false && (
          <FooterToolbar
            extra={
              <span>
                已选择 <b>{selectedRows.length}</b> 项
              </span>
            }
          >
            <Button danger onClick={handleBatchDelete}>
              批量删除
            </Button>
          </FooterToolbar>
        )}

      <ModalForm
        formRef={formRef}
        open={modalOpen}
        grid
        layout="horizontal"
        width={900}
        title={modalMode === 'create' ? `新建${resource.title}` : `编辑${resource.title}`}
        className="saas-form-modal saas-inline-form"
        labelAlign="right"
        labelWrap={false}
        labelCol={INLINE_FORM_LABEL_COL}
        wrapperCol={INLINE_FORM_WRAPPER_COL}
        rowProps={{
          gutter: [16, 0],
        }}
        modalProps={{
          destroyOnHidden: true,
          maskClosable: false,
          onCancel: () => {
            setModalOpen(false);
            setCurrentRecord(undefined);
          },
        }}
        onFinish={async (values) => {
          const serialized = serializeValues(values, resource.fields);
          const payload = resource.transformSubmit
            ? resource.transformSubmit(serialized, {
                mode: modalMode,
                currentRecord,
              })
            : serialized;

          if (modalMode === 'create') {
            const response = await createResource(resource.resourcePath, payload);
            const savedId = response.result?.[idField];
            if (resource.afterSave) {
              await resource.afterSave({
                mode: modalMode,
                savedId,
                values: payload,
              });
            }
            messageApi.success('创建成功');
          } else {
            const savedId = currentRecord?.[idField];
            await updateResource(resource.resourcePath, savedId, payload);
            if (resource.afterSave) {
              await resource.afterSave({
                mode: modalMode,
                savedId,
                values: payload,
                currentRecord,
              });
            }
            messageApi.success('更新成功');
          }

          setModalOpen(false);
          setCurrentRecord(undefined);
          actionRef.current?.reload();
          return true;
        }}
      >
        {loadingDetail && modalMode === 'edit' ? (
          <Result status="info" title="正在加载详情..." />
        ) : (
          resource.fields
            .filter((item) => !item.hideInForm)
            .map((item) =>
              renderFormField(
                item,
                modalMode,
                optionMap,
                treeOptionMap,
              ),
            )
        )}
      </ModalForm>
    </PageContainer>
  );
};
