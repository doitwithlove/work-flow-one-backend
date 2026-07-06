import { FormEvent, useEffect, useState } from 'react';
import { fetchMachines } from '../api/machinesApi';
import { fetchOperators } from '../api/operatorsApi';
import { fetchShifts } from '../api/shiftsApi';
import { endOperatorSession, fetchActiveOperatorSessions, startOperatorSession } from '../api/operatorSessionsApi';
import { useAuth } from '../context/AuthContext';
import { MachineSummary, Operator, OperatorMachineSession, Shift } from '../types/manufacturing';
import { StatusBadge } from '../components/common/StatusBadge';
import styles from './OperatorSessionPage.module.css';

function formatDateTime(value: string | null | undefined) {
  if (!value) {
    return 'None';
  }

  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? 'Invalid date' : parsed.toLocaleString();
}

export function OperatorSessionPage() {
  const { currentUser, session, hasAnyRole } = useAuth();
  const [operators, setOperators] = useState<Operator[]>([]);
  const [machines, setMachines] = useState<MachineSummary[]>([]);
  const [shifts, setShifts] = useState<Shift[]>([]);
  const [sessions, setSessions] = useState<OperatorMachineSession[]>([]);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const canSelectAnyOperator = hasAnyRole(['ROLE_ADMIN', 'ROLE_SUPER_USER', 'ROLE_MANAGER', 'ROLE_SUPERVISOR']);

  async function load() {
    try {
      const requests: Promise<unknown>[] = [
        fetchMachines(),
        fetchShifts(),
        fetchActiveOperatorSessions(),
      ];

      if (canSelectAnyOperator) {
        requests.unshift(fetchOperators());
      }

      const results = await Promise.all(requests);
      if (canSelectAnyOperator) {
        const [nextOperators, nextMachines, nextShifts, nextSessions] = results as [
          Operator[],
          MachineSummary[],
          Shift[],
          OperatorMachineSession[],
        ];
        setOperators(nextOperators);
        setMachines(nextMachines);
        setShifts(nextShifts);
        setSessions(nextSessions);
      } else {
        const [nextMachines, nextShifts, nextSessions] = results as [
          MachineSummary[],
          Shift[],
          OperatorMachineSession[],
        ];
        setOperators(
          session
            ? [
                {
                  id: currentUser?.id || session.userId,
                  employeeCode: currentUser?.username || session.username,
                  firstName: currentUser?.fullName?.split(' ')[0] || session.username,
                  lastName: currentUser?.fullName?.split(' ').slice(1).join(' ') || '',
                  role: 'OPERATOR',
                  skillLevel: 'STANDARD',
                  active: currentUser?.active ?? true,
                  createdAt: currentUser?.createdAt || new Date().toISOString(),
                  updatedAt: currentUser?.createdAt || new Date().toISOString(),
                },
              ]
            : [],
        );
        setMachines(nextMachines);
        setShifts(nextShifts);
        setSessions(nextSessions);
      }
      setError('');
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Unable to load operator sessions.');
    }
  }

  useEffect(() => {
    void load();
  }, []);

  async function onStart(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setBusy(true);
    try {
      await startOperatorSession({
        operatorId: String(form.get('operatorId') || ''),
        machineId: String(form.get('machineId') || ''),
        shiftId: String(form.get('shiftId') || ''),
      });
      event.currentTarget.reset();
      await load();
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Unable to start session.');
    } finally {
      setBusy(false);
    }
  }

  async function onEnd(sessionId: string) {
    setBusy(true);
    try {
      await endOperatorSession({ sessionId });
      await load();
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Unable to end session.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.pageShell}>
      <div className={styles.headerRow}>
        <div>
          <p className={styles.eyebrow}>Manufacturing</p>
          <h2>Operator sessions</h2>
        </div>
        {error && <span className={styles.error}>{error}</span>}
      </div>

      <form className={styles.createForm} onSubmit={onStart}>
        {canSelectAnyOperator ? (
          <select name="operatorId" required defaultValue="">
            <option value="" disabled>Select operator</option>
            {operators.map((operator) => (
              <option key={operator.id} value={operator.id}>{operator.employeeCode} - {operator.firstName} {operator.lastName}</option>
            ))}
          </select>
        ) : (
          <>
            <input type="hidden" name="operatorId" value={session?.userId || currentUser?.id || ''} />
            <div className={styles.readonlyField}>
              <span>Operator</span>
              <strong>{currentUser?.fullName || currentUser?.username || session?.username || 'Current user'}</strong>
            </div>
          </>
        )}
        <select name="machineId" required defaultValue="">
          <option value="" disabled>Select machine</option>
          {machines.map((machine) => (
            <option key={machine.id} value={machine.machineCode}>{machine.machineCode} - {machine.name}</option>
          ))}
        </select>
        <select name="shiftId" required defaultValue="">
          <option value="" disabled>Select shift</option>
          {shifts.map((shift) => (
            <option key={shift.id} value={shift.id}>{shift.name}</option>
          ))}
        </select>
        <button type="submit" disabled={busy}>Start session</button>
      </form>

      <div className={styles.tableWrap}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Operator</th>
              <th>Machine</th>
              <th>Shift</th>
              <th>Login Time</th>
              <th>Logout Time</th>
              <th>Status</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {sessions.map((session) => (
              <tr key={session.id}>
                <td>{session.operatorName}</td>
                <td>{session.machineName}</td>
                <td>{session.shiftName}</td>
                <td>{formatDateTime(session.loginTime)}</td>
                <td>{formatDateTime(session.logoutTime)}</td>
                <td><StatusBadge status={session.status} /></td>
                <td>
                  <button type="button" onClick={() => void onEnd(session.id)} disabled={busy}>
                    End
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
