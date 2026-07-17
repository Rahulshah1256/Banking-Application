import { useEffect, useState } from 'react';
import {
  Grid, Card, CardContent, Typography, Box, Stack, Chip, Divider, List, ListItem,
  ListItemText, CircularProgress, Button,
} from '@mui/material';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWalletRounded';
import CreditCardIcon from '@mui/icons-material/CreditCardRounded';
import RequestQuoteIcon from '@mui/icons-material/RequestQuoteRounded';
import SavingsIcon from '@mui/icons-material/SavingsRounded';
import SwapHorizRoundedIcon from '@mui/icons-material/SwapHorizRounded';
import ReceiptLongRoundedIcon from '@mui/icons-material/ReceiptLongRounded';
import GroupsRoundedIcon from '@mui/icons-material/GroupsRounded';
import { useNavigate } from 'react-router-dom';
import { useSnackbar } from 'notistack';
import StatCard from '../components/StatCard';
import { inr, formatDate, titleCase } from '../components/format';
import { getDashboard } from '../services/dashboard';
import { errorMessage } from '../api/client';
import { gradients } from '../theme';

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();

  useEffect(() => {
    getDashboard()
      .then(setData)
      .catch((e) => enqueueSnackbar(errorMessage(e, 'Failed to load dashboard'), { variant: 'error' }))
      .finally(() => setLoading(false));
  }, [enqueueSnackbar]);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}>
        <CircularProgress />
      </Box>
    );
  }
  if (!data) return null;

  return (
    <Box>
      <Box
        sx={{
          position: 'relative',
          overflow: 'hidden',
          borderRadius: 4,
          p: { xs: 3, md: 4 },
          mb: 3,
          color: '#fff',
          background: gradients.hero,
          boxShadow: '0 20px 45px -25px rgba(79,70,229,0.9)',
        }}
      >
        <Box sx={{ position: 'absolute', top: -90, right: -60, width: 260, height: 260, borderRadius: '50%', background: 'radial-gradient(circle at 30% 30%, rgba(255,255,255,0.4), transparent 70%)', opacity: 0.5 }} />
        <Box sx={{ position: 'absolute', bottom: -120, left: '30%', width: 300, height: 300, borderRadius: '50%', background: 'radial-gradient(circle, rgba(255,255,255,0.18), transparent 70%)' }} />
        <Stack
          direction={{ xs: 'column', md: 'row' }}
          justifyContent="space-between"
          alignItems={{ xs: 'flex-start', md: 'center' }}
          spacing={3}
          sx={{ position: 'relative' }}
        >
          <Box>
            <Typography variant="body2" sx={{ opacity: 0.85 }}>
              {data.lastLoginTime ? `Last login · ${formatDate(data.lastLoginTime)}` : 'Welcome back'}
            </Typography>
            <Typography variant="h4" sx={{ my: 0.5 }}>
              Hello, {data.customerName || 'Customer'} 👋
            </Typography>
            <Typography variant="overline" sx={{ opacity: 0.8 }}>Total Available Balance</Typography>
            <Typography variant="h3" sx={{ fontWeight: 800, letterSpacing: '-0.02em' }}>
              {inr(data.totalBalance)}
            </Typography>
          </Box>
          <Stack spacing={1.25} sx={{ width: { xs: '100%', md: 'auto' } }}>
            <HeroAction icon={<SwapHorizRoundedIcon />} label="New Transfer" onClick={() => navigate('/transfer')} primary />
            <Stack direction="row" spacing={1.25}>
              <HeroAction icon={<GroupsRoundedIcon />} label="Payees" onClick={() => navigate('/beneficiaries')} />
              <HeroAction icon={<ReceiptLongRoundedIcon />} label="Reports" onClick={() => navigate('/reports')} />
            </Stack>
          </Stack>
        </Stack>
      </Box>

      <Grid container spacing={3}>
        <Grid item xs={12} sm={6} md={4}>
          <StatCard
            label="Cards"
            value={data.cards?.count ?? 0}
            helper={data.cards?.detail}
            icon={<CreditCardIcon />}
            color="secondary.main"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4}>
          <StatCard
            label="Loans"
            value={data.loans?.count ?? 0}
            helper={data.loans?.detail}
            icon={<RequestQuoteIcon />}
            color="warning.main"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4}>
          <StatCard
            label="Deposits"
            value={data.deposits?.count ?? 0}
            helper={data.deposits?.detail}
            icon={<SavingsIcon />}
            color="success.main"
          />
        </Grid>

        <Grid item xs={12} md={7}>
          <Card>
            <CardContent>
              <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 1 }}>
                <Typography variant="h6">My Accounts</Typography>
                <Button size="small" onClick={() => navigate('/accounts')}>View all</Button>
              </Stack>
              <Divider />
              <List>
                {(data.accounts || []).map((a) => (
                  <ListItem key={a.id} disableGutters
                    secondaryAction={<Typography variant="subtitle1">{inr(a.balance)}</Typography>}>
                    <ListItemText
                      primary={`${a.accountType} · ${a.maskedAccountNumber}`}
                      secondary={`${a.ifscCode || ''} · Available ${inr(a.availableBalance)}`}
                    />
                  </ListItem>
                ))}
                {(data.accounts || []).length === 0 && (
                  <ListItem><ListItemText primary="No accounts found" /></ListItem>
                )}
              </List>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={5}>
          <Card>
            <CardContent>
              <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 1 }}>
                <Typography variant="h6">Recent Transactions</Typography>
                <Button size="small" onClick={() => navigate('/reports')}>Reports</Button>
              </Stack>
              <Divider />
              <List>
                {(data.recentTransactions || []).map((t) => (
                  <ListItem key={t.id} disableGutters
                    secondaryAction={
                      <Typography variant="subtitle2" color={t.direction === 'CREDIT' ? 'success.main' : 'error.main'}>
                        {t.direction === 'CREDIT' ? '+' : '-'}{inr(t.amount)}
                      </Typography>
                    }>
                    <ListItemText
                      primary={t.counterpartyAccount || titleCase(t.direction)}
                      secondary={formatDate(t.transactionDate)}
                    />
                  </ListItem>
                ))}
                {(data.recentTransactions || []).length === 0 && (
                  <ListItem><ListItemText primary="No recent transactions" /></ListItem>
                )}
              </List>
            </CardContent>
          </Card>
        </Grid>

        {data.pendingBeneficiaryRequests > 0 && (
          <Grid item xs={12}>
            <Chip
              color="warning"
              label={`${data.pendingBeneficiaryRequests} beneficiary request(s) pending activation`}
              onClick={() => navigate('/beneficiaries')}
            />
          </Grid>
        )}
      </Grid>
    </Box>
  );
}

function HeroAction({ icon, label, onClick, primary }) {
  return (
    <Button
      onClick={onClick}
      startIcon={icon}
      fullWidth
      sx={{
        justifyContent: 'flex-start',
        px: 2,
        py: 1.1,
        borderRadius: 2.5,
        color: '#fff',
        fontWeight: 700,
        backdropFilter: 'blur(6px)',
        border: '1px solid rgba(255,255,255,0.25)',
        backgroundColor: primary ? 'rgba(255,255,255,0.22)' : 'rgba(255,255,255,0.1)',
        '&:hover': { backgroundColor: 'rgba(255,255,255,0.3)' },
      }}
    >
      {label}
    </Button>
  );
}

