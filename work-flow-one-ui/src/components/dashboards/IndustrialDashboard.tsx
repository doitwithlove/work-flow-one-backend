import { useEffect, useMemo, useState } from 'react';
import { RefreshCw } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { apiClient } from '../../lib/apiClient';
import styles from './IndustrialDashboard.module.css';

const SAMPLE_MACHINE_IDS = ['HURCO-M10-01', 'TURMIK-Q15-01'] as const;

type TelemetryRow = {
  timestamp: string;
  machineId: string;
  jobId: string;
  partNumber: string;
  program: string;
  status: string;
  spindleRpm: number;
  feedRate: number;
  axisX: number;
  axisY: number;
  axisZ: number;
  axisC: number;
  tool: string;
  toolLifePct: number;
  spindleLoadPct: number;
  servoLoadPct: number;
  temperatureC: number;
  vibrationMmSec: number;
  coolantStatus: string;
  hydraulicPressureBar: number;
  lubricationStatus: string;
  cycleTimeSec: number;
  alarmCode: string;
  powerKW: number;
  partCount: number;
};

type OeeRow = {
  date: string;
  shift: string;
  machineId: string;
  availabilityPct: number;
  performancePct: number;
  qualityPct: number;
  oeePct: number;
};

type MachineRow = {
  machineId: string;
  name: string;
  createdAt: string;
  status: string;
};

type MachineDashboard = {
  machine: MachineRow | null;
  telemetry: TelemetryRow[];
  oeeRows: OeeRow[];
  alarms: AlarmRow[];
  error: string;
};

type AlarmRow = {
  timestamp: string;
  alarmCode: string;
  alarmType: string;
  severity: string;
  description: string;
  durationSec: number;
  status: string;
};

