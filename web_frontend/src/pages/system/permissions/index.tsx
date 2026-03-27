import {
  ArrowDownOutlined,
  ArrowUpOutlined,
  ApartmentOutlined,
  AppstoreOutlined,
  ClockCircleOutlined,
  ClusterOutlined,
  DashboardOutlined,
  DeleteOutlined,
  DownloadOutlined,
  EditOutlined,
  FileOutlined,
  FolderOpenOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  MoreOutlined,
  NotificationOutlined,
  PlusOutlined,
  ReloadOutlined,
  SafetyOutlined,
  SaveOutlined,
  SearchOutlined,
  SettingOutlined,
  TeamOutlined,
  UploadOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import {
  Alert,
  App,
  Button,
  Card,
  Col,
  Drawer,
  Dropdown,
  Empty,
  Form,
  Input,
  InputNumber,
  Radio,
  Row,
  Segmented,
  Select,
  Space,
  Switch,
  Table,
  Tag,
  Tooltip,
  TreeSelect,
  Typography,
} from 'antd';
import type { MenuProps, TableColumnsType } from 'antd';
import React, { useEffect, useMemo, useState } from 'react';
import { INLINE_FORM_LABEL_COL, INLINE_FORM_LABEL_WIDTH, INLINE_FORM_WRAPPER_COL } from '@/constants/formLayout';
import {
  createResource,
  deleteBatchResource,
  deleteResource,
  exportResourceExcel,
  getResourceDetail,
  importResourceExcel,
  pickLocalFile,
  updateResource,
} from '@/services/backend/resources';
import {
  getPermissionManagementTree,
  getPermissionNextSortNo,
  savePermissionSort,
  savePermissionTree,
} from '@/services/backend/system';
import type {
  GenericRecord,
  PermissionTreeNode,
  PermissionTreeSaveNode,
} from '@/services/backend/types';

const { Text, Paragraph } = Typography;

const PERMISSION_RESOURCE_PATH = '/api/permission';

const PERMISSION_TYPE_OPTIONS = [
  { label: '一级菜单', value: 0 },
  { label: '子菜单', value: 1 },
  { label: '按钮/权限', value: 2 },
];

const STATUS_OPTIONS = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
];

const ICON_COMPONENT_MAP = {
  apartment: ApartmentOutlined,
  appstore: AppstoreOutlined,
  cluster: ClusterOutlined,
  dashboard: DashboardOutlined,
  file: FileOutlined,
  folder: FolderOpenOutlined,
  notification: NotificationOutlined,
  safety: SafetyOutlined,
  setting: SettingOutlined,
  team: TeamOutlined,
  user: UserOutlined,
  'clock-circle': ClockCircleOutlined,
} as const;

const ICON_OPTIONS = [
  { label: 'Dashboard', value: 'dashboard' },
  { label: 'Setting', value: 'setting' },
  { label: 'Apartment', value: 'apartment' },
  { label: 'User', value: 'user' },
  { label: 'Team', value: 'team' },
  { label: 'Safety', value: 'safety' },
  { label: 'Cluster', value: 'cluster' },
  { label: 'Notification', value: 'notification' },
  { label: 'File', value: 'file' },
  { label: 'ClockCircle', value: 'clock-circle' },
  { label: 'Appstore', value: 'appstore' },
  { label: 'Folder', value: 'folder' },
];

type DrawerMode = 'create' | 'edit';

interface SearchValues {
  keyword?: string;
}

interface PermissionFormValues {
  type: number;
  parentId?: string;
  name: string;
  url?: string;
  component?: string;
  perms?: string;
  icon?: string;
  sortNo?: number;
  hidden?: boolean;
  alwaysShow?: boolean;
  status?: number;
  remark?: string;
}

const getTypeLabel = (type?: number) => {
  switch (type) {
    case 0:
      return '一级菜单';
    case 1:
      return '子菜单';
    case 2:
      return '按钮/权限';
    default:
      return '未分类';
  }
};

const getTypeColor = (type?: number) => {
  switch (type) {
    case 0:
      return 'processing';
    case 1:
      return 'success';
    case 2:
      return 'purple';
    default:
      return 'default';
  }
};

const normalizeKeyword = (value?: string) => value?.trim().toLowerCase() || '';

const collectExpandableKeys = (nodes: PermissionTreeNode[]) => {
  const keys: string[] = [];
  const walk = (items: PermissionTreeNode[]) => {
    items.forEach((item) => {
      if (item.children?.length) {
        keys.push(item.id);
        walk(item.children);
      }
    });
  };
  walk(nodes);
  return keys;
};

