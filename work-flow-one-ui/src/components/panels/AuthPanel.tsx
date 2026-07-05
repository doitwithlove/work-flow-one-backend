import { FormEvent } from 'react';
import { Loader2, LogIn, UserPlus } from 'lucide-react';
import styles from './AuthPanel.module.css';

type AuthMode = 'login' | 'register';

type AuthPanelProps = {
  mode: AuthMode;
  busy: boolean;
  onModeChange: (mode: AuthMode) => void;
  onSubmit: (event: FormEvent<HTMLFormElement>) => Promise<void>;
};

export function AuthPanel({ mode, busy, onModeChange, onSubmit }: AuthPanelProps) {
  return (
    <section className={`${styles.panel} ${styles.authPanel}`} id="auth">
      <div className={styles.panelHeading}>
        <div>
          <p className={styles.eyebrow}>Access</p>
          <h2>{mode === 'login' ? 'Sign in' : 'Create account'}</h2>
        </div>
        <div className={styles.segmented} role="tablist" aria-label="Authentication mode">
          <button className={mode === 'login' ? styles.selected : ''} onClick={() => onModeChange('login')} type="button">
            Login
          </button>
          <button className={mode === 'register' ? styles.selected : ''} onClick={() => onModeChange('register')} type="button">
            Register
          </button>
        </div>
      </div>

      <form className={styles.formStack} onSubmit={onSubmit}>
        <label className={styles.label}>
          <span>Username or email</span>
          <input className={styles.input} name="username" minLength={4} maxLength={254} autoComplete="username" required />
        </label>

        {mode === 'register' && (
          <label className={styles.label}>
            <span>Email</span>
            <input className={styles.input} name="email" type="email" autoComplete="email" required />
          </label>
        )}

        <label className={styles.label}>
          <span>Password</span>
          <input
            className={styles.input}
            name="password"
            type="password"
            minLength={8}
            autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
            required
          />
        </label>

        <button className={styles.primaryAction} type="submit" disabled={busy}>
          {busy ? <Loader2 className={styles.spin} size={18} /> : mode === 'login' ? <LogIn size={18} /> : <UserPlus size={18} />}
          {mode === 'login' ? 'Sign in' : 'Create and sign in'}
        </button>
      </form>
    </section>
  );
}
