/* eslint-disable react-refresh/only-export-components */
import { createContext, FormEvent, ReactNode, useContext, useEffect, useState } from 'react';
import axios, { AxiosRequestConfig } from 'axios';
import { ApiResponse } from '../types/ApiResponse';
import { Notice } from '../types/Notice';
import { Session } from '../types/Session';
import { TokenResponse } from '../types/TokenResponse';
import { UserResponse } from '../types/UserResponse';
import { apiClient } from '../lib/apiClient';
import { registerNavigationHandlers } from '../lib/navigation';
import { PATHS } from '../routes/paths';
import { useNavigate } from 'react-router-dom';

type AuthMode = 'login' | 'register';

type AuthContextValue = {
  mode: AuthMode;
  setMode: (mode: AuthMode) => void;
  session: Session | null;
  profile: UserResponse | null;
  currentUser: UserResponse | null;
  accessToken: string | null;
  roles: string[];
  isAuthenticated: boolean;
  adminUsers: UserResponse[];
  notice: Notice;
  busy: boolean;
  loadingProfile: boolean;
  isAdmin: boolean;
  isSuperUser: boolean;
  hasRole: (role: string) => boolean;
  hasAnyRole: (roles: string[]) => boolean;
  expiresIn: number;
  login: (event: FormEvent<HTMLFormElement>) => Promise<void>;
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
  fullName: string;
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

const SESSION_STORAGE_KEY = 'work-flow-one-session';

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
    fullName: null,
    roles: normalizeRoles(payload.roles ?? []),
    enabled: true,
    active: true,
    createdAt: new Date().toISOString(),
    phoneNumber: null,
    birthday: null,
    position: null,
    profilePictureUrl: null,
    socialContacts: {},
  };
}

function normalizeRole(role: string): string {
  const value = role.trim();
  if (!value) {
    return value;
  }

  return value.startsWith('ROLE_') ? value : `ROLE_${value.toUpperCase()}`;
}

function normalizeRoles(roles: string[]): string[] {
  return roles.map(normalizeRole).filter(Boolean);
}

function loadStoredSession(): Session | null {
  try {
    const raw = sessionStorage.getItem(SESSION_STORAGE_KEY);
    return raw ? (JSON.parse(raw) as Session) : null;
  } catch {
    return null;
  }
}

function saveStoredSession(session: Session | null) {
  if (!session) {
    sessionStorage.removeItem(SESSION_STORAGE_KEY);
    return;
  }

  sessionStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session));
}

