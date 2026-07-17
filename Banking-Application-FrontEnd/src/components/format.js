export const inr = (value) => {
  if (value === null || value === undefined || isNaN(value)) return '—';
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(value);
};

export const formatDate = (value) => {
  if (!value) return '—';
  // Handle Jackson array [y,m,d,h,mi,s,ns] as well as ISO strings / epoch.
  let date;
  if (Array.isArray(value)) {
    const [y, mo, d, h = 0, mi = 0, s = 0] = value;
    date = new Date(y, (mo || 1) - 1, d || 1, h, mi, s);
  } else {
    date = new Date(value);
  }
  if (isNaN(date.getTime())) return String(value);
  return date.toLocaleString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

export const formatDay = (value) => {
  if (!value) return '—';
  let date;
  if (Array.isArray(value)) {
    const [y, mo, d] = value;
    date = new Date(y, (mo || 1) - 1, d || 1);
  } else {
    date = new Date(value);
  }
  if (isNaN(date.getTime())) return String(value);
  return date.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
};

export const titleCase = (value) =>
  !value ? '' : String(value).replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase());
