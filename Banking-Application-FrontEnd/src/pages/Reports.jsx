import { useEffect, useState } from 'react';
import {
  Box, Grid, Card, CardContent, Typography, Stack, Button, TextField, Divider, Table, TableBody,
  TableCell, TableHead, TableRow, CircularProgress, Tabs, Tab, Chip,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import PageHeader from '../components/PageHeader';
import StatCard from '../components/StatCard';
import { inr, titleCase } from '../components/format';
import { myPortfolio, myTransactionReport } from '../services/reports';
import { errorMessage } from '../api/client';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';
import RequestQuoteIcon from '@mui/icons-material/RequestQuote';
import SavingsIcon from '@mui/icons-material/Savings';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';

export default function Reports() {
  const [tab, setTab] = useState(0);
  return (
    <Box>
      <PageHeader title="Reports" subtitle="Portfolio and transaction analytics" />
      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3 }}>
        <Tab label="Portfolio" />
        <Tab label="Transactions" />
      </Tabs>
      {tab === 0 && <PortfolioTab />}
      {tab === 1 && <TransactionTab />}
    </Box>
  );
}

function PortfolioTab() {
  const { enqueueSnackbar } = useSnackbar();
  const [p, setP] = useState(null);
  useEffect(() => {
    myPortfolio().then(setP).catch((e) => enqueueSnackbar(errorMessage(e), { variant: 'error' }));
  }, []); // eslint-disable-line

  if (!p) return <CircularProgress />;

  return (
    <Grid container spacing={3}>
      <Grid item xs={12} sm={6} md={3}>
        <StatCard label="Net Worth" value={inr(p.netWorth)} icon={<TrendingUpIcon />} />
      </Grid>
      <Grid item xs={12} sm={6} md={3}>
        <StatCard label="Total Balance" value={inr(p.totalBalance)} helper={`${p.accountsCount} account(s)`}
          icon={<AccountBalanceWalletIcon />} color="secondary.main" />
      </Grid>
      <Grid item xs={12} sm={6} md={3}>
        <StatCard label="Loan Outstanding" value={inr(p.totalLoanOutstanding)} helper={`${p.loansCount} loan(s)`}
          icon={<RequestQuoteIcon />} color="warning.main" />
      </Grid>
      <Grid item xs={12} sm={6} md={3}>
        <StatCard label="Deposit Maturity" value={inr(p.totalDepositMaturityValue)} helper={`${p.depositsCount} deposit(s)`}
          icon={<SavingsIcon />} color="success.main" />
      </Grid>
      <Grid item xs={12}>
        <Card>
          <CardContent>
            <Typography variant="h6" sx={{ mb: 1 }}>Account Holdings</Typography>
            <Divider />
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Account</TableCell>
                  <TableCell>Type</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell align="right">Balance</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {(p.accounts || []).map((a, i) => (
                  <TableRow key={i}>
                    <TableCell>{a.maskedAccountNumber}</TableCell>
                    <TableCell>{a.accountType}</TableCell>
                    <TableCell>{titleCase(a.status)}</TableCell>
                    <TableCell align="right">{inr(a.balance)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            <Divider sx={{ my: 2 }} />
            <Stack direction="row" spacing={3} flexWrap="wrap" useFlexGap>
              <Typography variant="body2">Deposit Principal: <b>{inr(p.totalDepositPrincipal)}</b></Typography>
              <Typography variant="body2">Cards: <b>{p.cardsCount}</b> ({p.activeCards} active)</Typography>
            </Stack>
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );
}

function TransactionTab() {
  const { enqueueSnackbar } = useSnackbar();
  const [range, setRange] = useState({ from: '', to: '' });
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(false);

  const run = async () => {
    setLoading(true);
    try {
      const params = {};
      if (range.from) params.from = range.from;
      if (range.to) params.to = range.to;
      setReport(await myTransactionReport(params));
    } catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
    finally { setLoading(false); }
  };

  useEffect(() => { run(); }, []); // eslint-disable-line

  return (
    <Box>
      <Stack direction="row" spacing={2} sx={{ mb: 3 }} alignItems="center">
        <TextField type="date" label="From" InputLabelProps={{ shrink: true }} size="small"
          value={range.from} onChange={(e) => setRange({ ...range, from: e.target.value })} />
        <TextField type="date" label="To" InputLabelProps={{ shrink: true }} size="small"
          value={range.to} onChange={(e) => setRange({ ...range, to: e.target.value })} />
        <Button variant="contained" onClick={run}>Generate</Button>
      </Stack>
      {loading ? <CircularProgress /> : report && (
        <Grid container spacing={3}>
          <Grid item xs={12} sm={6} md={3}>
            <StatCard label="Total Volume" value={inr(report.totalVolume)} helper={`${report.totalCount} txns`} />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <StatCard label="Total Debit" value={inr(report.totalDebit)} color="error.main" />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <StatCard label="Total Credit" value={inr(report.totalCredit)} color="success.main" />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <StatCard label="Scope" value={titleCase(report.scope || 'Self')} />
          </Grid>
          <Breakdown title="By Mode" rows={report.byMode} />
          <Breakdown title="By Type" rows={report.byType} />
          <Breakdown title="By Status" rows={report.byStatus} />
        </Grid>
      )}
    </Box>
  );
}

function Breakdown({ title, rows }) {
  return (
    <Grid item xs={12} md={4}>
      <Card>
        <CardContent>
          <Typography variant="h6" sx={{ mb: 1 }}>{title}</Typography>
          <Divider />
          <Table size="small">
            <TableBody>
              {(rows || []).map((r, i) => (
                <TableRow key={i}>
                  <TableCell><Chip size="small" variant="outlined" label={titleCase(r.key)} /></TableCell>
                  <TableCell align="right">{r.count ?? ''}</TableCell>
                  <TableCell align="right">{inr(r.volume)}</TableCell>
                </TableRow>
              ))}
              {(rows || []).length === 0 && <TableRow><TableCell align="center">No data</TableCell></TableRow>}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </Grid>
  );
}
