import { useEffect, useRef } from 'react';
import { AdminUsersPanel } from '../components/panels/AdminUsersPanel';
import { useAuth } from '../context/AuthContext';
import styles from './UserManagementPage.module.css';

export function UserManagementPage() {
  const { session, adminUsers, busy, isSuperUser, loadAdminUsers, createAdminUser, updateAdminUser } = useAuth();
  const loadedOnce = useRef(false);

  useEffect(() => {
    if (!session) {
      loadedOnce.current = false;
      return;
    }

    if (isSuperUser && !loadedOnce.current) {
      loadedOnce.current = true;
      void loadAdminUsers();
    }
  }, [isSuperUser, loadAdminUsers, session]);

  return (
    <section className={styles.pageShell} aria-label="User management page">
      <div className={styles.headerRow}>
        <div>
          <p className={styles.eyebrow}>Administration</p>
          <h2>User management</h2>
        </div>
      </div>

      <AdminUsersPanel
        session={session}
        busy={busy}
        isAdmin={isSuperUser}
        users={adminUsers}
        onLoadUsers={loadAdminUsers}
        onCreateUser={createAdminUser}
        onUpdateUser={updateAdminUser}
      />
    </section>
  );
}
