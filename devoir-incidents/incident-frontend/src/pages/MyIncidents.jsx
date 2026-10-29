import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getMyIncidents } from '../services/incidentService';
import { errorMessage } from '../services/api';
import IncidentTable from '../components/IncidentTable';

export default function MyIncidents() {
  const [incidents, setIncidents] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    getMyIncidents().then(setIncidents).catch((e) => setError(errorMessage(e)));
  }, []);

  return (
    <>
      <div className="page-head">
        <h1>Mes incidents</h1>
        <Link className="btn" to="/incidents/new">+ Déclarer un incident</Link>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="card">
        {incidents ? <IncidentTable incidents={incidents} showAssignee /> : <p>Chargement…</p>}
      </div>
    </>
  );
}
