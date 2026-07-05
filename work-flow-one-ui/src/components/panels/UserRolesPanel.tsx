import { ShieldCheck, ShieldOff } from 'lucide-react';
import { UserResponse } from '../../types/UserResponse';
import styles from './UserRolesPanel.module.css';

type UserRolesPanelProps = {
  profile: UserResponse | null;
  isAdmin: boolean;
};

const roleDescriptions = [
  {
    role: 'ROLE_USER',
    title: 'User access',
    description: 'Can authenticate, view the current session, and use standard user endpoints.',
  },
  {
    role: 'ROLE_ADMIN',
    title: 'Admin access',
    description: 'Can view the user directory and reach administrative endpoints.',
  },
] as const;

export function UserRolesPanel({ profile, isAdmin }: UserRolesPanelProps) {
  return (
    <section className={styles.panel} id="roles">
      <div className={styles.panelHeading}>
        <div>
          <p className={styles.eyebrow}>Roles</p>
          <h2>User roles</h2>
        </div>
        {isAdmin ? <ShieldCheck size={18} /> : <ShieldOff size={18} />}
      </div>

      <div className={styles.currentRoles}>
        <span className={styles.label}>Current account</span>
        <div className={styles.roleChips}>
          {profile?.roles.length ? (
            profile.roles.map((role) => (
              <span key={role} className={styles.roleChip}>
                {role}
              </span>
            ))
          ) : (
            <span className={styles.emptyRole}>No session roles loaded</span>
          )}
        </div>
      </div>

      <div className={styles.roleGrid}>
        {roleDescriptions.map((item) => (
          <article key={item.role} className={styles.roleCard}>
            <span className={styles.roleKey}>{item.role}</span>
            <strong>{item.title}</strong>
            <p>{item.description}</p>
          </article>
        ))}
      </div>
    </section>
  );
}
