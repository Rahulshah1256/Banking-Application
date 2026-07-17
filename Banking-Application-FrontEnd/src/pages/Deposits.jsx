import { useEffect, useState } from 'react';
import {
  Box, Grid, Card, CardContent, Typography, Stack, Button, Chip, Divider, MenuItem, TextField,
  Dialog, DialogTitle, DialogContent, DialogActions, CircularProgress, Alert, FormControlLabel, Switch, Tabs, Tab,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useSnackbar } from 'notistack';
import PageHeader from '../components/PageHeader';
import { inr, formatDay, titleCase } from '../components/format';
import {
  listDeposits, calculateDeposit, openFd, openRd, payInstallment, closeDeposit, setAutoRenew,
} from '../services/deposits';
import { listAccounts } from '../services/accounts';
import { errorMessage } from '../api/client';

export default function Deposits() {
  const { enqueueSnackbar } = useSnackbar();
  const [deposits, setDeposits] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [open, setOpen] = useState(false);
  const [tab, setTab] = useState(0); // 0 = FD, 1 = RD
  const [fd, setFd] = useState({ principal: '', tenureMonths: '', linkedAccountNumber: '', autoRenew: false });
  const [rd, setRd] = useState({ monthlyInstallment: '', tenureMonths: '', linkedAccountNumber: '', autoRenew: false });
  const [calc, setCalc] = useState(null);

  const load = () => {
    setLoading(true);
    listDeposits().then((d) => setDeposits(d || [])).catch((e) => enqueueSnackbar(errorMessage(e), { variant: 'error' })).finally(() => setLoading(false));
  };
  useEffect(() => { load(); listAccounts().then((d) => setAccounts(d || [])).catch(() => {}); }, []); // eslint-disable-line

  const act = async (fn, ok) => {
    try { await fn(); enqueueSnackbar(ok, { variant: 'success' }); load(); }
    catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
  };

  const doCalc = async () => {
    try {
      const payload = tab === 0
        ? { depositType: 'FIXED', amount: Number(fd.principal), tenureMonths: Number(fd.tenureMonths) }
        : { depositType: 'RECURRING', amount: Number(rd.monthlyInstallment), tenureMonths: Number(rd.tenureMonths) };
      setCalc(await calculateDeposit(payload));
    } catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
  };

  const doOpen = async () => {
    try {
      if (tab === 0) {
        await openFd({ principal: Number(fd.principal), tenureMonths: Number(fd.tenureMonths), linkedAccountNumber: fd.linkedAccountNumber, autoRenew: fd.autoRenew });
      } else {
        await openRd({ monthlyInstallment: Number(rd.monthlyInstallment), tenureMonths: Number(rd.tenureMonths), linkedAccountNumber: rd.linkedAccountNumber, autoRenew: rd.autoRenew });
      }
      enqueueSnackbar('Deposit opened', { variant: 'success' });
      setOpen(false); setCalc(null);
      setFd({ principal: '', tenureMonths: '', linkedAccountNumber: '', autoRenew: false });
      setRd({ monthlyInstallment: '', tenureMonths: '', linkedAccountNumber: '', autoRenew: false });
      load();
    } catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
  };

  const doInstallment = (d) => act(() => payInstallment(d.id, { accountNumber: d.linkedAccountNumber }), 'Installment paid');
  const doClose = (d) => act(() => closeDeposit(d.id, { accountNumber: d.linkedAccountNumber }), 'Deposit closed');

  const statusColor = (s) => ({ ACTIVE: 'success', MATURED: 'warning', CLOSED: 'default' }[s] || 'default');

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}><CircularProgress /></Box>;

  return (
    <Box>
      <PageHeader
        title="Deposits"
        subtitle="Fixed and recurring deposits"
        action={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setOpen(true)}>Open Deposit</Button>}
      />
      <Grid container spacing={3}>
        {deposits.map((d) => (
          <Grid item xs={12} md={6} key={d.id}>
            <Card>
              <CardContent>
                <Stack direction="row" justifyContent="space-between">
                  <Box>
                    <Typography variant="overline" color="text.secondary">
                      {d.depositType === 'FIXED' ? 'Fixed Deposit' : 'Recurring Deposit'}
                    </Typography>
                    <Typography variant="h6">{d.depositReferenceNumber}</Typography>
                  </Box>
                  <Chip label={titleCase(d.status)} color={statusColor(d.status)} size="small" />
                </Stack>
                <Divider sx={{ my: 1.5 }} />
                <Grid container spacing={1}>
                  {d.depositType === 'FIXED'
                    ? <Info label="Principal" value={inr(d.principal)} />
                    : <Info label="Installment" value={inr(d.installmentAmount)} />}
                  <Info label="Rate" value={`${d.annualInterestRate}%`} />
                  <Info label="Tenure" value={`${d.tenureMonths} mo`} />
                  <Info label="Maturity" value={inr(d.maturityAmount)} />
                  <Info label="Matures On" value={d.maturityDate ? formatDay(d.maturityDate) : '—'} />
                  {d.depositType === 'RECURRING' && <Info label="Installments Paid" value={d.installmentsPaid} />}
                </Grid>
                <Divider sx={{ my: 1.5 }} />
                <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap" useFlexGap>
                  <FormControlLabel control={
                    <Switch checked={d.autoRenew} disabled={d.status !== 'ACTIVE'}
                      onChange={(e) => act(() => setAutoRenew(d.id, e.target.checked), 'Auto-renew updated')} />
                  } label="Auto-renew" />
                  {d.status === 'ACTIVE' && d.depositType === 'RECURRING' && (
                    <Button size="small" variant="contained" onClick={() => doInstallment(d)}>Pay Installment</Button>
                  )}
                  {d.status !== 'CLOSED' && (
                    <Button size="small" color="error" onClick={() => doClose(d)}>Close</Button>
                  )}
                </Stack>
              </CardContent>
            </Card>
          </Grid>
        ))}
        {deposits.length === 0 && <Grid item xs={12}><Typography color="text.secondary">No deposits yet.</Typography></Grid>}
      </Grid>

      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Open a Deposit</DialogTitle>
        <DialogContent dividers>
          <Tabs value={tab} onChange={(_, v) => { setTab(v); setCalc(null); }} sx={{ mb: 2 }}>
            <Tab label="Fixed Deposit" />
            <Tab label="Recurring Deposit" />
          </Tabs>
          {tab === 0 ? (
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField label="Principal" type="number" value={fd.principal}
                  onChange={(e) => setFd({ ...fd, principal: e.target.value })} fullWidth />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField label="Tenure (months)" type="number" value={fd.tenureMonths}
                  onChange={(e) => setFd({ ...fd, tenureMonths: e.target.value })} fullWidth />
              </Grid>
              <Grid item xs={12}>
                <TextField select label="Linked Account" value={fd.linkedAccountNumber}
                  onChange={(e) => setFd({ ...fd, linkedAccountNumber: e.target.value })} fullWidth>
                  {accounts.map((a) => <MenuItem key={a.id} value={a.accountNumber}>{a.accountType} · {a.accountNumber}</MenuItem>)}
                </TextField>
              </Grid>
              <Grid item xs={12}>
                <FormControlLabel control={<Switch checked={fd.autoRenew}
                  onChange={(e) => setFd({ ...fd, autoRenew: e.target.checked })} />} label="Auto-renew on maturity" />
              </Grid>
            </Grid>
          ) : (
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField label="Monthly Installment" type="number" value={rd.monthlyInstallment}
                  onChange={(e) => setRd({ ...rd, monthlyInstallment: e.target.value })} fullWidth />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField label="Tenure (months)" type="number" value={rd.tenureMonths}
                  onChange={(e) => setRd({ ...rd, tenureMonths: e.target.value })} fullWidth />
              </Grid>
              <Grid item xs={12}>
                <TextField select label="Linked Account" value={rd.linkedAccountNumber}
                  onChange={(e) => setRd({ ...rd, linkedAccountNumber: e.target.value })} fullWidth>
                  {accounts.map((a) => <MenuItem key={a.id} value={a.accountNumber}>{a.accountType} · {a.accountNumber}</MenuItem>)}
                </TextField>
              </Grid>
              <Grid item xs={12}>
                <FormControlLabel control={<Switch checked={rd.autoRenew}
                  onChange={(e) => setRd({ ...rd, autoRenew: e.target.checked })} />} label="Auto-renew on maturity" />
              </Grid>
            </Grid>
          )}
          <Box sx={{ mt: 2 }}>
            <Button variant="outlined" onClick={doCalc}>Calculate Maturity</Button>
          </Box>
          {calc && (
            <Alert severity="info" sx={{ mt: 2 }}>
              Rate {calc.annualInterestRate}% · Maturity Amount <b>{inr(calc.maturityAmount)}</b>
              {calc.totalInterest != null && <> · Interest {inr(calc.totalInterest)}</>}
            </Alert>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={doOpen}>Open Deposit</Button>
        </DialogActions>
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
