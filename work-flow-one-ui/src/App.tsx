import { useEffect, useMemo, useState } from 'react';
import { useAuth } from './context/AuthContext';
import { AppFooter } from './components/layout/AppFooter';
import { AppHeader } from './components/layout/AppHeader';
import { SideNav } from './components/layout/SideNav';
import { DashboardPage } from './pages/DashboardPage';
import { MachinesPage } from './pages/MachinesPage';
import { PartsPage } from './pages/PartsPage';
import { PartDetailsPage } from './pages/PartDetailsPage';
import { MachineEventSimulatorPage } from './pages/MachineEventSimulatorPage';
import { ProfilePage } from './pages/ProfilePage';
import { OperatorsPage } from './pages/OperatorsPage';
import { OperatorDetailsPage } from './pages/OperatorDetailsPage';
import { OperatorSessionPage } from './pages/OperatorSessionPage';
import { ProductivityDashboardPage } from './pages/ProductivityDashboardPage';
import { LoginPage } from './pages/LoginPage';
import { SuperUserDashboardPage } from './pages/SuperUserDashboardPage';
import { UserManagementPage } from './pages/UserManagementPage';
import { RoleManagementPage } from './pages/RoleManagementPage';
import { UnauthorizedPage } from './pages/UnauthorizedPage';
import { ProtectedRoute } from './routes/ProtectedRoute';
import { RoleBasedRoute } from './routes/RoleBasedRoute';
import { DEFAULT_LANDING_BY_ROLE, isAllowedPath, normalizePath, type AppPath } from './config/routePermissions';
import { ROLES } from './constants/roles';
import styles from './App.module.css';

function getDetailId(pathname: string, prefix: string) {
  return pathname.startsWith(prefix) ? decodeURIComponent(pathname.slice(prefix.length)) : null;
}

function resolveLanding(roles: string[]) {
  for (const role of [ROLES.ADMIN, ROLES.SUPER_USER, ROLES.MANAGER, ROLES.SUPERVISOR, ROLES.OPERATOR, ROLES.QUALITY_INSPECTOR]) {
    if (roles.includes(role)) {
      return DEFAULT_LANDING_BY_ROLE[role] ?? '/dashboard';
    }
  }

  return '/profile';
}

export default function App() {
  const { isAuthenticated, roles, currentUser, profile, session, busy, loadingProfile, refreshSession, logout, setMode } = useAuth();
  const [currentPath, setCurrentPath] = useState<string>(() => window.location.pathname);
  const pathname = normalizePath(currentPath);
  const selectedPartId = useMemo(() => getDetailId(currentPath, '/parts/'), [currentPath]);
  const selectedOperatorId = useMemo(() => getDetailId(currentPath, '/operators/'), [currentPath]);

  const currentRoles = roles.length > 0 ? roles : session?.roles ?? [];
  const canAccessCurrentPath = isAllowedPath(pathname, currentRoles);
  const isLoginPage = pathname === '/login';
  const isAuthHydrating = isAuthenticated && (loadingProfile || currentRoles.length === 0);

  useEffect(() => {
    const syncFromLocation = () => {
      setCurrentPath(window.location.pathname);
    };

    window.addEventListener('popstate', syncFromLocation);
    return () => window.removeEventListener('popstate', syncFromLocation);
  }, []);

  useEffect(() => {
    const nextPath = normalizePath(window.location.pathname);

    if (!isAuthenticated) {
      if (nextPath !== '/login') {
        window.history.replaceState({}, '', '/login');
        setCurrentPath('/login');
      }
      return;
    }

    if (isAuthHydrating) {
      return;
    }

    if (nextPath === '/login') {
      const landing = resolveLanding(currentRoles);
      window.history.replaceState({}, '', landing);
      setCurrentPath(landing);
      return;
    }

    if (!canAccessCurrentPath) {
      window.history.replaceState({}, '', '/unauthorized');
      setCurrentPath('/unauthorized');
    }
  }, [canAccessCurrentPath, currentRoles, isAuthHydrating, isAuthenticated, profile]);

  function navigate(path: AppPath) {
    if (path === pathname) {
      return;
    }

    window.history.pushState({}, '', path);
    setCurrentPath(path);
  }

  function openPart(partId: string) {
    const path = `/parts/${encodeURIComponent(partId)}`;
    window.history.pushState({}, '', path);
    setCurrentPath(path);
  }

  function openOperator(operatorId: string) {
    const path = `/operators/${encodeURIComponent(operatorId)}`;
    window.history.pushState({}, '', path);
    setCurrentPath(path);
  }

  function renderWorkspace() {
    if (!isAuthenticated) {
      return <LoginPage />;
    }

    if (isAuthHydrating) {
      return (
        <section className={styles.workspace}>
          <p>Loading session...</p>
        </section>
      );
    }

    return (
      <ProtectedRoute allowed={isAuthenticated} fallback={<LoginPage />}>
        <RoleBasedRoute allowed={canAccessCurrentPath} fallback={<UnauthorizedPage />}>
          {pathname === '/dashboard' && <DashboardPage />}
          {pathname === '/super-user/dashboard' && <SuperUserDashboardPage />}
          {pathname === '/machines' && <MachinesPage />}
          {pathname === '/parts' && <PartsPage onOpenPart={openPart} />}
          {pathname.startsWith('/parts/') && <PartDetailsPage partId={selectedPartId} />}
          {pathname === '/operators' && <OperatorsPage onOpenOperator={openOperator} />}
          {pathname.startsWith('/operators/') && <OperatorDetailsPage operatorId={selectedOperatorId} />}
          {pathname === '/sessions' && <OperatorSessionPage />}
          {pathname === '/productivity' && <ProductivityDashboardPage />}
          {pathname === '/simulator' && <MachineEventSimulatorPage />}
          {pathname === '/profile' && <ProfilePage onLoginClick={() => navigate('/login')} />}
          {pathname === '/users' && <UserManagementPage />}
          {pathname === '/roles' && <RoleManagementPage />}
          {pathname === '/unauthorized' && <UnauthorizedPage />}
          {pathname === '/login' && <LoginPage />}
        </RoleBasedRoute>
      </ProtectedRoute>
    );
  }

  return (
    <div className={styles.appShell}>
      <AppHeader
        session={isAuthenticated}
        currentUser={currentUser ?? profile}
        roles={currentRoles}
        busy={busy}
        onLogin={() => {
          setMode('login');
          navigate('/login');
        }}
        onRegister={() => {
          setMode('register');
          navigate('/login');
        }}
        onRefresh={refreshSession}
        onLogout={async () => {
          await logout();
          navigate('/login');
        }}
        onNavigate={navigate}
      />

      <div className={styles.appBody}>
        {!isLoginPage && isAuthenticated && (
          <SideNav
            pathname={pathname}
            roles={currentRoles}
            onNavigate={navigate}
          />
        )}

        <main className={styles.workspace}>{renderWorkspace()}</main>
      </div>

      <AppFooter />
    </div>
  );
}
