import { CheckCircle2, Loader2, RefreshCw } from 'lucide-react';
import { Session } from '../../types/Session';
import { UserResponse } from '../../types/UserResponse';
import styles from './SessionPanel.module.css';

type SessionPanelProps = {
  session: Session | null;
  profile: UserResponse | null;
  busy: boolean;
  loadingProfile: boolean;
  expiresIn: number;
  onRefresh: () => void;
};

export function SessionPanel({ session, profile, busy, loadingProfile, expiresIn, onRefresh }: SessionPanelProps) {
  return (
    <section className={styles.panel} id="sessions">
      <div className={styles.panelHeading}>
        <div>
          <p className={styles.eyebrow}>Session</p>
          <h2>Token controls</h2>
        </div>
        {loadingProfile && <Loader2 className={`${styles.spin} ${styles.muted}`} size={20} />}
      </div>

      <div className={styles.metrics}>
        <div>
          <span>Access token</span>
          <strong>{session ? `${expiresIn}s` : 'None'}</strong>
        </div>
        <div>
          <span>Roles</span>
          <strong>{profile?.roles.join(', ') || 'None'}</strong>
        </div>
      </div>

      <div className={styles.buttonRow}>
        <button type="button" onClick={onRefresh} disabled={!session || busy}>
          <RefreshCw size={18} />
          Refresh
        </button>
      </div>

      {profile && (
        <dl className={styles.profileList}>
          <div>
            <dt>User</dt>
            <dd>{profile.username}</dd>
          </div>
          <div>
            <dt>Email</dt>
            <dd>{profile.email}</dd>
          </div>
          <div>
            <dt>Phone</dt>
            <dd>{profile.phoneNumber || 'Not set'}</dd>
          </div>
          <div>
            <dt>Birthday</dt>
            <dd>{profile.birthday || 'Not set'}</dd>
          </div>
          <div>
            <dt>Position</dt>
            <dd>{profile.position || 'Not set'}</dd>
          </div>
          <div>
            <dt>Status</dt>
            <dd>
              <CheckCircle2 size={16} />
              {profile.enabled ? 'Enabled' : 'Disabled'}
            </dd>
          </div>
        </dl>
      )}
    </section>
  );
}
