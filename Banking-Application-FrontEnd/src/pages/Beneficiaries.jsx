import { useEffect, useState } from 'react';
import {
  Box, Grid, Card, CardContent, Typography, Stack, Button, IconButton, Chip, Divider,
  Dialog, DialogTitle, DialogContent, DialogActions, TextField, MenuItem, Tooltip, CircularProgress,
} from '@mui/material';
import StarIcon from '@mui/icons-material/Star';
import StarBorderIcon from '@mui/icons-material/StarBorder';
import DeleteIcon from '@mui/icons-material/Delete';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import AddIcon from '@mui/icons-material/Add';
import { useSnackbar } from 'notistack';
import PageHeader from '../components/PageHeader';
import { inr, formatDate, titleCase } from '../components/format';
import {
  listBeneficiaries, addBeneficiary, deleteBeneficiary, approveBeneficiary, toggleFavourite,
} from '../services/beneficiaries';
import { listAccounts } from '../services/accounts';
import { errorMessage } from '../api/client';

export default function Beneficiaries() {
  const { enqueueSnackbar } = useSnackbar();
  const [items, setItems] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({
    beneficiaryAccountNumber: '', beneficiaryAccountName: '', beneficiaryAccountIfsc: '',
    amountLimit: '', nickname: '', linkAccountNumber: '',
  });

  const load = () => {
    setLoading(true);
    listBeneficiaries()
      .then((d) => setItems(d || []))
      .catch((e) => enqueueSnackbar(errorMessage(e), { variant: 'error' }))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
    listAccounts().then((d) => setAccounts(d || [])).catch(() => {});
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const save = async () => {
    try {
      await addBeneficiary({ ...form, amountLimit: Number(form.amountLimit) || 0 });
      enqueueSnackbar('Beneficiary added', { variant: 'success' });
      setOpen(false);
      setForm({
        beneficiaryAccountNumber: '', beneficiaryAccountName: '', beneficiaryAccountIfsc: '',
        amountLimit: '', nickname: '', linkAccountNumber: '',
      });
      load();
    } catch (e) {
      enqueueSnackbar(errorMessage(e), { variant: 'error' });
    }
  };

  const act = async (fn, ok) => {
    try {
      await fn();
      enqueueSnackbar(ok, { variant: 'success' });
      load();
    } catch (e) {
      enqueueSnackbar(errorMessage(e), { variant: 'error' });
    }
  };

  if (loading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}><CircularProgress /></Box>;
  }

  return (
    <Box>
      <PageHeader
        title="Beneficiaries"
        subtitle="Manage payees for fund transfers"
        action={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setOpen(true)}>Add Beneficiary</Button>}
      />
      <Grid container spacing={3}>
        {items.map((b) => (
          <Grid item xs={12} md={6} lg={4} key={b.id}>
            <Card>
              <CardContent>
                <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
                  <Box>
                    <Typography variant="h6">{b.nickname || b.beneficiaryAccountName}</Typography>
                    <Typography variant="body2" color="text.secondary">{b.beneficiaryAccountName}</Typography>
                  </Box>
                  <Tooltip title={b.favourite ? 'Unfavourite' : 'Favourite'}>
                    <IconButton onClick={() => act(() => toggleFavourite(b.id, !b.favourite), 'Updated')}>
                      {b.favourite ? <StarIcon color="warning" /> : <StarBorderIcon />}
                    </IconButton>
                  </Tooltip>
                </Stack>
                <Divider sx={{ my: 1.5 }} />
                <Stack spacing={0.5}>
                  <Typography variant="body2">A/C: {b.maskedAccountNumber || b.beneficiaryAccountNumber}</Typography>
                  <Typography variant="body2">IFSC: {b.beneficiaryAccountIfsc}</Typography>
                  <Typography variant="body2">Limit: {b.amountLimit ? inr(b.amountLimit) : 'No limit'}</Typography>
                  <Stack direction="row" spacing={1} alignItems="center">
                    <Chip size="small" label={titleCase(b.status)}
                      color={b.payable ? 'success' : 'warning'} />
                    {!b.payable && b.activateAfter && (
                      <Typography variant="caption" color="text.secondary">
                        Active after {formatDate(b.activateAfter)}
                      </Typography>
                    )}
                  </Stack>
                </Stack>
                <Divider sx={{ my: 1.5 }} />
                <Stack direction="row" spacing={1}>
                  {!b.payable && (
                    <Button size="small" startIcon={<CheckCircleIcon />}
                      onClick={() => act(() => approveBeneficiary(b.id), 'Beneficiary approved')}>
                      Approve
                    </Button>
                  )}
                  <Button size="small" color="error" startIcon={<DeleteIcon />}
                    onClick={() => act(() => deleteBeneficiary(b.id), 'Beneficiary removed')}>
                    Delete
                  </Button>
                </Stack>
              </CardContent>
            </Card>
          </Grid>
        ))}
        {items.length === 0 && (
          <Grid item xs={12}><Typography color="text.secondary">No beneficiaries added yet.</Typography></Grid>
        )}
      </Grid>

      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Add Beneficiary</DialogTitle>
        <DialogContent dividers>
          <Grid container spacing={2} sx={{ mt: 0 }}>
            <Grid item xs={12} sm={6}>
              <TextField label="Account Number" value={form.beneficiaryAccountNumber}
                onChange={set('beneficiaryAccountNumber')} required fullWidth />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField label="Account Holder Name" value={form.beneficiaryAccountName}
                onChange={set('beneficiaryAccountName')} required fullWidth />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField label="IFSC Code" value={form.beneficiaryAccountIfsc}
                onChange={set('beneficiaryAccountIfsc')} required fullWidth />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField label="Nickname" value={form.nickname} onChange={set('nickname')} fullWidth />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField label="Amount Limit" type="number" value={form.amountLimit}
                onChange={set('amountLimit')} fullWidth helperText="0 = no limit" />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField select label="Link to My Account" value={form.linkAccountNumber}
                onChange={set('linkAccountNumber')} fullWidth>
                <MenuItem value="">None</MenuItem>
                {accounts.map((a) => (
                  <MenuItem key={a.id} value={a.accountNumber}>{a.accountType} · {a.accountNumber}</MenuItem>
                ))}
              </TextField>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={save}>Add</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
