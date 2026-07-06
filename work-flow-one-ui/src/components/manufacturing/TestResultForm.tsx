import { FormEvent, useState } from 'react';
import { createTestResult } from '../../api/testResultsApi';
import styles from './TestResultForm.module.css';

type TestResultFormProps = {
  onSaved?: () => void;
};

export function TestResultForm({ onSaved }: TestResultFormProps) {
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState('');

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setBusy(true);
    setMessage('');

    try {
      await createTestResult({
        partId: String(form.get('partId') || ''),
        machineId: String(form.get('machineId') || ''),
        testType: String(form.get('testType') || ''),
        expectedValue: Number(form.get('expectedValue') || 0),
        actualValue: Number(form.get('actualValue') || 0),
        toleranceMin: Number(form.get('toleranceMin') || 0),
        toleranceMax: Number(form.get('toleranceMax') || 0),
        result: form.get('result') === 'FAIL' ? 'FAIL' : 'PASS',
      });
      setMessage('Test result saved.');
      event.currentTarget.reset();
      onSaved?.();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'Unable to save test result.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h3>Test result</h3>
      <form className={styles.formGrid} onSubmit={onSubmit}>
        <input name="partId" placeholder="Part ID" required />
        <input name="machineId" placeholder="Machine ID" required />
        <input name="testType" placeholder="Test type" required />
        <input name="expectedValue" type="number" step="0.001" placeholder="Expected value" required />
        <input name="actualValue" type="number" step="0.001" placeholder="Actual value" required />
        <input name="toleranceMin" type="number" step="0.001" placeholder="Tolerance min" required />
        <input name="toleranceMax" type="number" step="0.001" placeholder="Tolerance max" required />
        <select name="result" defaultValue="PASS">
          <option value="PASS">PASS</option>
          <option value="FAIL">FAIL</option>
        </select>
        <button type="submit" disabled={busy}>Save result</button>
      </form>
      {message && <p className={styles.message}>{message}</p>}
    </section>
  );
}
