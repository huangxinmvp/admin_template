import { request } from '@umijs/max';
import { clearAuthSession, setAuthSession } from '@/utils/auth';
import type {
  AuthPayload,
  BackendResponse,
  CurrentUserProfile,
  UserSession,
} from './types';

export type LoginParams = {
  username: string;
  password: string;
};

export type RegisterParams = {
  tenantId?: string;
  username: string;
  password: string;
  realname?: string;
};

export type ChangePasswordParams = {
  oldPassword: string;
  newPassword: string;
};

export const login = async (body: LoginParams) =>
  request<BackendResponse<AuthPayload>>('/api/auth/login', {
    method: 'POST',
    data: body,
  });

export const registerUser = async (body: RegisterParams) =>
  request<BackendResponse<AuthPayload>>('/api/auth/register', {
    method: 'POST',
    data: body,
  });

export const refreshAuthToken = async (refreshToken: string) =>
  request<BackendResponse<AuthPayload>>('/api/auth/refresh', {
    method: 'POST',
    params: {
      token: refreshToken,
    },
  });

export const getCurrentProfile = async () =>
  request<BackendResponse<CurrentUserProfile>>('/api/system/profile', {
    method: 'GET',
  });

export const changeCurrentPassword = async (body: ChangePasswordParams) =>
  request<BackendResponse<{ success: boolean; message: string }>>(
    '/api/auth/password',
    {
      method: 'PUT',
      data: body,
    },
  );

export const logoutCurrentSession = async () =>
  request<BackendResponse<boolean>>('/api/auth/logout', {
    method: 'POST',
  });

export const getCurrentUserSessions = async () =>
  request<BackendResponse<UserSession[]>>('/api/auth/sessions', {
    method: 'GET',
  });

export const revokeUserSession = async (sessionId: string) =>
  request<BackendResponse<boolean>>(`/api/auth/sessions/${sessionId}`, {
    method: 'DELETE',
  });

export const forceLogoutUserSessions = async (userId: string | number) =>
  request<BackendResponse<number>>(`/api/auth/sessions/users/${userId}/force-logout`, {
    method: 'POST',
  });

export const getUserSessionCounts = async (userIds: string[]) =>
  request<BackendResponse<Record<string, number>>>('/api/auth/sessions/user-counts', {
    method: 'GET',
    params: {
      userIds,
    },
    paramsSerializer: (params: { userIds?: string[] }) =>
      (params.userIds || [])
        .map((value) => `userIds=${encodeURIComponent(value)}`)
        .join('&'),
  });

export const persistAuthSession = (payload: AuthPayload) => {
  setAuthSession({
    accessToken: payload.accessToken,
    refreshToken: payload.refreshToken,
    expiresIn: payload.expiresIn,
    expiresAt: Date.now() + payload.expiresIn * 1000,
    sessionId: payload.sessionId,
  });
};

export const logout = async () => {
  try {
    await logoutCurrentSession();
  } catch (_error) {
    // Ignore logout API failures and fall back to clearing local state.
  }
  clearAuthSession();
};

export const toCurrentUser = (
  profile: CurrentUserProfile,
  unreadCount = 0,
): API.CurrentUser => {
  const roles = profile.roles || [];
  return {
    userId: profile.userId,
    username: profile.username,
    name: profile.realname || profile.username,
    realname: profile.realname,
    avatar: profile.avatar,
    email: profile.email,
    phone: profile.phone,
    tenantId: profile.tenantId,
    tenantCode: profile.tenantCode,
    tenantName: profile.tenantName,
    roles,
    depts: profile.depts || [],
    permissions: profile.permissions || [],
    unreadCount,
    access: roles.includes('ADMIN') ? 'admin' : 'user',
  };
};
