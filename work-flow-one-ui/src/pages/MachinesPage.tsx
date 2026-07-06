import { useEffect, useState } from 'react';
import { fetchMachines } from '../api/machinesApi';
import { MachineSummary } from '../types/manufacturing';
import { StatusBadge } from '../components/common/StatusBadge';
import styles from './MachinesPage.module.css';

function formatDateTime(value: string | null | undefined) {
  if (!value) {
    return 'None';
  }

  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? 'Invalid date' : parsed.toLocaleString();
}

export function MachinesPage() {
  const [machines, setMachines] = useState<MachineSummary[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    void fetchMachines()
      .then((data) => {
        if (active) {
          setMachines(data);
          setError('');
        }
      })
      .catch((requestError) => {
        if (active) {
          setError(requestError instanceof Error ? requestError.message : 'Unable to load machines.');
        }
      });
    return () => {
      active = false;
    };
  }, []);

  return (
    <section className={styles.pageShell}>
      <div className={styles.headerRow}>
        <div>
          <p className={styles.eyebrow}>Manufacturing</p>
          <h2>Machines</h2>
        </div>
        {error && <span className={styles.error}>{error}</span>}
      </div>

      <div className={styles.tableWrap}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Machine Code</th>
              <th>Name</th>
              <th>Type</th>
              <th>Status</th>
              <th>Last Signal Time</th>
              <th>Current Operator</th>
              <th>Active Session Start</th>
            </tr>
          </thead>
          <tbody>
            {machines.map((machine) => (
              <tr key={machine.id}>
                <td>{machine.machineCode}</td>
                <td>{machine.name}</td>
                <td>{machine.type}</td>
                <td><StatusBadge status={machine.status} /></td>
                <td>{formatDateTime(machine.lastSignalAt)}</td>
                <td>{machine.currentOperatorName || 'Unassigned'}</td>
                <td>{formatDateTime(machine.activeSessionStartTime)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
