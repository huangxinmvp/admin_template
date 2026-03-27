import { LinkOutlined } from '@ant-design/icons';
import type { Settings as LayoutSettings } from '@ant-design/pro-components';
import { SettingDrawer } from '@ant-design/pro-components';
import { history, Link } from '@umijs/max';
import React from 'react';
import {
  AvatarDropdown,
  AvatarName,
  Footer,
  Question,
  SelectLang,
} from '@/components';
import {
  getCurrentProfile,
  toCurrentUser,
} from '@/services/backend/auth';
import {
  getCurrentMenus,
  getPublicBranding,
  getUnreadAnnouncementCount,
} from '@/services/backend/system';
import type { BrandingConfig, MenuTree } from '@/services/backend/types';
import { clearAuthSession, hasAuthSession } from '@/utils/auth';
import { DEFAULT_BRANDING, normalizeBranding } from '@/utils/branding';
import defaultSettings from '../config/defaultSettings';
import { errorConfig } from './requestErrorConfig';
import '@ant-design/v5-patch-for-react-19';

const isDev =
  process.env.NODE_ENV === 'development' || process.env.CI;
const loginPath = '/user/login';
const authPaths = [loginPath, '/user/register', '/user/register-result'];

const MENU_PATH_ALIAS_MAP: Record<string, string> = {
  '/system/tenant': '/system/tenants',
  '/system/user': '/system/users',
  '/system/role': '/system/roles',
  '/system/permission': '/system/permissions',
  '/system/depart': '/system/departs',
  '/system/announcement': '/system/announcements',
  '/system/file': '/system/files',
  '/system/job': '/system/jobs',
};

const normalizeBackendMenuPath = (path?: string | null) => {
  if (!path) {
    return '';
  }
  return MENU_PATH_ALIAS_MAP[path] || path;
};

const flattenMenuTree = (menus: MenuTree[]) => {
  const map = new Map<string, MenuTree>();
  const walk = (items: MenuTree[]) => {
    items.forEach((item) => {
      const normalizedPath = normalizeBackendMenuPath(item.path);
      if (normalizedPath) {
        map.set(normalizedPath, item);
      }
      if (item.children?.length) {
        walk(item.children);
      }
    });
  };
  walk(menus);
  return map;
};

const shouldUseBackendMenu = (path?: string) => {
  if (!path) {
    return false;
  }
  return (
    path === '/dashboard' ||
    path.startsWith('/system') ||
    path.startsWith('/workflow')
  );
};

const mergeMenuDataWithBackend = (menuData: any[], backendMenus: MenuTree[]) => {
  if (!backendMenus?.length) {
    return menuData;
  }

  const backendMap = flattenMenuTree(backendMenus);

  const walk = (items: any[]): any[] =>
    items.reduce<any[]>((acc, item) => {
      const path = item.path || '';
      const backendNode = backendMap.get(path);
      const nextChildren = Array.isArray(item.children) ? walk(item.children) : [];

      if (shouldUseBackendMenu(path) && path !== '/dashboard' && path !== '/system' && path !== '/workflow' && !backendNode) {
        return acc;
      }

      if ((path === '/system' || path === '/workflow') && nextChildren.length === 0) {
        return acc;
      }

      const nextItem = {
        ...item,
        ...(backendNode
          ? {
              name: backendNode.name || item.name,
              icon: backendNode.icon || item.icon,
              hideInMenu: Number(backendNode.hidden) === 1 || item.hideInMenu,
            }
          : {}),
      };

      if (nextChildren.length) {
        nextItem.children = nextChildren;
      } else {
        delete nextItem.children;
      }

      acc.push(nextItem);
      return acc;
    }, []);

  return walk(menuData);
};

/**
 * @see https://umijs.org/docs/api/runtime-config#getinitialstate
 * */
