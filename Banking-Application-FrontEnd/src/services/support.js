import client, { unwrap } from '../api/client';

export const raiseTicket = (payload) => client.post('/api/support/tickets', payload).then(unwrap);
export const listTickets = () => client.get('/api/support/tickets').then(unwrap);
export const getTicket = (id) => client.get(`/api/support/tickets/${id}`).then(unwrap);
export const addMessage = (id, payload) =>
  client.post(`/api/support/tickets/${id}/messages`, payload).then(unwrap);
export const updateStatus = (id, payload) =>
  client.patch(`/api/support/tickets/${id}/status`, payload).then(unwrap);
export const listFaqs = (category) =>
  client.get('/api/support/faqs', { params: category ? { category } : {} }).then(unwrap);
export const listBranches = (city) =>
  client.get('/api/support/branches', { params: city ? { city } : {} }).then(unwrap);
export const listAtms = (city) =>
  client.get('/api/support/atms', { params: city ? { city } : {} }).then(unwrap);
