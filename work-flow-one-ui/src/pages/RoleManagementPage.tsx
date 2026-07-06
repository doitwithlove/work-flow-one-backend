import { Shield, ShieldCheck, ShieldQuestion, Users } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import styles from './RoleManagementPage.module.css';

const roles = [
  {
    role: 'ADMIN',
    icon: ShieldCheck,
    summary: 'Full factory access, user administration, and super-user dashboards.',
  },
  {
    role: 'SUPER_USER',
    icon: ShieldCheck,
    summary: 'Full factory access, user administration, and super-user dashboards.',
  },
  {
    role: 'MANAGER',
    icon: Users,
    summary: 'Dashboards, reports, and cross-area visibility.',
  },
  {
    role: 'SUPERVISOR',
    icon: Shield,
    summary: 'Machines, operators, parts, and productivity for the assigned area.',
  },
  {
    role: 'OPERATOR',
    icon: ShieldQuestion,
    summary: 'Assigned machine access and own session control.',
  },
  {
    role: 'QUALITY_INSPECTOR',
    icon: ShieldQuestion,
    summary: 'Submit and review test results.',
  },
];

export function RoleManagementPage() {
  const { isSuperUser } = useAuth();

  return (
    <section className={styles.pageShell} aria-label="Role management page">
      <div className={styles.headerRow}>
        <div>
          <p className={styles.eyebrow}>Administration</p>
          <h2>Role management</h2>
        </div>
      </div>

      {!isSuperUser ? (
        <p className={styles.emptyState}>Super user access is required.</p>
      ) : (
        <div className={styles.grid}>
          {roles.map(({ role, icon: Icon, summary }) => (
            <article key={role} className={styles.card}>
              <div className={styles.cardHeader}>
                <Icon size={18} />
                <strong>{role}</strong>
              </div>
              <p>{summary}</p>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
