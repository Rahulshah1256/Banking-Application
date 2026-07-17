import client, { unwrap } from '../api/client';

export const myTransactionReport = (params) =>
  client.get('/api/reports/transactions', { params }).then(unwrap);
export const myPortfolio = () => client.get('/api/reports/portfolio').then(unwrap);
