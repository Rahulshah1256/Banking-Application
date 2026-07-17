import { useEffect, useState } from 'react';
import {
  Box, Card, CardContent, Typography, Stack, Button, TextField, MenuItem, Grid, Tabs, Tab,
  Divider, Chip, Avatar, IconButton, Table, TableBody, TableCell, TableHead, TableRow,
  Dialog, DialogTitle, DialogContent, DialogActions, CircularProgress,
} from '@mui/material';
import PhotoCameraIcon from '@mui/icons-material/PhotoCamera';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import UploadFileIcon from '@mui/icons-material/UploadFile';
import { useSnackbar } from 'notistack';
import PageHeader from '../components/PageHeader';
import { formatDay, titleCase } from '../components/format';
import { API_BASE } from '../api/client';
import {
  getProfile, updateProfile, uploadPhoto, listNominees, addNominee, updateNominee, deleteNominee,
  kycStatus, uploadKyc,
} from '../services/profile';
import { errorMessage } from '../api/client';

const GENDERS = ['MALE', 'FEMALE', 'OTHER'];
const KYC_TYPES = ['PAN', 'AADHAAR', 'PASSPORT', 'DRIVING_LICENSE', 'ADDRESS_PROOF'];

export default function Profile() {
  const [tab, setTab] = useState(0);
  return (
    <Box>
      <PageHeader title="Profile & KYC" subtitle="Manage personal details, nominees and KYC documents" />
      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3 }}>
        <Tab label="Profile" />
        <Tab label="Nominees" />
        <Tab label="KYC" />
      </Tabs>
      {tab === 0 && <ProfileTab />}
      {tab === 1 && <NomineesTab />}
      {tab === 2 && <KycTab />}
    </Box>
  );
}

function ProfileTab() {
  const { enqueueSnackbar } = useSnackbar();
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState(null);
  const [photoV, setPhotoV] = useState(Date.now());

  const load = () => getProfile().then((p) => {
    setProfile(p);
    setForm({
      address: p.address || '', dateOfBirth: p.dateOfBirth || '', gender: p.gender || '',
      occupation: p.occupation || '', annualIncome: p.annualIncome || '',
      communicationAddress: p.communicationAddress || '', permanentAddress: p.permanentAddress || '',
      alternatePhone: p.alternatePhone || '',
    });
  }).catch((e) => enqueueSnackbar(errorMessage(e), { variant: 'error' }));

  useEffect(() => { load(); }, []); // eslint-disable-line

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const save = async () => {
    try {
      const payload = { ...form, annualIncome: form.annualIncome === '' ? null : Number(form.annualIncome),
        dateOfBirth: form.dateOfBirth || null, gender: form.gender || null };
      await updateProfile(payload);
      enqueueSnackbar('Profile updated', { variant: 'success' });
      load();
    } catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
  };

  const onPhoto = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try { await uploadPhoto(file); enqueueSnackbar('Photo updated', { variant: 'success' }); setPhotoV(Date.now()); load(); }
    catch (err) { enqueueSnackbar(errorMessage(err), { variant: 'error' }); }
  };

  if (!profile || !form) return <CircularProgress />;

  return (
    <Grid container spacing={3}>
      <Grid item xs={12} md={4}>
        <Card>
          <CardContent sx={{ textAlign: 'center' }}>
            <Avatar
              src={profile.hasProfilePhoto ? `${API_BASE}/api/profile/photo?v=${photoV}` : undefined}
              sx={{ width: 96, height: 96, mx: 'auto', mb: 2, bgcolor: 'primary.main' }}
            >
              {(profile.name || '?').charAt(0)}
            </Avatar>
            <Typography variant="h6">{profile.name}</Typography>
            <Typography variant="body2" color="text.secondary">@{profile.username}</Typography>
            <Typography variant="body2" color="text.secondary">{profile.email}</Typography>
            <Stack direction="row" spacing={1} justifyContent="center" sx={{ mt: 1 }}>
              <Chip size="small" label={profile.emailVerified ? 'Email Verified' : 'Email Unverified'}
                color={profile.emailVerified ? 'success' : 'default'} />
              <Chip size="small" label={`KYC ${titleCase(profile.kycStatus)}`} />
            </Stack>
            <Button component="label" startIcon={<PhotoCameraIcon />} sx={{ mt: 2 }}>
              Change Photo
              <input hidden type="file" accept="image/*" onChange={onPhoto} />
            </Button>
            <Divider sx={{ my: 2 }} />
            <Stack spacing={0.5} sx={{ textAlign: 'left' }}>
              <Typography variant="body2">Mobile: {profile.mobile || '—'}</Typography>
              <Typography variant="body2">PAN: {profile.maskedPan || '—'}</Typography>
              <Typography variant="body2">Aadhaar: {profile.maskedAadhaar || '—'}</Typography>
            </Stack>
          </CardContent>
        </Card>
      </Grid>
      <Grid item xs={12} md={8}>
        <Card>
          <CardContent>
            <Typography variant="h6" sx={{ mb: 2 }}>Personal Details</Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField label="Date of Birth" type="date" InputLabelProps={{ shrink: true }}
                  value={form.dateOfBirth} onChange={set('dateOfBirth')} fullWidth />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField select label="Gender" value={form.gender} onChange={set('gender')} fullWidth>
                  <MenuItem value="">—</MenuItem>
                  {GENDERS.map((g) => <MenuItem key={g} value={g}>{titleCase(g)}</MenuItem>)}
                </TextField>
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField label="Occupation" value={form.occupation} onChange={set('occupation')} fullWidth />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField label="Annual Income" type="number" value={form.annualIncome} onChange={set('annualIncome')} fullWidth />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField label="Alternate Phone" value={form.alternatePhone} onChange={set('alternatePhone')} fullWidth />
              </Grid>
              <Grid item xs={12}>
                <TextField label="Address" value={form.address} onChange={set('address')} fullWidth multiline minRows={2} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField label="Communication Address" value={form.communicationAddress}
                  onChange={set('communicationAddress')} fullWidth multiline minRows={2} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField label="Permanent Address" value={form.permanentAddress}
                  onChange={set('permanentAddress')} fullWidth multiline minRows={2} />
              </Grid>
              <Grid item xs={12}>
                <Button variant="contained" onClick={save}>Save Changes</Button>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );
}

