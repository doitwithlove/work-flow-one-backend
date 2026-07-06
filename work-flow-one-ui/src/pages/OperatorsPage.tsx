import { FormEvent, useEffect, useState } from 'react';
import { createOperator, fetchOperators } from '../api/operatorsApi';
import { Operator } from '../types/manufacturing';
import { StatusBadge } from '../components/common/StatusBadge';
import styles from './OperatorsPage.module.css';

type OperatorsPageProps = {
  onOpenOperator: (operatorId: string) => void;
};

export function OperatorsPage({ onOpenOperator }: OperatorsPageProps) {
  const [operators, setOperators] = useState<Operator[]>([]);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function loadOperators() {
    try {
      setOperators(await fetchOperators());
      setError('');
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Unable to load operators.');
    }
  }

  useEffect(() => {
    void loadOperators();
  }, []);

  async function onCreateOperator(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setBusy(true);
    try {
      await createOperator({
        employeeCode: String(form.get('employeeCode') || ''),
        firstName: String(form.get('firstName') || ''),
        lastName: String(form.get('lastName') || ''),
        role: String(form.get('role') || ''),
        skillLevel: String(form.get('skillLevel') || ''),
        active: form.get('active') !== null,
      });
      event.currentTarget.reset();
      await loadOperators();
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Unable to create operator.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.pageShell}>
      <div className={styles.headerRow}>
        <div>
          <p className={styles.eyebrow}>Manufacturing</p>
          <h2>Operators</h2>
        </div>
        {error && <span className={styles.error}>{error}</span>}
      </div>

      <form className={styles.createForm} onSubmit={onCreateOperator}>
        <input name="employeeCode" placeholder="Employee code" required />
        <input name="firstName" placeholder="First name" required />
        <input name="lastName" placeholder="Last name" required />
        <input name="role" placeholder="Role" required />
        <input name="skillLevel" placeholder="Skill level" required />
        <label className={styles.checkbox}>
          <input name="active" type="checkbox" defaultChecked />
          Active
        </label>
        <button type="submit" disabled={busy}>Create operator</button>
      </form>

      <div className={styles.tableWrap}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Employee Code</th>
              <th>Name</th>
              <th>Role</th>
              <th>Skill Level</th>
              <th>Active</th>
            </tr>
          </thead>
          <tbody>
            {operators.map((operator) => (
              <tr key={operator.id} onClick={() => onOpenOperator(operator.id)} role="button" tabIndex={0}>
                <td>{operator.employeeCode}</td>
                <td>{operator.firstName} {operator.lastName}</td>
                <td>{operator.role}</td>
                <td>{operator.skillLevel}</td>
                <td><StatusBadge status={operator.active ? 'ACTIVE' : 'INACTIVE'} /></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
