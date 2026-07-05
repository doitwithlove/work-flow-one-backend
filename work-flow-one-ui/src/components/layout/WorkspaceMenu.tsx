import { KeyRound, Shield, ShieldCheck, UserCircle2, Users } from 'lucide-react';
import styles from './WorkspaceMenu.module.css';

type WorkspaceMenuProps = {
  onProfile: () => void;
  onAccess: () => void;
  onSession: () => void;
  onRoles: () => void;
  onUsers: () => void;
};

export function WorkspaceMenu({ onProfile, onAccess, onSession, onRoles, onUsers }: WorkspaceMenuProps) {
  return (
    <div className={styles.workspaceMenu} aria-label="Workspace menu">
      <button type="button" className={styles.menuItem} onClick={onProfile}>
        <UserCircle2 size={16} />
        User profile
      </button>
      <button type="button" className={styles.menuItem} onClick={onAccess}>
        <KeyRound size={16} />
        Access
      </button>
      <button type="button" className={styles.menuItem} onClick={onSession}>
        <Shield size={16} />
        Session
      </button>
      <button type="button" className={styles.menuItem} onClick={onRoles}>
        <ShieldCheck size={16} />
        Roles
      </button>
      <button type="button" className={styles.menuItem} onClick={onUsers}>
        <Users size={16} />
        Users
      </button>
    </div>
  );
}
