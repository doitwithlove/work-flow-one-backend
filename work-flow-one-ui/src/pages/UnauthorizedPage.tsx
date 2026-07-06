import styles from './UnauthorizedPage.module.css';

export function UnauthorizedPage() {
  return (
    <section className={styles.pageShell} aria-label="Unauthorized page">
      <p className={styles.eyebrow}>Unauthorized</p>
      <h2>Unauthorized</h2>
      <p className={styles.message}>You do not have permission to access this page.</p>
    </section>
  );
}