async function request<T>(path: string, config: AxiosRequestConfig = {}): Promise<ApiResponse<T>> {
  try {
    const response = await apiClient.request<ApiResponse<T>>({
      url: path,
      method: config.method ?? 'GET',
      data: config.data,
      headers: {
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
        const method = String(config.method ?? 'GET').toUpperCase();
        const status = error.response?.status ?? 'unknown';
        const pathSuffix = `(${method} ${path})`;
        const baseMessage = payload?.message || error.message || `Request failed with status ${status}`;
        throw new Error(baseMessage.includes(pathSuffix) ? baseMessage : `${baseMessage} ${pathSuffix}`);
      }

      throw error;
    }
  }

export function AuthProvider({ children }: { children: ReactNode }) {
  const navigate = useNavigate();
  const [mode, setMode] = useState<AuthMode>('login');
  const [session, setSession] = useState<Session | null>(() => {
    const storedSession = loadStoredSession();
    if (storedSession?.accessToken) {
      apiClient.defaults.headers.common.Authorization = `Bearer ${storedSession.accessToken}`;
    }
    return storedSession;
  });
  const [profile, setProfile] = useState<UserResponse | null>(null);
  const [adminUsers, setAdminUsers] = useState<UserResponse[]>([]);
  const [notice, setNotice] = useState<Notice>({ tone: 'info', text: 'Connect to the backend on port 8081.' });
  const [busy, setBusy] = useState(false);
  const [loadingProfile, setLoadingProfile] = useState(false);

  const roles = normalizeRoles(profile?.roles ?? session?.roles ?? []);
  const isAuthenticated = Boolean(session?.accessToken);
  const accessToken = session?.accessToken ?? null;
  const currentUser = profile;
  const isAdmin = roles.some((role) => role === 'ROLE_ADMIN' || role === 'ROLE_SUPER_USER');
  const isSuperUser = roles.some((role) => role === 'ROLE_SUPER_USER' || role === 'ROLE_ADMIN');
  const hasRole = (role: string) => roles.includes(role);
  const hasAnyRole = (requiredRoles: string[]) => requiredRoles.some((role) => roles.includes(role));
  const expiresIn = session ? Math.max(0, Math.round((session.expiresAt - Date.now()) / 1000)) : 0;

  useEffect(() => {
    return registerNavigationHandlers({
      onLoginRedirect: () => {
        clearSession('Session expired. Please sign in again.');
        navigate(PATHS.LOGIN, { replace: true });
      },
      onForbiddenRedirect: () => {
        navigate(PATHS.UNAUTHORIZED, { replace: true });
      },
    });
  }, [navigate]);

  useEffect(() => {
    if (!session?.accessToken) {
      delete apiClient.defaults.headers.common.Authorization;
      saveStoredSession(null);
      setLoadingProfile(false);
      return;
    }

    apiClient.defaults.headers.common.Authorization = `Bearer ${session.accessToken}`;
    saveStoredSession(session);
    setProfile(profileFromToken(session.accessToken));
    setLoadingProfile(false);
  }, []);

  async function submitAuth(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const username = String(form.get('username') || '');
    const password = String(form.get('password') || '');
    const email = String(form.get('email') || '');

    setBusy(true);
    try {
      delete apiClient.defaults.headers.common.Authorization;
      if (mode === 'register') {
        await request<UserResponse>('/auth/register', {
          method: 'POST',
          data: { username, email, password },
        });
      }

      const login = await request<TokenResponse>('/auth/login', {
        method: 'POST',
        data: { username, password },
      });

      const nextSession: Session = {
        accessToken: login.data.accessToken,
        refreshToken: login.data.refreshToken,
        expiresAt: Date.now() + login.data.expiresIn * 1000,
        userId: login.data.userId,
        username: login.data.username,
        roles: normalizeRoles(login.data.roles),
      };

      apiClient.defaults.headers.common.Authorization = `Bearer ${nextSession.accessToken}`;
      saveStoredSession(nextSession);
      setSession(nextSession);
      setProfile(profileFromToken(login.data.accessToken));
      setLoadingProfile(false);
      setAdminUsers([]);
      setNotice({ tone: 'success', text: mode === 'register' ? 'Account created and signed in.' : 'Signed in successfully.' });
    } catch (error) {
      setNotice({ tone: 'error', text: error instanceof Error ? error.message : 'Authentication failed.' });
    } finally {
      setLoadingProfile(false);
      setBusy(false);
    }
  }

  const login = submitAuth;

  async function refreshSession() {
    if (!session) {
      return;
    }

    setBusy(true);
    try {
      const result = await request<TokenResponse>('/auth/refresh', {
        method: 'POST',
        data: { refreshToken: session.refreshToken },
      });

      const nextSession = {
        accessToken: result.data.accessToken,
        refreshToken: result.data.refreshToken,
        expiresAt: Date.now() + result.data.expiresIn * 1000,
        userId: result.data.userId,
        username: result.data.username,
        roles: normalizeRoles(result.data.roles),
      };
      apiClient.defaults.headers.common.Authorization = `Bearer ${nextSession.accessToken}`;
      saveStoredSession(nextSession);
      setSession(nextSession);
      setProfile(profileFromToken(result.data.accessToken));
      setLoadingProfile(false);
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
      await request<null>('/auth/logout', {
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
      const response = await apiClient.get<UserResponse[]>('/users', {
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
      const response = await apiClient.put<UserResponse>('/users/me', payload, {
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
      await apiClient.put('/users/me/password', payload, {
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
      await apiClient.post('/users', payload, {
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
      await apiClient.put(`/users/${id}`, payload, {
        headers: { Authorization: `Bearer ${session.accessToken}` },
      });
      await loadAdminUsers();
      if (profile?.id === id) {
        setProfile(profileFromToken(session.accessToken));
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
    saveStoredSession(null);
    delete apiClient.defaults.headers.common.Authorization;
    setNotice({ tone: 'info', text: message });
  }

  return (
    <AuthContext.Provider
      value={{
        mode,
        setMode,
        session,
        profile,
        currentUser,
        accessToken,
        roles,
        isAuthenticated,
        adminUsers,
        notice,
        busy,
        loadingProfile,
        isAdmin,
        isSuperUser,
        hasRole,
        hasAnyRole,
        expiresIn,
        login,
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
{/*
  
  
  • AuthContext.Provider is the piece that makes auth state global for the React app.

  In this code, the provider lives in src/context/AuthContext.tsx (work-flow-one-ui/src/context/AuthContext.tsx:183-487) and wraps the whole app from
  src/main.tsx (work-flow-one-ui/src/main.tsx:8-16). Because AuthProvider is inside BrowserRouter, it can use useNavigate() safely.

  How it works:

  1. createContext(...) creates a shared container
     At AuthContext = createContext<AuthContextValue | null>(null) (work-flow-one-ui/src/context/AuthContext.tsx:45-45), React creates a context
     object.
     That object itself has no data yet. It just defines the shape of the auth state.

  2. AuthProvider owns the auth state
     The provider component defines the actual state:
      - session
      - profile
      - roles
      - busy
      - loadingProfile
      - notice
      - adminUsers
      - and auth actions like login, logout, refreshSession, updateProfile
        See AuthProvider (work-flow-one-ui/src/context/AuthContext.tsx:183-487).

  3. It restores session on startup
     On initial render, it reads from sessionStorage in loadStoredSession() (work-flow-one-ui/src/context/AuthContext.tsx:134-150) and seeds session.
     If a token exists, it also sets the default axios Authorization header immediately at lines 186-191 (work-flow-one-ui/src/context/
     AuthContext.tsx:186-191).

  4. It derives useful auth values
     The provider computes:
      - isAuthenticated
      - accessToken
      - roles
      - currentUser
      - isAdmin
      - isSuperUser
      - hasRole()
      - hasAnyRole()
        See lines 199-207 (work-flow-one-ui/src/context/AuthContext.tsx:199-207).

     Important detail: roles is derived from either:
      - profile.roles if profile is loaded
      - otherwise session.roles

  5. It exposes auth actions
     Functions like submitAuth, refreshSession, and logout mutate the provider state and storage:
      - login stores the session and sets the bearer token
      - refresh replaces the token pair
      - logout clears state, storage, and the axios header
        See lines 235-334 (work-flow-one-ui/src/context/AuthContext.tsx:235-334).

  6. It updates profile and admin data
     updateProfile, changePassword, loadAdminUsers, createAdminUser, and updateAdminUser all call backend APIs and then update local context state.
     Those live at lines 362-442 (work-flow-one-ui/src/context/AuthContext.tsx:362-442).

  7. It pushes the final value into the context
     The actual provider render is at lines 454-486 (work-flow-one-ui/src/context/AuthContext.tsx:454-486).
     Whatever is passed in value={...} becomes available to every child component.

  8. Consumers read it with useAuth()
     Any component can call useAuth() and get the shared auth state without prop drilling.
     That hook is at lines 490-496 (work-flow-one-ui/src/context/AuthContext.tsx:490-496).

  Practical effect:

  - LoginPage reads mode, busy, and login() from context
  - AppHeader reads isAuthenticated, roles, and logout()
  - ProtectedRoute reads isAuthenticated and hasAnyRole()
  - App.tsx reads auth state to decide redirects and route access

  One implementation detail matters: the provider also registers navigation handlers in lines 209-219 (work-flow-one-ui/src/context/
  AuthContext.tsx:209-219). That lets the axios interceptor redirect to /login on 401 and /unauthorized on 403 without touching window.location.

  So in plain terms: AuthContext.Provider is the shared auth store and action hub for the app. It owns the session, derives roles, exposes auth
  helpers, and keeps the UI and API client in sync.


  
  */}
