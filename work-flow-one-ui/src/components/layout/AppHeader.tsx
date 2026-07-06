import { ChevronDown, LogIn, LogOut, RefreshCw, UserCircle2, UserPlus } from 'lucide-react';
import { useState } from 'react';
import { UserResponse } from '../../types/UserResponse';
import styles from './AppHeader.module.css';

type AppHeaderProps = {
  session: boolean;
  currentUser: UserResponse | null;
  roles: string[];
  busy: boolean;
  onLogin: () => void;
  onRegister: () => void;
  onRefresh: () => void;
  onLogout: () => void | Promise<void>;
  onNavigate: (path: '/profile' | '/sessions' | '/users' | '/roles' | '/super-user/dashboard') => void;
};

export function AppHeader({ session, currentUser, roles, busy, onLogin, onRegister, onRefresh, onLogout, onNavigate }: AppHeaderProps) {
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <header className={styles.appHeader}>
      <div className={styles.brand}>
        <span className={styles.brandIcon}>WF</span>
        <div>
          <strong>Work Flow One</strong>
          <span>Reactive operations console</span>
        </div>
      </div>

      <div className={styles.headerMeta}>
        <div className={`${styles.statusPill} ${session ? styles.online : ''}`}>
          <span />
          {session ? 'Authenticated' : 'Signed out'}
        </div>

        <div className={styles.headerActions}>
          {!session && (
            <>
              <button className={`${styles.headerAction} ${styles.secondary}`} type="button" onClick={onLogin}>
                <LogIn size={16} />
                Login
              </button>
              <button className={`${styles.headerAction} ${styles.secondary}`} type="button" onClick={onRegister}>
                <UserPlus size={16} />
                Register
              </button>
            </>
          )}
          {session && (
            <div className={styles.menuWrap}>
              <button className={`${styles.headerAction} ${styles.danger}`} type="button" onClick={onLogout} disabled={busy}>
                <LogOut size={16} />
                Logout
              </button>
              <button className={`${styles.headerAction} ${styles.secondary}`} type="button" onClick={() => setMenuOpen((value) => !value)}>
                <UserCircle2 size={16} />
                {currentUser?.username || 'Account'}
                {roles.length > 0 ? `(${roles.map((role) => role.replace('ROLE_', '')).join(', ')})` : ''}
                <ChevronDown size={16} className={`${styles.chevron} ${menuOpen ? styles.chevronOpen : ''}`} />
              </button>
              {menuOpen && (
                <div className={styles.menuPanel}>
                  <div className={styles.menuLink} onClick={() => { setMenuOpen(false); onNavigate('/profile'); }}>
                    Profile
                  </div>
                  <div className={styles.menuLink} onClick={() => { setMenuOpen(false); onNavigate('/sessions'); }}>
                    Sessions
                  </div>
                  {roles.includes('ROLE_SUPER_USER') || roles.includes('ROLE_ADMIN') ? (
                    <>
                      <div className={styles.menuLink} onClick={() => { setMenuOpen(false); onNavigate('/super-user/dashboard'); }}>
                        Super user dashboard
                      </div>
                      <div className={styles.menuLink} onClick={() => { setMenuOpen(false); onNavigate('/users'); }}>
                        Users
                      </div>
                      <div className={styles.menuLink} onClick={() => { setMenuOpen(false); onNavigate('/roles'); }}>
                        Roles
                      </div>
                    </>
                  ) : null}
                  <button className={styles.menuAction} type="button" onClick={() => { setMenuOpen(false); onRefresh(); }} disabled={busy}>
                    <RefreshCw size={16} />
                    Refresh
                  </button>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