function NomineesTab() {
  const { enqueueSnackbar } = useSnackbar();
  const [items, setItems] = useState([]);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const empty = { name: '', relationship: '', sharePercentage: '', dateOfBirth: '', phone: '', address: '' };
  const [form, setForm] = useState(empty);

  const load = () => listNominees().then((d) => setItems(d || [])).catch((e) => enqueueSnackbar(errorMessage(e), { variant: 'error' }));
  useEffect(() => { load(); }, []); // eslint-disable-line

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });
  const openNew = () => { setEditing(null); setForm(empty); setOpen(true); };
  const openEdit = (n) => {
    setEditing(n);
    setForm({ name: n.name, relationship: n.relationship, sharePercentage: n.sharePercentage,
      dateOfBirth: n.dateOfBirth || '', phone: n.phone || '', address: n.address || '' });
    setOpen(true);
  };

  const save = async () => {
    try {
      const payload = { ...form, sharePercentage: Number(form.sharePercentage), dateOfBirth: form.dateOfBirth || null };
      if (editing) await updateNominee(editing.id, payload);
      else await addNominee(payload);
      enqueueSnackbar('Nominee saved', { variant: 'success' });
      setOpen(false); load();
    } catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
  };
  const remove = async (n) => {
    try { await deleteNominee(n.id); enqueueSnackbar('Nominee removed', { variant: 'success' }); load(); }
    catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
  };

  return (
    <Card>
      <CardContent>
        <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
          <Typography variant="h6">Nominees</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={openNew}>Add Nominee</Button>
        </Stack>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Relationship</TableCell>
              <TableCell align="right">Share %</TableCell>
              <TableCell>DOB</TableCell>
              <TableCell>Phone</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {items.map((n) => (
              <TableRow key={n.id}>
                <TableCell>{n.name}</TableCell>
                <TableCell>{n.relationship}</TableCell>
                <TableCell align="right">{n.sharePercentage}</TableCell>
                <TableCell>{n.dateOfBirth ? formatDay(n.dateOfBirth) : '—'}</TableCell>
                <TableCell>{n.phone || '—'}</TableCell>
                <TableCell align="right">
                  <Button size="small" onClick={() => openEdit(n)}>Edit</Button>
                  <IconButton size="small" color="error" onClick={() => remove(n)}><DeleteIcon fontSize="small" /></IconButton>
                </TableCell>
              </TableRow>
            ))}
            {items.length === 0 && <TableRow><TableCell colSpan={6} align="center">No nominees added</TableCell></TableRow>}
          </TableBody>
        </Table>
      </CardContent>

      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editing ? 'Edit Nominee' : 'Add Nominee'}</DialogTitle>
        <DialogContent dividers>
          <Grid container spacing={2} sx={{ mt: 0 }}>
            <Grid item xs={12} sm={6}><TextField label="Name" value={form.name} onChange={set('name')} required fullWidth /></Grid>
            <Grid item xs={12} sm={6}><TextField label="Relationship" value={form.relationship} onChange={set('relationship')} required fullWidth /></Grid>
            <Grid item xs={12} sm={6}><TextField label="Share %" type="number" value={form.sharePercentage} onChange={set('sharePercentage')} required fullWidth /></Grid>
            <Grid item xs={12} sm={6}><TextField label="Date of Birth" type="date" InputLabelProps={{ shrink: true }} value={form.dateOfBirth} onChange={set('dateOfBirth')} fullWidth /></Grid>
            <Grid item xs={12} sm={6}><TextField label="Phone" value={form.phone} onChange={set('phone')} fullWidth /></Grid>
            <Grid item xs={12}><TextField label="Address" value={form.address} onChange={set('address')} fullWidth multiline minRows={2} /></Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={save}>Save</Button>
        </DialogActions>
      </Dialog>
    </Card>
  );
}

