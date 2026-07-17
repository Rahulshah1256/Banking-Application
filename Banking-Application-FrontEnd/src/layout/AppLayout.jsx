import { useState } from 'react';
import {
  AppBar, Toolbar, Typography, Drawer, Box, List, ListItemButton, ListItemIcon,
  ListItemText, IconButton, Avatar, Menu, MenuItem, Divider, useMediaQuery, Chip, Tooltip,
} from '@mui/material';
import { useTheme, alpha } from '@mui/material/styles';
import MenuIcon from '@mui/icons-material/Menu';
import DashboardIcon from '@mui/icons-material/SpaceDashboardRounded';
import AccountBalanceIcon from '@mui/icons-material/AccountBalanceRounded';
import SwapHorizIcon from '@mui/icons-material/SwapHorizRounded';
import PeopleIcon from '@mui/icons-material/GroupsRounded';
import CreditCardIcon from '@mui/icons-material/CreditCardRounded';
import RequestQuoteIcon from '@mui/icons-material/RequestQuoteRounded';
import SavingsIcon from '@mui/icons-material/SavingsRounded';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLongRounded';
import PersonIcon from '@mui/icons-material/PersonRounded';
import NotificationsIcon from '@mui/icons-material/NotificationsRounded';
import SupportAgentIcon from '@mui/icons-material/SupportAgentRounded';
import AssessmentIcon from '@mui/icons-material/InsightsRounded';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettingsRounded';
import LogoutIcon from '@mui/icons-material/LogoutRounded';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWalletRounded';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import NotificationBell from '../components/NotificationBell';
import { gradients } from '../theme';

const drawerWidth = 268;

const navSections = [
  {
    heading: 'Overview',
    items: [
      { to: '/dashboard', label: 'Dashboard', icon: <DashboardIcon /> },
      { to: '/accounts', label: 'Accounts', icon: <AccountBalanceIcon /> },
      { to: '/reports', label: 'Reports', icon: <AssessmentIcon /> },
    ],
  },
  {
    heading: 'Banking',
    items: [
      { to: '/transfer', label: 'Transfer', icon: <SwapHorizIcon /> },
      { to: '/beneficiaries', label: 'Beneficiaries', icon: <PeopleIcon /> },
      { to: '/cards', label: 'Cards', icon: <CreditCardIcon /> },
      { to: '/loans', label: 'Loans', icon: <RequestQuoteIcon /> },
      { to: '/deposits', label: 'Deposits', icon: <SavingsIcon /> },
      { to: '/cheques', label: 'Cheques', icon: <ReceiptLongIcon /> },
    ],
  },
  {
    heading: 'Account',
    items: [
      { to: '/notifications', label: 'Notifications', icon: <NotificationsIcon /> },
      { to: '/support', label: 'Support', icon: <SupportAgentIcon /> },
      { to: '/profile', label: 'Profile', icon: <PersonIcon /> },
    ],
  },
];

const adminSection = {
  heading: 'Administration',
  items: [{ to: '/admin', label: 'Admin Console', icon: <AdminPanelSettingsIcon /> }],
};

