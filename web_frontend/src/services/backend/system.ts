import { request } from '@umijs/max';
import type {
  BackendResponse,
  BrandingConfig,
  DashboardStats,
  GenericRecord,
  MessageCenterSummary,
  MenuTree,
  OptionItem,
  PageResult,
  PermissionTreeNode,
  PermissionTreeSaveNode,
  SystemConfigGroup,
  TreeOption,
} from './types';

const toOption = (
  item: GenericRecord,
  labelField = 'label',
  valueField = 'value',
): OptionItem => ({
  label: String(
    item[labelField] ??
      item.label ??
      item.name ??
      item.title ??
      item.roleName ??
      item.departName ??
      item.dictName ??
      item.tenantName ??
      item.username ??
      item.code ??
      item.id ??
      '',
  ),
  value: String(item[valueField] ?? item.value ?? item.id ?? item.code ?? ''),
  code: item.code ? String(item.code) : undefined,
});

const toTreeOption = (item: GenericRecord): TreeOption => {
  const label = String(
    item.label ??
      item.title ??
      item.name ??
      item.roleName ??
      item.departName ??
      item.dictName ??
      item.tenantName ??
      item.id ??
      '',
  );
  const value = item.value ?? item.id ?? '';

  return {
    title: label,
    label,
    value: String(value),
    key: String(value),
    children: Array.isArray(item.children)
      ? item.children.map((child: GenericRecord) => toTreeOption(child))
      : undefined,
  };
};

export const getDashboardStats = async () =>
  request<BackendResponse<DashboardStats>>('/api/system/dashboard', {
    method: 'GET',
  });

export const getPublicBranding = async () =>
  request<BackendResponse<BrandingConfig>>('/api/systemConfig/branding', {
    method: 'GET',
  });

export const getCurrentMenus = async () =>
  request<BackendResponse<MenuTree[]>>('/api/system/menus', {
    method: 'GET',
  });

export const getSystemConfigGroups = async () =>
  request<BackendResponse<SystemConfigGroup[]>>('/api/systemConfig/groups', {
    method: 'GET',
  });

export const updateSystemConfigBatch = async (
  items: Array<{ configKey: string; configValue?: string | null }>,
) =>
  request<BackendResponse<boolean>>('/api/systemConfig/batch', {
    method: 'PUT',
    data: {
      items,
    },
  });

export const getMessageCenterSummary = async () =>
  request<BackendResponse<MessageCenterSummary>>(
    '/api/system/message-center/summary',
    {
      method: 'GET',
    },
  );

export const getUnreadAnnouncementCount = async () =>
  request<BackendResponse<number>>('/api/announcement/unreadCount', {
    method: 'GET',
  });

export const queryAnnouncementInboxPage = async (params: {
  pageNo?: number;
  pageSize?: number;
  unreadOnly?: boolean;
}) =>
  request<BackendResponse<PageResult<GenericRecord>>>(
    '/api/announcement/inbox/page',
    {
      method: 'GET',
      params,
    },
  );

export const getAnnouncementDetail = async (
  id: string | number,
  autoRead = true,
) =>
  request<BackendResponse<GenericRecord>>(`/api/announcement/${id}`, {
    method: 'GET',
    params: {
      autoRead,
    },
  });

export const markAnnouncementRead = async (id: string | number) =>
  request<BackendResponse<boolean>>(`/api/announcement/${id}/read`, {
    method: 'POST',
  });

export const getRoleOptions = async () => {
  const response = await request<BackendResponse<GenericRecord[]>>(
    '/api/role/options',
    {
      method: 'GET',
    },
  );
  return response.result.map((item: GenericRecord) => toOption(item));
};

export const getDepartOptions = async () => {
  const response = await request<BackendResponse<GenericRecord[]>>(
    '/api/depart/options',
    {
      method: 'GET',
    },
  );
  return response.result.map((item: GenericRecord) => toOption(item));
};

