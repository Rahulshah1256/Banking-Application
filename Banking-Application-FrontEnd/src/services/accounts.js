import client, { unwrap } from '../api/client';

export const listAccounts = () => client.get('/api/accounts').then(unwrap);
export const getAccount = (id) => client.get(`/api/accounts/${id}`).then(unwrap);
export const getAccountDetails = (id) => client.get(`/api/accounts/${id}/details`).then(unwrap);
export const getAccountSummary = (id) => client.get(`/api/accounts/${id}/summary`).then(unwrap);
export const getStatement = (id, params) =>
  client.get(`/api/accounts/${id}/statement`, { params }).then(unwrap);
