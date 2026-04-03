import type { RequestOptions } from '@@/plugin-request/request';
import { history } from '@umijs/max';
import { message, notification } from 'antd';
import {
  clearAuthSession,
  getAccessToken,
  getRefreshToken,
  isAccessTokenExpiringSoon,
  setAuthSession,
} from '@/utils/auth';

enum ErrorShowType {
  SILENT = 0,
  WARN_MESSAGE = 1,
  ERROR_MESSAGE = 2,
  NOTIFICATION = 3,
  REDIRECT = 9,
}

interface ResponseStructure {
  success?: boolean;
  code?: number;
  message?: string;
  errorKey?: string;
  result?: any;
  data?: any;
  errorCode?: number;
  errorMessage?: string;
  showType?: ErrorShowType;
}

let refreshPromise: Promise<string | undefined> | null = null;
const REQUEST_ID_HEADER = 'X-Request-Id';

const getApiBaseUrl = () => process.env.REACT_APP_API_BASE_URL || '';

const createRequestId = () => {
  if (typeof globalThis !== 'undefined' && globalThis.crypto?.randomUUID) {
    return globalThis.crypto.randomUUID();
  }
  return `req-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
};

const getResponseRequestId = (response: any) => {
  const headers = response?.headers;
  if (!headers) {
    return undefined;
  }
  if (typeof headers.get === 'function') {
    return headers.get(REQUEST_ID_HEADER) || headers.get(REQUEST_ID_HEADER.toLowerCase()) || undefined;
  }
  return headers[REQUEST_ID_HEADER] || headers[REQUEST_ID_HEADER.toLowerCase()];
};

const withRequestId = (messageText: string, response?: any) => {
  const requestId = getResponseRequestId(response);
  return requestId ? `${messageText}（请求ID: ${requestId}）` : messageText;
};

const buildApiUrl = (path: string) => {
  if (/^https?:\/\//.test(path)) {
    return path;
  }
  return `${getApiBaseUrl()}${path}`;
};

const redirectToLogin = () => {
  const { pathname, search } = window.location;
  if (pathname !== '/user/login') {
    history.replace(
      `/user/login?${new URLSearchParams({
        redirect: `${pathname}${search}`,
      }).toString()}`,
    );
  }
};

const refreshAccessToken = async () => {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    return undefined;
  }

  if (!refreshPromise) {
    refreshPromise = (async () => {
      try {
        const response = await fetch(
          `${buildApiUrl('/api/auth/refresh')}?${new URLSearchParams({
            token: refreshToken,
          }).toString()}`,
          {
            method: 'POST',
            headers: {
              Accept: 'application/json',
              [REQUEST_ID_HEADER]: createRequestId(),
            },
          },
        );

        if (!response.ok) {
          throw new Error(`刷新令牌失败，状态码：${response.status}`);
        }

        const data = (await response.json()) as ResponseStructure;
        if (!data?.success || !data.result?.accessToken) {
          throw new Error(data?.message || '刷新令牌失败');
        }

        setAuthSession({
          accessToken: data.result.accessToken,
          refreshToken: data.result.refreshToken,
          expiresIn: data.result.expiresIn,
          sessionId: data.result.sessionId,
        });
        return String(data.result.accessToken);
      } catch (_error) {
        clearAuthSession();
        return undefined;
      } finally {
        refreshPromise = null;
      }
    })();
  }

  return refreshPromise;
};

export const errorConfig = {
  errorConfig: {
    errorThrower: (res: ResponseStructure) => {
      const { success, code, message: errorMessage, errorKey } = res;
      if (success === false) {
        const error: any = new Error(errorMessage);
        error.name = 'BizError';
        error.info = {
          errorCode: code,
          errorMessage,
          showType: ErrorShowType.ERROR_MESSAGE,
          data: res,
          errorKey,
        };
        throw error;
      }
    },
    errorHandler: (error: any, opts: any) => {
      if (opts?.skipErrorHandler) throw error;
      if (error.name === 'BizError') {
        const errorInfo: ResponseStructure | undefined = error.info;
        if (errorInfo) {
          const { errorMessage, errorCode } = errorInfo;
          switch (errorInfo.showType) {
            case ErrorShowType.SILENT:
              break;
            case ErrorShowType.WARN_MESSAGE:
              message.warning(errorMessage);
              break;
            case ErrorShowType.ERROR_MESSAGE:
              message.error(withRequestId(errorMessage || '请求失败', error.response));
              break;
            case ErrorShowType.NOTIFICATION:
              notification.open({
                description: withRequestId(errorMessage || '', error.response),
                message: errorCode,
              });
              break;
            case ErrorShowType.REDIRECT:
              break;
            default:
              message.error(withRequestId(errorMessage || '请求失败', error.response));
          }
        }
      } else if (error.response) {
        if (error.response.status === 401) {
          const responseMessage =
            error.response?.data?.message || error.response?.data?.errorMessage;
          clearAuthSession();
          redirectToLogin();
          message.error(withRequestId(responseMessage || '登录已失效，请重新登录', error.response));
          return;
        }
        if (error.response.status === 403) {
          message.error(withRequestId('当前账号没有访问权限', error.response));
          return;
        }
        message.error(withRequestId(`请求失败，状态码：${error.response.status}`, error.response));
      } else if (error.request) {
        message.error('服务暂时不可用，请稍后重试');
      } else {
        message.error('请求异常，请稍后重试');
      }
    },
  },

  requestInterceptors: [
    async (config: RequestOptions) => {
      const requestUrl = config.url || '';
      const shouldSkipAuth =
        requestUrl.includes('/api/auth/login') ||
        requestUrl.includes('/api/auth/register') ||
        requestUrl.includes('/api/auth/refresh');
      const headers = {
        ...(config.headers || {}),
      };
      if (!headers[REQUEST_ID_HEADER] && !headers[REQUEST_ID_HEADER.toLowerCase()]) {
        Object.assign(headers, {
          [REQUEST_ID_HEADER]: createRequestId(),
        });
      }

      let token = getAccessToken();
      if (!shouldSkipAuth && token && isAccessTokenExpiringSoon()) {
        token = (await refreshAccessToken()) || token;
      }

      if (token && !shouldSkipAuth) {
        Object.assign(headers, {
          Authorization: `Bearer ${token}`,
        });
      }

      return {
        ...config,
        headers,
      };
    },
  ],

  responseInterceptors: [
    (response: any) => {
      const data = response.data as ResponseStructure;

      if (data?.success === false) {
        message.error(data.message || '请求失败');
      }
      return response;
    },
  ],
};
