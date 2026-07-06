import { FormEvent, useEffect, useState } from 'react';
import { createPart, fetchParts } from '../api/partsApi';
import { PartSummary } from '../types/manufacturing';
import { StatusBadge } from '../components/common/StatusBadge';
import styles from './PartsPage.module.css';

type PartsPageProps = {
  onOpenPart: (partId: string) => void;
};

export function PartsPage({ onOpenPart }: PartsPageProps) {
  const [parts, setParts] = useState<PartSummary[]>([]);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function loadParts() {
    try {
      setParts(await fetchParts());
      setError('');
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Unable to load parts.');
    }
  }

  useEffect(() => {
    void loadParts();
  }, []);

  async function onCreatePart(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setBusy(true);
    try {
      await createPart({
        partNumber: String(form.get('partNumber') || ''),
        batchNumber: String(form.get('batchNumber') || ''),
        currentStepId: String(form.get('currentStepId') || '') || null,
      });
      event.currentTarget.reset();
      await loadParts();
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Unable to create part.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.pageShell}>
      <div className={styles.headerRow}>
        <div>
          <p className={styles.eyebrow}>Manufacturing</p>
          <h2>Parts</h2>
        </div>
        {error && <span className={styles.error}>{error}</span>}
      </div>

      <form className={styles.createForm} onSubmit={onCreatePart}>
        <input name="partNumber" placeholder="Part number" required />
        <input name="batchNumber" placeholder="Batch number" required />
        <input name="currentStepId" placeholder="Current step id (optional)" />
        <button type="submit" disabled={busy}>Create part</button>
      </form>

      <div className={styles.tableWrap}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Part Number</th>
              <th>Batch Number</th>
              <th>Current Step</th>
              <th>Current Machine</th>
              <th>Status</th>
              <th>Test Status</th>
            </tr>
          </thead>
          <tbody>
            {parts.map((part) => (
              <tr key={part.id} onClick={() => onOpenPart(part.id)} role="button" tabIndex={0}>
                <td>{part.partNumber}</td>
                <td>{part.batchNumber}</td>
                <td>{part.currentStepId || 'None'}</td>
                <td>{part.currentMachineId || 'None'}</td>
                <td><StatusBadge status={part.status} /></td>
                <td><StatusBadge status={part.testStatus} /></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
