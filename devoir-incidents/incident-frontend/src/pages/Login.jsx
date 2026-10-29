import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { errorMessage } from '../services/api';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Message affiche apres une redirection due a un 401 (section 17)
  const [expired] = useState(() => {
    const flag = sessionStorage.getItem('sessionExpired');
    sessionStorage.removeItem('sessionExpired');
    return !!flag;
  });

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(email, password);
      navigate('/');
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <form className="card auth-card" onSubmit={submit}>
        <h1>Connexion</h1>
        {expired && <div className="alert alert-warning">Votre session a expiré. Veuillez vous reconnecter.</div>}
        {error && <div className="alert alert-error">{error}</div>}
        <label>Email
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoFocus />
        </label>
        <label>Mot de passe
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </label>
        <button className="btn" disabled={loading}>{loading ? 'Connexion…' : 'Se connecter'}</button>
        <p className="muted">Pas de compte ? <Link to="/register">Créer un compte</Link></p>
      </form>
    </div>
  );
}
