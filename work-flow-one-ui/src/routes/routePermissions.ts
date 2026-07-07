import { ROLES, type RoleValue } from '../constants/roles';
import { PATHS, type AppPath } from './paths';

export const ROUTE_PERMISSIONS: Record<string, RoleValue[]> = {
  [PATHS.DASHBOARD]: [ROLES.ADMIN, ROLES.SUPER_USER, ROLES.MANAGER, ROLES.SUPERVISOR],
  [PATHS.MACHINES]: [ROLES.ADMIN, ROLES.SUPER_USER, ROLES.MANAGER, ROLES.SUPERVISOR, ROLES.OPERATOR],
  [PATHS.PARTS]: [ROLES.ADMIN, ROLES.SUPER_USER, ROLES.MANAGER, ROLES.SUPERVISOR, ROLES.OPERATOR, ROLES.QUALITY_INSPECTOR],
  [PATHS.OPERATORS]: [ROLES.ADMIN, ROLES.SUPER_USER, ROLES.MANAGER, ROLES.SUPERVISOR],
  [PATHS.SESSIONS]: [ROLES.ADMIN, ROLES.SUPER_USER, ROLES.MANAGER, ROLES.SUPERVISOR, ROLES.OPERATOR],
  [PATHS.PRODUCTIVITY]: [ROLES.ADMIN, ROLES.SUPER_USER, ROLES.MANAGER, ROLES.SUPERVISOR],
  [PATHS.SIMULATOR]: [ROLES.ADMIN, ROLES.SUPER_USER, ROLES.SUPERVISOR, ROLES.OPERATOR],
  [PATHS.USERS]: [ROLES.ADMIN, ROLES.SUPER_USER],
  [PATHS.ROLES]: [ROLES.ADMIN, ROLES.SUPER_USER],
  [PATHS.SUPER_USER_DASHBOARD]: [ROLES.ADMIN, ROLES.SUPER_USER],
  [PATHS.PROFILE]: [ROLES.ADMIN, ROLES.SUPER_USER, ROLES.MANAGER, ROLES.SUPERVISOR, ROLES.OPERATOR, ROLES.QUALITY_INSPECTOR],
};

export const DEFAULT_LANDING_BY_ROLE: Record<string, AppPath> = {
  [ROLES.ADMIN]: PATHS.DASHBOARD,
  [ROLES.SUPER_USER]: PATHS.DASHBOARD,
  [ROLES.MANAGER]: PATHS.DASHBOARD,
  [ROLES.SUPERVISOR]: PATHS.DASHBOARD,
  [ROLES.OPERATOR]: PATHS.MACHINES,
  [ROLES.QUALITY_INSPECTOR]: PATHS.PARTS,
};

export function isAllowedPath(pathname: string, roles: string[]) {
  const normalized = normalizePath(pathname);
  const requiredRoles = ROUTE_PERMISSIONS[normalized];
  if (!requiredRoles) {
    return true;
  }

  return roles.some((role) => requiredRoles.includes(role as RoleValue));
}

export function normalizePath(pathname: string): AppPath {
  if (!pathname || pathname === '/') {
    return PATHS.LOGIN;
  }

  const [base] = pathname.split('?');
  if (base.startsWith(`${PATHS.PARTS}/`)) {
    return PATHS.PARTS;
  }
  if (base.startsWith(`${PATHS.OPERATORS}/`)) {
    return PATHS.OPERATORS;
  }

  if (base in ROUTE_PERMISSIONS || base === PATHS.LOGIN || base === PATHS.UNAUTHORIZED || base === PATHS.PROFILE) {
    return base as AppPath;
  }

  return PATHS.DASHBOARD;
}
