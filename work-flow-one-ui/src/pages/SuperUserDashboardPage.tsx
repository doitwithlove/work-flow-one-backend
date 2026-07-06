import { useEffect, useMemo, useState } from 'react';
import { fetchSuperUserDashboard } from '../api/superUserDashboardApi';
import { StatusBadge } from '../components/common/StatusBadge';
import { SuperUserDashboardResponse } from '../types/superUser';
import styles from './SuperUserDashboardPage.module.css';

const initialDashboard: SuperUserDashboardResponse = {
  totalMachines: 0,
  runningMachines: 0,
  idleMachines: 0,
  errorMachines: 0,
  stoppedMachines: 0,
  totalParts: 0,
  partsInProcess: 0,
  partsWaitingForTest: 0,
  partsPassed: 0,
  partsFailed: 0,
  partsReadyForNextPhase: 0,
  activeOperators: 0,
  activeSessions: 0,
  productivitySummary: [],
  machineProgressList: [],
  processStepProgressList: [],
  activeSessionList: [],
  recentTestResults: [],
  failedParts: [],
  partsWaitingForTestList: [],
  operatorList: [],
};

function formatDateTime(value: string | null | undefined) {
  if (!value) {
    return 'None';
  }

  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? 'Invalid date' : parsed.toLocaleString();
}

export function SuperUserDashboardPage() {
  const [dashboard, setDashboard] = useState<SuperUserDashboardResponse>(initialDashboard);
  const [error, setError] = useState('');

  useEffect(() => {
    let mounted = true;

    async function load() {
      try {
        const data = await fetchSuperUserDashboard();
        if (mounted) {
          setDashboard(data);
          setError('');
        }
      } catch (requestError) {
        if (mounted) {
          setError(requestError instanceof Error ? requestError.message : 'Unable to load super user dashboard.');
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

  const summaryCards = useMemo(
    () => [
      ['Total Machines', dashboard.totalMachines, 'MACHINES'],
      ['Running Machines', dashboard.runningMachines, 'RUNNING'],
      ['Idle Machines', dashboard.idleMachines, 'IDLE'],
      ['Error Machines', dashboard.errorMachines, 'ERROR'],
      ['Stopped Machines', dashboard.stoppedMachines, 'STOPPED'],
      ['Total Parts', dashboard.totalParts, 'PARTS'],
      ['Parts In Process', dashboard.partsInProcess, 'IN_PROCESS'],
      ['Waiting For Test', dashboard.partsWaitingForTest, 'TEST_PENDING'],
      ['Parts Passed', dashboard.partsPassed, 'PASS'],
      ['Parts Failed', dashboard.partsFailed, 'FAILED'],
      ['Ready For Next Phase', dashboard.partsReadyForNextPhase, 'READY_FOR_NEXT_PHASE'],
      ['Active Operators', dashboard.activeOperators, 'ACTIVE'],
    ],
    [dashboard],
  );

  return (
    <section className={styles.pageShell} aria-label="Super user dashboard">
      <div className={styles.headerRow}>
        <div>
          <p className={styles.eyebrow}>Factory-wide</p>
          <h2>Super user dashboard</h2>
        </div>
        {error && <span className={styles.error}>{error}</span>}
      </div>

      <div className={styles.summaryGrid}>
        {summaryCards.map(([label, value, status]) => (
          <article key={label as string} className={styles.card}>
            <span className={styles.label}>{label as string}</span>
            <strong>{value as number}</strong>
            <StatusBadge status={status as string} />
          </article>
        ))}
      </div>

      <div className={styles.sectionGrid}>
        <section className={styles.panel}>
          <h3>Machine progress</h3>
          <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Machine</th>
                  <th>Status</th>
                  <th>Part</th>
                  <th>Operator</th>
                  <th>Step</th>
                  <th>Progress</th>
                  <th>Last signal</th>
                </tr>
              </thead>
              <tbody>
                {dashboard.machineProgressList.map((row) => (
                  <tr key={row.machineId}>
                    <td>
                      <strong>{row.machineCode}</strong>
                      <div>{row.machineName}</div>
                    </td>
                    <td><StatusBadge status={row.status} /></td>
                    <td>{row.currentPartNumber || 'None'}</td>
                    <td>{row.currentOperatorName || row.currentOperatorId || 'None'}</td>
                    <td>{row.currentStepName || 'None'}</td>
                    <td>{Math.round(row.progressPercentage)}%</td>
                    <td>{formatDateTime(row.lastSignalAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        <section className={styles.panel}>
          <h3>Process step progress</h3>
          <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Step</th>
                  <th>Total</th>
                  <th>Completed</th>
                  <th>In process</th>
                  <th>Failed</th>
                  <th>Waiting</th>
                  <th>Progress</th>
                </tr>
              </thead>
              <tbody>
                {dashboard.processStepProgressList.map((row) => (
                  <tr key={row.stepId}>
                    <td>
                      <strong>{row.stepNumber}</strong>
                      <div>{row.stepName}</div>
                    </td>
                    <td>{row.totalParts}</td>
                    <td>{row.completedParts}</td>
                    <td>{row.inProcessParts}</td>
                    <td>{row.failedParts}</td>
                    <td>{row.waitingParts}</td>
                    <td>{Math.round(row.progressPercentage)}%</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        <section className={styles.panel}>
          <h3>Active operator sessions</h3>
          <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Operator</th>
                  <th>Machine</th>
                  <th>Shift</th>
                  <th>Login</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {dashboard.activeSessionList.map((row) => (
                  <tr key={row.id}>
                    <td>{row.operatorName || row.operatorId}</td>
                    <td>{row.machineName || row.machineId}</td>
                    <td>{row.shiftName || row.shiftId}</td>
                    <td>{formatDateTime(row.loginTime)}</td>
                    <td><StatusBadge status={row.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        <section className={styles.panel}>
          <h3>Recent test results</h3>
          <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Part</th>
                  <th>Machine</th>
                  <th>Type</th>
                  <th>Result</th>
                  <th>Tested at</th>
                </tr>
              </thead>
              <tbody>
                {dashboard.recentTestResults.map((row) => (
                  <tr key={row.id}>
                    <td>{row.partId}</td>
                    <td>{row.machineId}</td>
                    <td>{row.testType}</td>
                    <td><StatusBadge status={row.result} /></td>
                    <td>{formatDateTime(row.testedAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        <section className={styles.panel}>
          <h3>Failed parts</h3>
          <div className={styles.list}>
            {dashboard.failedParts.length ? (
              dashboard.failedParts.map((part) => (
                <div key={part.id} className={styles.listRow}>
                  <strong>{part.partNumber}</strong>
                  <span>{part.batchNumber}</span>
                  <StatusBadge status={part.status} />
                </div>
              ))
            ) : (
              <p className={styles.emptyState}>No failed parts.</p>
            )}
          </div>
        </section>
      </div>
    </section>
  );
}
