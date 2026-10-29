import axios from 'axios';

export const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
const REFRESH_ENABLED = import.meta.env.VITE_ENABLE_REFRESH !== 'false';

const api = axios.create({ baseURL: API_URL });

// --- Gestion du stockage de session -------------------------------------
export function saveSession(data) {
  localStorage.setItem('token', data.token);
  if (data.refreshToken) localStorage.setItem('refreshToken', data.refreshToken);
  localStorage.setItem(
    'user',
    JSON.stringify({ role: data.role, fullName: data.fullName, email: data.email })
  );
}

export function clearSession() {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');
}

// --- Interceptor de requete : ajoute "Authorization: Bearer <JWT>" ----------
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// --- Interceptor de reponse : gestion du 401 (token expire / invalide) ------
let refreshPromise = null;

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { config, response } = error;
    const isAuthCall = config?.url?.startsWith('/auth/');

    if (response?.status === 401 && !isAuthCall) {
      // 1) tentative de renouvellement silencieux avec le refresh token (bonus section 23)
      const refreshToken = localStorage.getItem('refreshToken');
      if (REFRESH_ENABLED && refreshToken && !config._retry) {
        config._retry = true;
        try {
          // une seule requete /auth/refresh meme si plusieurs requetes echouent en meme temps
          refreshPromise =
            refreshPromise ||
            axios
              .post(`${API_URL}/auth/refresh`, { refreshToken })
              .finally(() => {
                refreshPromise = null;
              });
          const { data } = await refreshPromise;
          saveSession(data);
          config.headers.Authorization = `Bearer ${data.token}`;
          return api(config);
        } catch (e) {
          // refresh impossible : on tombe dans la deconnexion ci-dessous
        }
      }
      // 2) section 17 : supprimer le JWT, rediriger vers Login, afficher "session expiree"
      clearSession();
      sessionStorage.setItem('sessionExpired', '1');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

/** Extrait un message lisible d'une erreur axios. */
export function errorMessage(error) {
  const data = error?.response?.data;
  if (data?.errors) return Object.values(data.errors).join(' · ');
  if (data?.message) return data.message;
  if (error?.code === 'ERR_NETWORK') return 'Serveur injoignable';
  return 'Une erreur est survenue';
}

export default api;
