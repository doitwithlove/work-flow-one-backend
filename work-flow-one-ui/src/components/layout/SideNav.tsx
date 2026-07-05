import { ClipboardList, UserCircle2, Users } from 'lucide-react';
import styles from './SideNav.module.css';

export function SideNav() {
  return (
    <aside className={styles.sidePanel}>
      <nav className={styles.navStack} aria-label="Workspace navigation">
        <p className={styles.sectionLabel}>User profile</p>
        <a className={styles.navItem} href="#profile">
          <UserCircle2 size={18} />
          <span>Profile</span>
        </a>
        <a className={styles.navItem} href="#sessions">
          <ClipboardList size={18} />
          <span>Sessions</span>
        </a>
        <a className={styles.navItem} href="#users">
          <Users size={18} />
          <span>Users</span>
        </a>
      </nav>
    </aside>
  );
}
