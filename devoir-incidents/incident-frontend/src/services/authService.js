import api, { saveSession, clearSession } from './api';

export async function login(email, password) {
  const { data } = await api.post('/auth/login', { email, password });
  saveSession(data);
  return data;
}

export async function register(fullName, email, password) {
  const { data } = await api.post('/auth/register', { fullName, email, password });
  saveSession(data);
  return data;
}

/** Deconnexion : revoque le refresh token cote serveur puis nettoie le navigateur. */
export async function logout() {
  const refreshToken = localStorage.getItem('refreshToken');
  try {
    if (refreshToken) await api.post('/auth/logout', { refreshToken });
  } catch (e) {
    /* meme si le serveur est injoignable, on deconnecte localement */
  }
  clearSession();
}

export const getProfile = () => api.get('/api/profile').then((r) => r.data);

export async function updateProfile(fullName, email) {
  const { data } = await api.put('/api/profile', { fullName, email });
  // un nouveau token est renvoye (le sujet du JWT est l'email)
  localStorage.setItem('token', data.token);
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  localStorage.setItem(
    'user',
    JSON.stringify({ ...user, fullName: data.profile.fullName, email: data.profile.email })
  );
  return data.profile;
}

export const changePassword = (oldPassword, newPassword, confirmPassword) =>
  api.put('/api/profile/password', { oldPassword, newPassword, confirmPassword }).then((r) => r.data);
