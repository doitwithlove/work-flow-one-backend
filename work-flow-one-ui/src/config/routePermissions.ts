import { ROLES, RoleValue } from '../constants/roles';

export type AppPath =
  | '/login'
  | '/unauthorized'
  | '/dashboard'
  | '/machines'
  | '/parts'
  | '/operators'
  | '/sessions'
  | '/productivity'
  | '/simulator'
  | '/users'
  | '/roles'
  | '/profile'
  | '/super-user/dashboard';

export const ROUTE_PERMISSIONS: Record<string, RoleValue[]> = {
  '/dashboard': [ROLES.ADMIN, ROLES.SUPER_USER, ROLES.MANAGER, ROLES.SUPERVISOR],
  '/machines': [ROLES.ADMIN, ROLES.SUPER_USER, ROLES.MANAGER, ROLES.SUPERVISOR, ROLES.OPERATOR],
  '/parts': [ROLES.ADMIN, ROLES.SUPER_USER, ROLES.MANAGER, ROLES.SUPERVISOR, ROLES.OPERATOR, ROLES.QUALITY_INSPECTOR],
  '/operators': [ROLES.ADMIN, ROLES.SUPER_USER, ROLES.MANAGER, ROLES.SUPERVISOR],
  '/sessions': [ROLES.ADMIN, ROLES.SUPER_USER, ROLES.MANAGER, ROLES.SUPERVISOR, ROLES.OPERATOR],
  '/productivity': [ROLES.ADMIN, ROLES.SUPER_USER, ROLES.MANAGER, ROLES.SUPERVISOR],
  '/simulator': [ROLES.ADMIN, ROLES.SUPER_USER, ROLES.SUPERVISOR, ROLES.OPERATOR],
  '/users': [ROLES.ADMIN, ROLES.SUPER_USER],
  '/roles': [ROLES.ADMIN, ROLES.SUPER_USER],
  '/super-user/dashboard': [ROLES.ADMIN, ROLES.SUPER_USER],
  '/profile': [ROLES.ADMIN, ROLES.SUPER_USER, ROLES.MANAGER, ROLES.SUPERVISOR, ROLES.OPERATOR, ROLES.QUALITY_INSPECTOR],
};

export const DEFAULT_LANDING_BY_ROLE: Record<string, AppPath> = {
  [ROLES.ADMIN]: '/dashboard',
  [ROLES.SUPER_USER]: '/dashboard',
  [ROLES.MANAGER]: '/dashboard',
  [ROLES.SUPERVISOR]: '/dashboard',
  [ROLES.OPERATOR]: '/profile',
  [ROLES.QUALITY_INSPECTOR]: '/parts',
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
    return '/login';
  }

  const [base] = pathname.split('?');
  if (base.startsWith('/parts/')) {
    return '/parts';
  }
  if (base.startsWith('/operators/')) {
    return '/operators';
  }

  if (base in ROUTE_PERMISSIONS || base === '/login' || base === '/unauthorized' || base === '/profile') {
    return base as AppPath;
  }

  return '/dashboard';
}
