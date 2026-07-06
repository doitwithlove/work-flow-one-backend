import axios from 'axios';

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api';
const SESSION_STORAGE_KEY = 'work-flow-one-session';

function clearSession() {
  try {
    sessionStorage.removeItem(SESSION_STORAGE_KEY);
  } catch {
    // Ignore storage failures in private mode or restricted environments.
  }

  delete apiClient.defaults.headers.common.Authorization;
}

export const apiClient = axios.create({
  baseURL: apiBaseUrl,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: false,
});

apiClient.interceptors.request.use((config) => {
  config.headers = config.headers ?? {};
  config.headers['Cache-Control'] = 'no-store, no-cache, max-age=0, must-revalidate';
  config.headers.Pragma = 'no-cache';
  config.headers.Expires = '0';

  const url = String(config.url ?? '');
  const isAuthRequest =
    url.includes('/auth/login') ||
    url.includes('/auth/register') ||
    url.includes('/auth/refresh') ||
    url.includes('/auth/me');

  const stored = sessionStorage.getItem(SESSION_STORAGE_KEY);
  if (stored && !config.headers?.Authorization && !isAuthRequest) {
    try {
      const parsed = JSON.parse(stored) as { accessToken?: string };
      if (parsed.accessToken) {
        config.headers.Authorization = `Bearer ${parsed.accessToken}`;
      }
    } catch {
      // Ignore malformed session data and continue without auth.
    }
  }

  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const url = String(error.config?.url ?? '');
    const isAuthRequest =
      url.includes('/auth/login') ||
      url.includes('/auth/register') ||
      url.includes('/auth/refresh') ||
      url.includes('/auth/me');

    if (status === 401 && !isAuthRequest) {
      clearSession();
      if (window.location.pathname !== '/login') {
        window.location.assign('/login');
      }
    }

    if (error && typeof error === 'object') {
      const method = String(error.config?.method ?? 'GET').toUpperCase();
      if (url && typeof error.message === 'string' && !error.message.includes(url)) {
        error.message = `${error.message} (${method} ${url})`;
      }
    }

    return Promise.reject(error);
  },
);
