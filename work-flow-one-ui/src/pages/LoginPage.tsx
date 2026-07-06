import { AuthPanel } from '../components/panels/AuthPanel';
import { useAuth } from '../context/AuthContext';
import styles from './LoginPage.module.css';

export function LoginPage() {
  const { mode, setMode, notice, busy, login } = useAuth();

  return (
    <section className={styles.pageShell} aria-label="Login page">
      <div className={styles.hero}>
        <p className={styles.eyebrow}>Authentication</p>
        <h2>Sign in to continue</h2>
        <p className={styles.message}>Use your username or email to access the factory console.</p>
      </div>

      <div className={styles.card}>
        <div className={`${styles.notice} ${styles[notice.tone]}`} role="status">
          {notice.text}
        </div>
        <AuthPanel
          mode={mode}
          busy={busy}
          onModeChange={setMode}
          onSubmit={login}
        />
      </div>
    </section>
  );
}
