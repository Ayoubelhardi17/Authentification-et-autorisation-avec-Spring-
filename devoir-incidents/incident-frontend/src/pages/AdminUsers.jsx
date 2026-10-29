import { useCallback, useEffect, useState } from 'react';
import { createUser, getUsers, setUserEnabled } from '../services/incidentService';
import { errorMessage } from '../services/api';
import { fmt } from '../components/Badges';
import { useAuth } from '../context/AuthContext';

const EMPTY = { fullName: '', email: '', password: '', role: 'TECHNICIAN' };

export default function AdminUsers() {
  const { user: me } = useAuth();
  const [users, setUsers] = useState([]);
  const [error, setError] = useState('');
  const [form, setForm] = useState(EMPTY);

  const load = useCallback(() => {
    getUsers().then(setUsers).catch((e) => setError(errorMessage(e)));
  }, []);
  useEffect(() => { load(); }, [load]);

  const toggle = async (u) => {
    setError('');
    try {
      await setUserEnabled(u.id, !u.enabled);
      load();
    } catch (e) {
      setError(errorMessage(e));
    }
  };

  const create = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await createUser(form);
      setForm(EMPTY);
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  return (
    <>
      <h1>Gestion des utilisateurs</h1>
      {error && <div className="alert alert-error">{error}</div>}

      <div className="card">
        <div className="table-wrap">
          <table>
            <thead>
              <tr><th>#</th><th>Nom</th><th>Email</th><th>Rôle</th><th>Créé le</th><th>Actif</th><th></th></tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>{u.id}</td>
                  <td>{u.fullName}</td>
                  <td>{u.email}</td>
                  <td><span className={`badge role-${u.role}`}>{u.role}</span></td>
                  <td>{fmt(u.createdAt)}</td>
                  <td>{u.enabled ? '✅' : '⛔'}</td>
                  <td>
                    <button className="btn btn-small" disabled={u.email === me.email} onClick={() => toggle(u)}>
                      {u.enabled ? 'Désactiver' : 'Activer'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <form className="card form-narrow" onSubmit={create}>
        <h2>Créer un compte (technicien / admin)</h2>
        <label>Nom complet<input name="fullName" value={form.fullName} onChange={change} required /></label>
        <label>Email<input name="email" type="email" value={form.email} onChange={change} required /></label>
        <label>Mot de passe<input name="password" type="password" minLength={8} value={form.password} onChange={change} required /></label>
        <label>Rôle
          <select name="role" value={form.role} onChange={change}>
            <option>EMPLOYEE</option><option>TECHNICIAN</option><option>ADMIN</option>
          </select>
        </label>
        <button className="btn">Créer</button>
      </form>
    </>
  );
}
