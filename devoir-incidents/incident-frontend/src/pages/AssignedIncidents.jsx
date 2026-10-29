import { useEffect, useState } from 'react';
import { getAssignedIncidents } from '../services/incidentService';
import { errorMessage } from '../services/api';
import IncidentTable from '../components/IncidentTable';

export default function AssignedIncidents() {
  const [incidents, setIncidents] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    getAssignedIncidents().then(setIncidents).catch((e) => setError(errorMessage(e)));
  }, []);

  return (
    <>
      <h1>Incidents qui me sont affectés</h1>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="card">
        {incidents ? <IncidentTable incidents={incidents} showOwner /> : <p>Chargement…</p>}
      </div>
    </>
  );
}
