type NavigationHandlers = {
  onLoginRedirect: () => void;
  onForbiddenRedirect: () => void;
};

let handlers: NavigationHandlers | null = null;

export function registerNavigationHandlers(nextHandlers: NavigationHandlers) {
  handlers = nextHandlers;

  return () => {
    if (handlers === nextHandlers) {
      handlers = null;
    }
  };
}

export function redirectToLogin() {
  if (handlers) {
    handlers.onLoginRedirect();
    return;
  }

  window.location.assign('/login');
}

export function redirectToForbidden() {
  if (handlers) {
    handlers.onForbiddenRedirect();
    return;
  }

  window.location.assign('/unauthorized');
}
