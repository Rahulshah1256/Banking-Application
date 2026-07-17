import { Box, Typography, Stack } from '@mui/material';

export default function PageHeader({ title, subtitle, action }) {
  return (
    <Box
      className="jb-fade-in"
      sx={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: 2,
        mb: 3.5,
      }}
    >
      <Stack direction="row" spacing={1.75} alignItems="center">
        <Box
          sx={{
            width: 6,
            height: 42,
            borderRadius: 3,
            background: (t) => t.custom.gradients.brand,
          }}
        />
        <Box>
          <Typography variant="h4">{title}</Typography>
          {subtitle && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.25 }}>
              {subtitle}
            </Typography>
          )}
        </Box>
      </Stack>
      {action}
    </Box>
  );
}
