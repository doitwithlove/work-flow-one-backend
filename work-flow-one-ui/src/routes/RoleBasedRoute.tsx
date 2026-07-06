import { ReactNode } from 'react';

type RoleBasedRouteProps = {
  allowed: boolean;
  fallback: ReactNode;
  children: ReactNode;
};

export function RoleBasedRoute({ allowed, fallback, children }: RoleBasedRouteProps) {
  return allowed ? children : fallback;
}
