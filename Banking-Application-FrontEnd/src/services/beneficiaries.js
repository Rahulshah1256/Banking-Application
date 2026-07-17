import client, { unwrap } from '../api/client';

export const listBeneficiaries = () => client.get('/api/beneficiaries').then(unwrap);
export const addBeneficiary = (payload) => client.post('/api/beneficiaries', payload).then(unwrap);
export const updateBeneficiary = (id, payload) =>
  client.put(`/api/beneficiaries/${id}`, payload).then(unwrap);
export const deleteBeneficiary = (id) => client.delete(`/api/beneficiaries/${id}`).then(unwrap);
export const approveBeneficiary = (id) => client.post(`/api/beneficiaries/${id}/approve`).then(unwrap);
export const toggleFavourite = (id, favourite) =>
  client.patch(`/api/beneficiaries/${id}/favourite`, null, { params: { favourite } }).then(unwrap);
