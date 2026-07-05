import { Loader2, Lock, Users } from 'lucide-react';
import { Session } from '../../types/Session';
import { UserResponse } from '../../types/UserResponse';
import styles from './UsersPanel.module.css';

type UsersPanelProps = {
  session: Session | null;
  busy: boolean;
  isAdmin: boolean;
  users: UserResponse[];
  onLoadUsers: () => void;
};

export function UsersPanel({ session, busy, isAdmin, users, onLoadUsers }: UsersPanelProps) {
  return (
    <section className={`${styles.panel} ${styles.usersPanel}`} id="users">
      <div className={styles.panelHeading}>
        <div>
          <p className={styles.eyebrow}>Users</p>
          <h2>Admin directory</h2>
        </div>
        <button className={styles.iconButton} type="button" onClick={onLoadUsers} disabled={!session || busy} title="Load admin users">
          {busy ? <Loader2 className={styles.spin} size={18} /> : <Users size={18} />}
        </button>
      </div>

      {!isAdmin && <p className={styles.emptyState}><Lock size={18} /> Sign in with an admin account to load the directory.</p>}

      {users.length > 0 && (
        <div className={styles.tableWrap}>
          <table>
            <thead>
              <tr>
                <th>Username</th>
                <th>Email</th>
                <th>Roles</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td>{user.username}</td>
                  <td>{user.email}</td>
                  <td>{user.roles.join(', ')}</td>
                  <td>{user.enabled ? 'Enabled' : 'Disabled'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
