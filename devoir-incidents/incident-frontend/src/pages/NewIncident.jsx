import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createIncident } from '../services/incidentService';
import { errorMessage } from '../services/api';

const CATEGORIES = ['NETWORK', 'MALWARE', 'PHISHING', 'ACCESS', 'SOFTWARE', 'HARDWARE', 'DATA_LEAK', 'OTHER'];
const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

export default function NewIncident() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ title: '', description: '', category: 'OTHER', priority: 'MEDIUM' });
  const [error, setError] = useState('');

  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const created = await createIncident(form);
      navigate(`/incidents/${created.id}`);
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  return (
    <>
      <h1>Déclarer un incident</h1>
      <form className="card form-narrow" onSubmit={submit}>
        {error && <div className="alert alert-error">{error}</div>}
        <label>Titre
          <input name="title" value={form.title} onChange={change} maxLength={150} />
        </label>
        <label>Description
          <textarea name="description" rows={6} value={form.description} onChange={change} />
        </label>
        <div className="grid-2">
          <label>Catégorie
            <select name="category" value={form.category} onChange={change}>
              {CATEGORIES.map((c) => <option key={c}>{c}</option>)}
            </select>
          </label>
          <label>Priorité
            <select name="priority" value={form.priority} onChange={change}>
              {PRIORITIES.map((p) => <option key={p}>{p}</option>)}
            </select>
          </label>
        </div>
        <button className="btn">Envoyer</button>
      </form>
    </>
  );
}
