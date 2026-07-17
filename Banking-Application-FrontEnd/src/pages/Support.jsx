import { useEffect, useState } from 'react';
import {
  Box, Grid, Card, CardContent, Typography, Stack, Button, Chip, Divider, MenuItem, TextField,
  Dialog, DialogTitle, DialogContent, DialogActions, Tabs, Tab, List, ListItem, ListItemText,
  CircularProgress, Accordion, AccordionSummary, AccordionDetails, Paper,
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import AddIcon from '@mui/icons-material/Add';
import SendIcon from '@mui/icons-material/Send';
import { useSnackbar } from 'notistack';
import PageHeader from '../components/PageHeader';
import { formatDate, titleCase } from '../components/format';
import {
  listTickets, raiseTicket, getTicket, addMessage, updateStatus, listFaqs, listBranches, listAtms,
} from '../services/support';
import { errorMessage } from '../api/client';

const CATEGORIES = ['ACCOUNT', 'CARD', 'LOAN', 'DEPOSIT', 'TRANSACTION', 'KYC', 'TECHNICAL', 'GENERAL'];
const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];
const STATUSES = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];

export default function Support() {
  const [tab, setTab] = useState(0);
  return (
    <Box>
      <PageHeader title="Support" subtitle="Raise tickets and find help" />
      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3 }}>
        <Tab label="My Tickets" />
        <Tab label="FAQs" />
        <Tab label="Branches & ATMs" />
      </Tabs>
      {tab === 0 && <TicketsTab />}
      {tab === 1 && <FaqTab />}
      {tab === 2 && <LocatorTab />}
    </Box>
  );
}

function statusColor(s) {
  return { OPEN: 'warning', IN_PROGRESS: 'info', RESOLVED: 'success', CLOSED: 'default' }[s] || 'default';
}

function TicketsTab() {
  const { enqueueSnackbar } = useSnackbar();
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({ category: 'GENERAL', subject: '', description: '', priority: 'MEDIUM' });
  const [active, setActive] = useState(null);
  const [msg, setMsg] = useState('');

  const load = () => {
    setLoading(true);
    listTickets().then((d) => setTickets(d || [])).catch((e) => enqueueSnackbar(errorMessage(e), { variant: 'error' })).finally(() => setLoading(false));
  };
  useEffect(() => { load(); }, []); // eslint-disable-line

  const create = async () => {
    try {
      await raiseTicket(form);
      enqueueSnackbar('Ticket raised', { variant: 'success' });
      setOpen(false);
      setForm({ category: 'GENERAL', subject: '', description: '', priority: 'MEDIUM' });
      load();
    } catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
  };

  const openTicket = async (t) => {
    setActive({ ...t, messages: null });
    try { setActive(await getTicket(t.id)); }
    catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); setActive(null); }
  };

  const send = async () => {
    if (!msg.trim()) return;
    try {
      await addMessage(active.id, { message: msg });
      setMsg('');
      setActive(await getTicket(active.id));
      load();
    } catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
  };

  const changeStatus = async (status) => {
    try {
      await updateStatus(active.id, { status });
      enqueueSnackbar('Status updated', { variant: 'success' });
      setActive(await getTicket(active.id));
      load();
    } catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
  };

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}><CircularProgress /></Box>;

  return (
    <Box>
      <Stack direction="row" justifyContent="flex-end" sx={{ mb: 2 }}>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setOpen(true)}>Raise Ticket</Button>
      </Stack>
      <Grid container spacing={3}>
        {tickets.map((t) => (
          <Grid item xs={12} md={6} key={t.id}>
            <Card>
              <CardContent>
                <Stack direction="row" justifyContent="space-between">
                  <Box>
                    <Typography variant="overline" color="text.secondary">{t.ticketReference}</Typography>
                    <Typography variant="h6">{t.subject}</Typography>
                  </Box>
                  <Chip size="small" label={titleCase(t.status)} color={statusColor(t.status)} />
                </Stack>
                <Stack direction="row" spacing={1} sx={{ my: 1 }}>
                  <Chip size="small" variant="outlined" label={titleCase(t.category)} />
                  <Chip size="small" variant="outlined" label={titleCase(t.priority)} />
                </Stack>
                <Typography variant="body2" color="text.secondary" noWrap>{t.description}</Typography>
                <Typography variant="caption" color="text.secondary">Updated {formatDate(t.updatedAt)}</Typography>
                <Divider sx={{ my: 1.5 }} />
                <Button size="small" onClick={() => openTicket(t)}>Open Conversation</Button>
              </CardContent>
            </Card>
          </Grid>
        ))}
        {tickets.length === 0 && <Grid item xs={12}><Typography color="text.secondary">No tickets yet.</Typography></Grid>}
      </Grid>

      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Raise a Ticket</DialogTitle>
        <DialogContent dividers>
          <Grid container spacing={2} sx={{ mt: 0 }}>
            <Grid item xs={12} sm={6}>
              <TextField select label="Category" value={form.category}
                onChange={(e) => setForm({ ...form, category: e.target.value })} fullWidth>
                {CATEGORIES.map((c) => <MenuItem key={c} value={c}>{titleCase(c)}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField select label="Priority" value={form.priority}
                onChange={(e) => setForm({ ...form, priority: e.target.value })} fullWidth>
                {PRIORITIES.map((p) => <MenuItem key={p} value={p}>{titleCase(p)}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12}>
              <TextField label="Subject" value={form.subject}
                onChange={(e) => setForm({ ...form, subject: e.target.value })} required fullWidth />
            </Grid>
            <Grid item xs={12}>
              <TextField label="Description" value={form.description}
                onChange={(e) => setForm({ ...form, description: e.target.value })} required fullWidth multiline minRows={3} />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={create} disabled={!form.subject || !form.description}>Submit</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!active} onClose={() => setActive(null)} maxWidth="sm" fullWidth>
        <DialogTitle>
          {active?.subject}
          <Typography variant="caption" display="block" color="text.secondary">{active?.ticketReference}</Typography>
        </DialogTitle>
        <DialogContent dividers>
          <Stack direction="row" spacing={1} sx={{ mb: 2 }} alignItems="center" flexWrap="wrap" useFlexGap>
            <Chip size="small" label={titleCase(active?.status)} color={statusColor(active?.status)} />
            <TextField select size="small" label="Set Status" value=""
              onChange={(e) => changeStatus(e.target.value)} sx={{ minWidth: 160 }}>
              {STATUSES.map((s) => <MenuItem key={s} value={s}>{titleCase(s)}</MenuItem>)}
            </TextField>
          </Stack>
          {!active?.messages ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 2 }}><CircularProgress size={24} /></Box>
          ) : (
            <Stack spacing={1} sx={{ maxHeight: 320, overflowY: 'auto', mb: 2 }}>
              <Paper variant="outlined" sx={{ p: 1.5 }}>
                <Typography variant="caption" color="text.secondary">Original request</Typography>
                <Typography variant="body2">{active.description}</Typography>
              </Paper>
              {active.messages.map((m) => (
                <Paper key={m.id} variant="outlined"
                  sx={{ p: 1.5, alignSelf: m.senderType === 'CUSTOMER' ? 'flex-end' : 'flex-start',
                    bgcolor: m.senderType === 'CUSTOMER' ? 'primary.main' : 'grey.100',
                    color: m.senderType === 'CUSTOMER' ? 'common.white' : 'text.primary', maxWidth: '85%' }}>
                  <Typography variant="caption" sx={{ opacity: 0.8 }}>{titleCase(m.senderType)} · {formatDate(m.createdAt)}</Typography>
                  <Typography variant="body2">{m.message}</Typography>
                </Paper>
              ))}
              {active.messages.length === 0 && <Typography variant="body2" color="text.secondary">No replies yet.</Typography>}
            </Stack>
          )}
          <Stack direction="row" spacing={1}>
            <TextField size="small" fullWidth placeholder="Type a message…" value={msg}
              onChange={(e) => setMsg(e.target.value)}
              onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); send(); } }} />
            <Button variant="contained" endIcon={<SendIcon />} onClick={send}>Send</Button>
          </Stack>
        </DialogContent>
        <DialogActions><Button onClick={() => setActive(null)}>Close</Button></DialogActions>
      </Dialog>
    </Box>
  );
}

