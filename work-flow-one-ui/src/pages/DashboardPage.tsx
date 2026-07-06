import { useEffect, useState } from 'react';
import { fetchDashboardSummary } from '../api/dashboardApi';
import { DashboardSummary } from '../types/manufacturing';
import { StatusBadge } from '../components/common/StatusBadge';
import styles from './DashboardPage.module.css';

const initialSummary: DashboardSummary = {
  runningMachines: 0,
  idleMachines: 0,
  errorMachines: 0,
  partsInProcess: 0,
  partsPassed: 0,
  partsFailed: 0,
  partsReadyForNextPhase: 0,
};

export function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary>(initialSummary);
  const [error, setError] = useState('');

  useEffect(() => {
    let mounted = true;

    async function load() {
      try {
        const data = await fetchDashboardSummary();
        if (mounted) {
          setSummary(data);
          setError('');
        }
      } catch (requestError) {
        if (mounted) {
          setError(requestError instanceof Error ? requestError.message : 'Unable to load dashboard.');
        }
      }
    }

    void load();
    const timer = window.setInterval(() => void load(), 5000);
    return () => {
      mounted = false;
      window.clearInterval(timer);
    };
  }, []);

  return (
    <section className={styles.pageShell} aria-label="Dashboard page">
      <div className={styles.headerRow}>
        <div>
          <p className={styles.eyebrow}>Manufacturing</p>
          <h2>Production dashboard</h2>
        </div>
        {error && <span className={styles.error}>{error}</span>}
      </div>

      <div className={styles.cardGrid}>
        {[
          ['Running Machines', summary.runningMachines, 'RUNNING'],
          ['Idle Machines', summary.idleMachines, 'IDLE'],
          ['Error Machines', summary.errorMachines, 'ERROR'],
          ['Parts In Process', summary.partsInProcess, 'IN_PROCESS'],
          ['Passed Parts', summary.partsPassed, 'PASS'],
          ['Failed Parts', summary.partsFailed, 'FAILED'],
          ['Ready For Next Phase', summary.partsReadyForNextPhase, 'READY_FOR_NEXT_PHASE'],
        ].map(([label, value, status]) => (
          <article key={label as string} className={styles.card}>
            <span className={styles.label}>{label as string}</span>
            <strong>{value as number}</strong>
            <StatusBadge status={status as string} />
          </article>
        ))}
      </div>
    </section>
  );
}
