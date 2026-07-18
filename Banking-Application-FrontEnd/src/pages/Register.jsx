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
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

  const set = (k) => (e) => {
    setForm({ ...form, [k]: e.target.value });
    if (errors[k]) setErrors((prev) => ({ ...prev, [k]: undefined }));
  };

  const validate = (f) => {
    const next = {};
    if (!f.name.trim()) next.name = 'Full name is required';
    if (f.username.trim().length < 4 || f.username.trim().length > 50)
      next.username = 'Username must be 4–50 characters';
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(f.email))
      next.email = 'Enter a valid email address';
    if (f.password.length < 8) next.password = 'Password must be at least 8 characters';
    if (!/^[0-9]{12}$/.test(f.aadhaarno))
      next.aadhaarno = 'Aadhaar must be exactly 12 digits';
    if (!/^[A-Z]{5}[0-9]{4}[A-Z]$/.test(f.panno))
      next.panno = 'PAN must match the format ABCDE1234F';
    if (!/^[6-9][0-9]{9}$/.test(f.mobile))
      next.mobile = 'Enter a valid 10-digit mobile starting 6–9';
    if (!f.address.trim()) next.address = 'Address is required';
    return next;
  };

  const submit = async (e) => {
    e.preventDefault();
    const payload = { ...form, panno: form.panno.toUpperCase() };
    const validation = validate(payload);
    if (Object.keys(validation).length) {
      setErrors(validation);
      enqueueSnackbar('Please correct the highlighted fields.', { variant: 'warning' });
      return;
    }
    setLoading(true);
    try {
      await register(payload);
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
              error={!!errors.name} helperText={errors.name}
              inputProps={{ maxLength: 100 }} />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField label="Username" value={form.username} onChange={set('username')} required fullWidth
              error={!!errors.username} helperText={errors.username || '4–50 characters'}
              inputProps={{ minLength: 4, maxLength: 50 }} />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField label="Email" type="email" value={form.email} onChange={set('email')} required fullWidth
              error={!!errors.email} helperText={errors.email} />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField label="Mobile Number" value={form.mobile}
              onChange={(e) => set('mobile')({ target: { value: e.target.value.replace(/\D/g, '') } })}
              required fullWidth
              error={!!errors.mobile} helperText={errors.mobile || '10-digit Indian mobile'}
              inputProps={{ inputMode: 'numeric', maxLength: 10 }} />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField label="Aadhaar Number" value={form.aadhaarno}
              onChange={(e) => set('aadhaarno')({ target: { value: e.target.value.replace(/\D/g, '') } })}
              required fullWidth
              error={!!errors.aadhaarno} helperText={errors.aadhaarno || '12 digits'}
              inputProps={{ inputMode: 'numeric', maxLength: 12 }} />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField label="PAN" value={form.panno}
              onChange={(e) => {
                setForm({ ...form, panno: e.target.value.toUpperCase() });
                if (errors.panno) setErrors((prev) => ({ ...prev, panno: undefined }));
              }} required fullWidth
              error={!!errors.panno} helperText={errors.panno || 'e.g. ABCDE1234F'}
              inputProps={{ maxLength: 10, style: { textTransform: 'uppercase' } }} />
          </Grid>
          <Grid item xs={12}>
            <TextField label="Address" value={form.address} onChange={set('address')} required fullWidth multiline minRows={2}
              error={!!errors.address} helperText={errors.address} />
          </Grid>
          <Grid item xs={12}>
            <TextField label="Password" type="password" value={form.password} onChange={set('password')} required fullWidth
              error={!!errors.password} helperText={errors.password || 'At least 8 characters'}
              inputProps={{ minLength: 8 }} />
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