function formatTime(value: string) {
  return new Date(value).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function sparkPoints(values: number[]) {
  const max = Math.max(...values);
  if (!values.length || !Number.isFinite(max) || max <= 0) {
    return '0,100 33,100 66,100 100,100';
  }

  return values
    .map((value, index) => {
      const x = (index / Math.max(1, values.length - 1)) * 100;
      const y = 100 - (Math.max(0, value) / max) * 100;
      return `${x},${y}`;
    })
    .join(' ');
}

function statusTone(status: string) {
  if (status === 'MAINTENANCE') {
    return 'maint';
  }
  return status.toLowerCase();
}

export function IndustrialDashboard() {
  const { session } = useAuth();
  const [machines, setMachines] = useState<Record<string, MachineDashboard>>({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [refreshTick, setRefreshTick] = useState(0);

  useEffect(() => {
    let active = true;

    async function loadDashboard() {
      setLoading(true);
      setError('');
      try {
        const results = await Promise.all(
          SAMPLE_MACHINE_IDS.map(async (machineId) => {
            const [machineResponse, telemetryResponse, oeeResponse, alarmResponse] = await Promise.allSettled([
              apiClient.get<MachineRow>(`/sample/machine?machineId=${machineId}`),
              apiClient.get<TelemetryRow[]>(`/sample/telemetry?machineId=${machineId}&limit=240`),
              apiClient.get<OeeRow[]>(`/sample/oee?machineId=${machineId}`),
              apiClient.get<AlarmRow[]>(`/sample/alarms?machineId=${machineId}&limit=12`),
            ]);

            const endpointErrors: string[] = [];

            const machine = machineResponse.status === 'fulfilled' ? machineResponse.value.data ?? null : null;
            const telemetry = telemetryResponse.status === 'fulfilled' ? telemetryResponse.value.data ?? [] : [];
            const oeeRows = oeeResponse.status === 'fulfilled' ? oeeResponse.value.data ?? [] : [];
            const alarms = alarmResponse.status === 'fulfilled' ? alarmResponse.value.data ?? [] : [];

            if (machineResponse.status === 'rejected') {
              endpointErrors.push(`machine(${machineId})`);
            }
            if (telemetryResponse.status === 'rejected') {
              endpointErrors.push(`telemetry(${machineId})`);
            }
            if (oeeResponse.status === 'rejected') {
              endpointErrors.push(`oee(${machineId})`);
            }
            if (alarmResponse.status === 'rejected') {
              endpointErrors.push(`alarms(${machineId})`);
            }

            return {
              machineId,
              machine,
              telemetry,
              oeeRows,
              alarms,
              error: endpointErrors.length ? endpointErrors.join(', ') : '',
            };
          }),
        );

        if (!active) {
          return;
        }

        const nextMachines: Record<string, MachineDashboard> = {};
        const failedEndpoints: string[] = [];

        results.forEach((result) => {
          nextMachines[result.machineId] = {
            machine: result.machine,
            telemetry: result.telemetry,
            oeeRows: result.oeeRows,
            alarms: result.alarms,
            error: result.error,
          };
          if (result.error) {
            failedEndpoints.push(result.error);
          }
        });

        setMachines(nextMachines);
        setError(failedEndpoints.length ? `Some dashboard endpoints failed: ${failedEndpoints.join(', ')}` : '');
      } catch (fetchError) {
        if (!active) {
          return;
        }
        setMachines({});
        setError(fetchError instanceof Error ? fetchError.message : 'Unable to load sample dashboard data.');
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    void loadDashboard();
    return () => {
      active = false;
    };
  }, [session, refreshTick]);

  const machineCards = useMemo(
    () =>
      SAMPLE_MACHINE_IDS.map((machineId) => {
        const snapshot = machines[machineId];
        const telemetry = snapshot?.telemetry ?? [];
        const latest = telemetry[0] ?? null;
        const statusCounts = telemetry.reduce<Record<string, number>>((result, row) => {
          result[row.status] = (result[row.status] ?? 0) + 1;
          return result;
        }, {});
        const statusTotal = Object.values(statusCounts).reduce((sum, value) => sum + value, 0);
        const machineCreatedAt = snapshot?.machine?.createdAt ? new Date(snapshot.machine.createdAt).toLocaleDateString([], { dateStyle: 'medium' }) : 'Not set';
        const machineTimeline = [
          { label: 'RUNNING', value: statusTotal ? Math.round(((statusCounts.RUNNING ?? 0) / statusTotal) * 100) : 0, tone: 'running' },
          { label: 'IDLE', value: statusTotal ? Math.round(((statusCounts.IDLE ?? 0) / statusTotal) * 100) : 0, tone: 'idle' },
          { label: 'SETUP', value: statusTotal ? Math.round(((statusCounts.SETUP ?? 0) / statusTotal) * 100) : 0, tone: 'setup' },
          { label: 'MAINT.', value: statusTotal ? Math.round(((statusCounts.MAINTENANCE ?? 0) / statusTotal) * 100) : 0, tone: 'maint' },
          { label: 'ALARM', value: statusTotal ? Math.round(((statusCounts.ALARM ?? 0) / statusTotal) * 100) : 0, tone: 'alarm' },
        ];
        const shiftOee = (snapshot?.oeeRows ?? []).map((row) => ({
          shift: row.shift,
          availability: row.availabilityPct,
          performance: row.performancePct,
          quality: row.qualityPct,
          oee: row.oeePct,
        }));
        const alarmRows = (snapshot?.alarms ?? []).map((alarm) => ({
          code: alarm.alarmCode,
          severity: alarm.severity || 'LOW',
          title: alarm.description,
          when: formatTime(alarm.timestamp),
        }));
        const productionSeries = telemetry.slice(0, 12).reverse().map((row) => row.partCount);
        const energySeries = telemetry.slice(0, 12).reverse().map((row) => row.powerKW);

        return {
          machineId,
          snapshot,
          latest,
          statusTotal,
          machineCreatedAt,
          machineTimeline,
          shiftOee,
          alarmRows,
          productionSeries,
          energySeries,
        };
      }),
    [machines],
  );

  return (
    <section className={styles.dashboardShell} id="analytics">
      <div className={styles.headingRow}>
        <div>
          <p className={styles.eyebrow}>Dashboards</p>
          <h2>Industrial sample pages</h2>
        </div>
        <div className={styles.dashboardMeta}>
          <button className={styles.refreshButton} type="button" onClick={() => setRefreshTick((value) => value + 1)} aria-label="Refresh sample data">
            <RefreshCw size={16} />
            Refresh
          </button>
          {loading && <span className={styles.metaPill}>Loading sample data</span>}
          {error && <span className={`${styles.metaPill} ${styles.errorPill}`}>{error}</span>}
          {!session && <span className={styles.metaPill}>Sign in to load live sample data</span>}
          {machineCards.some((card) => card.snapshot?.machine) && <span className={styles.metaPill}>Both machines loaded</span>}
        </div>
      </div>

      <div className={styles.machineStrip}>
        {machineCards.map((card) => (
          <article key={card.machineId} className={styles.machineCard}>
            <div className={styles.cardHeading}>
              <div>
                <p className={styles.cardEyebrow}>{card.machineId}</p>
                <h3>{card.snapshot?.machine?.name ?? card.latest?.machineId ?? card.machineId}</h3>
              </div>
              <span className={`${styles.statusBadge} ${styles[statusTone(card.latest?.status ?? card.snapshot?.machine?.status ?? 'RUNNING')]}`}>
                {card.latest?.status ?? card.snapshot?.machine?.status ?? 'RUNNING'}
              </span>
            </div>
            <div className={styles.metricRow}>
              <div>
                <span>Cycle</span>
                <strong>{card.latest ? `${card.latest.cycleTimeSec || 0} sec` : '0 sec'}</strong>
              </div>
              <div>
                <span>Created</span>
                <strong>{card.machineCreatedAt}</strong>
              </div>
              <div>
                <span>Spindle</span>
                <strong>{card.latest ? `${card.latest.spindleRpm.toLocaleString()} rpm` : '0 rpm'}</strong>
              </div>
            </div>
          </article>
        ))}
      </div>

      <div className={styles.grid}>
        <article className={styles.card}>
          <div className={styles.cardHeading}>
            <div>
              <p className={styles.cardEyebrow}>Machine status</p>
              <h3>Hurco M10 and Turmik Q15</h3>
            </div>
          </div>

          {machineCards.map((card) => (
            <div key={`${card.machineId}-timeline`} className={styles.machineTimelineBlock}>
              <div className={styles.timelineHeader}>
                <strong>{card.machineId}</strong>
                <span>{card.machineCreatedAt}</span>
              </div>
              <div className={styles.timeline}>
                {card.machineTimeline.map((item) => (
                  <div key={`${card.machineId}-${item.label}`} className={styles.timelineRow}>
                    <span>{item.label}</span>
                    <div className={styles.timelineTrack}>
                      <div className={`${styles.timelineFill} ${styles[item.tone]}`} style={{ width: `${Math.min(100, item.value)}%` }} />
                    </div>
                    <strong>{item.value}%</strong>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </article>

        <article className={styles.card}>
          <div className={styles.cardHeading}>
            <div>
              <p className={styles.cardEyebrow}>OEE</p>
              <h3>Shift comparison</h3>
            </div>
          </div>

          <div className={styles.kpiGrid}>
            <div>
              <span>Availability</span>
              <strong>{machineCards[0]?.shiftOee[0] ? `${machineCards[0].shiftOee[0].availability.toFixed(0)}%` : '0%'}</strong>
            </div>
            <div>
              <span>Performance</span>
              <strong>{machineCards[0]?.shiftOee[0] ? `${machineCards[0].shiftOee[0].performance.toFixed(0)}%` : '0%'}</strong>
            </div>
            <div>
              <span>Quality</span>
              <strong>{machineCards[0]?.shiftOee[0] ? `${machineCards[0].shiftOee[0].quality.toFixed(0)}%` : '0%'}</strong>
            </div>
            <div>
              <span>OEE</span>
              <strong>{machineCards[0]?.shiftOee[0] ? `${machineCards[0].shiftOee[0].oee.toFixed(0)}%` : '0%'}</strong>
            </div>
          </div>

          <div className={styles.shiftTable}>
            {machineCards.flatMap((card) =>
              card.shiftOee.map((shift) => (
                <div key={`${card.machineId}-${shift.shift}`} className={styles.shiftRow}>
                  <strong>{card.machineId} Shift {shift.shift}</strong>
                  <span>A {shift.availability}%</span>
                  <span>P {shift.performance}%</span>
                  <span>Q {shift.quality}%</span>
                  <span>OEE {shift.oee}%</span>
                </div>
              )),
            )}
          </div>
        </article>

        <article className={styles.card}>
          <div className={styles.cardHeading}>
            <div>
              <p className={styles.cardEyebrow}>Alarms</p>
              <h3>Active and recent</h3>
            </div>
          </div>

          <div className={styles.alarmGrid}>
            {machineCards.flatMap((card) =>
              card.alarmRows.map((alarm) => (
                <div key={`${card.machineId}-${alarm.code}-${alarm.when}`} className={styles.alarmRow}>
                  <span className={`${styles.severity} ${styles[alarm.severity.toLowerCase()]}`}>{alarm.severity}</span>
                  <div>
                    <strong>{card.machineId} {alarm.code}</strong>
                    <p>{alarm.title}</p>
                  </div>
                  <time>{alarm.when}</time>
                </div>
              )),
            )}
            {!machineCards.some((card) => card.alarmRows.length) && <p className={styles.emptyState}>No alarm rows were returned.</p>}
          </div>
        </article>

        <article className={styles.card}>
          <div className={styles.cardHeading}>
            <div>
              <p className={styles.cardEyebrow}>Production</p>
              <h3>Throughput and energy</h3>
            </div>
          </div>

          <div className={styles.chartWrap}>
            <svg viewBox="0 0 100 100" className={styles.chart}>
              <polyline points={sparkPoints(machineCards[0]?.productionSeries.length ? machineCards[0].productionSeries : [0, 0, 0, 0])} className={styles.productionLine} />
            </svg>
            <svg viewBox="0 0 100 100" className={styles.chart}>
              <polyline points={sparkPoints(machineCards[1]?.energySeries.length ? machineCards[1].energySeries : [0, 0, 0, 0])} className={styles.energyLine} />
            </svg>
          </div>

          {!machineCards.some((card) => card.productionSeries.length) && <p className={styles.emptyState}>No telemetry rows were returned.</p>}

          <div className={styles.legend}>
            <span><i className={styles.productionSwatch} /> {SAMPLE_MACHINE_IDS[0]} parts</span>
            <span><i className={styles.energySwatch} /> {SAMPLE_MACHINE_IDS[1]} parts</span>
          </div>
        </article>
      </div>
    </section>
  );
}
