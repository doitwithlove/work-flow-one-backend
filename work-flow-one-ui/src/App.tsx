import { FormEvent, useEffect, useMemo, useState } from 'react';
import {
  Activity,
  CheckCircle2,
  KeyRound,
  Loader2,
  Lock,
  LogIn,
  LogOut,
  RefreshCw,
  Shield,
  UserPlus,
  Users,
} from 'lucide-react';
import './App.css';

type AuthMode = 'login' | 'register';

type ApiResponse<T> = {
  timestamp: string;
  status: number;
  message: string;
  data: T;
};

type TokenResponse = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
};

type UserResponse = {
  id: string;
  username: string;
  email: string;
  roles: string[];
  enabled: boolean;
  createdAt: string;
};

type Session = {
  accessToken: string;
  refreshToken: string;
  expiresAt: number;
};

type Notice = {
  tone: 'success' | 'error' | 'info';
  text: string;
};

const sessionKey = 'workflowone.session';

function readSession(): Session | null {
  const raw = localStorage.getItem(sessionKey);
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as Session;
  } catch {
    localStorage.removeItem(sessionKey);
    return null;
  }
}

function storeSession(tokens: TokenResponse): Session {
  const session = {
    accessToken: tokens.accessToken,
    refreshToken: tokens.refreshToken,
    expiresAt: Date.now() + tokens.expiresIn * 1000,
  };
  localStorage.setItem(sessionKey, JSON.stringify(session));
  return session;
}

async function request<T>(path: string, options: RequestInit = {}): Promise<ApiResponse<T>> {
  const response = await fetch(path, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  });

  const payload = (await response.json().catch(() => null)) as ApiResponse<T> | null;
  if (!response.ok) {
    throw new Error(payload?.message || `Request failed with status ${response.status}`);
  }

  if (!payload) {
    throw new Error('The server returned an empty response.');
  }

  return payload;
}

