import client, { unwrap } from '../api/client';

export const listNotifications = (unreadOnly = false) =>
  client.get('/api/notifications', { params: { unreadOnly } }).then(unwrap);
export const unreadCount = () => client.get('/api/notifications/unread-count').then(unwrap);
export const markRead = (id) => client.patch(`/api/notifications/${id}/read`).then(unwrap);
export const markAllRead = () => client.patch('/api/notifications/read-all').then(unwrap);
export const deleteNotification = (id) => client.delete(`/api/notifications/${id}`).then(unwrap);
