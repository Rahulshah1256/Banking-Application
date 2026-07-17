import client, { unwrap } from '../api/client';

export const getProfile = () => client.get('/api/profile').then(unwrap);
export const updateProfile = (payload) => client.put('/api/profile', payload).then(unwrap);
export const uploadPhoto = (file) => {
  const form = new FormData();
  form.append('file', file);
  return client
    .post('/api/profile/photo', form, { headers: { 'Content-Type': 'multipart/form-data' } })
    .then(unwrap);
};
export const photoUrl = () => '/api/profile/photo';

// Nominees
export const listNominees = () => client.get('/api/nominees').then(unwrap);
export const addNominee = (payload) => client.post('/api/nominees', payload).then(unwrap);
export const updateNominee = (id, payload) => client.put(`/api/nominees/${id}`, payload).then(unwrap);
export const deleteNominee = (id) => client.delete(`/api/nominees/${id}`).then(unwrap);

// KYC
export const kycStatus = () => client.get('/api/kyc/status').then(unwrap);
export const listKycDocuments = () => client.get('/api/kyc/documents').then(unwrap);
export const uploadKyc = (documentType, documentNumber, file) => {
  const form = new FormData();
  form.append('documentType', documentType);
  form.append('documentNumber', documentNumber);
  form.append('file', file);
  return client
    .post('/api/kyc/documents', form, { headers: { 'Content-Type': 'multipart/form-data' } })
    .then(unwrap);
};
export const verifyKyc = (id, payload) =>
  client.post(`/api/kyc/documents/${id}/verify`, payload).then(unwrap);