const buildSiblingMoveStateMap = (nodes: PermissionTreeNode[]) => {
  const moveStateMap = new Map<string, { canMoveUp: boolean; canMoveDown: boolean }>();
  const walk = (items: PermissionTreeNode[]) => {
    items.forEach((item, index) => {
      moveStateMap.set(item.id, {
        canMoveUp: index > 0,
        canMoveDown: index < items.length - 1,
      });
      if (item.children?.length) {
        walk(item.children);
      }
    });
  };
  walk(nodes);
  return moveStateMap;
};

const moveTreeNodeWithinLevel = (
  nodes: PermissionTreeNode[],
  targetId: string,
  direction: 'up' | 'down',
): PermissionTreeNode[] => {
  let changed = false;

  const walk = (items: PermissionTreeNode[]): PermissionTreeNode[] => {
    const currentIndex = items.findIndex((item) => item.id === targetId);
    if (currentIndex >= 0) {
      const nextIndex = direction === 'up' ? currentIndex - 1 : currentIndex + 1;
      if (nextIndex < 0 || nextIndex >= items.length) {
        return items;
      }
      const nextItems = [...items];
      [nextItems[currentIndex], nextItems[nextIndex]] = [
        nextItems[nextIndex],
        nextItems[currentIndex],
      ];
      changed = true;
      return nextItems;
    }

    return items.map((item) => {
      if (!item.children?.length || changed) {
        return item;
      }
      const nextChildren = walk(item.children);
      if (nextChildren === item.children) {
        return item;
      }
      return {
        ...item,
        children: nextChildren,
      };
    });
  };

  const nextTree = walk(nodes);
  return changed ? nextTree : nodes;
};

const buildPermissionSortPayload = (
  nodes: PermissionTreeNode[],
  parentId?: string | null,
): Array<{ id: string; parentId?: string | null; sortNo: number }> => {
  const result: Array<{ id: string; parentId?: string | null; sortNo: number }> = [];

  nodes.forEach((item, index) => {
    result.push({
      id: item.id,
      parentId: parentId || null,
      sortNo: (index + 1) * 10,
    });

    if (item.children?.length) {
      result.push(...buildPermissionSortPayload(item.children, item.id));
    }
  });

  return result;
};

const buildPermissionTreeSavePayload = (
  nodes: PermissionTreeNode[],
): PermissionTreeSaveNode[] =>
  nodes.map((item) => ({
    id: item.id,
    children: item.children?.length
      ? buildPermissionTreeSavePayload(item.children)
      : undefined,
  }));

const flattenTree = (nodes: PermissionTreeNode[]) => {
  const result: PermissionTreeNode[] = [];
  const walk = (items: PermissionTreeNode[]) => {
    items.forEach((item) => {
      result.push(item);
      if (item.children?.length) {
        walk(item.children);
      }
    });
  };
  walk(nodes);
  return result;
};

const filterTreeByKeyword = (nodes: PermissionTreeNode[], keyword: string): PermissionTreeNode[] => {
  if (!keyword) {
    return nodes;
  }

  return nodes.reduce<PermissionTreeNode[]>((acc, item) => {
    const children = filterTreeByKeyword(item.children || [], keyword);
    const matched =
      normalizeKeyword(item.name).includes(keyword) ||
      normalizeKeyword(item.url).includes(keyword) ||
      normalizeKeyword(item.perms).includes(keyword) ||
      normalizeKeyword(item.component).includes(keyword);

    if (matched || children.length) {
      acc.push({
        ...item,
        children,
      });
    }

    return acc;
  }, []);
};

const findNodeById = (nodes: PermissionTreeNode[], id?: string): PermissionTreeNode | undefined => {
  if (!id) {
    return undefined;
  }

  for (const item of nodes) {
    if (item.id === id) {
      return item;
    }
    if (item.children?.length) {
      const child = findNodeById(item.children, id);
      if (child) {
        return child;
      }
    }
  }

  return undefined;
};

const collectDescendantIds = (node?: PermissionTreeNode) => {
  const ids = new Set<string>();
  if (!node) {
    return ids;
  }

  const walk = (current: PermissionTreeNode) => {
    ids.add(current.id);
    current.children?.forEach(walk);
  };
  walk(node);
  return ids;
};

