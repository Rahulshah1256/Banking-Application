import { createTheme, alpha } from '@mui/material/styles';

const indigo = '#6366f1';
const violet = '#8b5cf6';
const teal = '#14b8a6';

export const gradients = {
  brand: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 55%, #a855f7 100%)',
  brandSoft: 'linear-gradient(135deg, #818cf8 0%, #c084fc 100%)',
  teal: 'linear-gradient(135deg, #14b8a6 0%, #0ea5e9 100%)',
  amber: 'linear-gradient(135deg, #f59e0b 0%, #f97316 100%)',
  rose: 'linear-gradient(135deg, #f43f5e 0%, #ec4899 100%)',
  emerald: 'linear-gradient(135deg, #10b981 0%, #22c55e 100%)',
  sidebar: 'linear-gradient(185deg, #1b1637 0%, #241a52 45%, #2e1f6b 100%)',
  hero: 'linear-gradient(120deg, #4f46e5 0%, #7c3aed 45%, #9333ea 100%)',
};

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: indigo, dark: '#4f46e5', light: '#818cf8', contrastText: '#ffffff' },
    secondary: { main: teal, dark: '#0f766e', light: '#5eead4', contrastText: '#ffffff' },
    background: { default: '#eef0f8', paper: '#ffffff' },
    text: { primary: '#191b2e', secondary: '#6b7194' },
    success: { main: '#10b981' },
    warning: { main: '#f59e0b' },
    error: { main: '#ef4444' },
    info: { main: '#0ea5e9' },
    divider: 'rgba(24, 27, 51, 0.08)',
  },
  shape: { borderRadius: 16 },
  typography: {
    fontFamily: "'Plus Jakarta Sans', 'Inter', system-ui, sans-serif",
    h3: { fontWeight: 800, letterSpacing: '-0.02em' },
    h4: { fontWeight: 800, letterSpacing: '-0.02em' },
    h5: { fontWeight: 700, letterSpacing: '-0.01em' },
    h6: { fontWeight: 700, letterSpacing: '-0.01em' },
    subtitle1: { fontWeight: 600 },
    subtitle2: { fontWeight: 600 },
    overline: { fontWeight: 700, letterSpacing: '0.12em' },
    button: { textTransform: 'none', fontWeight: 700 },
  },
  custom: { gradients },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: { backgroundColor: 'transparent' },
      },
    },
    MuiPaper: {
      styleOverrides: {
        rounded: { borderRadius: 18 },
      },
    },
    MuiCard: {
      defaultProps: { elevation: 0 },
      styleOverrides: {
        root: {
          borderRadius: 20,
          border: '1px solid rgba(24, 27, 51, 0.06)',
          backgroundImage: 'none',
          boxShadow: '0 1px 2px rgba(16, 24, 60, 0.04), 0 12px 32px -18px rgba(49, 46, 129, 0.35)',
          transition: 'transform 0.25s cubic-bezier(.4,0,.2,1), box-shadow 0.25s cubic-bezier(.4,0,.2,1)',
        },
      },
    },
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: {
        root: { borderRadius: 12, paddingInline: 18 },
        sizeLarge: { paddingBlock: 11, fontSize: '1rem' },
        containedPrimary: {
          background: gradients.brand,
          boxShadow: '0 8px 20px -8px rgba(99, 102, 241, 0.7)',
          '&:hover': { background: gradients.brand, filter: 'brightness(1.06)' },
        },
        containedSecondary: {
          background: gradients.teal,
          boxShadow: '0 8px 20px -8px rgba(20, 184, 166, 0.7)',
          '&:hover': { background: gradients.teal, filter: 'brightness(1.06)' },
        },
        outlined: { borderColor: 'rgba(99,102,241,0.35)' },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: { fontWeight: 600, borderRadius: 8 },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        head: {
          fontWeight: 700,
          color: '#6b7194',
          textTransform: 'uppercase',
          fontSize: '0.7rem',
          letterSpacing: '0.06em',
          borderBottom: '1px solid rgba(24,27,51,0.08)',
        },
        root: { borderBottom: '1px solid rgba(24,27,51,0.05)' },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        hover: { '&:hover': { backgroundColor: alpha(indigo, 0.04) } },
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          backgroundColor: '#fff',
          '& .MuiOutlinedInput-notchedOutline': { borderColor: 'rgba(24,27,51,0.12)' },
        },
      },
    },
    MuiToggleButton: {
      styleOverrides: {
        root: {
          borderRadius: 10,
          textTransform: 'none',
          fontWeight: 600,
          '&.Mui-selected': {
            background: gradients.brand,
            color: '#fff',
            '&:hover': { background: gradients.brand, filter: 'brightness(1.05)' },
          },
        },
      },
    },
    MuiTab: {
      styleOverrides: {
        root: { textTransform: 'none', fontWeight: 700, fontSize: '0.95rem' },
      },
    },
    MuiTabs: {
      styleOverrides: {
        indicator: { height: 3, borderRadius: 3 },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: { borderRadius: 22 },
      },
    },
    MuiAvatar: {
      styleOverrides: {
        root: { fontWeight: 700 },
      },
    },
    MuiTooltip: {
      styleOverrides: {
        tooltip: { borderRadius: 8, fontSize: '0.75rem', backgroundColor: '#241a52' },
      },
    },
  },
});

export default theme;
