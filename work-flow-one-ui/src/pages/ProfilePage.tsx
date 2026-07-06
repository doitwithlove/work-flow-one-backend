import { AdminUsersPanel } from '../components/panels/AdminUsersPanel';
import { AuthPanel } from '../components/panels/AuthPanel';
import { ProfilePanel } from '../components/panels/ProfilePanel';
import { SessionPanel } from '../components/panels/SessionPanel';
import { useAuth } from '../context/AuthContext';
import styles from './ProfilePage.module.css';

type ProfilePageProps = {
  onLoginClick: () => void;
};

export function ProfilePage({ onLoginClick }: ProfilePageProps) {
  const {
    mode,
    setMode,
    session,
    profile,
    adminUsers,
    notice,
    busy,
    loadingProfile,
    isAdmin,
    expiresIn,
    submitAuth,
    refreshSession,
    updateProfile,
    changePassword,
    loadAdminUsers,
    createAdminUser,
    updateAdminUser,
  } = useAuth();

  return (
    <section className={styles.pageShell} aria-label="User profile page">
      <div className={`${styles.notice} ${styles[notice.tone]}`} role="status">
        {notice.text}
      </div>

      {!session ? (
        <div className={styles.authCard}>
          <AuthPanel
            mode={mode}
            busy={busy}
            onModeChange={(nextMode) => {
              setMode(nextMode);
              onLoginClick();
            }}
            onSubmit={submitAuth}
          />
        </div>
      ) : null}

      <ProfilePanel
        profile={profile}
        busy={busy}
        onSaveProfile={updateProfile}
        onChangePassword={changePassword}
      />

      <div className={styles.contentGrid}>
        <SessionPanel
          session={session}
          profile={profile}
          busy={busy}
          loadingProfile={loadingProfile}
          expiresIn={expiresIn}
          onRefresh={refreshSession}
        />

        <AdminUsersPanel
          session={session}
          busy={busy}
          isAdmin={isAdmin}
          users={adminUsers}
          onLoadUsers={loadAdminUsers}
          onCreateUser={createAdminUser}
          onUpdateUser={updateAdminUser}
        />
      </div>
    </section>
  );
}