const buildParentTreeOptions = (
  nodes: PermissionTreeNode[],
  excludedIds: Set<string>,
): { title: React.ReactNode; value: string; key: string; children?: any[] }[] =>
  nodes.reduce<any[]>((acc, item) => {
    if (excludedIds.has(item.id)) {
      return acc;
    }

    const childOptions = buildParentTreeOptions(item.children || [], excludedIds);
    if (item.type === 2) {
      return acc;
    }

    acc.push({
      title: (
        <Space size={8}>
          <span>{item.name}</span>
          <Tag color={getTypeColor(item.type)}>{getTypeLabel(item.type)}</Tag>
        </Space>
      ),
      value: item.id,
      key: item.id,
      children: childOptions.length ? childOptions : undefined,
    });
    return acc;
  }, []);

const resolveIconNode = (icon?: string) => {
  if (!icon) {
    return null;
  }
  const IconComponent =
    ICON_COMPONENT_MAP[icon.toLowerCase() as keyof typeof ICON_COMPONENT_MAP] || AppstoreOutlined;
  return <IconComponent />;
};

const PermissionManagementPage: React.FC = () => {
  const { message, modal } = App.useApp();
  const [searchForm] = Form.useForm<SearchValues>();
  const [drawerForm] = Form.useForm<PermissionFormValues>();

  const [loading, setLoading] = useState(false);
  const [treeData, setTreeData] = useState<PermissionTreeNode[]>([]);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [expandedRowKeys, setExpandedRowKeys] = useState<React.Key[]>([]);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [drawerMode, setDrawerMode] = useState<DrawerMode>('create');
  const [drawerType, setDrawerType] = useState<number>(0);
  const [currentId, setCurrentId] = useState<string>();
  const [drawerLoading, setDrawerLoading] = useState(false);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [initializedExpandState, setInitializedExpandState] = useState(false);
  const [sortDirty, setSortDirty] = useState(false);
  const [sortSaving, setSortSaving] = useState(false);
  const [treeSaving, setTreeSaving] = useState(false);
  const [transferLoading, setTransferLoading] = useState<'export' | 'import'>();

  const filteredTree = useMemo(
    () => filterTreeByKeyword(treeData, searchKeyword),
    [treeData, searchKeyword],
  );

  const allExpandableKeys = useMemo(
    () => collectExpandableKeys(filteredTree),
    [filteredTree],
  );

  const flattenedNodes = useMemo(() => flattenTree(treeData), [treeData]);
  const nodeMap = useMemo(
    () => new Map(flattenedNodes.map((item) => [item.id, item])),
    [flattenedNodes],
  );
  const siblingMoveStateMap = useMemo(
    () => buildSiblingMoveStateMap(treeData),
    [treeData],
  );

  const selectedNodes = useMemo(
    () =>
      selectedRowKeys
        .map((key) => nodeMap.get(String(key)))
        .filter((item): item is PermissionTreeNode => Boolean(item)),
    [nodeMap, selectedRowKeys],
  );

  const currentNode = useMemo(
    () => findNodeById(treeData, currentId),
    [currentId, treeData],
  );

  const excludedParentIds = useMemo(
    () => collectDescendantIds(currentNode),
    [currentNode],
  );

  const parentTreeOptions = useMemo(
    () => buildParentTreeOptions(treeData, excludedParentIds),
    [excludedParentIds, treeData],
  );

  const loadTree = async (keepSelection = true) => {
    setLoading(true);
    try {
      const response = await getPermissionManagementTree();
      const nextTree = response.result || [];
      const nextIds = new Set(flattenTree(nextTree).map((item) => item.id));
      setTreeData(nextTree);
      setSelectedRowKeys((prev) =>
        keepSelection ? prev.filter((key) => nextIds.has(String(key))) : [],
      );
      setSortDirty(false);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadTree();
  }, []);

  useEffect(() => {
    if (!initializedExpandState && treeData.length) {
      setExpandedRowKeys(
        treeData.filter((item) => item.children?.length).map((item) => item.id),
      );
      setInitializedExpandState(true);
    }
  }, [initializedExpandState, treeData]);

  useEffect(() => {
    if (searchKeyword) {
      setExpandedRowKeys(allExpandableKeys);
    }
  }, [allExpandableKeys, searchKeyword]);

  const syncSuggestedSortNo = async (parentId?: string) => {
    const nextSortNo = await getPermissionNextSortNo(parentId);
    drawerForm.setFieldValue('sortNo', nextSortNo);
  };

  const openCreateDrawer = async (type: number, parent?: PermissionTreeNode) => {
    const parentId = type === 0 ? undefined : parent?.id;
    setDrawerMode('create');
    setDrawerType(type);
    setCurrentId(undefined);
    setDrawerOpen(true);
    drawerForm.resetFields();
    drawerForm.setFieldsValue({
      type,
      parentId,
      hidden: type === 2,
      alwaysShow: false,
      status: 1,
      sortNo: 10,
    });
    try {
      await syncSuggestedSortNo(parentId);
    } catch (_error) {
      // 请求失败时保留默认排序值
    }
  };

  const openEditDrawer = async (record: PermissionTreeNode) => {
    setDrawerMode('edit');
    setCurrentId(record.id);
    setDrawerType(record.type ?? 1);
    setDrawerOpen(true);
    drawerForm.resetFields();
    setDrawerLoading(true);
    try {
      const response = await getResourceDetail(PERMISSION_RESOURCE_PATH, record.id);
      const detail = response.result as GenericRecord;
      setDrawerType(Number(detail.type ?? record.type ?? 1));
      drawerForm.setFieldsValue({
        type: Number(detail.type ?? record.type ?? 1),
        parentId: detail.parentId || undefined,
        name: detail.name,
        url: detail.url || undefined,
        component: detail.component || undefined,
        perms: detail.perms || undefined,
        icon: detail.icon || undefined,
        sortNo: detail.sortNo ?? undefined,
        hidden: Number(detail.hidden || 0) === 1,
        alwaysShow: Number(detail.alwaysShow || 0) === 1,
        status: Number(detail.status ?? 1),
        remark: detail.remark || undefined,
      });
    } finally {
      setDrawerLoading(false);
    }
  };

  const closeDrawer = () => {
    setDrawerOpen(false);
    setCurrentId(undefined);
    setDrawerLoading(false);
    drawerForm.resetFields();
  };

  const handleDelete = async (record: PermissionTreeNode) => {
    await deleteResource(PERMISSION_RESOURCE_PATH, record.id);
    message.success(`已删除 ${record.name}`);
    await loadTree(false);
  };

  const confirmDelete = (record: PermissionTreeNode) => {
    modal.confirm({
      title: `删除 ${record.name}`,
      content:
        '删除前会校验是否仍存在子节点或角色授权。如果后端校验不通过，会直接返回真实原因。',
      okText: '确认删除',
      okButtonProps: {
        danger: true,
      },
      cancelText: '取消',
      onOk: async () => {
        await handleDelete(record);
      },
    });
  };

  const handleBatchDelete = () => {
    if (!selectedNodes.length) {
      return;
    }

    modal.confirm({
      title: `批量删除 ${selectedNodes.length} 项`,
      content: `即将删除：${selectedNodes
        .slice(0, 5)
        .map((item) => item.name)
        .join('、')}${selectedNodes.length > 5 ? ' 等' : ''}`,
      okText: '确认删除',
      okButtonProps: {
        danger: true,
      },
      cancelText: '取消',
      onOk: async () => {
        await deleteBatchResource(
          PERMISSION_RESOURCE_PATH,
          selectedNodes.map((item) => item.id),
        );
        message.success('批量删除成功');
        await loadTree(false);
      },
    });
  };

  const handleMove = (record: PermissionTreeNode, direction: 'up' | 'down') => {
    setTreeData((prev) => moveTreeNodeWithinLevel(prev, record.id, direction));
    setSortDirty(true);
  };

  const handleSaveSort = async () => {
    setSortSaving(true);
    try {
      await savePermissionSort(buildPermissionSortPayload(treeData));
      message.success('菜单排序已保存');
      await loadTree();
    } finally {
      setSortSaving(false);
    }
  };

  const handleSaveTree = async () => {
    setTreeSaving(true);
    try {
      const response = await savePermissionTree(buildPermissionTreeSavePayload(treeData));
      setTreeData(response.result?.tree || []);
      setSortDirty(false);
      message.success(`菜单整树已保存，共更新 ${response.result?.updatedCount || 0} 项`);
    } finally {
      setTreeSaving(false);
    }
  };

  const handleExport = async () => {
    setTransferLoading('export');
    try {
      await exportResourceExcel(
        PERMISSION_RESOURCE_PATH,
        {
          name: searchForm.getFieldValue('keyword')?.trim() || undefined,
        },
        '权限菜单',
      );
      message.success('权限菜单导出成功');
    } finally {
      setTransferLoading(undefined);
    }
  };

  const handleImport = async () => {
    const file = await pickLocalFile('.xlsx,.xls');
    if (!file) {
      return;
    }

    setTransferLoading('import');
    try {
      const response = await importResourceExcel(PERMISSION_RESOURCE_PATH, file);
      message.success(response.message || response.result || '权限菜单导入成功');
      await loadTree(false);
    } finally {
      setTransferLoading(undefined);
    }
  };

  const handleSubmit = async () => {
    const values = await drawerForm.validateFields();
    const type = Number(values.type ?? drawerType);
    const payload = {
      parentId: type === 0 ? null : values.parentId || null,
      name: values.name?.trim(),
      url: values.url?.trim() || null,
      component: type === 2 ? null : values.component?.trim() || null,
      perms: type === 2 ? values.perms?.trim() || null : values.perms?.trim() || null,
      type,
      icon: type === 2 ? null : values.icon || null,
      sortNo: values.sortNo ?? null,
      hidden: type === 2 ? 1 : values.hidden ? 1 : 0,
      alwaysShow: type === 2 ? 0 : values.alwaysShow ? 1 : 0,
      status: Number(values.status ?? 1),
      remark: values.remark?.trim() || null,
    };

    setSubmitLoading(true);
    try {
      if (drawerMode === 'create') {
        await createResource(PERMISSION_RESOURCE_PATH, payload);
        message.success('新增成功');
      } else if (currentId) {
        await updateResource(PERMISSION_RESOURCE_PATH, currentId, payload);
        message.success('保存成功');
      }
      closeDrawer();
      await loadTree(false);
    } finally {
      setSubmitLoading(false);
    }
  };

  const handleSearch = (values: SearchValues) => {
    setSearchKeyword(normalizeKeyword(values.keyword));
  };

  const handleResetSearch = () => {
    searchForm.resetFields();
    setSearchKeyword('');
    setExpandedRowKeys(treeData.filter((item) => item.children?.length).map((item) => item.id));
  };

  const handleDrawerTypeChange = async (value: number) => {
    if (drawerMode === 'edit') {
      return;
    }

    setDrawerType(value);
    drawerForm.setFieldsValue({
      type: value,
      parentId: value === 0 ? undefined : drawerForm.getFieldValue('parentId'),
      hidden: value === 2,
      alwaysShow: value === 2 ? false : drawerForm.getFieldValue('alwaysShow'),
    });
    if (value === 0) {
      drawerForm.setFieldValue('parentId', undefined);
    }
    try {
      await syncSuggestedSortNo(value === 0 ? undefined : drawerForm.getFieldValue('parentId'));
    } catch (_error) {
      // ignore next sort suggestion failures
    }
  };

  const handleParentChange = async (parentId?: string) => {
    if (drawerMode !== 'create') {
      return;
    }
    try {
      await syncSuggestedSortNo(parentId);
    } catch (_error) {
      // ignore next sort suggestion failures
    }
  };

  const columns: TableColumnsType<PermissionTreeNode> = [
    {
      title: '菜单名称',
      dataIndex: 'name',
      width: 320,
      render: (_, record) => (
        <Space size={8} wrap>
          <span
            style={{
              display: 'inline-flex',
              width: 18,
              justifyContent: 'center',
              color: '#1677ff',
            }}
          >
            {resolveIconNode(record.icon) || <AppstoreOutlined />}
          </span>
          <Text strong={record.type !== 2}>{record.name}</Text>
          {record.hidden === 1 && record.type !== 2 ? <Tag>隐藏</Tag> : null}
          {record.alwaysShow === 1 ? <Tag color="processing">常显</Tag> : null}
        </Space>
      ),
    },
    {
      title: '菜单类型',
      dataIndex: 'type',
      width: 120,
      align: 'center',
      render: (value) => <Tag color={getTypeColor(value)}>{getTypeLabel(value)}</Tag>,
    },
    {
      title: '图标',
      dataIndex: 'icon',
      width: 96,
      align: 'center',
      render: (value) =>
        value ? (
          <Tooltip title={value}>
            <span
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                width: 32,
                height: 32,
                borderRadius: 10,
                background: 'rgba(22, 119, 255, 0.08)',
                color: '#1677ff',
              }}
            >
              {resolveIconNode(value)}
            </span>
          </Tooltip>
        ) : (
          <Text type="secondary">-</Text>
        ),
    },
    {
      title: '组件/权限',
      key: 'component-perms',
      width: 220,
      render: (_, record) => {
        const primary = record.component || record.perms;
        const secondary =
          record.component && record.perms ? record.perms : undefined;

        if (!primary) {
          return <Text type="secondary">-</Text>;
        }

        return (
          <div>
            <Text ellipsis={{ tooltip: primary }} style={{ display: 'block' }}>
              {primary}
            </Text>
            {secondary ? (
              <Text
                ellipsis={{ tooltip: secondary }}
                style={{
                  display: 'block',
                  color: 'rgba(15, 23, 42, 0.6)',
                  fontSize: 12,
                  marginTop: 2,
                }}
              >
                {secondary}
              </Text>
            ) : null}
          </div>
        );
      },
    },
    {
      title: '路径',
      dataIndex: 'url',
      width: 210,
      ellipsis: true,
      render: (value) =>
        value ? (
          <Text ellipsis={{ tooltip: value }} style={{ display: 'block' }}>
            {value}
          </Text>
        ) : (
          <Text type="secondary">-</Text>
        ),
    },
    {
      title: '排序',
      dataIndex: 'sortNo',
      width: 88,
      align: 'center',
      render: (value) => value ?? '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 210,
      fixed: 'right',
      render: (_, record) => {
        const moveState = siblingMoveStateMap.get(record.id);
        const moreItems: MenuProps['items'] = [
          {
            key: 'add-submenu',
            label: '新增子菜单',
            disabled: record.type === 2,
          },
          {
            key: 'add-button',
            label: '新增按钮/权限',
            disabled: record.type === 2,
          },
          {
            type: 'divider',
          },
          {
            key: 'delete',
            label: '删除',
            danger: true,
          },
        ];

        return (
          <Space split={<span style={{ color: '#d9d9d9' }}>|</span>}>
            <Tooltip title="上移">
              <Button
                icon={<ArrowUpOutlined />}
                disabled={!moveState?.canMoveUp}
                style={{ paddingInline: 0 }}
                type="link"
                onClick={() => {
                  handleMove(record, 'up');
                }}
              />
            </Tooltip>
            <Tooltip title="下移">
              <Button
                icon={<ArrowDownOutlined />}
                disabled={!moveState?.canMoveDown}
                style={{ paddingInline: 0 }}
                type="link"
                onClick={() => {
                  handleMove(record, 'down');
                }}
              />
            </Tooltip>
            <Button
              type="link"
              icon={<EditOutlined />}
              style={{ paddingInline: 0 }}
              onClick={() => {
                void openEditDrawer(record);
              }}
            >
              编辑
            </Button>
            <Dropdown
              menu={{
                items: moreItems,
                onClick: ({ key }) => {
                  if (key === 'add-submenu') {
                    void openCreateDrawer(1, record);
                    return;
                  }
                  if (key === 'add-button') {
                    void openCreateDrawer(2, record);
                    return;
                  }
                  if (key === 'delete') {
                    confirmDelete(record);
                  }
                },
              }}
            >
              <Button type="link" icon={<MoreOutlined />} style={{ paddingInline: 0 }}>
                更多
              </Button>
            </Dropdown>
          </Space>
        );
      },
    },
  ];

  const currentNameLabel = drawerType === 2 ? '按钮/权限' : '菜单名称';
  const currentUrlLabel = drawerType === 2 ? '接口地址' : '访问路径';
  const showParentSelector = drawerType !== 0;
  const showMenuFields = drawerType !== 2;
  const selectionSummary = selectedNodes.length
    ? `已选中 ${selectedNodes.length} 项：${selectedNodes
        .slice(0, 4)
        .map((item) => item.name)
        .join('、')}${selectedNodes.length > 4 ? ' 等' : ''}`
    : '未选中任何数据';

  return (
    <PageContainer
      className="saas-page-container"
      title="权限菜单"
      content="菜单、子菜单、按钮/权限统一在一个页面内维护，树形展示并支持展开与折叠。"
      extra={[
        <Tag key="endpoint" color="blue">
          /api/permission
        </Tag>,
      ]}
    >
      <Space direction="vertical" size={16} style={{ display: 'flex' }}>
        <Card variant="borderless">
          <Form<SearchValues>
            form={searchForm}
            className="saas-inline-form"
            layout="horizontal"
            labelAlign="right"
            labelWrap={false}
            labelCol={INLINE_FORM_LABEL_COL}
            wrapperCol={INLINE_FORM_WRAPPER_COL}
            onFinish={handleSearch}
          >
            <Row gutter={16}>
              <Col xs={24} xl={12}>
                <Form.Item<SearchValues> label="菜单名称" name="keyword">
                  <Input
                    allowClear
                    placeholder="请输入菜单名称"
                    onPressEnter={() => {
                      void searchForm.submit();
                    }}
                  />
                </Form.Item>
              </Col>
              <Col xs={24} xl={12}>
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'flex-start',
                    gap: 12,
                    paddingTop: 1,
                  }}
                >
                  <Button htmlType="submit" icon={<SearchOutlined />} type="primary">
                    查询
                  </Button>
                  <Button icon={<ReloadOutlined />} onClick={handleResetSearch}>
                    重置
                  </Button>
                </div>
              </Col>
            </Row>
          </Form>
        </Card>

        <Card variant="borderless">
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              gap: 16,
              marginBottom: 16,
              flexWrap: 'wrap',
            }}
          >
            <Space wrap>
              <Button
                icon={<PlusOutlined />}
                type="primary"
                onClick={() => {
                  void openCreateDrawer(0);
                }}
              >
                新增菜单
              </Button>
              <Button
                icon={<MenuUnfoldOutlined />}
                onClick={() => {
                  setExpandedRowKeys(allExpandableKeys);
                }}
              >
                展开全部
              </Button>
              <Button
                icon={<MenuFoldOutlined />}
                onClick={() => {
                  setExpandedRowKeys([]);
                }}
              >
                折叠全部
              </Button>
              <Button
                icon={<DownloadOutlined />}
                loading={transferLoading === 'export'}
                onClick={() => {
                  void handleExport();
                }}
              >
                导出 Excel
              </Button>
              <Button
                icon={<UploadOutlined />}
                loading={transferLoading === 'import'}
                onClick={() => {
                  void handleImport();
                }}
              >
                导入 Excel
              </Button>
              <Button
                icon={<SaveOutlined />}
                loading={sortSaving}
                type={sortDirty ? 'primary' : 'default'}
                onClick={() => {
                  void handleSaveSort();
                }}
              >
                保存排序
              </Button>
              <Button
                icon={<SaveOutlined />}
                loading={treeSaving}
                onClick={() => {
                  void handleSaveTree();
                }}
              >
                整树保存
              </Button>
              {selectedNodes.length > 0 ? (
                <Button danger icon={<DeleteOutlined />} onClick={handleBatchDelete}>
                  批量删除
                </Button>
              ) : null}
            </Space>

            <Space>
              <Tooltip title="刷新数据">
                <Button
                  icon={<ReloadOutlined />}
                  onClick={() => {
                    void loadTree();
                  }}
                />
              </Tooltip>
            </Space>
          </div>

          <Alert
            message={selectionSummary}
            type={selectedNodes.length ? 'success' : 'info'}
            showIcon
            style={{ marginBottom: 16 }}
          />
          {sortDirty ? (
            <Alert
              message="当前菜单顺序已调整，记得点击“保存排序”或“整树保存”同步到后端。"
              type="warning"
              showIcon
              style={{ marginBottom: 16 }}
            />
          ) : null}

          <Table<PermissionTreeNode>
            bordered
            childrenColumnName="children"
            columns={columns}
            dataSource={filteredTree}
            loading={loading}
            pagination={false}
            rowKey="id"
            rowSelection={{
              selectedRowKeys,
              onChange: (keys) => {
                setSelectedRowKeys(keys);
              },
            }}
            expandable={{
              expandedRowKeys,
              onExpandedRowsChange: (keys) => {
                setExpandedRowKeys([...keys]);
              },
            }}
            locale={{
              emptyText: (
                <Empty
                  description={searchKeyword ? '没有匹配的菜单数据' : '暂无菜单数据'}
                  image={Empty.PRESENTED_IMAGE_SIMPLE}
                />
              ),
            }}
            scroll={{ x: 1280 }}
            size="middle"
            tableLayout="fixed"
          />
        </Card>
      </Space>

      <Drawer
        destroyOnHidden
        extra={
          drawerMode === 'edit' && currentId ? (
            <Text type="secondary">ID: {currentId}</Text>
          ) : null
        }
        footer={
          <div
            style={{
              display: 'flex',
              justifyContent: 'flex-end',
              gap: 12,
            }}
          >
            <Button onClick={closeDrawer}>取消</Button>
            <Button loading={submitLoading} type="primary" onClick={() => void handleSubmit()}>
              确认
            </Button>
          </div>
        }
        forceRender
        open={drawerOpen}
        placement="right"
        title={drawerMode === 'create' ? '新增菜单' : '编辑菜单'}
        width={980}
        onClose={closeDrawer}
      >
        <div style={{ paddingInlineEnd: 8 }}>
          <Form<PermissionFormValues>
            form={drawerForm}
            className="saas-inline-form"
            disabled={drawerLoading}
            initialValues={{
              type: 0,
              status: 1,
              hidden: false,
              alwaysShow: false,
            }}
            layout="horizontal"
            labelAlign="right"
            labelWrap={false}
            labelCol={INLINE_FORM_LABEL_COL}
            style={{
              maxWidth: '100%',
            }}
            wrapperCol={INLINE_FORM_WRAPPER_COL}
          >
            <Form.Item<PermissionFormValues> hidden name="type">
              <Input />
            </Form.Item>

            <Form.Item label="菜单类型" style={{ marginBottom: 28 }}>
              <Segmented
                block={false}
                disabled={drawerMode === 'edit'}
                onChange={(value) => {
                  void handleDrawerTypeChange(Number(value));
                }}
                options={PERMISSION_TYPE_OPTIONS}
                value={drawerType}
              />
            </Form.Item>

            <Row gutter={20}>
              <Col span={24}>
                <Form.Item<PermissionFormValues>
                  label={currentNameLabel}
                  name="name"
                  rules={[
                    {
                      required: true,
                      message: `请输入${currentNameLabel}`,
                    },
                  ]}
                >
                  <Input
                    maxLength={100}
                    placeholder={`请输入${currentNameLabel}`}
                    showCount
                  />
                </Form.Item>
              </Col>

              {showParentSelector ? (
                <Col span={24}>
                  <Form.Item<PermissionFormValues>
                    label="上级菜单"
                    name="parentId"
                    rules={[
                      {
                        required: true,
                        message: '请选择上级菜单',
                      },
                    ]}
                  >
                    <TreeSelect
                      allowClear
                      placeholder="请选择上级菜单"
                      showSearch
                      treeData={parentTreeOptions}
                      treeDefaultExpandAll
                      onChange={(value) => {
                        void handleParentChange(value as string | undefined);
                      }}
                    />
                  </Form.Item>
                </Col>
              ) : null}

              <Col span={24}>
                <Form.Item<PermissionFormValues>
                  label={currentUrlLabel}
                  name="url"
                  rules={
                    drawerType === 2
                      ? undefined
                      : [
                          {
                            required: true,
                            message: `请输入${currentUrlLabel}`,
                          },
                        ]
                  }
                >
                  <Input placeholder={`请输入${currentUrlLabel}`} />
                </Form.Item>
              </Col>

              {showMenuFields ? (
                <Col span={24}>
                  <Form.Item<PermissionFormValues>
                    label="前端组件"
                    name="component"
                    rules={[
                      {
                        required: true,
                        message: '请输入前端组件',
                      },
                    ]}
                  >
                    <Input placeholder="请输入前端组件，例如 system/user/index" />
                  </Form.Item>
                </Col>
              ) : null}

              {drawerType === 2 ? (
                <Col span={24}>
                  <Form.Item<PermissionFormValues>
                    label="授权标识"
                    name="perms"
                    rules={[
                      {
                        required: true,
                        message: '请输入授权标识',
                      },
                    ]}
                  >
                    <Input placeholder="请输入授权标识，例如 sys:permission:create" />
                  </Form.Item>
                </Col>
              ) : null}

              {showMenuFields ? (
                <Col xs={24} xl={12}>
                  <Form.Item<PermissionFormValues> label="菜单图标" name="icon">
                    <Select
                      allowClear
                      options={ICON_OPTIONS}
                      placeholder="请选择菜单图标"
                      showSearch
                    />
                  </Form.Item>
                </Col>
              ) : null}

              <Col xs={24} xl={showMenuFields ? 12 : 24}>
                <Form.Item<PermissionFormValues> label="排序" name="sortNo">
                  <InputNumber
                    min={0}
                    placeholder="请输入排序值"
                    precision={0}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
              </Col>

              {showMenuFields ? (
                <>
                  <Col xs={24} xl={12}>
                    <Form.Item<PermissionFormValues>
                      label="是否隐藏"
                      name="hidden"
                      valuePropName="checked"
                    >
                      <Switch checkedChildren="是" unCheckedChildren="否" />
                    </Form.Item>
                  </Col>
                  <Col xs={24} xl={12}>
                    <Form.Item<PermissionFormValues>
                      label="总是显示"
                      name="alwaysShow"
                      valuePropName="checked"
                    >
                      <Switch checkedChildren="是" unCheckedChildren="否" />
                    </Form.Item>
                  </Col>
                </>
              ) : null}

              <Col span={24}>
                <Form.Item<PermissionFormValues> label="状态" name="status">
                  <Radio.Group optionType="default" options={STATUS_OPTIONS} />
                </Form.Item>
              </Col>

              <Col span={24}>
                <Form.Item<PermissionFormValues> label="备注" name="remark">
                  <Input.TextArea
                    maxLength={300}
                    placeholder="请输入备注"
                    rows={4}
                    showCount
                  />
                </Form.Item>
              </Col>
            </Row>
          </Form>

          {drawerType === 2 ? (
            <Paragraph type="secondary" style={{ marginBottom: 0 }}>
              按钮/权限节点不会出现在侧边导航里，通常用于控制接口访问和页面按钮显隐。
            </Paragraph>
          ) : null}
        </div>
      </Drawer>
    </PageContainer>
  );
};

export default PermissionManagementPage;
