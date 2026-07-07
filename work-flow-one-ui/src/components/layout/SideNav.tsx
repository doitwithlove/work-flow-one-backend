import { NavLink } from 'react-router-dom';
import { BarChart3, ClipboardList, Factory, ScanLine, ShieldCheck, Users, Clock3 } from 'lucide-react';
import { type AppPath, isAllowedPath } from '../../config/routePermissions';
import styles from './SideNav.module.css';

type SideNavProps = {
  pathname: AppPath;
  roles: string[];
};

const items: Array<{ path: AppPath; label: string; icon: typeof ClipboardList }> = [
  { path: '/dashboard', label: 'Dashboard', icon: ClipboardList },
  { path: '/machines', label: 'Machines', icon: Factory },
  { path: '/parts', label: 'Parts', icon: ClipboardList },
  { path: '/operators', label: 'Operators', icon: Users },
  { path: '/sessions', label: 'Sessions', icon: Clock3 },
  { path: '/productivity', label: 'Productivity', icon: BarChart3 },
  { path: '/simulator', label: 'Simulator', icon: ScanLine },
  { path: '/users', label: 'Users', icon: Users },
  { path: '/roles', label: 'Roles', icon: ShieldCheck },
  { path: '/super-user/dashboard', label: 'Super user', icon: ShieldCheck },
];

export function SideNav({ pathname, roles }: SideNavProps) {
  const activePath = pathname.startsWith('/parts/') ? '/parts' : pathname.startsWith('/operators/') ? '/operators' : pathname;

  return (
    <aside className={styles.sidePanel}>
      <nav className={styles.navStack} aria-label="Workspace navigation">
        <p className={styles.sectionLabel}>Pages</p>
        {items
          .filter((item) => isAllowedPath(item.path, roles))
          .map(({ path, label, icon: Icon }) => (
            <NavLink
              key={path}
              to={path}
              className={({ isActive }) => `${styles.navItem} ${isActive || activePath === path ? styles.active : ''}`}
            >
              <Icon size={18} />
              <span>{label}</span>
            </NavLink>
          ))}
      </nav>
    </aside>
  );
}
