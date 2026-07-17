import client, { unwrap } from '../api/client';

export const calculateDeposit = (payload) => client.post('/api/deposits/calculate', payload).then(unwrap);
export const openFd = (payload) => client.post('/api/deposits/fd', payload).then(unwrap);
export const openRd = (payload) => client.post('/api/deposits/rd', payload).then(unwrap);
export const listDeposits = () => client.get('/api/deposits').then(unwrap);
export const getDeposit = (id) => client.get(`/api/deposits/${id}`).then(unwrap);
export const payInstallment = (id, payload) =>
  client.post(`/api/deposits/${id}/installment`, payload).then(unwrap);
export const closeDeposit = (id, payload) => client.post(`/api/deposits/${id}/close`, payload).then(unwrap);
export const setAutoRenew = (id, autoRenew) =>
  client.patch(`/api/deposits/${id}/auto-renew`, null, { params: { autoRenew } }).then(unwrap);
