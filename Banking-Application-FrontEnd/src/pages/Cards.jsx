import { useEffect, useState } from 'react';
import {
  Box, Grid, Card, CardContent, Typography, Stack, Button, Chip, Divider, Switch,
  FormControlLabel, Dialog, DialogTitle, DialogContent, DialogActions, TextField, MenuItem,
  CircularProgress, Tooltip,
} from '@mui/material';
import CreditCardIcon from '@mui/icons-material/CreditCard';
import AddIcon from '@mui/icons-material/Add';
import { useSnackbar } from 'notistack';
import PageHeader from '../components/PageHeader';
import { inr, formatDate, titleCase } from '../components/format';
import {
  listCards, issueCard, blockCard, unblockCard, replaceCard, setPin, updateControls, updateLimits,
} from '../services/cards';
import { listAccounts } from '../services/accounts';
import { errorMessage } from '../api/client';

const NETWORKS = ['VISA', 'MASTERCARD', 'RUPAY'];

export default function Cards() {
  const { enqueueSnackbar } = useSnackbar();
  const [cards, setCards] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [issueOpen, setIssueOpen] = useState(false);
  const [issueForm, setIssueForm] = useState({ accountNumber: '', network: 'VISA' });
  const [limitCard, setLimitCard] = useState(null);
  const [limits, setLimits] = useState({ atmDailyLimit: '', posDailyLimit: '', onlineDailyLimit: '' });
  const [pinCard, setPinCard] = useState(null);
  const [pin, setPinVal] = useState('');

  const load = () => {
    setLoading(true);
    listCards().then((d) => setCards(d || [])).catch((e) => enqueueSnackbar(errorMessage(e), { variant: 'error' })).finally(() => setLoading(false));
  };
  useEffect(() => { load(); listAccounts().then((d) => setAccounts(d || [])).catch(() => {}); }, []); // eslint-disable-line

  const act = async (fn, ok) => {
    try { await fn(); enqueueSnackbar(ok, { variant: 'success' }); load(); }
    catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
  };

  const doIssue = async () => {
    try {
      await issueCard(issueForm);
      enqueueSnackbar('Card issued', { variant: 'success' });
      setIssueOpen(false);
      load();
    } catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
  };

  const doBlock = (c) => {
    const reason = window.prompt('Reason for blocking the card?') || 'User requested';
    act(() => blockCard(c.id, { reason }), 'Card blocked');
  };

  const openLimits = (c) => {
    setLimitCard(c);
    setLimits({ atmDailyLimit: c.atmDailyLimit, posDailyLimit: c.posDailyLimit, onlineDailyLimit: c.onlineDailyLimit });
  };
  const saveLimits = async () => {
    await act(() => updateLimits(limitCard.id, {
      atmDailyLimit: Number(limits.atmDailyLimit),
      posDailyLimit: Number(limits.posDailyLimit),
      onlineDailyLimit: Number(limits.onlineDailyLimit),
    }), 'Limits updated');
    setLimitCard(null);
  };

  const savePin = async () => {
    await act(() => setPin(pinCard.id, { pin }), 'PIN set');
    setPinCard(null); setPinVal('');
  };

  const statusColor = (s) => ({ ACTIVE: 'success', BLOCKED: 'error', REPLACED: 'default', EXPIRED: 'warning' }[s] || 'default');

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}><CircularProgress /></Box>;

  return (
    <Box>
      <PageHeader
        title="Cards"
        subtitle="Manage your debit cards, controls and limits"
        action={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setIssueOpen(true)}>Issue Card</Button>}
      />
      <Grid container spacing={3}>
        {cards.map((c) => (
          <Grid item xs={12} md={6} key={c.id}>
            <Card>
              <CardContent>
                <Box sx={{
                  p: 2, borderRadius: 2, mb: 2, color: 'white',
                  background: 'linear-gradient(120deg,#0b5cab,#00897b)',
                }}>
                  <Stack direction="row" justifyContent="space-between" alignItems="center">
                    <CreditCardIcon />
                    <Typography variant="body2">{c.network} · {c.cardType}</Typography>
                  </Stack>
                  <Typography variant="h6" sx={{ letterSpacing: 2, mt: 2 }}>{c.maskedCardNumber}</Typography>
                  <Stack direction="row" justifyContent="space-between" sx={{ mt: 1 }}>
                    <Typography variant="caption">{c.cardHolderName}</Typography>
                    <Typography variant="caption">Exp {c.expiry}</Typography>
                  </Stack>
                </Box>
                <Stack direction="row" spacing={1} sx={{ mb: 1 }} alignItems="center">
                  <Chip size="small" label={titleCase(c.status)} color={statusColor(c.status)} />
                  <Chip size="small" variant="outlined" label={c.pinSet ? 'PIN set' : 'No PIN'} />
                  <Typography variant="caption" color="text.secondary">A/C {c.maskedAccountNumber}</Typography>
                </Stack>
                {c.blockedReason && (
                  <Typography variant="caption" color="error">Blocked: {c.blockedReason}</Typography>
                )}
                <Divider sx={{ my: 1.5 }} />
                <Stack spacing={1}>
                  <Stack direction="row" spacing={2} flexWrap="wrap">
                    <FormControlLabel control={
                      <Switch checked={c.internationalEnabled}
                        onChange={(e) => act(() => updateControls(c.id, { internationalEnabled: e.target.checked }), 'Updated')} />
                    } label="International" />
                    <FormControlLabel control={
                      <Switch checked={c.onlineEnabled}
                        onChange={(e) => act(() => updateControls(c.id, { onlineEnabled: e.target.checked }), 'Updated')} />
                    } label="Online" />
                    <FormControlLabel control={
                      <Switch checked={c.contactlessEnabled}
                        onChange={(e) => act(() => updateControls(c.id, { contactlessEnabled: e.target.checked }), 'Updated')} />
                    } label="Contactless" />
                  </Stack>
                  <Typography variant="caption" color="text.secondary">
                    Limits · ATM {inr(c.atmDailyLimit)} · POS {inr(c.posDailyLimit)} · Online {inr(c.onlineDailyLimit)}
                  </Typography>
                </Stack>
                <Divider sx={{ my: 1.5 }} />
                <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                  <Button size="small" onClick={() => setPinCard(c)}>Set PIN</Button>
                  <Button size="small" onClick={() => openLimits(c)}>Limits</Button>
                  {c.status === 'ACTIVE'
                    ? <Button size="small" color="error" onClick={() => doBlock(c)}>Block</Button>
                    : c.status === 'BLOCKED'
                      ? <Button size="small" color="success" onClick={() => act(() => unblockCard(c.id), 'Card unblocked')}>Unblock</Button>
                      : null}
                  <Tooltip title="Order a replacement card">
                    <Button size="small" onClick={() => act(() => replaceCard(c.id), 'Replacement issued')}>Replace</Button>
                  </Tooltip>
                </Stack>
              </CardContent>
            </Card>
          </Grid>
        ))}
        {cards.length === 0 && <Grid item xs={12}><Typography color="text.secondary">No cards yet.</Typography></Grid>}
      </Grid>

      <Dialog open={issueOpen} onClose={() => setIssueOpen(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Issue New Card</DialogTitle>
        <DialogContent dividers>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField select label="Account" value={issueForm.accountNumber}
              onChange={(e) => setIssueForm({ ...issueForm, accountNumber: e.target.value })} required fullWidth>
              {accounts.map((a) => <MenuItem key={a.id} value={a.accountNumber}>{a.accountType} · {a.accountNumber}</MenuItem>)}
            </TextField>
            <TextField select label="Network" value={issueForm.network}
              onChange={(e) => setIssueForm({ ...issueForm, network: e.target.value })} fullWidth>
              {NETWORKS.map((n) => <MenuItem key={n} value={n}>{n}</MenuItem>)}
            </TextField>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setIssueOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={doIssue} disabled={!issueForm.accountNumber}>Issue</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!limitCard} onClose={() => setLimitCard(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Daily Limits</DialogTitle>
        <DialogContent dividers>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="ATM Daily Limit" type="number" value={limits.atmDailyLimit}
              onChange={(e) => setLimits({ ...limits, atmDailyLimit: e.target.value })} fullWidth />
            <TextField label="POS Daily Limit" type="number" value={limits.posDailyLimit}
              onChange={(e) => setLimits({ ...limits, posDailyLimit: e.target.value })} fullWidth />
            <TextField label="Online Daily Limit" type="number" value={limits.onlineDailyLimit}
              onChange={(e) => setLimits({ ...limits, onlineDailyLimit: e.target.value })} fullWidth />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setLimitCard(null)}>Cancel</Button>
          <Button variant="contained" onClick={saveLimits}>Save</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!pinCard} onClose={() => setPinCard(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Set Card PIN</DialogTitle>
        <DialogContent dividers>
          <TextField label="New PIN" type="password" value={pin} onChange={(e) => setPinVal(e.target.value)}
            fullWidth inputProps={{ maxLength: 6 }} sx={{ mt: 1 }} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setPinCard(null)}>Cancel</Button>
          <Button variant="contained" onClick={savePin} disabled={pin.length < 4}>Save PIN</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
