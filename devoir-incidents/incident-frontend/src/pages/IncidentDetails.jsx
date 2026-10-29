import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  addComment, assignIncident, getIncident, getTechnicians, updateStatus,
} from '../services/incidentService';
import { errorMessage } from '../services/api';
import { PriorityBadge, StatusBadge, fmt } from '../components/Badges';

const TECH_STATUSES = ['IN_PROGRESS', 'RESOLVED', 'CLOSED'];
const ADMIN_STATUSES = ['OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];

export default function IncidentDetails() {
  const { id } = useParams();
  const { user } = useAuth();
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [info, setInfo] = useState('');
  const [note, setNote] = useState('');
  const [technicians, setTechnicians] = useState([]);

  const load = useCallback(() => {
    setError('');
    getIncident(id).then(setData).catch((e) => setError(errorMessage(e)));
  }, [id]);

  useEffect(() => { load(); }, [load]);
  useEffect(() => {
    if (user.role === 'ADMIN') getTechnicians().then(setTechnicians).catch(() => {});
  }, [user.role]);

  const run = async (action) => {
    setError(''); setInfo('');
    try {
      await action();
      load();
    } catch (e) {
      setError(errorMessage(e));
    }
  };

  if (error && !data) {
    return (
      <div className="card error-card">
        <h2>Impossible d'afficher l'incident</h2>
        <p>{error}</p>
        <Link to="/">Retour</Link>
      </div>
    );
  }
  if (!data) return <p>Chargement…</p>;

  const { incident: i, comments, history } = data;
  const canChangeStatus = user.role === 'ADMIN' || user.role === 'TECHNICIAN';
  const statuses = user.role === 'ADMIN' ? ADMIN_STATUSES : TECH_STATUSES;

  return (
    <>
      <h1>Incident #{i.id} – {i.title}</h1>
      {error && <div className="alert alert-error">{error}</div>}
      {info && <div className="alert alert-success">{info}</div>}

      <div className="grid-2">
        <div className="card">
          <h2>Informations</h2>
          <dl className="details">
            <dt>Statut</dt><dd><StatusBadge value={i.status} /></dd>
            <dt>Priorité</dt><dd><PriorityBadge value={i.priority} /></dd>
            <dt>Catégorie</dt><dd>{i.category}</dd>
            <dt>Déclaré par</dt><dd>{i.createdBy}</dd>
            <dt>Technicien</dt><dd>{i.assignedTo || '—'}</dd>
            <dt>Créé le</dt><dd>{fmt(i.createdAt)}</dd>
            <dt>Mis à jour</dt><dd>{fmt(i.updatedAt)}</dd>
          </dl>
          <h3>Description</h3>
          <p className="pre">{i.description}</p>
        </div>

        <div className="card">
          <h2>Historique</h2>
          <ul className="timeline">
            {history.map((h) => (
              <li key={h.id}>
                <span className="muted">{fmt(h.createdAt)}</span> – {h.message}
                <span className="muted"> ({h.actor})</span>
              </li>
            ))}
          </ul>
        </div>
      </div>

      {user.role === 'ADMIN' && (
        <div className="card">
          <h2>Affectation</h2>
          <select
            defaultValue=""
            onChange={(e) => e.target.value && run(async () => {
              await assignIncident(i.id, e.target.value);
              setInfo('Incident affecté');
            })}
          >
            <option value="" disabled>Choisir un technicien…</option>
            {technicians.map((t) => <option key={t.id} value={t.id}>{t.fullName}</option>)}
          </select>
        </div>
      )}

      {canChangeStatus && (
        <div className="card">
          <h2>Traitement</h2>
          <label>Changer le statut
            <select
              value=""
              onChange={(e) => e.target.value && run(async () => {
                await updateStatus(i.id, e.target.value);
                setInfo('Statut mis à jour');
              })}
            >
              <option value="" disabled>Nouveau statut…</option>
              {statuses.filter((s) => s !== i.status).map((s) => <option key={s}>{s}</option>)}
            </select>
          </label>
        </div>
      )}

      <div className="card">
        <h2>Notes de traitement</h2>
        {comments.length === 0 && <p className="muted">Aucune note.</p>}
        <ul className="timeline">
          {comments.map((c) => (
            <li key={c.id}>
              <strong>{c.author}</strong> <span className="muted">{fmt(c.createdAt)}</span>
              <div className="pre">{c.content}</div>
            </li>
          ))}
        </ul>
        {canChangeStatus && (
          <form onSubmit={(e) => {
            e.preventDefault();
            run(async () => { await addComment(i.id, note); setNote(''); });
          }}>
            <textarea rows={3} placeholder="Ajouter une note de traitement…" value={note} onChange={(e) => setNote(e.target.value)} />
            <button className="btn">Ajouter la note</button>
          </form>
        )}
      </div>
    </>
  );
}
