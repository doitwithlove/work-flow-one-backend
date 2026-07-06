import { useEffect, useMemo, useState } from 'react';
import { fetchProductivitySummary } from '../api/productivityApi';
import { ProductivitySummary } from '../types/manufacturing';
import { StatusBadge } from '../components/common/StatusBadge';
import styles from './ProductivityDashboardPage.module.css';

function formatDateTime(value: string | null | undefined) {
  if (!value) {
    return 'None';
  }

  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? 'Invalid date' : parsed.toLocaleString();
}

export function ProductivityDashboardPage() {
  const [rows, setRows] = useState<ProductivitySummary[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    let mounted = true;

    async function load() {
      try {
        const data = await fetchProductivitySummary();
        if (mounted) {
          setRows(data);
          setError('');
        }
      } catch (requestError) {
        if (mounted) {
          setError(requestError instanceof Error ? requestError.message : 'Unable to load productivity data.');
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

  const metrics = useMemo(() => {
    const totalParts = rows.reduce((sum, row) => sum + row.totalPartsProcessed, 0);
    const passedParts = rows.reduce((sum, row) => sum + row.passedParts, 0);
    const failedParts = rows.reduce((sum, row) => sum + row.failedParts, 0);
    const reworkParts = rows.reduce((sum, row) => sum + row.reworkParts, 0);
    const avgQualityRate = rows.length ? rows.reduce((sum, row) => sum + row.qualityRate, 0) / rows.length : 0;
    const avgProductivity = rows.length ? rows.reduce((sum, row) => sum + row.productivityScore, 0) / rows.length : 0;

    return { totalParts, passedParts, failedParts, reworkParts, avgQualityRate, avgProductivity };
  }, [rows]);

  return (
    <section className={styles.pageShell}>
      <div className={styles.headerRow}>
        <div>
          <p className={styles.eyebrow}>Manufacturing</p>
          <h2>Productivity dashboard</h2>
        </div>
        {error && <span className={styles.error}>{error}</span>}
      </div>

      <div className={styles.cardGrid}>
        {[
          ['Total parts processed', metrics.totalParts, 'PRODUCTION'],
          ['Passed parts', metrics.passedParts, 'PASS'],
          ['Failed parts', metrics.failedParts, 'FAILED'],
          ['Rework parts', metrics.reworkParts, 'REWORK_REQUIRED'],
          ['Average quality rate', `${Math.round(metrics.avgQualityRate * 100)}%`, 'QUALITY'],
          ['Average productivity score', `${Math.round(metrics.avgProductivity * 100)}%`, 'SCORE'],
        ].map(([label, value, status]) => (
          <article key={label as string} className={styles.card}>
            <span className={styles.label}>{label as string}</span>
            <strong>{value as string | number}</strong>
            <StatusBadge status={status as string} />
          </article>
        ))}
      </div>

      <div className={styles.columns}>
        <section className={styles.cardPanel}>
          <h3>Productivity by operator</h3>
          <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Operator</th>
                  <th>Parts</th>
                  <th>Score</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr key={`${row.operatorId}-${row.shiftId}-${row.machineId}`}>
                    <td>{row.operatorName}</td>
                    <td>{row.totalPartsProcessed}</td>
                    <td>
                      <div className={styles.barTrack}>
                        <div className={styles.barFill} style={{ width: `${Math.min(100, Math.round(row.productivityScore * 100))}%` }} />
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        <section className={styles.cardPanel}>
          <h3>Productivity by machine</h3>
          <div className={styles.list}>
            {rows.map((row) => (
              <div key={`${row.machineId}-${row.shiftId}-${row.operatorId}`} className={styles.listRow}>
                <span>{row.machineName}</span>
                <span>{row.shiftName}</span>
                <span>{Math.round(row.productivityScore * 100)}%</span>
                <span>{formatDateTime(row.calculatedAt)}</span>
              </div>
            ))}
          </div>
        </section>
      </div>
    </section>
  );
}
