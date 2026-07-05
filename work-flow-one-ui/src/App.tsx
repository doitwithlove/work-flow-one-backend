import { useAuth } from './context/AuthContext';
import { AppFooter } from './components/layout/AppFooter';
import { AppHeader } from './components/layout/AppHeader';
import { SessionPanel } from './components/panels/SessionPanel';
import { SideNav } from './components/layout/SideNav';
import { ProfilePanel } from './components/panels/ProfilePanel';
import { AdminUsersPanel } from './components/panels/AdminUsersPanel';
import { AuthPanel } from './components/panels/AuthPanel';
import styles from './App.module.css';

export default function App() {
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
    logout,
    updateProfile,
    changePassword,
    loadAdminUsers,
    createAdminUser,
    updateAdminUser,
  } = useAuth();

  return (
    <div className={styles.appShell}>
      <AppHeader
        session={Boolean(session)}
        profile={profile}
        busy={busy}
        onLogin={() => setMode('login')}
        onRegister={() => setMode('register')}
        onRefresh={refreshSession}
        onLogout={logout}
      />

      <div className={styles.appBody}>
        <SideNav />

        <main className={styles.workspace}>
          <section className={styles.profileShell}>
            <div className={`${styles.notice} ${styles[notice.tone]}`} role="status">
              {notice.text}
            </div>

            {!session && (
              <AuthPanel
                mode={mode}
                busy={busy}
                onModeChange={setMode}
                onSubmit={submitAuth}
              />
            )}

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
        </main>
      </div>

      <AppFooter />
    </div>
  );
}
