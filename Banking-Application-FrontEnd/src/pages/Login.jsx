import { useState } from 'react';
import { useNavigate, useLocation, Link as RouterLink } from 'react-router-dom';
import {
  Box, TextField, Button, Typography, Stack, Link, InputAdornment, IconButton,
} from '@mui/material';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import PersonRoundedIcon from '@mui/icons-material/PersonRounded';
import LockRoundedIcon from '@mui/icons-material/LockRounded';
import { useSnackbar } from 'notistack';
import { useAuth } from '../auth/AuthContext';
import { errorMessage } from '../api/client';
import AuthShell from './AuthShell';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const { enqueueSnackbar } = useSnackbar();
  const [form, setForm] = useState({ usernameoremail: '', password: '' });
  const [showPw, setShowPw] = useState(false);
  const [loading, setLoading] = useState(false);

  const from = location.state?.from?.pathname || '/dashboard';

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const user = await login(form.usernameoremail.trim(), form.password);
      enqueueSnackbar(`Welcome back, ${user.name || user.username}`, { variant: 'success' });
      navigate(from, { replace: true });
    } catch (err) {
      enqueueSnackbar(errorMessage(err, 'Login failed'), { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell>
      <Typography variant="overline" color="primary" sx={{ fontWeight: 800 }}>
        Welcome back
      </Typography>
      <Typography variant="h4" sx={{ mb: 1 }}>Sign in to your account</Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 4 }}>
        Enter your credentials to access your NetBanking dashboard.
      </Typography>

      <form onSubmit={submit}>
        <Stack spacing={2.5}>
          <TextField
            label="Username or Email"
            value={form.usernameoremail}
            onChange={(e) => setForm({ ...form, usernameoremail: e.target.value })}
            required
            autoFocus
            fullWidth
            InputProps={{
              startAdornment: (
                <InputAdornment position="start"><PersonRoundedIcon color="action" /></InputAdornment>
              ),
            }}
          />
          <TextField
            label="Password"
            type={showPw ? 'text' : 'password'}
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            required
            fullWidth
            InputProps={{
              startAdornment: (
                <InputAdornment position="start"><LockRoundedIcon color="action" /></InputAdornment>
              ),
              endAdornment: (
                <InputAdornment position="end">
                  <IconButton onClick={() => setShowPw((s) => !s)} edge="end">
                    {showPw ? <VisibilityOff /> : <Visibility />}
                  </IconButton>
                </InputAdornment>
              ),
            }}
          />
          <Button type="submit" variant="contained" size="large" disabled={loading}>
            {loading ? 'Signing in…' : 'Sign In'}
          </Button>
        </Stack>
      </form>

      <Typography variant="body2" align="center" sx={{ mt: 4 }}>
        New to Janta Bank?{' '}
        <Link component={RouterLink} to="/register" fontWeight={700} underline="hover">
          Create an account
        </Link>
      </Typography>

      <Box sx={{ mt: 3, p: 1.5, borderRadius: 2, bgcolor: 'action.hover', textAlign: 'center' }}>
        <Typography variant="caption" color="text.secondary">
          Demo · <b>user1 / user123</b> &nbsp;·&nbsp; <b>admin / admin123</b>
        </Typography>
      </Box>
    </AuthShell>
  );
}
