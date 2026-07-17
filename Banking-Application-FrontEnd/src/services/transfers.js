import client, { unwrap } from '../api/client';

export const transfer = (payload) => client.post('/api/transactions/transfer', payload).then(unwrap);

export const getHistory = (params) => client.get('/api/transactions', { params }).then(unwrap);

export const getTransaction = (reference) =>
  client.get(`/api/transactions/${reference}`).then(unwrap);

export const listScheduled = () => client.get('/api/transactions/scheduled').then(unwrap);

export const createScheduled = (payload) =>
  client.post('/api/transactions/scheduled', payload).then(unwrap);

export const cancelScheduled = (id) =>
  client.delete(`/api/transactions/scheduled/${id}`).then(unwrap);
