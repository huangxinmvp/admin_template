import { request } from '@umijs/max';
import { getAccessToken } from '@/utils/auth';
import type {
  BackendResponse,
  GenericRecord,
  PageResult,
} from './types';

const getApiBaseUrl = () => process.env.REACT_APP_API_BASE_URL || '';

const buildApiUrl = (path: string) => {
  if (/^https?:\/\//.test(path)) {
    return path;
  }
  return `${getApiBaseUrl()}${path}`;
};

const buildAuthHeaders = (): Record<string, string> => {
  const token = getAccessToken();
  return token
    ? {
        Authorization: `Bearer ${token}`,
      }
    : {};
};

const parseBackendMessage = async (response: Response) => {
  try {
    const data = (await response.clone().json()) as BackendResponse<any>;
    return data.message || data.result || `请求失败，状态码：${response.status}`;
  } catch (_error) {
    return `请求失败，状态码：${response.status}`;
  }
};

const parseFileName = (contentDisposition?: string | null) => {
  if (!contentDisposition) {
    return undefined;
  }

  const encodedMatch = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
  if (encodedMatch?.[1]) {
    return decodeURIComponent(encodedMatch[1]);
  }

  const plainMatch = contentDisposition.match(/filename="?([^"]+)"?/i);
  return plainMatch?.[1];
};

const triggerBlobDownload = (blob: Blob, fileName: string) => {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  link.click();
  window.setTimeout(() => {
    URL.revokeObjectURL(url);
  }, 1000);
};

export const pickLocalFile = (accept = '*') =>
  new Promise<File | undefined>((resolve) => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = accept;
    input.style.display = 'none';
    document.body.appendChild(input);

    input.addEventListener(
      'change',
      () => {
        const file = input.files?.[0];
        input.remove();
        resolve(file);
      },
      { once: true },
    );

    input.click();
  });

export const pickLocalFiles = (accept = '*') =>
  new Promise<File[]>((resolve) => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = accept;
    input.multiple = true;
    input.style.display = 'none';
    document.body.appendChild(input);

    input.addEventListener(
      'change',
      () => {
        const files = Array.from(input.files || []);
        input.remove();
        resolve(files);
      },
      { once: true },
    );

    input.click();
  });

export const queryResourcePage = async (
  resourcePath: string,
  params: Record<string, any>,
) =>
  request<BackendResponse<PageResult<GenericRecord>>>(
    `${resourcePath}/page`,
    {
      method: 'GET',
      params,
    },
  );

export const getResourceDetail = async (
  resourcePath: string,
  id: string | number,
  params?: Record<string, any>,
) =>
  request<BackendResponse<GenericRecord>>(`${resourcePath}/${id}`, {
    method: 'GET',
    params,
  });

export const createResource = async (
  resourcePath: string,
  data: Record<string, any>,
) =>
  request<BackendResponse<GenericRecord>>(resourcePath, {
    method: 'POST',
    data,
  });

export const updateResource = async (
  resourcePath: string,
  id: string | number,
  data: Record<string, any>,
) =>
  request<BackendResponse<boolean>>(`${resourcePath}/${id}`, {
    method: 'PUT',
    data,
  });

export const deleteResource = async (
  resourcePath: string,
  id: string | number,
) =>
  request<BackendResponse<boolean>>(`${resourcePath}/${id}`, {
    method: 'DELETE',
  });

export const deleteBatchResource = async (
  resourcePath: string,
  ids: Array<string | number>,
) =>
  request<BackendResponse<boolean>>(resourcePath, {
    method: 'DELETE',
    params: {
      ids: ids.join(','),
    },
  });

export const exportResourceExcel = async (
  resourcePath: string,
  params: Record<string, any>,
  fileNameHint: string,
) => {
  const query = new URLSearchParams();
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return;
    }
    query.set(key, String(value));
  });

  const response = await fetch(
    `${buildApiUrl(`${resourcePath}/exportXls`)}${query.toString() ? `?${query.toString()}` : ''}`,
    {
      method: 'GET',
      headers: {
        ...buildAuthHeaders(),
      },
    },
  );

  if (!response.ok) {
    throw new Error(await parseBackendMessage(response));
  }

  const blob = await response.blob();
  triggerBlobDownload(
    blob,
    parseFileName(response.headers.get('content-disposition')) ||
      `${fileNameHint}.xlsx`,
  );
};

export const importResourceExcel = async (resourcePath: string, file: File) => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch(buildApiUrl(`${resourcePath}/importExcel`), {
    method: 'POST',
    headers: {
      ...buildAuthHeaders(),
    },
    body: formData,
  });

  const data = (await response.json()) as BackendResponse<string>;
  if (!response.ok || !data.success) {
    throw new Error(data.message || '导入失败');
  }
  return data;
};

export const uploadResourceBinary = async (
  path: string,
  file: File,
  extraFields?: Record<string, string | undefined>,
) => {
  const formData = new FormData();
  formData.append('file', file);
  Object.entries(extraFields || {}).forEach(([key, value]) => {
    if (value) {
      formData.append(key, value);
    }
  });

  const response = await fetch(buildApiUrl(path), {
    method: 'POST',
    headers: {
      ...buildAuthHeaders(),
    },
    body: formData,
  });

  const data = (await response.json()) as BackendResponse<GenericRecord>;
  if (!response.ok || !data.success) {
    throw new Error(data.message || '上传失败');
  }
  return data;
};

export const previewProtectedResource = async (
  path: string,
  fileNameHint = 'preview',
) => {
  const response = await fetch(buildApiUrl(path), {
    method: 'GET',
    headers: {
      ...buildAuthHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(await parseBackendMessage(response));
  }

  const blob = await response.blob();
  const objectUrl = URL.createObjectURL(blob);
  const newWindow = window.open(objectUrl, '_blank', 'noopener,noreferrer');

  if (!newWindow) {
    triggerBlobDownload(
      blob,
      parseFileName(response.headers.get('content-disposition')) || fileNameHint,
    );
  }

  window.setTimeout(() => {
    URL.revokeObjectURL(objectUrl);
  }, 60_000);
};

export const downloadProtectedResource = async (
  path: string,
  fileNameHint = 'download',
) => {
  const response = await fetch(buildApiUrl(path), {
    method: 'GET',
    headers: {
      ...buildAuthHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(await parseBackendMessage(response));
  }

  const blob = await response.blob();
  triggerBlobDownload(
    blob,
    parseFileName(response.headers.get('content-disposition')) || fileNameHint,
  );
};
