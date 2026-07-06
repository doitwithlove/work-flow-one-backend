import { FormEvent, useState } from 'react';
import { createMachineEvent } from '../api/machineEventsApi';
import { MachineEventType } from '../types/manufacturing';
import styles from './MachineEventSimulatorPage.module.css';

export function MachineEventSimulatorPage() {
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setBusy(true);
    setMessage('');

    try {
      await createMachineEvent({
        machineId: String(form.get('machineId') || ''),
        partId: String(form.get('partId') || ''),
        eventType: form.get('eventType') as MachineEventType,
        status: String(form.get('status') || ''),
        payload: {
          temperature: Number(form.get('temperature') || 0),
          speed: Number(form.get('speed') || 0),
          durationSeconds: Number(form.get('durationSeconds') || 0),
        },
      });
      setMessage('Machine event saved.');
      event.currentTarget.reset();
    } catch (requestError) {
      setMessage(requestError instanceof Error ? requestError.message : 'Unable to save machine event.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.pageShell}>
      <div>
        <p className={styles.eyebrow}>Manufacturing</p>
        <h2>Machine event simulator</h2>
      </div>

      <form className={styles.formGrid} onSubmit={onSubmit}>
        <input name="machineId" placeholder="Machine ID" required />
        <input name="partId" placeholder="Part ID" required />
        <select name="eventType" defaultValue="PROCESS_COMPLETED">
          <option value="PROCESS_STARTED">PROCESS_STARTED</option>
          <option value="PROCESS_COMPLETED">PROCESS_COMPLETED</option>
          <option value="STATUS_CHANGED">STATUS_CHANGED</option>
          <option value="HEARTBEAT">HEARTBEAT</option>
          <option value="ERROR">ERROR</option>
          <option value="QUALITY_RECORDED">QUALITY_RECORDED</option>
        </select>
        <input name="status" placeholder="Status" required />
        <input name="temperature" type="number" step="0.1" placeholder="Temperature" />
        <input name="speed" type="number" step="1" placeholder="Speed" />
        <input name="durationSeconds" type="number" step="1" placeholder="Duration seconds" />
        <button type="submit" disabled={busy}>Send event</button>
      </form>

      {message && <p className={styles.message}>{message}</p>}
    </section>
  );
}
