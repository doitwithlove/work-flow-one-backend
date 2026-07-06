import { useEffect, useState } from 'react';
import { fetchPartHistory, movePartNext } from '../api/partsApi';
import { fetchTestResults } from '../api/testResultsApi';
import { PartHistory, PartSummary, TestResult } from '../types/manufacturing';
import { StatusBadge } from '../components/common/StatusBadge';
import { TestResultForm } from '../components/manufacturing/TestResultForm';
import styles from './PartDetailsPage.module.css';

type PartDetailsPageProps = {
  partId: string | null;
};

export function PartDetailsPage({ partId }: PartDetailsPageProps) {
  const [history, setHistory] = useState<PartHistory | null>(null);
  const [testResults, setTestResults] = useState<TestResult[]>([]);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function load() {
    if (!partId) {
      setHistory(null);
      setTestResults([]);
      return;
    }

    try {
      const [nextHistory, nextResults] = await Promise.all([fetchPartHistory(partId), fetchTestResults(partId)]);
      setHistory(nextHistory);
      setTestResults(nextResults);
      setError('');
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Unable to load part details.');
    }
  }

  useEffect(() => {
    void load();
  }, [partId]);

  async function onMoveNext() {
    if (!partId) {
      return;
    }
    setBusy(true);
    try {
      await movePartNext(partId);
      await load();
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Unable to move part.');
    } finally {
      setBusy(false);
    }
  }

  const part = history?.part ?? null;
  const latestEvent = history?.machineEvents?.[0] || null;

  if (!partId) {
    return (
      <section className={styles.pageShell}>
        <p className={styles.emptyState}>Select a part to view details.</p>
      </section>
    );
  }

  return (
    <section className={styles.pageShell}>
      <div className={styles.headerRow}>
        <div>
          <p className={styles.eyebrow}>Manufacturing</p>
          <h2>Part details</h2>
        </div>
        {error && <span className={styles.error}>{error}</span>}
      </div>

      {part && (
        <article className={styles.summaryCard}>
          <div>
            <strong>{part.partNumber}</strong>
            <p>{part.batchNumber}</p>
          </div>
          <div className={styles.badgeRow}>
            <StatusBadge status={part.status} />
            <StatusBadge status={part.testStatus} />
          </div>
          <div className={styles.metaRow}>
            <span>Current step: {part.currentStepId || 'None'}</span>
            <span>Current machine: {part.currentMachineId || 'None'}</span>
            <span>Operator: {latestEvent?.operatorId || 'Unassigned'}</span>
            <span>Operator session: {latestEvent?.operatorSessionId || 'None'}</span>
          </div>
          <button type="button" disabled={part.status !== 'READY_FOR_NEXT_PHASE' || busy} onClick={onMoveNext}>
            Move to Next Phase
          </button>
        </article>
      )}

      <div className={styles.columns}>
        <section className={styles.card}>
          <h3>Machine event history</h3>
          <div className={styles.list}>
            {history?.machineEvents.map((event) => (
              <div key={event.id} className={styles.listRow}>
                <StatusBadge status={event.status} />
                <span>{event.eventType}</span>
                <span>{event.operatorId || 'UNASSIGNED'}</span>
                <span>{new Date(event.receivedAt).toLocaleString()}</span>
              </div>
            ))}
          </div>
        </section>

        <section className={styles.card}>
          <h3>Test result history</h3>
          <div className={styles.list}>
            {testResults.map((result) => (
              <div key={result.id} className={styles.listRow}>
                <StatusBadge status={result.result} />
                <span>{result.testType}</span>
                <span>{new Date(result.testedAt).toLocaleString()}</span>
              </div>
            ))}
          </div>
        </section>
      </div>

      <TestResultForm onSaved={load} />
    </section>
  );
}