export default function AppLayout() {
  const theme = useTheme();
  const isDesktop = useMediaQuery(theme.breakpoints.up('md'));
  const [mobileOpen, setMobileOpen] = useState(false);
  const [anchor, setAnchor] = useState(null);
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const sections = isAdmin ? [...navSections, adminSection] : navSections;
  const initial = (user?.name || user?.username || '?').charAt(0).toUpperCase();

  const navButtonSx = {
    borderRadius: 2.5,
    mx: 1.5,
    my: 0.35,
    py: 1,
    color: 'rgba(255,255,255,0.66)',
    transition: 'all .2s ease',
    '& .MuiListItemIcon-root': { color: 'rgba(255,255,255,0.6)', minWidth: 38 },
    '&:hover': {
      backgroundColor: 'rgba(255,255,255,0.07)',
      color: '#fff',
      '& .MuiListItemIcon-root': { color: '#fff' },
    },
    '&.active': {
      background: 'linear-gradient(135deg, rgba(129,140,248,0.35), rgba(168,85,247,0.28))',
      color: '#fff',
      boxShadow: 'inset 0 0 0 1px rgba(255,255,255,0.12)',
      '& .MuiListItemIcon-root': { color: '#fff' },
    },
  };

  const drawer = (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%', background: gradients.sidebar, color: '#fff' }}>
      <Toolbar sx={{ gap: 1.5, py: 2.5 }}>
        <Box
          sx={{
            width: 40, height: 40, borderRadius: 2.5, display: 'grid', placeItems: 'center',
            background: gradients.brandSoft, boxShadow: '0 8px 20px -8px rgba(168,85,247,0.9)',
          }}
        >
          <AccountBalanceWalletIcon sx={{ color: '#fff' }} />
        </Box>
        <Box>
          <Typography variant="h6" sx={{ color: '#fff', lineHeight: 1 }}>Janta Bank</Typography>
          <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.5)', letterSpacing: '0.14em' }}>
            NETBANKING
          </Typography>
        </Box>
      </Toolbar>

      <Box sx={{ flex: 1, overflowY: 'auto', pb: 2 }}>
        {sections.map((section) => (
          <Box key={section.heading} sx={{ mt: 1.5 }}>
            <Typography
              variant="caption"
              sx={{ px: 3, color: 'rgba(255,255,255,0.4)', fontWeight: 700, letterSpacing: '0.12em', textTransform: 'uppercase' }}
            >
              {section.heading}
            </Typography>
            <List sx={{ py: 0.5 }}>
              {section.items.map((item) => (
                <ListItemButton
                  key={item.to}
                  component={NavLink}
                  to={item.to}
                  onClick={() => setMobileOpen(false)}
                  sx={navButtonSx}
                >
                  <ListItemIcon>{item.icon}</ListItemIcon>
                  <ListItemText primary={item.label} primaryTypographyProps={{ fontWeight: 600, fontSize: '0.9rem' }} />
                </ListItemButton>
              ))}
            </List>
          </Box>
        ))}
      </Box>

      <Box sx={{ p: 2 }}>
        <Box
          sx={{
            display: 'flex', alignItems: 'center', gap: 1.5, p: 1.5, borderRadius: 3,
            backgroundColor: 'rgba(255,255,255,0.08)', border: '1px solid rgba(255,255,255,0.1)',
          }}
        >
          <Avatar sx={{ bgcolor: 'transparent', background: gradients.brandSoft, width: 38, height: 38 }}>{initial}</Avatar>
          <Box sx={{ minWidth: 0, flex: 1 }}>
            <Typography variant="body2" sx={{ color: '#fff', fontWeight: 700 }} noWrap>
              {user?.name || user?.username}
            </Typography>
            <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.55)' }} noWrap>
              {isAdmin ? 'Administrator' : 'Customer'}
            </Typography>
          </Box>
          <Tooltip title="Logout">
            <IconButton size="small" onClick={handleLogout} sx={{ color: 'rgba(255,255,255,0.7)' }}>
              <LogoutIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <AppBar
        position="fixed"
        color="inherit"
        elevation={0}
        sx={{
          zIndex: (t) => t.zIndex.drawer + 1,
          borderBottom: '1px solid rgba(24,27,51,0.06)',
          backgroundColor: alpha('#ffffff', 0.72),
          backdropFilter: 'blur(14px)',
        }}
      >
        <Toolbar>
          {!isDesktop && (
            <>
              <IconButton edge="start" onClick={() => setMobileOpen(true)} sx={{ mr: 1 }}>
                <MenuIcon />
              </IconButton>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Box sx={{ width: 30, height: 30, borderRadius: 2, display: 'grid', placeItems: 'center', background: gradients.brand }}>
                  <AccountBalanceWalletIcon sx={{ color: '#fff', fontSize: 18 }} />
                </Box>
                <Typography variant="h6" sx={{ background: gradients.brand, WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
                  Janta Bank
                </Typography>
              </Box>
            </>
          )}
          <Box sx={{ flexGrow: 1 }} />
          <NotificationBell />
          <Box
            onClick={(e) => setAnchor(e.currentTarget)}
            sx={{
              display: 'flex', alignItems: 'center', gap: 1.25, ml: 1, pl: 0.5, pr: { xs: 0.5, sm: 1.5 }, py: 0.5,
              borderRadius: 99, cursor: 'pointer', transition: 'background .2s',
              '&:hover': { backgroundColor: alpha(theme.palette.primary.main, 0.08) },
            }}
          >
            <Avatar sx={{ background: gradients.brand, width: 36, height: 36 }}>{initial}</Avatar>
            {isDesktop && (
              <Box sx={{ lineHeight: 1 }}>
                <Typography variant="body2" sx={{ fontWeight: 700 }}>{user?.name || user?.username}</Typography>
                <Typography variant="caption" color="text.secondary">{isAdmin ? 'Administrator' : 'Customer'}</Typography>
              </Box>
            )}
          </Box>
          <Menu
            anchorEl={anchor}
            open={!!anchor}
            onClose={() => setAnchor(null)}
            transformOrigin={{ horizontal: 'right', vertical: 'top' }}
            anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
            slotProps={{ paper: { sx: { mt: 1, minWidth: 220, borderRadius: 3 } } }}
          >
            <Box sx={{ px: 2, py: 1.5 }}>
              <Typography variant="subtitle2">{user?.name || user?.username}</Typography>
              <Typography variant="caption" color="text.secondary">{user?.email}</Typography>
              <Box sx={{ mt: 1 }}>
                <Chip size="small" label={isAdmin ? 'Administrator' : 'Customer'}
                  color={isAdmin ? 'secondary' : 'primary'} variant="outlined" />
              </Box>
            </Box>
            <Divider />
            <MenuItem onClick={() => { setAnchor(null); navigate('/profile'); }}>
              <ListItemIcon><PersonIcon fontSize="small" /></ListItemIcon>
              Profile
            </MenuItem>
            <MenuItem onClick={handleLogout}>
              <ListItemIcon><LogoutIcon fontSize="small" /></ListItemIcon>
              Logout
            </MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>

      <Box component="nav" sx={{ width: { md: drawerWidth }, flexShrink: { md: 0 } }}>
        <Drawer
          variant={isDesktop ? 'permanent' : 'temporary'}
          open={isDesktop ? true : mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{
            '& .MuiDrawer-paper': { width: drawerWidth, boxSizing: 'border-box', border: 'none' },
          }}
        >
          {drawer}
        </Drawer>
      </Box>

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          width: { md: `calc(100% - ${drawerWidth}px)` },
          p: { xs: 2, md: 4 },
        }}
      >
        <Toolbar />
        <Box className="jb-fade-in">
          <Outlet />
        </Box>
      </Box>
    </Box>
  );
}
