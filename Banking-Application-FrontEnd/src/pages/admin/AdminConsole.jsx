import { useEffect, useState, useCallback } from 'react';
import {
  Box, Grid, Card, CardContent, Typography, Stack, Button, Chip, Divider, MenuItem, TextField,
  Table, TableBody, TableCell, TableHead, TableRow, TablePagination, Tabs, Tab, CircularProgress,
  Dialog, DialogTitle, DialogContent, DialogActions,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import PageHeader from '../../components/PageHeader';
import StatCard from '../../components/StatCard';
import { inr, formatDate, titleCase } from '../../components/format';
import * as admin from '../../services/admin';
import { errorMessage } from '../../api/client';
import PeopleIcon from '@mui/icons-material/People';
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';
import SwapHorizIcon from '@mui/icons-material/SwapHoriz';
import SupportAgentIcon from '@mui/icons-material/SupportAgent';

const USER_STATUSES = ['REQUESTED', 'ACTIVE', 'INACTIVE', 'REJECTED'];
const MODES = ['WITHIN_BANK', 'IMPS', 'NEFT', 'RTGS', 'UPI'];

export default function AdminConsole() {
  const [tab, setTab] = useState(0);
  return (
    <Box>
      <PageHeader title="Admin Console" subtitle="Bank-wide operations and oversight" />
      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3 }}>
        <Tab label="Overview" />
        <Tab label="Users" />
        <Tab label="Transactions" />
        <Tab label="Reports" />
      </Tabs>
      {tab === 0 && <OverviewTab />}
      {tab === 1 && <UsersTab />}
      {tab === 2 && <TransactionsTab />}
      {tab === 3 && <ReportsTab />}
    </Box>
  );
}

function OverviewTab() {
  const { enqueueSnackbar } = useSnackbar();
  const [o, setO] = useState(null);
  useEffect(() => {
    admin.overview().then(setO).catch((e) => enqueueSnackbar(errorMessage(e), { variant: 'error' }));
  }, []); // eslint-disable-line
  if (!o) return <CircularProgress />;
  return (
    <Grid container spacing={3}>
      <Grid item xs={12} sm={6} md={3}><StatCard label="Total Users" value={o.totalUsers} icon={<PeopleIcon />} /></Grid>
      <Grid item xs={12} sm={6} md={3}><StatCard label="Total Accounts" value={o.totalAccounts} helper={inr(o.totalBalance)} icon={<AccountBalanceIcon />} color="secondary.main" /></Grid>
      <Grid item xs={12} sm={6} md={3}><StatCard label="Transactions" value={o.totalTransactions} helper={inr(o.totalTransactionVolume)} icon={<SwapHorizIcon />} color="warning.main" /></Grid>
      <Grid item xs={12} sm={6} md={3}><StatCard label="Open Tickets" value={o.openSupportTickets} helper={`${o.pendingKycDocuments} KYC pending`} icon={<SupportAgentIcon />} color="error.main" /></Grid>

      <Grid item xs={12} md={4}>
        <Card><CardContent>
          <Typography variant="h6" sx={{ mb: 1 }}>Lending & Deposits</Typography>
          <Divider />
          <Stack spacing={1} sx={{ mt: 1 }}>
            <Row label="Active Loans" value={o.activeLoans} />
            <Row label="Loan Outstanding" value={inr(o.totalLoanOutstanding)} />
            <Row label="Active Deposits" value={o.activeDeposits} />
            <Row label="Deposit Principal" value={inr(o.totalDepositPrincipal)} />
            <Row label="Total Cards" value={o.totalCards} />
          </Stack>
        </CardContent></Card>
      </Grid>
      <Grid item xs={12} md={4}>
        <Card><CardContent>
          <Typography variant="h6" sx={{ mb: 1 }}>Users by Status</Typography>
          <Divider />
          <Stack spacing={1} sx={{ mt: 1 }}>
            {Object.entries(o.usersByStatus || {}).map(([k, v]) => <Row key={k} label={titleCase(k)} value={v} />)}
          </Stack>
        </CardContent></Card>
      </Grid>
      <Grid item xs={12} md={4}>
        <Card><CardContent>
          <Typography variant="h6" sx={{ mb: 1 }}>Cards by Status</Typography>
          <Divider />
          <Stack spacing={1} sx={{ mt: 1 }}>
            {Object.entries(o.cardsByStatus || {}).map(([k, v]) => <Row key={k} label={titleCase(k)} value={v} />)}
          </Stack>
        </CardContent></Card>
      </Grid>
    </Grid>
  );
}

