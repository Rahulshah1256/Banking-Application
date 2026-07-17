import { useEffect, useState } from 'react';
import {
  Box, Grid, Card, CardContent, Typography, Stack, Button, Chip, Divider, MenuItem, TextField,
  Dialog, DialogTitle, DialogContent, DialogActions, Table, TableBody, TableCell, TableHead,
  TableRow, CircularProgress, Alert,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useSnackbar } from 'notistack';
import PageHeader from '../components/PageHeader';
import { inr, formatDay, titleCase } from '../components/format';
import {
  listLoans, calculateLoan, applyLoan, getSchedule, payEmi, prepay,
} from '../services/loans';
import { listAccounts } from '../services/accounts';
import { errorMessage } from '../api/client';

const LOAN_TYPES = ['HOME', 'CAR', 'EDUCATION', 'PERSONAL'];

export default function Loans() {
  const { enqueueSnackbar } = useSnackbar();
  const [loans, setLoans] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [applyOpen, setApplyOpen] = useState(false);
  const [form, setForm] = useState({ loanType: 'PERSONAL', principal: '', tenureMonths: '', disbursementAccountNumber: '' });
  const [calc, setCalc] = useState(null);
  const [schedule, setSchedule] = useState(null);

  const load = () => {
    setLoading(true);
    listLoans().then((d) => setLoans(d || [])).catch((e) => enqueueSnackbar(errorMessage(e), { variant: 'error' })).finally(() => setLoading(false));
  };
  useEffect(() => { load(); listAccounts().then((d) => setAccounts(d || [])).catch(() => {}); }, []); // eslint-disable-line

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const doCalc = async () => {
    try {
      const c = await calculateLoan({ loanType: form.loanType, principal: Number(form.principal), tenureMonths: Number(form.tenureMonths) });
      setCalc(c);
    } catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
  };

  const doApply = async () => {
    try {
      await applyLoan({
        loanType: form.loanType, principal: Number(form.principal),
        tenureMonths: Number(form.tenureMonths), disbursementAccountNumber: form.disbursementAccountNumber,
      });
      enqueueSnackbar('Loan application submitted', { variant: 'success' });
      setApplyOpen(false); setCalc(null);
      setForm({ loanType: 'PERSONAL', principal: '', tenureMonths: '', disbursementAccountNumber: '' });
      load();
    } catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
  };

  const act = async (fn, ok) => {
    try { await fn(); enqueueSnackbar(ok, { variant: 'success' }); load(); }
    catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
  };

  const doPayEmi = (l) => act(() => payEmi(l.id, { accountNumber: l.disbursementAccountNumber }), 'EMI paid');
  const doPrepay = (l) => {
    const amt = window.prompt('Prepayment amount?');
    if (!amt) return;
    act(() => prepay(l.id, { amount: Number(amt) }), 'Prepayment successful');
  };
  const openSchedule = async (l) => {
    setSchedule({ loan: l, rows: null });
    try {
      const s = await getSchedule(l.id);
      setSchedule({ loan: l, rows: s || [] });
    } catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); setSchedule(null); }
  };

  const statusColor = (s) => ({ ACTIVE: 'success', PENDING: 'warning', CLOSED: 'default', REJECTED: 'error' }[s] || 'default');

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}><CircularProgress /></Box>;

  return (
    <Box>
      <PageHeader
        title="Loans"
        subtitle="Apply for and manage your loans"
        action={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setApplyOpen(true)}>Apply for Loan</Button>}
      />
      <Grid container spacing={3}>
        {loans.map((l) => (
          <Grid item xs={12} md={6} key={l.id}>
            <Card>
              <CardContent>
                <Stack direction="row" justifyContent="space-between">
                  <Box>
                    <Typography variant="overline" color="text.secondary">{titleCase(l.loanType)} Loan</Typography>
                    <Typography variant="h6">{l.loanReferenceNumber}</Typography>
                  </Box>
                  <Chip label={titleCase(l.status)} color={statusColor(l.status)} size="small" />
                </Stack>
                <Divider sx={{ my: 1.5 }} />
                <Grid container spacing={1}>
                  <Info label="Principal" value={inr(l.principal)} />
                  <Info label="Rate" value={`${l.annualInterestRate}%`} />
                  <Info label="EMI" value={inr(l.emiAmount)} />
                  <Info label="Tenure" value={`${l.tenureMonths} mo`} />
                  <Info label="Outstanding" value={inr(l.outstandingPrincipal)} />
                  <Info label="EMIs Paid" value={`${l.emisPaid}/${l.tenureMonths}`} />
                  <Info label="Next EMI" value={l.nextEmiDate ? formatDay(l.nextEmiDate) : '—'} />
                </Grid>
                {l.status === 'ACTIVE' && (
                  <>
                    <Divider sx={{ my: 1.5 }} />
                    <Stack direction="row" spacing={1}>
                      <Button size="small" variant="contained" onClick={() => doPayEmi(l)}>Pay EMI</Button>
                      <Button size="small" onClick={() => doPrepay(l)}>Prepay</Button>
                      <Button size="small" onClick={() => openSchedule(l)}>Schedule</Button>
                    </Stack>
                  </>
                )}
                {l.status !== 'ACTIVE' && (
                  <Button size="small" sx={{ mt: 1 }} onClick={() => openSchedule(l)}>Schedule</Button>
                )}
              </CardContent>
            </Card>
          </Grid>
        ))}
        {loans.length === 0 && <Grid item xs={12}><Typography color="text.secondary">No loans yet.</Typography></Grid>}
      </Grid>

      <Dialog open={applyOpen} onClose={() => setApplyOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Apply for a Loan</DialogTitle>
        <DialogContent dividers>
          <Grid container spacing={2} sx={{ mt: 0 }}>
            <Grid item xs={12} sm={6}>
              <TextField select label="Loan Type" value={form.loanType} onChange={set('loanType')} fullWidth>
                {LOAN_TYPES.map((t) => <MenuItem key={t} value={t}>{titleCase(t)}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField label="Principal" type="number" value={form.principal} onChange={set('principal')} fullWidth />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField label="Tenure (months)" type="number" value={form.tenureMonths} onChange={set('tenureMonths')} fullWidth />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField select label="Disbursement Account" value={form.disbursementAccountNumber}
                onChange={set('disbursementAccountNumber')} fullWidth>
                {accounts.map((a) => <MenuItem key={a.id} value={a.accountNumber}>{a.accountType} · {a.accountNumber}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12}>
              <Button variant="outlined" onClick={doCalc}
                disabled={!form.principal || !form.tenureMonths}>Calculate EMI</Button>
            </Grid>
            {calc && (
              <Grid item xs={12}>
                <Alert severity="info">
                  EMI <b>{inr(calc.emiAmount)}</b> · Rate {calc.annualInterestRate}% · Total Payable {inr(calc.totalPayable)} · Interest {inr(calc.totalInterest)}
                </Alert>
              </Grid>
            )}
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setApplyOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={doApply}
            disabled={!form.principal || !form.tenureMonths || !form.disbursementAccountNumber}>Submit Application</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!schedule} onClose={() => setSchedule(null)} maxWidth="md" fullWidth>
        <DialogTitle>Amortization Schedule · {schedule?.loan?.loanReferenceNumber}</DialogTitle>
        <DialogContent dividers>
          {!schedule?.rows ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}><CircularProgress size={28} /></Box>
          ) : (
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>#</TableCell>
                  <TableCell>Due Date</TableCell>
                  <TableCell align="right">EMI</TableCell>
                  <TableCell align="right">Principal</TableCell>
                  <TableCell align="right">Interest</TableCell>
                  <TableCell align="right">Balance</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {schedule.rows.map((r, i) => (
                  <TableRow key={i}>
                    <TableCell>{r.installmentNumber ?? i + 1}</TableCell>
                    <TableCell>{formatDay(r.dueDate)}</TableCell>
                    <TableCell align="right">{inr(r.emiAmount ?? r.emi)}</TableCell>
                    <TableCell align="right">{inr(r.principalComponent ?? r.principal)}</TableCell>
                    <TableCell align="right">{inr(r.interestComponent ?? r.interest)}</TableCell>
                    <TableCell align="right">{inr(r.outstandingBalance ?? r.balance)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </DialogContent>
        <DialogActions><Button onClick={() => setSchedule(null)}>Close</Button></DialogActions>
      </Dialog>
    </Box>
  );
}

function Info({ label, value }) {
  return (
    <Grid item xs={6}>
      <Typography variant="caption" color="text.secondary">{label}</Typography>
      <Typography variant="body2" fontWeight={600}>{value}</Typography>
    </Grid>
  );
}
