import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { changePassword, getProfile, updateProfile } from '../services/authService';
import { errorMessage } from '../services/api';

export default function Profile() {
  const { refreshUser } = useAuth();
  const [form, setForm] = useState({ fullName: '', email: '' });
  const [role, setRole] = useState('');
  const [msg, setMsg] = useState(null); // {type, text}
  const [pwd, setPwd] = useState({ oldPassword: '', newPassword: '', confirmPassword: '' });
  const [pwdMsg, setPwdMsg] = useState(null);

  useEffect(() => {
    getProfile()
      .then((p) => {
        setForm({ fullName: p.fullName, email: p.email });
        setRole(p.role);
      })
      .catch((e) => setMsg({ type: 'error', text: errorMessage(e) }));
  }, []);

  const saveProfile = async (e) => {
    e.preventDefault();
    setMsg(null);
    try {
      await updateProfile(form.fullName, form.email);
      refreshUser();
      setMsg({ type: 'success', text: 'Profil mis à jour' });
    } catch (err) {
      setMsg({ type: 'error', text: errorMessage(err) });
    }
  };

  const savePassword = async (e) => {
    e.preventDefault();
    setPwdMsg(null);
    if (pwd.newPassword !== pwd.confirmPassword) {
      setPwdMsg({ type: 'error', text: 'La confirmation ne correspond pas au nouveau mot de passe' });
      return;
    }
    try {
      await changePassword(pwd.oldPassword, pwd.newPassword, pwd.confirmPassword);
      setPwd({ oldPassword: '', newPassword: '', confirmPassword: '' });
      setPwdMsg({ type: 'success', text: 'Mot de passe modifié' });
    } catch (err) {
      setPwdMsg({ type: 'error', text: errorMessage(err) });
    }
  };

  const Alert = ({ m }) => (m ? <div className={`alert alert-${m.type}`}>{m.text}</div> : null);

  return (
    <>
      <h1>Mon profil</h1>
      <div className="grid-2">
        <form className="card" onSubmit={saveProfile}>
          <h2>Informations</h2>
          <Alert m={msg} />
          <label>Nom complet
            <input value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} required />
          </label>
          <label>Email
            <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
          </label>
          <label>Rôle
            <input value={role} disabled />
          </label>
          <button className="btn">Enregistrer</button>
        </form>

        <form className="card" onSubmit={savePassword}>
          <h2>Changer le mot de passe</h2>
          <Alert m={pwdMsg} />
          <label>Ancien mot de passe
            <input type="password" value={pwd.oldPassword} onChange={(e) => setPwd({ ...pwd, oldPassword: e.target.value })} required />
          </label>
          <label>Nouveau mot de passe
            <input type="password" minLength={8} value={pwd.newPassword} onChange={(e) => setPwd({ ...pwd, newPassword: e.target.value })} required />
          </label>
          <label>Confirmation
            <input type="password" minLength={8} value={pwd.confirmPassword} onChange={(e) => setPwd({ ...pwd, confirmPassword: e.target.value })} required />
          </label>
          <button className="btn">Modifier</button>
        </form>
      </div>
    </>
  );
}
