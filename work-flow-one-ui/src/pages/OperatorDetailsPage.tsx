import { useEffect, useState } from 'react';
import { fetchOperator } from '../api/operatorsApi';
import { fetchOperatorSessionsByOperator } from '../api/operatorSessionsApi';
import { fetchProductivityByOperator } from '../api/productivityApi';
import { Operator, OperatorMachineSession, ProductivitySummary } from '../types/manufacturing';
import { StatusBadge } from '../components/common/StatusBadge';
import styles from './OperatorDetailsPage.module.css';

type OperatorDetailsPageProps = {
  operatorId: string | null;
};

export function OperatorDetailsPage({ operatorId }: OperatorDetailsPageProps) {
  const [operator, setOperator] = useState<Operator | null>(null);
  const [sessions, setSessions] = useState<OperatorMachineSession[]>([]);
  const [productivity, setProductivity] = useState<ProductivitySummary[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    let mounted = true;

    async function load() {
      if (!operatorId) {
        setOperator(null);
        setSessions([]);
        setProductivity([]);
        return;
      }

      try {
        const [nextOperator, nextSessions, nextProductivity] = await Promise.all([
          fetchOperator(operatorId),
          fetchOperatorSessionsByOperator(operatorId),
          fetchProductivityByOperator(operatorId),
        ]);
        if (mounted) {
          setOperator(nextOperator);
          setSessions(nextSessions);
          setProductivity(nextProductivity);
          setError('');
        }
      } catch (requestError) {
        if (mounted) {
          setError(requestError instanceof Error ? requestError.message : 'Unable to load operator details.');
        }
      }
    }

    void load();
    return () => {
      mounted = false;
    };
  }, [operatorId]);

  if (!operatorId) {
    return (
      <section className={styles.pageShell}>
        <p className={styles.emptyState}>Select an operator to view details.</p>
      </section>
    );
  }

  return (
    <section className={styles.pageShell}>
      <div className={styles.headerRow}>
        <div>
          <p className={styles.eyebrow}>Manufacturing</p>
          <h2>Operator details</h2>
        </div>
        {error && <span className={styles.error}>{error}</span>}
      </div>

      {operator && (
        <article className={styles.summaryCard}>
          <div>
            <strong>{operator.firstName} {operator.lastName}</strong>
            <p>{operator.employeeCode}</p>
          </div>
          <div className={styles.metaRow}>
            <StatusBadge status={operator.role} />
            <StatusBadge status={operator.skillLevel} />
            <StatusBadge status={operator.active ? 'ACTIVE' : 'CLOSED'} />
          </div>
        </article>
      )}

      <div className={styles.columns}>
        <section className={styles.card}>
          <h3>Sessions</h3>
          <div className={styles.list}>
            {sessions.map((session) => (
              <div key={session.id} className={styles.listRow}>
                <StatusBadge status={session.status} />
                <span>{session.machineId}</span>
                <span>{session.shiftName}</span>
              </div>
            ))}
          </div>
        </section>

        <section className={styles.card}>
          <h3>Productivity</h3>
          <div className={styles.list}>
            {productivity.map((row) => (
              <div key={`${row.operatorId}-${row.shiftId}-${row.machineId}`} className={styles.listRow}>
                <StatusBadge status="PRODUCTION" />
                <span>{row.machineName}</span>
                <span>{Math.round(row.productivityScore * 100)}%</span>
              </div>
            ))}
          </div>
        </section>
      </div>
    </section>
  );
}