export async function getInitialState(): Promise<{
  settings?: Partial<LayoutSettings>;
  currentUser?: API.CurrentUser;
  branding: BrandingConfig;
  menuTree?: MenuTree[];
  loading?: boolean;
  fetchUserInfo?: () => Promise<API.CurrentUser | undefined>;
}> {
  const fetchUserInfo = async () => {
    if (!hasAuthSession()) {
      return undefined;
    }
    try {
      const [profileResponse, unreadResponse] = await Promise.all([
        getCurrentProfile(),
        getUnreadAnnouncementCount().catch(() => undefined),
      ]);
      return toCurrentUser(
        profileResponse.result,
        unreadResponse?.result || 0,
      );
    } catch (_error) {
      clearAuthSession();
      if (!authPaths.includes(window.location.pathname)) {
        history.push(loginPath);
      }
    }
    return undefined;
  };
  // 如果不是登录页面，执行
  const brandingResponse = await getPublicBranding().catch(() => undefined);
  const branding = normalizeBranding(brandingResponse?.result || DEFAULT_BRANDING);

  if (!authPaths.includes(window.location.pathname)) {
    const [currentUser, menuResponse] = await Promise.all([
      fetchUserInfo(),
      getCurrentMenus().catch(() => undefined),
    ]);
    return {
      fetchUserInfo,
      branding,
      currentUser,
      menuTree: menuResponse?.result || [],
      settings: defaultSettings as Partial<LayoutSettings>,
    };
  }
  return {
    fetchUserInfo,
    branding,
    settings: defaultSettings as Partial<LayoutSettings>,
  };
}

// ProLayout 支持的api https://procomponents.ant.design/components/layout
export const layout = ({
  initialState,
  setInitialState,
}: any) => {
  return {
    actionsRender: () => [
      <Question key="doc" />,
      <SelectLang key="SelectLang" />,
    ],
    avatarProps: {
      src: initialState?.currentUser?.avatar,
      title: <AvatarName />,
      render: (_: React.ReactNode, avatarChildren: React.ReactNode) => {
        return <AvatarDropdown>{avatarChildren}</AvatarDropdown>;
      },
    },
    title: initialState?.branding?.siteName || defaultSettings.title,
    logo: initialState?.branding?.logoUrl || defaultSettings.logo,
    footerRender: () => <Footer />,
    onPageChange: () => {
      // 如果没有登录，重定向到 login
      if (
        !initialState?.currentUser &&
        !authPaths.includes(window.location.pathname)
      ) {
        history.push(loginPath);
      }
    },
    bgLayoutImgList: [
      {
        src: 'https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/D2LWSqNny4sAAAAAAAAAAAAAFl94AQBr',
        left: 85,
        bottom: 100,
        height: '303px',
      },
      {
        src: 'https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/C2TWRpJpiC0AAAAAAAAAAAAAFl94AQBr',
        bottom: -68,
        right: -45,
        height: '303px',
      },
      {
        src: 'https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/F6vSTbj8KpYAAAAAAAAAAAAAFl94AQBr',
        bottom: 0,
        left: 0,
        width: '331px',
      },
    ],
    links: isDev
      ? [
          <Link key="openapi" to="/umi/plugin/openapi" target="_blank">
            <LinkOutlined />
            <span>OpenAPI 文档</span>
          </Link>,
          <a
            href={
              process.env.REACT_APP_API_BASE_URL
                ? `${process.env.REACT_APP_API_BASE_URL}/swagger-ui/index.html`
                : '/swagger-ui/index.html'
            }
            key="swagger"
            rel="noreferrer"
            target="_blank"
          >
            <LinkOutlined />
            <span>后端 Swagger</span>
          </a>,
        ]
      : [],
    menuHeaderRender: undefined,
    menuDataRender: (menuData: any[]) =>
      mergeMenuDataWithBackend(menuData, initialState?.menuTree || []),
    // 自定义 403 页面
    // unAccessible: <div>unAccessible</div>,
    // 增加一个 loading 的状态
    childrenRender: (children: React.ReactNode) => {
      // if (initialState?.loading) return <PageLoading />;
      return (
        <>
          {children}
          {isDev && (
            <SettingDrawer
              disableUrlParams
              enableDarkTheme
              settings={initialState?.settings}
              onSettingChange={(settings) => {
                setInitialState((preInitialState: any) => ({
                  ...preInitialState,
                  settings,
                }));
              }}
            />
          )}
        </>
      );
    },
    ...initialState?.settings,
  };
};

/**
 * @name request 配置，可以配置错误处理
 * 它基于 axios 和 ahooks 的 useRequest 提供了一套统一的网络请求和错误处理方案。
 * @doc https://umijs.org/docs/max/request#配置
 */
export const request: Record<string, any> = {
  baseURL: process.env.REACT_APP_API_BASE_URL || '',
  ...errorConfig,
};