export default function App() {
  const [mode, setMode] = useState<AuthMode>('login');
  const [session, setSession] = useState<Session | null>(() => readSession());
  const [profile, setProfile] = useState<UserResponse | null>(null);
  const [adminUsers, setAdminUsers] = useState<UserResponse[]>([]);
  const [notice, setNotice] = useState<Notice>({ tone: 'info', text: 'Connect to the backend on port 8080.' });
  const [busy, setBusy] = useState(false);
  const [loadingProfile, setLoadingProfile] = useState(false);

  const isAdmin = useMemo(() => profile?.roles.includes('ROLE_ADMIN') ?? false, [profile]);
  const expiresIn = session ? Math.max(0, Math.round((session.expiresAt - Date.now()) / 1000)) : 0;

  useEffect(() => {
    if (!session) {
      setProfile(null);
      setAdminUsers([]);
      return;
    }

    void loadProfile(session.accessToken);
  }, [session]);

  async function loadProfile(accessToken: string) {
    setLoadingProfile(true);
    try {
      const result = await request<UserResponse>('/api/users/me', {
        headers: { Authorization: `Bearer ${accessToken}` },
      });
      setProfile(result.data);
      setNotice({ tone: 'success', text: 'Session is active.' });
    } catch (error) {
      setNotice({ tone: 'error', text: error instanceof Error ? error.message : 'Unable to load profile.' });
    } finally {
      setLoadingProfile(false);
    }
  }

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
          body: JSON.stringify({ username, email, password }),
        });
      }

      const login = await request<TokenResponse>('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password }),
      });
      setSession(storeSession(login.data));
      setNotice({ tone: 'success', text: mode === 'register' ? 'Account created and signed in.' : 'Signed in successfully.' });
    } catch (error) {
      setNotice({ tone: 'error', text: error instanceof Error ? error.message : 'Authentication failed.' });
    } finally {
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
        body: JSON.stringify({ refreshToken: session.refreshToken }),
      });
      setSession(storeSession(result.data));
      setNotice({ tone: 'success', text: 'Token refreshed.' });
    } catch (error) {
      clearSession(error instanceof Error ? error.message : 'Refresh failed.');
    } finally {
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
        body: JSON.stringify({ refreshToken: session.refreshToken }),
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
      const response = await fetch('/api/admin/users', {
        headers: { Authorization: `Bearer ${session.accessToken}` },
      });
      if (!response.ok) {
        throw new Error(response.status === 403 ? 'Admin access is required.' : `Request failed with status ${response.status}`);
      }
      setAdminUsers((await response.json()) as UserResponse[]);
      setNotice({ tone: 'success', text: 'Admin users loaded.' });
    } catch (error) {
      setNotice({ tone: 'error', text: error instanceof Error ? error.message : 'Unable to load users.' });
    } finally {
      setBusy(false);
    }
  }

  function clearSession(message: string) {
    localStorage.removeItem(sessionKey);
    setSession(null);
    setProfile(null);
    setAdminUsers([]);
    setNotice({ tone: 'info', text: message });
  }

  return (
    <main className="app-shell">
      <aside className="side-panel">
        <div className="brand">
          <span className="brand-icon"><Activity size={22} /></span>
          <div>
            <strong>Work Flow One</strong>
            <span>Reactive operations console</span>
          </div>
        </div>

        <img className="brand-visual" src="/brand-mark.svg" alt="" />

        <nav className="nav-stack" aria-label="Application sections">
          <a className="nav-item active" href="#auth"><KeyRound size={18} />Access</a>
          <a className="nav-item" href="#session"><Shield size={18} />Session</a>
          <a className="nav-item" href="#users"><Users size={18} />Users</a>
        </nav>
      </aside>

      <section className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">Backend API</p>
            <h1>Authentication workspace</h1>
          </div>
          <div className={`status-pill ${session ? 'online' : ''}`}>
            <span />
            {session ? 'Authenticated' : 'Signed out'}
          </div>
        </header>

        <div className={`notice ${notice.tone}`} role="status">
          {notice.text}
        </div>

        <div className="content-grid">
          <section className="panel auth-panel" id="auth">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">Access</p>
                <h2>{mode === 'login' ? 'Sign in' : 'Create account'}</h2>
              </div>
              <div className="segmented" role="tablist" aria-label="Authentication mode">
                <button className={mode === 'login' ? 'selected' : ''} onClick={() => setMode('login')} type="button">
                  Login
                </button>
                <button className={mode === 'register' ? 'selected' : ''} onClick={() => setMode('register')} type="button">
                  Register
                </button>
              </div>
            </div>

            <form className="form-stack" onSubmit={submitAuth}>
              <label>
                <span>Username</span>
                <input name="username" minLength={4} maxLength={30} autoComplete="username" required />
              </label>

              {mode === 'register' && (
                <label>
                  <span>Email</span>
                  <input name="email" type="email" autoComplete="email" required />
                </label>
              )}

              <label>
                <span>Password</span>
                <input name="password" type="password" minLength={8} autoComplete={mode === 'login' ? 'current-password' : 'new-password'} required />
              </label>

              <button className="primary-action" type="submit" disabled={busy}>
                {busy ? <Loader2 className="spin" size={18} /> : mode === 'login' ? <LogIn size={18} /> : <UserPlus size={18} />}
                {mode === 'login' ? 'Sign in' : 'Create and sign in'}
              </button>
            </form>
          </section>

          <section className="panel" id="session">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">Session</p>
                <h2>Token controls</h2>
              </div>
              {loadingProfile && <Loader2 className="spin muted" size={20} />}
            </div>

            <div className="metrics">
              <div>
                <span>Access token</span>
                <strong>{session ? `${expiresIn}s` : 'None'}</strong>
              </div>
              <div>
                <span>Roles</span>
                <strong>{profile?.roles.join(', ') || 'None'}</strong>
              </div>
            </div>

            <div className="button-row">
              <button type="button" onClick={refreshSession} disabled={!session || busy}>
                <RefreshCw size={18} />
                Refresh
              </button>
              <button type="button" onClick={logout} disabled={!session || busy}>
                <LogOut size={18} />
                Logout
              </button>
            </div>

            {profile && (
              <dl className="profile-list">
                <div><dt>User</dt><dd>{profile.username}</dd></div>
                <div><dt>Email</dt><dd>{profile.email}</dd></div>
                <div><dt>Status</dt><dd><CheckCircle2 size={16} />{profile.enabled ? 'Enabled' : 'Disabled'}</dd></div>
              </dl>
            )}
          </section>

          <section className="panel users-panel" id="users">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">Users</p>
                <h2>Admin directory</h2>
              </div>
              <button className="icon-button" type="button" onClick={loadAdminUsers} disabled={!session || busy} title="Load admin users">
                {busy ? <Loader2 className="spin" size={18} /> : <Users size={18} />}
              </button>
            </div>

            {!isAdmin && <p className="empty-state"><Lock size={18} /> Sign in with an admin account to load the directory.</p>}

            {adminUsers.length > 0 && (
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>Username</th>
                      <th>Email</th>
                      <th>Roles</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {adminUsers.map((user) => (
                      <tr key={user.id}>
                        <td>{user.username}</td>
                        <td>{user.email}</td>
                        <td>{user.roles.join(', ')}</td>
                        <td>{user.enabled ? 'Enabled' : 'Disabled'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        </div>
      </section>
    </main>
  );
}
