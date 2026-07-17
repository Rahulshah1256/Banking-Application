import client, { unwrap } from '../api/client';

export const overview = () => client.get('/api/admin/overview').then(unwrap);
export const listUsers = (params) => client.get('/api/admin/users', { params }).then(unwrap);
export const getUser = (id) => client.get(`/api/admin/users/${id}`).then(unwrap);
export const updateUserStatus = (id, status) =>
  client.patch(`/api/admin/users/${id}/status`, { status }).then(unwrap);
export const transactions = (params) => client.get('/api/admin/transactions', { params }).then(unwrap);
export const transactionReport = (params) =>
  client.get('/api/admin/reports/transactions', { params }).then(unwrap);
