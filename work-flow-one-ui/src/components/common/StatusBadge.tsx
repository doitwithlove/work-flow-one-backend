import styles from './StatusBadge.module.css';

type StatusBadgeProps = {
  status: string;
};

export function StatusBadge({ status }: StatusBadgeProps) {
  const tone = status.toLowerCase().replace(/[^a-z0-9]+/g, '-');
  return <span className={`${styles.badge} ${styles[tone] ?? styles.default}`}>{status}</span>;
}
