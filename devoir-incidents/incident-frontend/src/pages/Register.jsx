import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { errorMessage } from '../services/api';

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ fullName: '', email: '', password: '' });
  const [error, setError] = useState('');

  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      // aucun champ "role" n'est envoye : le backend attribue EMPLOYEE
      await register(form.fullName, form.email, form.password);
      navigate('/');
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  return (
    <div className="auth-page">
      <form className="card auth-card" onSubmit={submit}>
        <h1>Créer un compte</h1>
        {error && <div className="alert alert-error">{error}</div>}
        <label>Nom complet
          <input name="fullName" value={form.fullName} onChange={change} required />
        </label>
        <label>Email
          <input name="email" type="email" value={form.email} onChange={change} required />
        </label>
        <label>Mot de passe (8 caractères minimum)
          <input name="password" type="password" minLength={8} value={form.password} onChange={change} required />
        </label>
        <button className="btn">S'inscrire</button>
        <p className="muted">Déjà inscrit ? <Link to="/login">Se connecter</Link></p>
      </form>
    </div>
  );
}
