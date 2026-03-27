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

const getApiBaseUrl = () => process.env.REACT_APP_API_BASE_URL || '';

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
              message.error(errorMessage);
              break;
            case ErrorShowType.NOTIFICATION:
              notification.open({
                description: errorMessage,
                message: errorCode,
              });
              break;
            case ErrorShowType.REDIRECT:
              break;
            default:
              message.error(errorMessage);
          }
        }
      } else if (error.response) {
        if (error.response.status === 401) {
          const responseMessage =
            error.response?.data?.message || error.response?.data?.errorMessage;
          clearAuthSession();
          redirectToLogin();
          message.error(responseMessage || '登录已失效，请重新登录');
          return;
        }
        if (error.response.status === 403) {
          message.error('当前账号没有访问权限');
          return;
        }
        message.error(`请求失败，状态码：${error.response.status}`);
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
