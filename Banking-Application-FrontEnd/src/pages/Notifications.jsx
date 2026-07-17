import { useEffect, useState } from 'react';
import {
  Box, Card, CardContent, Typography, Stack, Button, Chip, List, ListItem, ListItemText,
  IconButton, Divider, CircularProgress, ToggleButton, ToggleButtonGroup,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import DoneAllIcon from '@mui/icons-material/DoneAll';
import { useSnackbar } from 'notistack';
import PageHeader from '../components/PageHeader';
import { formatDate, titleCase } from '../components/format';
import {
  listNotifications, markRead, markAllRead, deleteNotification,
} from '../services/notifications';
import { errorMessage } from '../api/client';

export default function Notifications() {
  const { enqueueSnackbar } = useSnackbar();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');

  const load = () => {
    setLoading(true);
    listNotifications(filter === 'unread')
      .then((d) => setItems(d || []))
      .catch((e) => enqueueSnackbar(errorMessage(e), { variant: 'error' }))
      .finally(() => setLoading(false));
  };
  useEffect(() => { load(); }, [filter]); // eslint-disable-line

  const act = async (fn, ok) => {
    try { await fn(); if (ok) enqueueSnackbar(ok, { variant: 'success' }); load(); }
    catch (e) { enqueueSnackbar(errorMessage(e), { variant: 'error' }); }
  };

  return (
    <Box>
      <PageHeader
        title="Notifications"
        subtitle="Alerts and account activity"
        action={<Button startIcon={<DoneAllIcon />} onClick={() => act(() => markAllRead(), 'All marked read')}>Mark all read</Button>}
      />
      <ToggleButtonGroup exclusive size="small" value={filter} onChange={(_, v) => v && setFilter(v)} sx={{ mb: 2 }}>
        <ToggleButton value="all">All</ToggleButton>
        <ToggleButton value="unread">Unread</ToggleButton>
      </ToggleButtonGroup>
      <Card>
        <CardContent>
          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}><CircularProgress size={28} /></Box>
          ) : (
            <List>
              {items.map((n, i) => (
                <Box key={n.id}>
                  {i > 0 && <Divider />}
                  <ListItem
                    sx={{ bgcolor: n.read ? 'transparent' : 'action.hover' }}
                    secondaryAction={
                      <Stack direction="row" spacing={0.5}>
                        {!n.read && <Button size="small" onClick={() => act(() => markRead(n.id))}>Read</Button>}
                        <IconButton edge="end" size="small" color="error" onClick={() => act(() => deleteNotification(n.id), 'Deleted')}>
                          <DeleteIcon fontSize="small" />
                        </IconButton>
                      </Stack>
                    }
                  >
                    <ListItemText
                      primary={
                        <Stack direction="row" spacing={1} alignItems="center">
                          <Typography variant="subtitle2" fontWeight={n.read ? 400 : 700}>{n.title}</Typography>
                          <Chip size="small" variant="outlined" label={titleCase(n.type)} />
                        </Stack>
                      }
                      secondary={`${n.message}  ·  ${formatDate(n.createdAt)}`}
                    />
                  </ListItem>
                </Box>
              ))}
              {items.length === 0 && <ListItem><ListItemText primary="No notifications" /></ListItem>}
            </List>
          )}
        </CardContent>
      </Card>
    </Box>
  );
}
