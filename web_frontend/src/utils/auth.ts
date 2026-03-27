export type StoredAuthSession = {
  accessToken?: string;
  refreshToken?: string;
  expiresIn?: number;
  expiresAt?: number;
  sessionId?: string;
};

const ACCESS_TOKEN_KEY = 'admin_template_access_token';
const REFRESH_TOKEN_KEY = 'admin_template_refresh_token';
const EXPIRES_IN_KEY = 'admin_template_expires_in';
const EXPIRES_AT_KEY = 'admin_template_expires_at';
const SESSION_ID_KEY = 'admin_template_session_id';

export const getAccessToken = () => localStorage.getItem(ACCESS_TOKEN_KEY) || '';

export const getRefreshToken = () =>
  localStorage.getItem(REFRESH_TOKEN_KEY) || '';

export const getExpiresIn = () => {
  const value = localStorage.getItem(EXPIRES_IN_KEY);
  return value ? Number(value) : undefined;
};

export const getExpiresAt = () => {
  const value = localStorage.getItem(EXPIRES_AT_KEY);
  return value ? Number(value) : undefined;
};

export const getSessionId = () => localStorage.getItem(SESSION_ID_KEY) || '';

export const isAccessTokenExpiringSoon = (thresholdMs = 60_000) => {
  const expiresAt = getExpiresAt();
  if (!expiresAt) {
    return false;
  }
  return Date.now() + thresholdMs >= expiresAt;
};

export const setAuthSession = (session: StoredAuthSession) => {
  if (session.accessToken) {
    localStorage.setItem(ACCESS_TOKEN_KEY, session.accessToken);
  }
  if (session.refreshToken) {
    localStorage.setItem(REFRESH_TOKEN_KEY, session.refreshToken);
  }
  if (typeof session.expiresIn === 'number') {
    localStorage.setItem(EXPIRES_IN_KEY, String(session.expiresIn));
    localStorage.setItem(
      EXPIRES_AT_KEY,
      String(
        typeof session.expiresAt === 'number'
          ? session.expiresAt
          : Date.now() + session.expiresIn * 1000,
      ),
    );
  } else if (typeof session.expiresAt === 'number') {
    localStorage.setItem(EXPIRES_AT_KEY, String(session.expiresAt));
  }
  if (session.sessionId) {
    localStorage.setItem(SESSION_ID_KEY, session.sessionId);
  }
};

export const clearAuthSession = () => {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(EXPIRES_IN_KEY);
  localStorage.removeItem(EXPIRES_AT_KEY);
  localStorage.removeItem(SESSION_ID_KEY);
};

export const hasAuthSession = () => Boolean(getAccessToken());
