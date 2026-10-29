import { useCallback, useEffect, useState } from 'react';
import { assignIncident, getAllIncidents, getTechnicians } from '../services/incidentService';
import { errorMessage } from '../services/api';
import IncidentTable from '../components/IncidentTable';

export default function AdminIncidents() {
  const [incidents, setIncidents] = useState(null);
  const [technicians, setTechnicians] = useState([]);
  const [error, setError] = useState('');

  const load = useCallback(() => {
    getAllIncidents().then(setIncidents).catch((e) => setError(errorMessage(e)));
  }, []);

  useEffect(() => {
    load();
    getTechnicians().then(setTechnicians).catch((e) => setError(errorMessage(e)));
  }, [load]);

  const assign = async (incidentId, technicianId) => {
    setError('');
    try {
      await assignIncident(incidentId, technicianId);
      load();
    } catch (e) {
      setError(errorMessage(e));
    }
  };

  return (
    <>
      <h1>Tous les incidents</h1>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="card">
        {incidents ? (
          <IncidentTable
            incidents={incidents}
            showOwner
            showAssignee
            renderActions={(i) => (
              <select value="" onChange={(e) => e.target.value && assign(i.id, e.target.value)}>
                <option value="" disabled>Affecter à…</option>
                {technicians.map((t) => <option key={t.id} value={t.id}>{t.fullName}</option>)}
              </select>
            )}
          />
        ) : <p>Chargement…</p>}
      </div>
    </>
  );
}