function UsersTab() {
  const { enqueueSnackbar } = useSnackbar();
  const [rows, setRows] = useState([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [q, setQ] = useState('');
  const [status, setStatus] = useState('');
  const [detail, setDetail] = useState(null);

  const load = useCallback(async () => {
    try {
      const params = { page, size };
      if (q) params.q = q;
      if (status) params.status = status;
      const data = await admin.listUsers(params);
      setRows(data?.content || []);
      setTotal(data?.totalElements ?? 0);
    } catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
  }, [page, size, q, status, enqueueSnackbar]);
  useEffect(() => { load(); }, [load]);

  const openDetail = async (u) => {
    setDetail({ id: u.id, data: null });
    try { setDetail({ id: u.id, data: await admin.getUser(u.id) }); }
    catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); setDetail(null); }
  };

  const changeStatus = async (u, newStatus) => {
    try {
      await admin.updateUserStatus(u.id, newStatus);
      enqueueSnackbar('User status updated', { variant: 'success' });
      load();
      if (detail?.id === u.id) setDetail({ id: u.id, data: await admin.getUser(u.id) });
    } catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
  };

  const statusColor = (s) => ({ ACTIVE: 'success', REQUESTED: 'warning', INACTIVE: 'default', REJECTED: 'error' }[s] || 'default');

  return (
    <Card>
      <CardContent>
        <Stack direction="row" spacing={2} sx={{ mb: 2 }} flexWrap="wrap" useFlexGap>
          <TextField size="small" label="Search" value={q} onChange={(e) => setQ(e.target.value)}
            onKeyDown={(e) => { if (e.key === 'Enter') { setPage(0); load(); } }} />
          <TextField select size="small" label="Status" value={status} sx={{ minWidth: 160 }}
            onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
            <MenuItem value="">All</MenuItem>
            {USER_STATUSES.map((s) => <MenuItem key={s} value={s}>{titleCase(s)}</MenuItem>)}
          </TextField>
          <Button variant="outlined" onClick={() => { setPage(0); load(); }}>Filter</Button>
        </Stack>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Username</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Accounts</TableCell>
              <TableCell>Status</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((u) => (
              <TableRow key={u.id} hover>
                <TableCell>{u.name}</TableCell>
                <TableCell>{u.username}</TableCell>
                <TableCell>{u.maskedEmail}</TableCell>
                <TableCell>{u.accountsCount}</TableCell>
                <TableCell><Chip size="small" label={titleCase(u.status)} color={statusColor(u.status)} /></TableCell>
                <TableCell align="right">
                  <Button size="small" onClick={() => openDetail(u)}>View</Button>
                  <TextField select size="small" value="" sx={{ width: 130, ml: 1 }}
                    onChange={(e) => changeStatus(u, e.target.value)}
                    SelectProps={{ displayEmpty: true }} label="">
                    <MenuItem value="" disabled>Set status</MenuItem>
                    {USER_STATUSES.map((s) => <MenuItem key={s} value={s}>{titleCase(s)}</MenuItem>)}
                  </TextField>
                </TableCell>
              </TableRow>
            ))}
            {rows.length === 0 && <TableRow><TableCell colSpan={6} align="center">No users</TableCell></TableRow>}
          </TableBody>
        </Table>
        <TablePagination component="div" count={total} page={page} onPageChange={(_, p) => setPage(p)}
          rowsPerPage={size} onRowsPerPageChange={(e) => { setSize(parseInt(e.target.value, 10)); setPage(0); }}
          rowsPerPageOptions={[5, 10, 25]} />
      </CardContent>

      <Dialog open={!!detail} onClose={() => setDetail(null)} maxWidth="md" fullWidth>
        <DialogTitle>User 360</DialogTitle>
        <DialogContent dividers>
          {!detail?.data ? <CircularProgress /> : (() => {
            const d = detail.data;
            return (
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Stack spacing={0.5}>
                    <Typography variant="h6">{d.name}</Typography>
                    <Typography variant="body2" color="text.secondary">@{d.username}</Typography>
                    <Row label="Email" value={d.maskedEmail} />
                    <Row label="Mobile" value={d.maskedMobile} />
                    <Row label="PAN" value={d.maskedPan} />
                    <Row label="Aadhaar" value={d.maskedAadhaar} />
                    <Row label="Status" value={titleCase(d.status)} />
                    <Row label="Roles" value={(d.roles || []).join(', ')} />
                  </Stack>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Stack spacing={0.5}>
                    <Row label="Total Balance" value={inr(d.totalBalance)} />
                    <Row label="Loans" value={d.loansCount} />
                    <Row label="Deposits" value={d.depositsCount} />
                    <Row label="Cards" value={d.cardsCount} />
                    <Row label="KYC Docs" value={d.kycDocumentsCount} />
                    <Row label="Support Tickets" value={d.supportTicketsCount} />
                  </Stack>
                </Grid>
                <Grid item xs={12}>
                  <Typography variant="subtitle2" sx={{ mb: 1 }}>Accounts</Typography>
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
                      {(d.accounts || []).map((a, i) => (
                        <TableRow key={i}>
                          <TableCell>{a.maskedAccountNumber}</TableCell>
                          <TableCell>{a.accountType}</TableCell>
                          <TableCell>{titleCase(a.status)}</TableCell>
                          <TableCell align="right">{inr(a.balance)}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </Grid>
              </Grid>
            );
          })()}
        </DialogContent>
        <DialogActions><Button onClick={() => setDetail(null)}>Close</Button></DialogActions>
      </Dialog>
    </Card>
  );
}

function TransactionsTab() {
  const { enqueueSnackbar } = useSnackbar();
  const [rows, setRows] = useState([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [filter, setFilter] = useState({ from: '', to: '', mode: '' });

  const load = useCallback(async () => {
    try {
      const params = { page, size };
      if (filter.from) params.from = filter.from;
      if (filter.to) params.to = filter.to;
      if (filter.mode) params.mode = filter.mode;
      const data = await admin.transactions(params);
      setRows(data?.content || []);
      setTotal(data?.totalElements ?? 0);
    } catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
  }, [page, size, filter, enqueueSnackbar]);
  useEffect(() => { load(); }, [load]);

  return (
    <Card>
      <CardContent>
        <Stack direction="row" spacing={2} sx={{ mb: 2 }} flexWrap="wrap" useFlexGap>
          <TextField type="date" size="small" label="From" InputLabelProps={{ shrink: true }}
            value={filter.from} onChange={(e) => setFilter({ ...filter, from: e.target.value })} />
          <TextField type="date" size="small" label="To" InputLabelProps={{ shrink: true }}
            value={filter.to} onChange={(e) => setFilter({ ...filter, to: e.target.value })} />
          <TextField select size="small" label="Mode" value={filter.mode} sx={{ minWidth: 150 }}
            onChange={(e) => setFilter({ ...filter, mode: e.target.value })}>
            <MenuItem value="">All</MenuItem>
            {MODES.map((m) => <MenuItem key={m} value={m}>{m.replace('_', ' ')}</MenuItem>)}
          </TextField>
          <Button variant="outlined" onClick={() => { setPage(0); load(); }}>Filter</Button>
        </Stack>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Date</TableCell>
              <TableCell>Reference</TableCell>
              <TableCell>From</TableCell>
              <TableCell>To</TableCell>
              <TableCell>Mode</TableCell>
              <TableCell align="right">Amount</TableCell>
              <TableCell>Status</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((t) => (
              <TableRow key={t.id} hover>
                <TableCell>{formatDate(t.transactionDate)}</TableCell>
                <TableCell>{t.referenceNumber}</TableCell>
                <TableCell>{t.maskedFromAccount}</TableCell>
                <TableCell>{t.maskedToAccount}</TableCell>
                <TableCell>{t.transferMode}</TableCell>
                <TableCell align="right">{inr(t.amount)}</TableCell>
                <TableCell><Chip size="small" label={titleCase(t.status)} /></TableCell>
              </TableRow>
            ))}
            {rows.length === 0 && <TableRow><TableCell colSpan={7} align="center">No transactions</TableCell></TableRow>}
          </TableBody>
        </Table>
        <TablePagination component="div" count={total} page={page} onPageChange={(_, p) => setPage(p)}
          rowsPerPage={size} onRowsPerPageChange={(e) => { setSize(parseInt(e.target.value, 10)); setPage(0); }}
          rowsPerPageOptions={[5, 10, 25]} />
      </CardContent>
    </Card>
  );
}

function ReportsTab() {
  const { enqueueSnackbar } = useSnackbar();
  const [range, setRange] = useState({ from: '', to: '' });
  const [report, setReport] = useState(null);

  const run = async () => {
    try {
      const params = {};
      if (range.from) params.from = range.from;
      if (range.to) params.to = range.to;
      setReport(await admin.transactionReport(params));
    } catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
  };
  useEffect(() => { run(); }, []); // eslint-disable-line

  return (
    <Box>
      <Stack direction="row" spacing={2} sx={{ mb: 3 }}>
        <TextField type="date" size="small" label="From" InputLabelProps={{ shrink: true }}
          value={range.from} onChange={(e) => setRange({ ...range, from: e.target.value })} />
        <TextField type="date" size="small" label="To" InputLabelProps={{ shrink: true }}
          value={range.to} onChange={(e) => setRange({ ...range, to: e.target.value })} />
        <Button variant="contained" onClick={run}>Generate</Button>
      </Stack>
      {report && (
        <Grid container spacing={3}>
          <Grid item xs={12} sm={6} md={3}><StatCard label="Total Volume" value={inr(report.totalVolume)} helper={`${report.totalCount} txns`} /></Grid>
          <Grid item xs={12} sm={6} md={3}><StatCard label="Total Debit" value={inr(report.totalDebit)} color="error.main" /></Grid>
          <Grid item xs={12} sm={6} md={3}><StatCard label="Total Credit" value={inr(report.totalCredit)} color="success.main" /></Grid>
          <Grid item xs={12} sm={6} md={3}><StatCard label="Scope" value={titleCase(report.scope || 'Bank')} /></Grid>
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
      <Card><CardContent>
        <Typography variant="h6" sx={{ mb: 1 }}>{title}</Typography>
        <Divider />
        <Table size="small">
          <TableBody>
            {(rows || []).map((r, i) => (
              <TableRow key={i}>
                <TableCell><Chip size="small" variant="outlined" label={titleCase(r.key)} /></TableCell>
                <TableCell align="right">{r.count}</TableCell>
                <TableCell align="right">{inr(r.volume)}</TableCell>
              </TableRow>
            ))}
            {(rows || []).length === 0 && <TableRow><TableCell align="center">No data</TableCell></TableRow>}
          </TableBody>
        </Table>
      </CardContent></Card>
    </Grid>
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
