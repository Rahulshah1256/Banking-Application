import { useEffect, useState, useCallback } from 'react';
import {
  Grid, Card, CardContent, Typography, Box, Stack, Button, MenuItem, TextField,
  Table, TableBody, TableCell, TableHead, TableRow, Chip, TablePagination, Divider, Autocomplete,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import PageHeader from '../components/PageHeader';
import { inr, formatDate, titleCase } from '../components/format';
import { listAccounts } from '../services/accounts';
import { listBeneficiaries } from '../services/beneficiaries';
import { transfer, getHistory } from '../services/transfers';
import { errorMessage } from '../api/client';

const MODES = ['WITHIN_BANK', 'IMPS', 'NEFT', 'RTGS', 'UPI'];

export default function Transfer() {
  const { enqueueSnackbar } = useSnackbar();
  const [accounts, setAccounts] = useState([]);
  const [beneficiaries, setBeneficiaries] = useState([]);
  const [form, setForm] = useState({
    fromAccountNumber: '', toAccountNumber: '', amount: '', transferMode: 'WITHIN_BANK', description: '',
  });
  const [submitting, setSubmitting] = useState(false);
  const [history, setHistory] = useState([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);

  const loadHistory = useCallback(async () => {
    try {
      const data = await getHistory({ page, size });
      setHistory(data?.content || []);
      setTotal(data?.totalElements ?? 0);
    } catch (e) {
      enqueueSnackbar(errorMessage(e), { variant: 'error' });
    }
  }, [page, size, enqueueSnackbar]);

  useEffect(() => {
    listAccounts().then((d) => setAccounts(d || [])).catch(() => {});
    listBeneficiaries().then((d) => setBeneficiaries(d || [])).catch(() => {});
  }, []);

  useEffect(() => { loadHistory(); }, [loadHistory]);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const res = await transfer({ ...form, amount: Number(form.amount) });
      enqueueSnackbar(`Transfer successful · Ref ${res?.referenceNumber || ''}`, { variant: 'success' });
      setForm({ ...form, toAccountNumber: '', amount: '', description: '' });
      setPage(0);
      loadHistory();
    } catch (err) {
      enqueueSnackbar(errorMessage(err, 'Transfer failed'), { variant: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Box>
      <PageHeader title="Fund Transfer" subtitle="Send money within Janta Bank or to other banks" />
      <Grid container spacing={3}>
        <Grid item xs={12} md={5}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2 }}>New Transfer</Typography>
              <form onSubmit={submit}>
                <Stack spacing={2}>
                  <TextField select label="From Account" value={form.fromAccountNumber}
                    onChange={set('fromAccountNumber')} required fullWidth>
                    {accounts.map((a) => (
                      <MenuItem key={a.id} value={a.accountNumber}>
                        {a.accountType} · {a.accountNumber}
                      </MenuItem>
                    ))}
                  </TextField>

                  <Autocomplete
                    freeSolo
                    options={beneficiaries.filter((b) => b.payable)}
                    getOptionLabel={(o) => (typeof o === 'string' ? o : o.beneficiaryAccountNumber)}
                    onChange={(_, v) =>
                      setForm({ ...form, toAccountNumber: typeof v === 'string' ? v : v?.beneficiaryAccountNumber || '' })
                    }
                    onInputChange={(_, v) => setForm((f) => ({ ...f, toAccountNumber: v }))}
                    renderOption={(props, o) => (
                      <li {...props} key={o.id}>
                        {o.nickname || o.beneficiaryAccountName} · {o.maskedAccountNumber}
                      </li>
                    )}
                    renderInput={(params) => (
                      <TextField {...params} label="To Account" required
                        helperText="Type an account number or pick a beneficiary" />
                    )}
                    value={form.toAccountNumber}
                  />

                  <TextField select label="Transfer Mode" value={form.transferMode}
                    onChange={set('transferMode')} fullWidth>
                    {MODES.map((m) => <MenuItem key={m} value={m}>{m.replace('_', ' ')}</MenuItem>)}
                  </TextField>

                  <TextField label="Amount" type="number" value={form.amount}
                    onChange={set('amount')} required fullWidth inputProps={{ min: 1, step: '0.01' }} />

                  <TextField label="Description" value={form.description}
                    onChange={set('description')} fullWidth />

                  <Button type="submit" variant="contained" size="large" disabled={submitting}>
                    {submitting ? 'Processing…' : 'Transfer Now'}
                  </Button>
                </Stack>
              </form>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={7}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 1 }}>Transaction History</Typography>
              <Divider />
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Date</TableCell>
                    <TableCell>Reference</TableCell>
                    <TableCell>Counterparty</TableCell>
                    <TableCell align="right">Amount</TableCell>
                    <TableCell>Status</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {history.map((t) => (
                    <TableRow key={t.id}>
                      <TableCell>{formatDate(t.transactionDate)}</TableCell>
                      <TableCell>{t.referenceNumber}</TableCell>
                      <TableCell>{t.counterpartyAccount || '—'}</TableCell>
                      <TableCell align="right">
                        <Typography variant="body2"
                          color={t.direction === 'CREDIT' ? 'success.main' : 'error.main'}>
                          {t.direction === 'CREDIT' ? '+' : '-'}{inr(t.amount)}
                        </Typography>
                      </TableCell>
                      <TableCell><Chip size="small" label={titleCase(t.status)} /></TableCell>
                    </TableRow>
                  ))}
                  {history.length === 0 && (
                    <TableRow><TableCell colSpan={5} align="center">No transactions yet</TableCell></TableRow>
                  )}
                </TableBody>
              </Table>
              <TablePagination
                component="div"
                count={total}
                page={page}
                onPageChange={(_, p) => setPage(p)}
                rowsPerPage={size}
                onRowsPerPageChange={(e) => { setSize(parseInt(e.target.value, 10)); setPage(0); }}
                rowsPerPageOptions={[5, 10, 25]}
              />
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
