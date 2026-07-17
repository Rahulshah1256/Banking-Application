import client, { unwrap } from '../api/client';

export const listCards = () => client.get('/api/cards').then(unwrap);
export const issueCard = (payload) => client.post('/api/cards', payload).then(unwrap);
export const getCard = (id) => client.get(`/api/cards/${id}`).then(unwrap);
export const blockCard = (id, payload) => client.post(`/api/cards/${id}/block`, payload).then(unwrap);
export const unblockCard = (id) => client.post(`/api/cards/${id}/unblock`).then(unwrap);
export const replaceCard = (id) => client.post(`/api/cards/${id}/replace`).then(unwrap);
export const setPin = (id, payload) => client.post(`/api/cards/${id}/pin`, payload).then(unwrap);
export const updateControls = (id, payload) =>
  client.patch(`/api/cards/${id}/controls`, payload).then(unwrap);
export const updateLimits = (id, payload) =>
  client.patch(`/api/cards/${id}/limits`, payload).then(unwrap);
export const cardHistory = (id) => client.get(`/api/cards/${id}/history`).then(unwrap);
