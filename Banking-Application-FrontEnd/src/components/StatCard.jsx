import { Card, CardContent, Box, Typography, Stack } from '@mui/material';
import { gradients } from '../theme';

const gradientFor = (color) => {
  const map = {
    'primary.main': gradients.brand,
    'secondary.main': gradients.teal,
    'warning.main': gradients.amber,
    'success.main': gradients.emerald,
    'error.main': gradients.rose,
    'info.main': gradients.teal,
  };
  return map[color] || gradients.brand;
};

export default function StatCard({ label, value, icon, color = 'primary.main', helper }) {
  const gradient = gradientFor(color);
  return (
    <Card
      sx={{
        height: '100%',
        position: 'relative',
        overflow: 'hidden',
        '&:hover': {
          transform: 'translateY(-4px)',
          boxShadow: '0 18px 40px -20px rgba(49, 46, 129, 0.5)',
        },
      }}
    >
      <Box
        sx={{
          position: 'absolute',
          top: -34,
          right: -34,
          width: 120,
          height: 120,
          borderRadius: '50%',
          background: gradient,
          opacity: 0.12,
        }}
      />
      <CardContent sx={{ position: 'relative' }}>
        <Stack direction="row" alignItems="center" spacing={2}>
          {icon && (
            <Box
              sx={{
                width: 52,
                height: 52,
                borderRadius: 3,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#fff',
                background: gradient,
                boxShadow: '0 10px 22px -10px rgba(99,102,241,0.8)',
                flexShrink: 0,
              }}
            >
              {icon}
            </Box>
          )}
          <Box sx={{ minWidth: 0 }}>
            <Typography
              variant="overline"
              color="text.secondary"
              sx={{ display: 'block', lineHeight: 1.4 }}
              noWrap
            >
              {label}
            </Typography>
            <Typography variant="h5" sx={{ fontWeight: 800 }} noWrap>
              {value}
            </Typography>
            {helper && (
              <Typography variant="caption" color="text.secondary" noWrap sx={{ display: 'block' }}>
                {helper}
              </Typography>
            )}
          </Box>
        </Stack>
      </CardContent>
    </Card>
  );
}
