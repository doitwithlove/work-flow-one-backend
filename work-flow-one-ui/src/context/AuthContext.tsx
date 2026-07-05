/* eslint-disable react-refresh/only-export-components */
import { createContext, FormEvent, ReactNode, useContext, useState } from 'react';
import axios, { AxiosRequestConfig } from 'axios';
import { ApiResponse } from '../types/ApiResponse';
import { Notice } from '../types/Notice';
import { Session } from '../types/Session';
import { TokenResponse } from '../types/TokenResponse';
import { UserResponse } from '../types/UserResponse';

type AuthMode = 'login' | 'register';

type AuthContextValue = {
  mode: AuthMode;
  setMode: (mode: AuthMode) => void;
  session: Session | null;
  profile: UserResponse | null;
  adminUsers: UserResponse[];
  notice: Notice;
  busy: boolean;
  loadingProfile: boolean;
  isAdmin: boolean;
  expiresIn: number;
  submitAuth: (event: FormEvent<HTMLFormElement>) => Promise<void>;
  refreshSession: () => Promise<void>;
  logout: () => Promise<void>;
  updateProfile: (payload: ProfilePayload) => Promise<void>;
  changePassword: (payload: PasswordPayload) => Promise<void>;
  loadAdminUsers: () => Promise<void>;
  createAdminUser: (payload: AdminUserPayload) => Promise<void>;
  updateAdminUser: (id: string, payload: AdminUserPayload) => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export type ProfilePayload = {
  email: string;
  phoneNumber: string;
  birthday: string;
  position: string;
  profilePictureUrl: string;
  socialContacts: Record<string, string>;
};

export type PasswordPayload = {
  currentPassword: string;
  newPassword: string;
};

export type AdminUserPayload = {
  username: string;
  email: string;
  password?: string;
  roles: string[];
  enabled: boolean;
  phoneNumber: string;
  birthday: string;
  position: string;
  profilePictureUrl: string;
  socialContacts: Record<string, string>;
};

type JwtPayload = {
  sub?: string;
  roles?: string[];
  exp?: number;
};

function decodeJwt(token: string): JwtPayload | null {
  const parts = token.split('.');
  if (parts.length !== 3) {
    return null;
  }

  try {
    const normalized = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=');
    return JSON.parse(atob(padded)) as JwtPayload;
  } catch {
    return null;
  }
}

function profileFromToken(accessToken: string): UserResponse | null {
  const payload = decodeJwt(accessToken);
  if (!payload?.sub) {
    return null;
  }

  return {
    id: payload.sub,
    username: payload.sub,
    email: '',
    roles: payload.roles ?? [],
    enabled: true,
    createdAt: new Date().toISOString(),
    phoneNumber: null,
    birthday: null,
    position: null,
    profilePictureUrl: null,
    socialContacts: {},
  };
}

async function request<T>(path: string, config: AxiosRequestConfig = {}): Promise<ApiResponse<T>> {
  try {
    const response = await axios.request<ApiResponse<T>>({
      url: path,
      method: config.method ?? 'GET',
      data: config.data,
      headers: {
        'Content-Type': 'application/json',
        ...(config.headers ?? {}),
      },
      withCredentials: config.withCredentials ?? false,
    });

    if (!response.data) {
      throw new Error('The server returned an empty response.');
    }

    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const payload = error.response?.data as ApiResponse<T> | undefined;
      throw new Error(payload?.message || error.message || `Request failed with status ${error.response?.status ?? 'unknown'}`);
    }

    throw error;
  }
}

