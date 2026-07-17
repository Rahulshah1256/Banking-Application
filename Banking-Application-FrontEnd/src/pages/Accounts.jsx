import { useEffect, useState } from 'react';
import {
  Grid, Card, CardContent, Typography, Box, Stack, Chip, Button, CircularProgress,
  Dialog, DialogTitle, DialogContent, DialogActions, Divider, Table, TableBody, TableCell,
  TableHead, TableRow, TextField,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import PageHeader from '../components/PageHeader';
import { inr, formatDate, titleCase } from '../components/format';
import { listAccounts, getAccountSummary, getStatement } from '../services/accounts';
import { errorMessage } from '../api/client';

export default function Accounts() {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [summary, setSummary] = useState(null);
  const [statement, setStatement] = useState(null);
  const [range, setRange] = useState({ from: '', to: '' });
  const [current, setCurrent] = useState(null);
  const { enqueueSnackbar } = useSnackbar();

  useEffect(() => {
    listAccounts()
      .then((d) => setAccounts(d || []))
      .catch((e) => enqueueSnackbar(errorMessage(e), { variant: 'error' }))
      .finally(() => setLoading(false));
  }, [enqueueSnackbar]);

  const openSummary = async (acc) => {
    try {
      const s = await getAccountSummary(acc.id);
      setSummary(s);
    } catch (e) {
      enqueueSnackbar(errorMessage(e), { variant: 'error' });
    }
  };

  const openStatement = async (acc) => {
    setCurrent(acc);
    setStatement(null);
    try {
      const params = {};
      if (range.from) params.from = range.from;
      if (range.to) params.to = range.to;
      const s = await getStatement(acc.id, params);
      setStatement(s || []);
    } catch (e) {
      enqueueSnackbar(errorMessage(e), { variant: 'error' });
    }
  };

  if (loading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}><CircularProgress /></Box>;
  }

  return (
    <Box>
      <PageHeader title="Accounts" subtitle="View balances, summaries and statements" />
      <Grid container spacing={3}>
        {accounts.map((a) => (
          <Grid item xs={12} md={6} key={a.id}>
            <Card>
              <CardContent>
                <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
                  <Box>
                    <Typography variant="overline" color="text.secondary">{a.accountType}</Typography>
                    <Typography variant="h6">{a.accountNumber}</Typography>
                    <Typography variant="body2" color="text.secondary">{a.ifscCode} · {a.branchId}</Typography>
                  </Box>
                  <Chip label={a.accountHolderName} size="small" />
                </Stack>
                <Divider sx={{ my: 2 }} />
                <Stack direction="row" spacing={1}>
                  <Button size="small" variant="outlined" onClick={() => openSummary(a)}>Summary</Button>
                  <Button size="small" variant="contained" onClick={() => openStatement(a)}>Statement</Button>
                </Stack>
              </CardContent>
            </Card>
          </Grid>
        ))}
        {accounts.length === 0 && (
          <Grid item xs={12}><Typography color="text.secondary">No accounts found.</Typography></Grid>
        )}
      </Grid>

      <Dialog open={!!summary} onClose={() => setSummary(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Account Summary</DialogTitle>
        <DialogContent dividers>
          {summary && (
            <Stack spacing={1}>
              <Row label="Account" value={summary.maskedAccountNumber} />
              <Row label="Type" value={summary.accountType} />
              <Row label="Status" value={titleCase(summary.status)} />
              <Row label="Current Balance" value={inr(summary.currentBalance)} />
              <Row label="Available Balance" value={inr(summary.availableBalance)} />
              <Row label="Interest Rate" value={`${summary.annualInterestRate}%`} />
              <Row label="Projected Annual Interest" value={inr(summary.projectedAnnualInterest)} />
              <Row label="Account Age" value={`${summary.accountAgeDays} days`} />
              <Row label="Nominee" value={summary.nomineeName || '—'} />
              <Row label="KYC" value={titleCase(summary.kycStatus)} />
              <Row label="Cheque Book" value={titleCase(summary.chequeBookStatus)} />
            </Stack>
          )}
        </DialogContent>
        <DialogActions><Button onClick={() => setSummary(null)}>Close</Button></DialogActions>
      </Dialog>

      <Dialog open={!!current} onClose={() => setCurrent(null)} maxWidth="md" fullWidth>
        <DialogTitle>Statement · {current?.accountNumber}</DialogTitle>
        <DialogContent dividers>
          <Stack direction="row" spacing={2} sx={{ mb: 2 }}>
            <TextField type="date" label="From" InputLabelProps={{ shrink: true }} size="small"
              value={range.from} onChange={(e) => setRange({ ...range, from: e.target.value })} />
            <TextField type="date" label="To" InputLabelProps={{ shrink: true }} size="small"
              value={range.to} onChange={(e) => setRange({ ...range, to: e.target.value })} />
            <Button variant="outlined" onClick={() => openStatement(current)}>Apply</Button>
          </Stack>
          {!statement ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}><CircularProgress size={28} /></Box>
          ) : (
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Date</TableCell>
                  <TableCell>Direction</TableCell>
                  <TableCell>Counterparty</TableCell>
                  <TableCell align="right">Amount</TableCell>
                  <TableCell>Status</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {statement.map((s) => (
                  <TableRow key={s.id}>
                    <TableCell>{formatDate(s.valueDate)}</TableCell>
                    <TableCell>
                      <Chip size="small" label={s.direction}
                        color={s.direction === 'CREDIT' ? 'success' : 'default'} />
                    </TableCell>
                    <TableCell>{s.counterpartyAccount || '—'}</TableCell>
                    <TableCell align="right">{inr(s.amount)}</TableCell>
                    <TableCell>{titleCase(s.status)}</TableCell>
                  </TableRow>
                ))}
                {statement.length === 0 && (
                  <TableRow><TableCell colSpan={5} align="center">No transactions in range</TableCell></TableRow>
                )}
              </TableBody>
            </Table>
          )}
        </DialogContent>
        <DialogActions><Button onClick={() => setCurrent(null)}>Close</Button></DialogActions>
      </Dialog>
    </Box>
  );
}

function Row({ label, value }) {
  return (
    <Stack direction="row" justifyContent="space-between">
      <Typography variant="body2" color="text.secondary">{label}</Typography>
      <Typography variant="body2" fontWeight={600}>{value}</Typography>
    </Stack>
  );
}
