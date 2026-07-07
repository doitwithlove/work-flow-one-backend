import { useEffect, useMemo } from 'react';
import { Navigate, Route, Routes, useLocation, useNavigate, useParams } from 'react-router-dom';
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
import { DEFAULT_LANDING_BY_ROLE, ROUTE_PERMISSIONS, normalizePath } from './routes/routePermissions';
import { PATHS } from './routes/paths';
import { ROLES } from './constants/roles';
import styles from './App.module.css';

function resolveLanding(roles: string[]) {
  for (const role of [ROLES.ADMIN, ROLES.SUPER_USER, ROLES.MANAGER, ROLES.SUPERVISOR, ROLES.OPERATOR, ROLES.QUALITY_INSPECTOR]) {
    if (roles.includes(role)) {
      return DEFAULT_LANDING_BY_ROLE[role] ?? PATHS.DASHBOARD;
    }
  }

  return PATHS.PROFILE;
}

function PartDetailsRoute() {
  const { id } = useParams();
  return <PartDetailsPage partId={id ? decodeURIComponent(id) : null} />;
}

function OperatorDetailsRoute() {
  const { id } = useParams();
  return <OperatorDetailsPage operatorId={id ? decodeURIComponent(id) : null} />;
}

export default function App() {
  const { isAuthenticated, roles, currentUser, profile, session, busy, loadingProfile, refreshSession, logout, setMode } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const currentRoles = roles.length > 0 ? roles : session?.roles ?? [];
  const pathname = normalizePath(location.pathname);
  const isAuthHydrating = isAuthenticated && (loadingProfile || currentRoles.length === 0);
  const landingPath = useMemo(() => resolveLanding(currentRoles), [currentRoles]);

  useEffect(() => {
    if (!isAuthenticated) {
      if (pathname !== PATHS.LOGIN) {
        navigate(PATHS.LOGIN, { replace: true });
      }
      return;
    }

    if (isAuthHydrating) {
      return;
    }

    if (pathname === PATHS.LOGIN) {
      navigate(landingPath, { replace: true });
    }
  }, [isAuthenticated, isAuthHydrating, landingPath, navigate, pathname]);

  const showSidebar = isAuthenticated && pathname !== PATHS.LOGIN;

  return (
    <div className={styles.appShell}>
      <AppHeader
        session={isAuthenticated}
        currentUser={currentUser ?? profile}
        roles={currentRoles}
        busy={busy}
        onLogin={() => {
          setMode('login');
          navigate(PATHS.LOGIN);
        }}
        onRegister={() => {
          setMode('register');
          navigate(PATHS.LOGIN);
        }}
        onRefresh={refreshSession}
        onLogout={async () => {
          await logout();
          navigate(PATHS.LOGIN, { replace: true });
        }}
      />

      <div className={styles.appBody}>
        {showSidebar && <SideNav pathname={pathname} roles={currentRoles} />}

        <main className={styles.workspace}>
          {isAuthHydrating ? (
            <section className={styles.workspace}>
              <p>Loading session...</p>
            </section>
          ) : (
            <Routes>
              <Route path="/" element={<Navigate to={isAuthenticated ? landingPath : PATHS.LOGIN} replace />} />
              <Route path={PATHS.LOGIN} element={<LoginPage />} />
              <Route path={PATHS.UNAUTHORIZED} element={<UnauthorizedPage />} />
              <Route
                path={PATHS.DASHBOARD}
                element={
                  <ProtectedRoute allowedRoles={ROUTE_PERMISSIONS[PATHS.DASHBOARD]}>
                    <DashboardPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path={PATHS.SUPER_USER_DASHBOARD}
                element={
                  <ProtectedRoute allowedRoles={ROUTE_PERMISSIONS[PATHS.SUPER_USER_DASHBOARD]}>
                    <SuperUserDashboardPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path={PATHS.MACHINES}
                element={
                  <ProtectedRoute allowedRoles={ROUTE_PERMISSIONS[PATHS.MACHINES]}>
                    <MachinesPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path={PATHS.PARTS}
                element={
                  <ProtectedRoute allowedRoles={ROUTE_PERMISSIONS[PATHS.PARTS]}>
                    <PartsPage onOpenPart={(partId) => navigate(`${PATHS.PARTS}/${encodeURIComponent(partId)}`)} />
                  </ProtectedRoute>
                }
              />
              <Route
                path={`${PATHS.PARTS}/:id`}
                element={
                  <ProtectedRoute allowedRoles={ROUTE_PERMISSIONS[PATHS.PARTS]}>
                    <PartDetailsRoute />
                  </ProtectedRoute>
                }
              />
              <Route
                path={PATHS.OPERATORS}
                element={
                  <ProtectedRoute allowedRoles={ROUTE_PERMISSIONS[PATHS.OPERATORS]}>
                    <OperatorsPage onOpenOperator={(operatorId) => navigate(`${PATHS.OPERATORS}/${encodeURIComponent(operatorId)}`)} />
                  </ProtectedRoute>
                }
              />
              <Route
                path={`${PATHS.OPERATORS}/:id`}
                element={
                  <ProtectedRoute allowedRoles={ROUTE_PERMISSIONS[PATHS.OPERATORS]}>
                    <OperatorDetailsRoute />
                  </ProtectedRoute>
                }
              />
              <Route
                path={PATHS.SESSIONS}
                element={
                  <ProtectedRoute allowedRoles={ROUTE_PERMISSIONS[PATHS.SESSIONS]}>
                    <OperatorSessionPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path={PATHS.PRODUCTIVITY}
                element={
                  <ProtectedRoute allowedRoles={ROUTE_PERMISSIONS[PATHS.PRODUCTIVITY]}>
                    <ProductivityDashboardPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path={PATHS.SIMULATOR}
                element={
                  <ProtectedRoute allowedRoles={ROUTE_PERMISSIONS[PATHS.SIMULATOR]}>
                    <MachineEventSimulatorPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path={PATHS.PROFILE}
                element={
                  <ProtectedRoute allowedRoles={ROUTE_PERMISSIONS[PATHS.PROFILE]}>
                    <ProfilePage onLoginClick={() => navigate(PATHS.LOGIN)} />
                  </ProtectedRoute>
                }
              />
              <Route
                path={PATHS.USERS}
                element={
                  <ProtectedRoute allowedRoles={ROUTE_PERMISSIONS[PATHS.USERS]}>
                    <UserManagementPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path={PATHS.ROLES}
                element={
                  <ProtectedRoute allowedRoles={ROUTE_PERMISSIONS[PATHS.ROLES]}>
                    <RoleManagementPage />
                  </ProtectedRoute>
                }
              />
              <Route path="*" element={<Navigate to={isAuthenticated ? landingPath : PATHS.LOGIN} replace />} />
            </Routes>
          )}
        </main>
      </div>

      <AppFooter />
    </div>
  );
}
