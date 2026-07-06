import { ReactNode } from 'react';

type ProtectedRouteProps = {
  allowed: boolean;
  fallback: ReactNode;
  children: ReactNode;
};

export function ProtectedRoute({ allowed, fallback, children }: ProtectedRouteProps) {
  return allowed ? children : fallback;
}
