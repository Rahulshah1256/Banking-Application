import axios from 'axios';

export const API_BASE = 'http://localhost:8080';
const TOKEN_KEY = 'jb_token';

export const getToken = () => localStorage.getItem(TOKEN_KEY);
export const setToken = (token) => localStorage.setItem(TOKEN_KEY, token);
export const clearToken = () => localStorage.removeItem(TOKEN_KEY);

const client = axios.create({ baseURL: API_BASE });

client.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      clearToken();
      if (!window.location.pathname.startsWith('/login')) {
        window.location.assign('/login');
      }
    }
    return Promise.reject(error);
  }
);

// Unwrap the standard ApiResponse envelope { success, message, data } when present.
export const unwrap = (response) => {
  const body = response.data;
  if (body && typeof body === 'object' && 'data' in body && 'success' in body) {
    return body.data;
  }
  return body;
};

// Extract a human-readable message from an axios error.
export const errorMessage = (error, fallback = 'Something went wrong') => {
  const data = error?.response?.data;
  if (data) {
    if (typeof data === 'string') return data;
    if (data.message) return data.message;
    if (data.error) return data.error;
  }
  return error?.message || fallback;
};

export default client;