async function loadCurrentProfile(accessToken: string): Promise<UserResponse> {
  const response = await axios.get<UserResponse>('/api/users/me', {
    headers: { Authorization: `Bearer ${accessToken}` },
  });
  return response.data;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [mode, setMode] = useState<AuthMode>('login');
  const [session, setSession] = useState<Session | null>(null);
  const [profile, setProfile] = useState<UserResponse | null>(null);
  const [adminUsers, setAdminUsers] = useState<UserResponse[]>([]);
  const [notice, setNotice] = useState<Notice>({ tone: 'info', text: 'Connect to the backend on port 8080.' });
  const [busy, setBusy] = useState(false);
  const [loadingProfile, setLoadingProfile] = useState(false);

  const isAdmin = profile?.roles.includes('ROLE_ADMIN') ?? false;
  const expiresIn = session ? Math.max(0, Math.round((session.expiresAt - Date.now()) / 1000)) : 0;

  async function submitAuth(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const username = String(form.get('username') || '');
    const password = String(form.get('password') || '');
    const email = String(form.get('email') || '');

    setBusy(true);
    try {
      if (mode === 'register') {
        await request<UserResponse>('/api/auth/register', {
          method: 'POST',
          data: { username, email, password },
        });
      }

      const login = await request<TokenResponse>('/api/auth/login', {
        method: 'POST',
        data: { username, password },
      });

      const nextSession: Session = {
        accessToken: login.data.accessToken,
        refreshToken: login.data.refreshToken,
        expiresAt: Date.now() + login.data.expiresIn * 1000,
      };

      setSession(nextSession);
      setLoadingProfile(true);
      const currentProfile = await loadCurrentProfile(login.data.accessToken).catch(() => profileFromToken(login.data.accessToken));
      setProfile(currentProfile);
      setAdminUsers([]);
      setNotice({ tone: 'success', text: mode === 'register' ? 'Account created and signed in.' : 'Signed in successfully.' });
    } catch (error) {
      setNotice({ tone: 'error', text: error instanceof Error ? error.message : 'Authentication failed.' });
    } finally {
      setLoadingProfile(false);
      setBusy(false);
    }
  }

  async function refreshSession() {
    if (!session) {
      return;
    }

    setBusy(true);
    try {
      const result = await request<TokenResponse>('/api/auth/refresh', {
        method: 'POST',
        data: { refreshToken: session.refreshToken },
      });

      setSession({
        accessToken: result.data.accessToken,
        refreshToken: result.data.refreshToken,
        expiresAt: Date.now() + result.data.expiresIn * 1000,
      });
      setLoadingProfile(true);
      const currentProfile = await loadCurrentProfile(result.data.accessToken).catch(() => profileFromToken(result.data.accessToken));
      setProfile(currentProfile);
      setNotice({ tone: 'success', text: 'Token refreshed.' });
    } catch (error) {
      clearSession(error instanceof Error ? error.message : 'Refresh failed.');
    } finally {
      setLoadingProfile(false);
      setBusy(false);
    }
  }

  async function logout() {
    if (!session) {
      return;
    }

    setBusy(true);
    try {
      await request<null>('/api/auth/logout', {
        method: 'POST',
        data: { refreshToken: session.refreshToken },
      });
      clearSession('Signed out.');
    } catch {
      clearSession('Local session cleared.');
    } finally {
      setBusy(false);
    }
  }

  async function loadAdminUsers() {
    if (!session) {
      return;
    }

    setBusy(true);
    try {
      const response = await axios.get<UserResponse[]>('/api/admin/users', {
        headers: { Authorization: `Bearer ${session.accessToken}` },
      });
      setAdminUsers(response.data);
      setNotice({ tone: 'success', text: 'Admin users loaded.' });
    } catch (error) {
      if (axios.isAxiosError(error)) {
        setNotice({
          tone: 'error',
          text: error.response?.status === 403 ? 'Admin access is required.' : error.message || 'Unable to load users.',
        });
      } else {
        setNotice({ tone: 'error', text: error instanceof Error ? error.message : 'Unable to load users.' });
      }
    } finally {
      setBusy(false);
    }
  }

  async function updateProfile(payload: ProfilePayload) {
    if (!session) {
      return;
    }

    setBusy(true);
    try {
      const response = await axios.put<UserResponse>('/api/users/me', payload, {
        headers: { Authorization: `Bearer ${session.accessToken}` },
      });
      setProfile(response.data);
      setNotice({ tone: 'success', text: 'Profile updated.' });
    } catch (error) {
      setNotice({ tone: 'error', text: error instanceof Error ? error.message : 'Unable to update profile.' });
      throw error;
    } finally {
      setBusy(false);
    }
  }

  async function changePassword(payload: PasswordPayload) {
    if (!session) {
      return;
    }

    setBusy(true);
    try {
      await axios.put('/api/users/me/password', payload, {
        headers: { Authorization: `Bearer ${session.accessToken}` },
      });
      setNotice({ tone: 'success', text: 'Password changed.' });
    } catch (error) {
      setNotice({ tone: 'error', text: error instanceof Error ? error.message : 'Unable to change password.' });
      throw error;
    } finally {
      setBusy(false);
    }
  }

  async function createAdminUser(payload: AdminUserPayload) {
    if (!session) {
      return;
    }

    setBusy(true);
    try {
      await axios.post('/api/admin/users', payload, {
        headers: { Authorization: `Bearer ${session.accessToken}` },
      });
      await loadAdminUsers();
      setNotice({ tone: 'success', text: 'User created.' });
    } catch (error) {
      setNotice({ tone: 'error', text: error instanceof Error ? error.message : 'Unable to create user.' });
      throw error;
    } finally {
      setBusy(false);
    }
  }

  async function updateAdminUser(id: string, payload: AdminUserPayload) {
    if (!session) {
      return;
    }

    setBusy(true);
    try {
      await axios.put(`/api/admin/users/${id}`, payload, {
        headers: { Authorization: `Bearer ${session.accessToken}` },
      });
      await loadAdminUsers();
      if (profile?.id === id) {
        const updated = await loadCurrentProfile(session.accessToken);
        setProfile(updated);
      }
      setNotice({ tone: 'success', text: 'User updated.' });
    } catch (error) {
      setNotice({ tone: 'error', text: error instanceof Error ? error.message : 'Unable to update user.' });
      throw error;
    } finally {
      setBusy(false);
    }
  }

  function clearSession(message: string) {
    setSession(null);
    setProfile(null);
    setAdminUsers([]);
    setLoadingProfile(false);
    setNotice({ tone: 'info', text: message });
  }

  return (
    <AuthContext.Provider
      value={{
        mode,
        setMode,
        session,
        profile,
        adminUsers,
        notice,
        busy,
        loadingProfile,
        isAdmin,
        expiresIn,
        submitAuth,
        refreshSession,
        logout,
        updateProfile,
        changePassword,
        loadAdminUsers,
        createAdminUser,
        updateAdminUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}