function KycTab() {
  const { enqueueSnackbar } = useSnackbar();
  const [status, setStatus] = useState(null);
  const [form, setForm] = useState({ documentType: 'PAN', documentNumber: '', file: null });

  const load = () => kycStatus().then(setStatus).catch((e) => enqueueSnackbar(errorMessage(e), { variant: 'error' }));
  useEffect(() => { load(); }, []); // eslint-disable-line

  const upload = async () => {
    if (!form.file) { enqueueSnackbar('Select a file', { variant: 'warning' }); return; }
    try {
      await uploadKyc(form.documentType, form.documentNumber, form.file);
      enqueueSnackbar('Document uploaded', { variant: 'success' });
      setForm({ documentType: 'PAN', documentNumber: '', file: null });
      load();
    } catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
  };

  const color = (s) => ({ VERIFIED: 'success', PENDING: 'warning', REJECTED: 'error' }[s] || 'default');

  return (
    <Grid container spacing={3}>
      <Grid item xs={12} md={5}>
        <Card>
          <CardContent>
            <Typography variant="h6" sx={{ mb: 2 }}>Upload Document</Typography>
            <Stack spacing={2}>
              <TextField select label="Document Type" value={form.documentType}
                onChange={(e) => setForm({ ...form, documentType: e.target.value })} fullWidth>
                {KYC_TYPES.map((t) => <MenuItem key={t} value={t}>{titleCase(t)}</MenuItem>)}
              </TextField>
              <TextField label="Document Number" value={form.documentNumber}
                onChange={(e) => setForm({ ...form, documentNumber: e.target.value })} fullWidth />
              <Button component="label" variant="outlined" startIcon={<UploadFileIcon />}>
                {form.file ? form.file.name : 'Choose File'}
                <input hidden type="file" onChange={(e) => setForm({ ...form, file: e.target.files?.[0] || null })} />
              </Button>
              <Button variant="contained" onClick={upload}>Upload</Button>
            </Stack>
          </CardContent>
        </Card>
      </Grid>
      <Grid item xs={12} md={7}>
        <Card>
          <CardContent>
            {status && (
              <Stack direction="row" spacing={1} sx={{ mb: 2 }} flexWrap="wrap" useFlexGap>
                <Chip label={`Overall: ${titleCase(status.overallStatus)}`} color={color(status.overallStatus)} />
                <Chip variant="outlined" label={`Verified ${status.verifiedDocuments}`} />
                <Chip variant="outlined" label={`Pending ${status.pendingDocuments}`} />
                <Chip variant="outlined" label={`Rejected ${status.rejectedDocuments}`} />
              </Stack>
            )}
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Type</TableCell>
                  <TableCell>Number</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Remarks</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {(status?.documents || []).map((d) => (
                  <TableRow key={d.id}>
                    <TableCell>{titleCase(d.documentType)}</TableCell>
                    <TableCell>{d.maskedDocumentNumber}</TableCell>
                    <TableCell><Chip size="small" label={titleCase(d.status)} color={color(d.status)} /></TableCell>
                    <TableCell>{d.remarks || '—'}</TableCell>
                  </TableRow>
                ))}
                {(status?.documents || []).length === 0 && (
                  <TableRow><TableCell colSpan={4} align="center">No documents uploaded</TableCell></TableRow>
                )}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );
}
