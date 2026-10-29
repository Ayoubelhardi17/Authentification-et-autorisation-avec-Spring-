import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { getDashboard } from '../services/incidentService';
import { errorMessage } from '../services/api';
import IncidentTable from '../components/IncidentTable';

const LABELS = {
  EMPLOYEE: [
    ['total', 'Total de mes incidents'],
    ['open', 'OPEN'],
    ['inProgress', 'IN_PROGRESS'],
    ['resolvedOrClosed', 'RESOLVED / CLOSED'],
  ],
  TECHNICIAN: [
    ['assigned', 'Incidents affectés'],
    ['critical', 'Incidents critiques'],
    ['inProgress', 'En cours'],
    ['resolved', 'Résolus'],
  ],
  ADMIN: [
    ['totalUsers', 'Utilisateurs'],
    ['employees', 'Employés'],
    ['technicians', 'Techniciens'],
    ['totalIncidents', 'Incidents au total'],
    ['openIncidents', 'OPEN'],
    ['inProgressIncidents', 'IN_PROGRESS'],
    ['closedIncidents', 'CLOSED'],
  ],
};

export default function Dashboard() {
  const { user } = useAuth();
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    getDashboard().then(setData).catch((e) => setError(errorMessage(e)));
  }, []);

  if (error) return <div className="alert alert-error">{error}</div>;
  if (!data) return <p>Chargement…</p>;

  return (
    <>
      <h1>Bonjour {user.fullName}</h1>
      <p className="muted">Tableau de bord – rôle {user.role}</p>
      <div className="stats">
        {LABELS[user.role].map(([key, label]) => (
          <div className="card stat" key={key}>
            <div className="stat-value">{data.stats[key] ?? 0}</div>
            <div className="stat-label">{label}</div>
          </div>
        ))}
      </div>

      {user.role !== 'ADMIN' && (
        <div className="card">
          <h2>{user.role === 'EMPLOYEE' ? 'Mes derniers incidents' : 'Derniers incidents affectés'}</h2>
          <IncidentTable incidents={data.latestIncidents} />
        </div>
      )}
    </>
  );
}
