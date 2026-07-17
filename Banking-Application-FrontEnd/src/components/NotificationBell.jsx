import { useEffect, useState, useCallback } from 'react';
import {
  IconButton, Badge, Menu, Box, Typography, Divider, List, ListItem,
  ListItemText, Button, Tooltip,
} from '@mui/material';
import NotificationsIcon from '@mui/icons-material/Notifications';
import { useNavigate } from 'react-router-dom';
import { listNotifications, unreadCount, markAllRead } from '../services/notifications';
import { formatDate } from './format';

export default function NotificationBell() {
  const [anchor, setAnchor] = useState(null);
  const [count, setCount] = useState(0);
  const [items, setItems] = useState([]);
  const navigate = useNavigate();

  const refreshCount = useCallback(async () => {
    try {
      const data = await unreadCount();
      setCount(data?.unreadCount ?? 0);
    } catch {
      /* ignore polling errors */
    }
  }, []);

  useEffect(() => {
    refreshCount();
    const id = setInterval(refreshCount, 30000);
    return () => clearInterval(id);
  }, [refreshCount]);

  const open = async (e) => {
    setAnchor(e.currentTarget);
    try {
      const data = await listNotifications(false);
      setItems((data || []).slice(0, 6));
    } catch {
      setItems([]);
    }
  };

  const handleMarkAll = async () => {
    await markAllRead();
    await refreshCount();
    const data = await listNotifications(false);
    setItems((data || []).slice(0, 6));
  };

  return (
    <>
      <Tooltip title="Notifications">
        <IconButton color="inherit" onClick={open}>
          <Badge badgeContent={count} color="error">
            <NotificationsIcon />
          </Badge>
        </IconButton>
      </Tooltip>
      <Menu
        anchorEl={anchor}
        open={!!anchor}
        onClose={() => setAnchor(null)}
        slotProps={{ paper: { sx: { width: 360, maxWidth: '90vw' } } }}
      >
        <Box sx={{ px: 2, py: 1, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="subtitle1">Notifications</Typography>
          <Button size="small" onClick={handleMarkAll} disabled={count === 0}>
            Mark all read
          </Button>
        </Box>
        <Divider />
        <List dense sx={{ py: 0 }}>
          {items.length === 0 && (
            <ListItem>
              <ListItemText primary="No notifications" />
            </ListItem>
          )}
          {items.map((n) => (
            <ListItem key={n.id} sx={{ bgcolor: n.read ? 'transparent' : 'action.hover' }}>
              <ListItemText
                primary={n.title}
                secondary={`${n.message}  ·  ${formatDate(n.createdAt)}`}
                primaryTypographyProps={{ fontWeight: n.read ? 400 : 600, variant: 'body2' }}
                secondaryTypographyProps={{ variant: 'caption' }}
              />
            </ListItem>
          ))}
        </List>
        <Divider />
        <Box sx={{ p: 1, textAlign: 'center' }}>
          <Button
            size="small"
            onClick={() => {
              setAnchor(null);
              navigate('/notifications');
            }}
          >
            View all
          </Button>
        </Box>
      </Menu>
    </>
  );
}