function FaqTab() {
  const { enqueueSnackbar } = useSnackbar();
  const [faqs, setFaqs] = useState([]);
  useEffect(() => {
    listFaqs().then((d) => setFaqs(d || [])).catch((e) => enqueueSnackbar(errorMessage(e), { variant: 'error' }));
  }, []); // eslint-disable-line
  return (
    <Box>
      {faqs.map((f, i) => (
        <Accordion key={i}>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Stack direction="row" spacing={1} alignItems="center">
              <Chip size="small" variant="outlined" label={titleCase(f.category)} />
              <Typography>{f.question}</Typography>
            </Stack>
          </AccordionSummary>
          <AccordionDetails><Typography variant="body2" color="text.secondary">{f.answer}</Typography></AccordionDetails>
        </Accordion>
      ))}
      {faqs.length === 0 && <Typography color="text.secondary">No FAQs available.</Typography>}
    </Box>
  );
}

function LocatorTab() {
  const { enqueueSnackbar } = useSnackbar();
  const [city, setCity] = useState('');
  const [branches, setBranches] = useState([]);
  const [atms, setAtms] = useState([]);

  const load = () => {
    listBranches(city || undefined).then((d) => setBranches(d || [])).catch((e) => enqueueSnackbar(errorMessage(e), { variant: 'error' }));
    listAtms(city || undefined).then((d) => setAtms(d || [])).catch(() => {});
  };
  useEffect(() => { load(); }, []); // eslint-disable-line

  return (
    <Box>
      <Stack direction="row" spacing={2} sx={{ mb: 2 }}>
        <TextField size="small" label="Filter by city" value={city} onChange={(e) => setCity(e.target.value)} />
        <Button variant="outlined" onClick={load}>Search</Button>
      </Stack>
      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Typography variant="h6" sx={{ mb: 1 }}>Branches</Typography>
          <List>
            {branches.map((b, i) => (
              <ListItem key={i} divider>
                <ListItemText primary={`${b.name} · ${b.ifsc || ''}`}
                  secondary={`${b.address}, ${b.city} ${b.pincode || ''} · ${b.phone || ''}`} />
              </ListItem>
            ))}
            {branches.length === 0 && <Typography color="text.secondary">No branches found.</Typography>}
          </List>
        </Grid>
        <Grid item xs={12} md={6}>
          <Typography variant="h6" sx={{ mb: 1 }}>ATMs</Typography>
          <List>
            {atms.map((a, i) => (
              <ListItem key={i} divider>
                <ListItemText primary={a.name}
                  secondary={`${a.address}, ${a.city} ${a.pincode || ''}`} />
              </ListItem>
            ))}
            {atms.length === 0 && <Typography color="text.secondary">No ATMs found.</Typography>}
          </List>
        </Grid>
      </Grid>
    </Box>
  );
}
