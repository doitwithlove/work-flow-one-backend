export const PATHS = {
  LOGIN: '/login',
  UNAUTHORIZED: '/unauthorized',
  DASHBOARD: '/dashboard',
  MACHINES: '/machines',
  PARTS: '/parts',
  OPERATORS: '/operators',
  SESSIONS: '/sessions',
  PRODUCTIVITY: '/productivity',
  SIMULATOR: '/simulator',
  USERS: '/users',
  ROLES: '/roles',
  PROFILE: '/profile',
  SUPER_USER_DASHBOARD: '/super-user/dashboard',
} as const;

export type AppPath = (typeof PATHS)[keyof typeof PATHS];