export const getDepartTree = async () => {
  const response = await request<BackendResponse<GenericRecord[]>>(
    '/api/depart/tree',
    {
      method: 'GET',
    },
  );
  return response.result.map((item: GenericRecord) => toTreeOption(item));
};

export const getPermissionTree = async () => {
  const response = await request<BackendResponse<GenericRecord[]>>(
    '/api/permission/tree',
    {
      method: 'GET',
    },
  );
  return response.result.map((item: GenericRecord) => toTreeOption(item));
};

export const getPermissionManagementTree = async () =>
  request<BackendResponse<PermissionTreeNode[]>>('/api/permission/tree', {
    method: 'GET',
  });

export const getPermissionNextSortNo = async (parentId?: string) => {
  const response = await request<BackendResponse<number>>(
    '/api/permission/nextSortNo',
    {
      method: 'GET',
      params: parentId ? { parentId } : undefined,
    },
  );
  return response.result;
};

export const savePermissionTree = async (tree: PermissionTreeSaveNode[]) =>
  request<BackendResponse<{ updatedCount?: number; tree: PermissionTreeNode[] }>>(
    '/api/permission/tree/save',
    {
      method: 'POST',
      data: tree,
    },
  );

export const savePermissionSort = async (
  items: Array<{ id: string; parentId?: string | null; sortNo: number }>,
) =>
  request<BackendResponse<boolean>>('/api/permission/sort', {
    method: 'POST',
    data: items,
  });

export const getRolePermissionIds = async (id: string | number) => {
  const response = await request<BackendResponse<string[]>>(
    `/api/role/${id}/permissionIds`,
    {
      method: 'GET',
    },
  );
  return response.result;
};

export const saveRolePermissionIds = async (
  id: string | number,
  permissionIds: Array<string | number>,
) =>
  request<BackendResponse<boolean>>(`/api/role/${id}/permissionIds`, {
    method: 'PUT',
    data: {
      permissionIds: permissionIds.map(String),
    },
  });

export const publishAnnouncement = async (id: string | number) =>
  request<BackendResponse<boolean>>(`/api/announcement/${id}/publish`, {
    method: 'POST',
  });

export const revokeAnnouncement = async (id: string | number) =>
  request<BackendResponse<boolean>>(`/api/announcement/${id}/revoke`, {
    method: 'POST',
  });

export const triggerQuartzJob = async (id: string | number) =>
  request<BackendResponse<boolean>>(`/api/quartzJob/${id}/trigger`, {
    method: 'POST',
  });

export const getUserAggregate = async (id: string | number) =>
  request<BackendResponse<GenericRecord>>(`/api/user/${id}/aggregate`, {
    method: 'GET',
  });

export const resetUserPassword = async (
  id: string | number,
  password: string,
) =>
  request<BackendResponse<{ success: boolean; message: string }>>(
    `/api/user/${id}/password`,
    {
      method: 'PUT',
      data: {
        password,
      },
    },
  );

export const lockUser = async (
  id: string | number,
  lockUntil: string,
) =>
  request<BackendResponse<boolean>>(`/api/user/${id}/lock`, {
    method: 'PUT',
    data: {
      lockUntil,
    },
  });

export const unlockUser = async (id: string | number) =>
  request<BackendResponse<boolean>>(`/api/user/${id}/unlock`, {
    method: 'PUT',
  });

export const queryPageOptions = async (
  resourcePath: string,
  labelField: string,
  valueField = 'id',
  extraParams?: Record<string, any>,
) => {
  const response = await request<BackendResponse<PageResult<GenericRecord>>>(
    `${resourcePath}/page`,
    {
      method: 'GET',
      params: {
        pageNo: 1,
        pageSize: 200,
        ...(extraParams || {}),
      },
    },
  );

  return (response.result.records || []).map((item: GenericRecord) =>
    toOption(item, labelField, valueField),
  );
};
