import { useEffect, useState } from 'react';
import {
  Box, Grid, Card, CardContent, Typography, Stack, Button, Chip, Divider, MenuItem, TextField,
  Dialog, DialogTitle, DialogContent, DialogActions, CircularProgress, Table, TableBody, TableCell,
  TableHead, TableRow,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useSnackbar } from 'notistack';
import PageHeader from '../components/PageHeader';
import { formatDate, formatDay, titleCase } from '../components/format';
import {
  listBooks, requestBook, getBook, issueBook, deliverBook, stopCheque, registerPositivePay,
} from '../services/cheques';
import { listAccounts } from '../services/accounts';
import { errorMessage } from '../api/client';

export default function Cheques() {
  const { enqueueSnackbar } = useSnackbar();
  const [books, setBooks] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [reqOpen, setReqOpen] = useState(false);
  const [form, setForm] = useState({ accountNumber: '', numberOfLeaves: 25, deliveryAddress: '' });
  const [detail, setDetail] = useState(null);

  const load = () => {
    setLoading(true);
    listBooks().then((d) => setBooks(d || [])).catch((e) => enqueueSnackbar(errorMessage(e), { variant: 'error' })).finally(() => setLoading(false));
  };
  useEffect(() => { load(); listAccounts().then((d) => setAccounts(d || [])).catch(() => {}); }, []); // eslint-disable-line

  const act = async (fn, ok, after) => {
    try { await fn(); enqueueSnackbar(ok, { variant: 'success' }); load(); if (after) after(); }
    catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
  };

  const doRequest = async () => {
    try {
      await requestBook({ accountNumber: form.accountNumber, numberOfLeaves: Number(form.numberOfLeaves), deliveryAddress: form.deliveryAddress });
      enqueueSnackbar('Cheque book requested', { variant: 'success' });
      setReqOpen(false);
      setForm({ accountNumber: '', numberOfLeaves: 25, deliveryAddress: '' });
      load();
    } catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
  };

  const openDetail = async (b) => {
    setDetail({ book: b, full: null });
    try { setDetail({ book: b, full: await getBook(b.id) }); }
    catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); setDetail(null); }
  };

  const refreshDetail = async (id) => { try { setDetail((d) => d && { ...d }); const f = await getBook(id); setDetail((d) => d && { ...d, full: f }); } catch { /* ignore */ } };

  const doStop = (leaf) => {
    const reason = window.prompt('Reason to stop this cheque?') || 'User requested';
    act(() => stopCheque(leaf.id, { reason }), 'Cheque stopped', () => refreshDetail(detail.full.id));
  };
  const doPositivePay = (leaf) => {
    const amount = window.prompt('Positive-pay amount?');
    if (!amount) return;
    const payeeName = window.prompt('Payee name?') || '';
    const chequeDate = window.prompt('Cheque date (YYYY-MM-DD)?') || new Date().toISOString().slice(0, 10);
    act(() => registerPositivePay(leaf.id, { amount: Number(amount), payeeName, chequeDate }),
      'Positive pay registered', () => refreshDetail(detail.full.id));
  };

  const statusColor = (s) => ({ ISSUED: 'success', DELIVERED: 'success', REQUESTED: 'warning', ACTIVE: 'success', USED: 'default', STOPPED: 'error' }[s] || 'default');

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}><CircularProgress /></Box>;

  return (
    <Box>
      <PageHeader
        title="Cheque Books"
        subtitle="Request cheque books and manage leaves"
        action={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setReqOpen(true)}>Request Book</Button>}
      />
      <Grid container spacing={3}>
        {books.map((b) => (
          <Grid item xs={12} md={6} key={b.id}>
            <Card>
              <CardContent>
                <Stack direction="row" justifyContent="space-between">
                  <Box>
                    <Typography variant="overline" color="text.secondary">Cheque Book</Typography>
                    <Typography variant="h6">{b.bookReferenceNumber}</Typography>
                  </Box>
                  <Chip label={titleCase(b.status)} color={statusColor(b.status)} size="small" />
                </Stack>
                <Divider sx={{ my: 1.5 }} />
                <Typography variant="body2">Account: {b.accountNumber}</Typography>
                <Typography variant="body2">Leaves: {b.numberOfLeaves} ({b.startChequeNumber}–{b.endChequeNumber})</Typography>
                <Typography variant="body2" color="text.secondary">Requested {formatDate(b.requestedAt)}</Typography>
                <Divider sx={{ my: 1.5 }} />
                <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                  <Button size="small" onClick={() => openDetail(b)}>View Leaves</Button>
                  {b.status === 'REQUESTED' && (
                    <Button size="small" onClick={() => act(() => issueBook(b.id), 'Book issued')}>Issue</Button>
                  )}
                  {b.status === 'ISSUED' && (
                    <Button size="small" onClick={() => act(() => deliverBook(b.id), 'Book delivered')}>Mark Delivered</Button>
                  )}
                </Stack>
              </CardContent>
            </Card>
          </Grid>
        ))}
        {books.length === 0 && <Grid item xs={12}><Typography color="text.secondary">No cheque books yet.</Typography></Grid>}
      </Grid>

      <Dialog open={reqOpen} onClose={() => setReqOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Request Cheque Book</DialogTitle>
        <DialogContent dividers>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField select label="Account" value={form.accountNumber}
              onChange={(e) => setForm({ ...form, accountNumber: e.target.value })} required fullWidth>
              {accounts.map((a) => <MenuItem key={a.id} value={a.accountNumber}>{a.accountType} · {a.accountNumber}</MenuItem>)}
            </TextField>
            <TextField select label="Number of Leaves" value={form.numberOfLeaves}
              onChange={(e) => setForm({ ...form, numberOfLeaves: e.target.value })} fullWidth>
              {[10, 25, 50, 100].map((n) => <MenuItem key={n} value={n}>{n}</MenuItem>)}
            </TextField>
            <TextField label="Delivery Address" value={form.deliveryAddress}
              onChange={(e) => setForm({ ...form, deliveryAddress: e.target.value })} fullWidth multiline minRows={2} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setReqOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={doRequest} disabled={!form.accountNumber}>Request</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!detail} onClose={() => setDetail(null)} maxWidth="md" fullWidth>
        <DialogTitle>Leaves · {detail?.book?.bookReferenceNumber}</DialogTitle>
        <DialogContent dividers>
          {!detail?.full ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}><CircularProgress size={28} /></Box>
          ) : (
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Cheque #</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Positive Pay</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {(detail.full.leaves || []).map((leaf) => (
                  <TableRow key={leaf.id}>
                    <TableCell>{leaf.chequeNumber}</TableCell>
                    <TableCell><Chip size="small" label={titleCase(leaf.status)} color={statusColor(leaf.status)} /></TableCell>
                    <TableCell>
                      {leaf.positivePayRegistered
                        ? `${leaf.positivePayPayee || ''} · ${leaf.positivePayAmount ?? ''} · ${leaf.positivePayDate ? formatDay(leaf.positivePayDate) : ''}`
                        : '—'}
                    </TableCell>
                    <TableCell align="right">
                      {leaf.status !== 'STOPPED' && (
                        <Stack direction="row" spacing={1} justifyContent="flex-end">
                          <Button size="small" onClick={() => doPositivePay(leaf)}>Positive Pay</Button>
                          <Button size="small" color="error" onClick={() => doStop(leaf)}>Stop</Button>
                        </Stack>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
                {(detail.full.leaves || []).length === 0 && (
                  <TableRow><TableCell colSpan={4} align="center">No leaves available</TableCell></TableRow>
                )}
              </TableBody>
            </Table>
          )}
        </DialogContent>
        <DialogActions><Button onClick={() => setDetail(null)}>Close</Button></DialogActions>
      </Dialog>
    </Box>
  );
}
