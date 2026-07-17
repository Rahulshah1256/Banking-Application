import { useState } from 'react';
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import { TextField, Button, Typography, Link, Grid } from '@mui/material';
import { useSnackbar } from 'notistack';
import { register } from '../services/auth';
import { errorMessage } from '../api/client';
import AuthShell from './AuthShell';

export default function Register() {
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();
  const [form, setForm] = useState({
    name: '', username: '', email: '', password: '',
    aadhaarno: '', panno: '', mobile: '', address: '',
  });
  const [loading, setLoading] = useState(false);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await register({ ...form, panno: form.panno.toUpperCase() });
      enqueueSnackbar('Registration successful. Please sign in.', { variant: 'success' });
      navigate('/login');
    } catch (err) {
      enqueueSnackbar(errorMessage(err, 'Registration failed'), { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell>
      <Typography variant="overline" color="primary" sx={{ fontWeight: 800 }}>
        Get started
      </Typography>
      <Typography variant="h4" sx={{ mb: 1 }}>Create your account</Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 4 }}>
        Join Janta Bank NetBanking in just a minute.
      </Typography>

      <form onSubmit={submit}>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <TextField label="Full Name" value={form.name} onChange={set('name')} required fullWidth
              inputProps={{ maxLength: 100 }} />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField label="Username" value={form.username} onChange={set('username')} required fullWidth
              helperText="4–50 characters" inputProps={{ minLength: 4, maxLength: 50 }} />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField label="Email" type="email" value={form.email} onChange={set('email')} required fullWidth />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField label="Mobile Number" value={form.mobile} onChange={set('mobile')} required fullWidth
              helperText="10-digit Indian mobile" inputProps={{ inputMode: 'numeric', maxLength: 10 }} />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField label="Aadhaar Number" value={form.aadhaarno} onChange={set('aadhaarno')} required fullWidth
              helperText="12 digits" inputProps={{ inputMode: 'numeric', maxLength: 12 }} />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField label="PAN" value={form.panno}
              onChange={(e) => setForm({ ...form, panno: e.target.value.toUpperCase() })} required fullWidth
              helperText="e.g. ABCDE1234F" inputProps={{ maxLength: 10, style: { textTransform: 'uppercase' } }} />
          </Grid>
          <Grid item xs={12}>
            <TextField label="Address" value={form.address} onChange={set('address')} required fullWidth multiline minRows={2} />
          </Grid>
          <Grid item xs={12}>
            <TextField label="Password" type="password" value={form.password} onChange={set('password')} required fullWidth
              helperText="At least 8 characters" inputProps={{ minLength: 8 }} />
          </Grid>
          <Grid item xs={12}>
            <Button type="submit" variant="contained" size="large" fullWidth disabled={loading}>
              {loading ? 'Creating…' : 'Create Account'}
            </Button>
          </Grid>
        </Grid>
      </form>

      <Typography variant="body2" align="center" sx={{ mt: 3 }}>
        Already have an account?{' '}
        <Link component={RouterLink} to="/login" fontWeight={700} underline="hover">
          Sign in
        </Link>
      </Typography>
    </AuthShell>
  );
}
