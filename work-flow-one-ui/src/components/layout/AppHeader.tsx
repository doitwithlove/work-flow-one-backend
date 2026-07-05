import { ChevronDown, LogIn, LogOut, RefreshCw, UserCircle2, UserPlus } from 'lucide-react';
import { useState } from 'react';
import { UserResponse } from '../../types/UserResponse';
import styles from './AppHeader.module.css';

type AppHeaderProps = {
  session: boolean;
  profile: UserResponse | null;
  busy: boolean;
  onLogin: () => void;
  onRegister: () => void;
  onRefresh: () => void;
  onLogout: () => void;
};

export function AppHeader({ session, profile, busy, onLogin, onRegister, onRefresh, onLogout }: AppHeaderProps) {
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
              <button className={`${styles.headerAction} ${styles.secondary}`} type="button" onClick={() => setMenuOpen((value) => !value)}>
                <UserCircle2 size={16} />
                {profile?.username || 'Account'}
                <ChevronDown size={16} className={`${styles.chevron} ${menuOpen ? styles.chevronOpen : ''}`} />
              </button>
              {menuOpen && (
                <div className={styles.menuPanel}>
                  <a className={styles.menuLink} href="#profile" onClick={() => setMenuOpen(false)}>
                    Profile
                  </a>
                  <a className={styles.menuLink} href="#sessions" onClick={() => setMenuOpen(false)}>
                    Sessions
                  </a>
                  <a className={styles.menuLink} href="#users" onClick={() => setMenuOpen(false)}>
                    Users
                  </a>
                  <button className={styles.menuAction} type="button" onClick={() => { setMenuOpen(false); onRefresh(); }} disabled={busy}>
                    <RefreshCw size={16} />
                    Refresh
                  </button>
                  <button className={`${styles.menuAction} ${styles.danger}`} type="button" onClick={() => { setMenuOpen(false); onLogout(); }} disabled={busy}>
                    <LogOut size={16} />
                    Logout
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
