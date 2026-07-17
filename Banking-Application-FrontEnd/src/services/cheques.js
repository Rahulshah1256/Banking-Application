import client, { unwrap } from '../api/client';

export const requestBook = (payload) => client.post('/api/cheques/books', payload).then(unwrap);
export const listBooks = () => client.get('/api/cheques/books').then(unwrap);
export const getBook = (id) => client.get(`/api/cheques/books/${id}`).then(unwrap);
export const issueBook = (id) => client.post(`/api/cheques/books/${id}/issue`).then(unwrap);
export const deliverBook = (id) => client.post(`/api/cheques/books/${id}/deliver`).then(unwrap);
export const listLeaves = (params) => client.get('/api/cheques/leaves', { params }).then(unwrap);
export const stopCheque = (id, payload) =>
  client.post(`/api/cheques/leaves/${id}/stop`, payload).then(unwrap);
export const registerPositivePay = (id, payload) =>
  client.post(`/api/cheques/leaves/${id}/positive-pay`, payload).then(unwrap);
