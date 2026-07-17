import client, { unwrap } from '../api/client';

export const getDashboard = () => client.get('/api/dashboard').then(unwrap);
