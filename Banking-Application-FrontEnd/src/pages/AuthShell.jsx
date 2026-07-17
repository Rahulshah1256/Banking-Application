import { Box, Stack, Typography } from '@mui/material';
import ShieldRoundedIcon from '@mui/icons-material/ShieldRounded';
import BoltRoundedIcon from '@mui/icons-material/BoltRounded';
import InsightsRoundedIcon from '@mui/icons-material/InsightsRounded';
import AccountBalanceWalletRoundedIcon from '@mui/icons-material/AccountBalanceWalletRounded';
import { gradients } from '../theme';

const features = [
  { icon: <ShieldRoundedIcon />, title: 'Bank-grade security', desc: 'JWT auth, device lockout & masked data.' },
  { icon: <BoltRoundedIcon />, title: 'Instant transfers', desc: 'IMPS, NEFT, RTGS & UPI in seconds.' },
  { icon: <InsightsRoundedIcon />, title: 'Smart insights', desc: 'Track portfolio, loans & deposits.' },
];

export default function AuthShell({ children }) {
  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'grid',
        gridTemplateColumns: { xs: '1fr', md: '1.05fr 1fr' },
      }}
    >
      {/* Hero panel */}
      <Box
        sx={{
          display: { xs: 'none', md: 'flex' },
          position: 'relative',
          overflow: 'hidden',
          background: gradients.hero,
          color: '#fff',
          flexDirection: 'column',
          justifyContent: 'space-between',
          p: 6,
        }}
      >
        <Blob sx={{ top: -120, right: -80, width: 360, height: 360 }} />
        <Blob sx={{ bottom: -140, left: -100, width: 400, height: 400, opacity: 0.18 }} />

        <Stack direction="row" spacing={1.5} alignItems="center" sx={{ position: 'relative' }}>
          <Box sx={{ width: 44, height: 44, borderRadius: 3, display: 'grid', placeItems: 'center', backgroundColor: 'rgba(255,255,255,0.16)' }}>
            <AccountBalanceWalletRoundedIcon />
          </Box>
          <Typography variant="h6" sx={{ fontWeight: 800 }}>Janta Bank</Typography>
        </Stack>

        <Box sx={{ position: 'relative' }}>
          <Typography variant="h3" sx={{ fontWeight: 800, mb: 2, maxWidth: 460 }}>
            Banking that moves at the speed of you.
          </Typography>
          <Typography sx={{ opacity: 0.85, maxWidth: 420, mb: 4 }}>
            Manage accounts, cards, loans, deposits and more — all from one beautifully simple, secure NetBanking dashboard.
          </Typography>
          <Stack spacing={2.5}>
            {features.map((f) => (
              <Stack key={f.title} direction="row" spacing={2} alignItems="center">
                <Box sx={{ width: 46, height: 46, borderRadius: 2.5, display: 'grid', placeItems: 'center', backgroundColor: 'rgba(255,255,255,0.14)', border: '1px solid rgba(255,255,255,0.18)' }}>
                  {f.icon}
                </Box>
                <Box>
                  <Typography sx={{ fontWeight: 700 }}>{f.title}</Typography>
                  <Typography variant="body2" sx={{ opacity: 0.8 }}>{f.desc}</Typography>
                </Box>
              </Stack>
            ))}
          </Stack>
        </Box>

        <Typography variant="caption" sx={{ opacity: 0.6, position: 'relative' }}>
          © {new Date().getFullYear()} Janta Bank. All rights reserved.
        </Typography>
      </Box>

      {/* Form panel */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          p: { xs: 3, sm: 6 },
        }}
      >
        <Box className="jb-fade-in" sx={{ width: '100%', maxWidth: 440 }}>
          {children}
        </Box>
      </Box>
    </Box>
  );
}

function Blob({ sx }) {
  return (
    <Box
      sx={{
        position: 'absolute',
        borderRadius: '50%',
        background: 'radial-gradient(circle at 30% 30%, rgba(255,255,255,0.45), rgba(255,255,255,0) 70%)',
        opacity: 0.25,
        pointerEvents: 'none',
        ...sx,
      }}
    />
  );
}
