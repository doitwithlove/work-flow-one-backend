import styles from './AppFooter.module.css';

export function AppFooter() {
  return (
    <footer className={styles.appFooter}>
      <span>JWT session handled in React context</span>
      <span>Backend: `/api`</span>
    </footer>
  );
}
