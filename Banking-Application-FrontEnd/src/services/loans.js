import client, { unwrap } from '../api/client';

export const calculateLoan = (payload) => client.post('/api/loans/calculate', payload).then(unwrap);
export const applyLoan = (payload) => client.post('/api/loans', payload).then(unwrap);
export const listLoans = () => client.get('/api/loans').then(unwrap);
export const getLoan = (id) => client.get(`/api/loans/${id}`).then(unwrap);
export const getSchedule = (id) => client.get(`/api/loans/${id}/schedule`).then(unwrap);
export const getStatement = (id) => client.get(`/api/loans/${id}/statement`).then(unwrap);
export const payEmi = (id, payload) => client.post(`/api/loans/${id}/emi`, payload).then(unwrap);
export const prepay = (id, payload) => client.post(`/api/loans/${id}/prepay`, payload).then(unwrap);
