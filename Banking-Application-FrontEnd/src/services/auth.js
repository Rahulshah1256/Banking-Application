import client, { setToken, clearToken, getToken } from '../api/client';
import { jwtDecode } from 'jwt-decode';

export const login = async (usernameoremail, password) => {
  const res = await client.post('/api/auth/login', { usernameoremail, password });
  const accessToken = res.data.accessToken;
  setToken(accessToken);
  return decodeUser(accessToken);
};

export const register = (payload) => client.post('/api/auth/register', payload);

export const logout = () => clearToken();

export const decodeUser = (token) => {
  const claims = jwtDecode(token);
  return {
    username: claims.sub,
    role: claims.role,
    name: claims.name,
    email: claims.email,
  };
};

export const loadUserFromToken = () => {
  const token = getToken();
  if (!token) return null;
  try {
    const claims = jwtDecode(token);
    if (claims.exp && claims.exp * 1000 < Date.now()) {
      clearToken();
      return null;
    }
    return decodeUser(token);
  } catch {
    clearToken();
    return null;
  }
};
