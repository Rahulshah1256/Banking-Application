import { createContext, useContext, useMemo, useState, useCallback } from 'react';
import { login as loginApi, logout as logoutApi, loadUserFromToken } from '../services/auth';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => loadUserFromToken());

  const login = useCallback(async (usernameoremail, password) => {
    const u = await loginApi(usernameoremail, password);
    setUser(u);
    return u;
  }, []);

  const logout = useCallback(() => {
    logoutApi();
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: !!user,
      isAdmin: user?.role === 'ROLE_ADMIN',
      login,
      logout,
    }),
    [user, login, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
