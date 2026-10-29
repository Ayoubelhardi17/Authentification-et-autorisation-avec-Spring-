import api from './api';

export const createIncident = (incident) => api.post('/api/incidents', incident).then((r) => r.data);
export const getMyIncidents = () => api.get('/api/incidents/my').then((r) => r.data);
export const getAssignedIncidents = () => api.get('/api/incidents/assigned').then((r) => r.data);
export const getAllIncidents = () => api.get('/api/incidents').then((r) => r.data);
export const getIncident = (id) => api.get(`/api/incidents/${id}`).then((r) => r.data);
export const updateStatus = (id, status) =>
  api.put(`/api/incidents/${id}/status`, { status }).then((r) => r.data);
export const assignIncident = (id, technicianId) =>
  api.put(`/api/incidents/${id}/assign/${technicianId}`).then((r) => r.data);
export const addComment = (id, content) =>
  api.post(`/api/incidents/${id}/comments`, { content }).then((r) => r.data);
export const getDashboard = () => api.get('/api/dashboard').then((r) => r.data);

// ---- administration
export const getUsers = () => api.get('/api/admin/users').then((r) => r.data);
export const getTechnicians = () => api.get('/api/admin/technicians').then((r) => r.data);
export const createUser = (user) => api.post('/api/admin/users', user).then((r) => r.data);
export const setUserEnabled = (id, enabled) =>
  api.put(`/api/admin/users/${id}/${enabled ? 'enable' : 'disable'}`).then((r) => r.data);
