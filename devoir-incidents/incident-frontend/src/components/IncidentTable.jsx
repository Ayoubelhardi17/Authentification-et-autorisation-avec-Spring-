import { Link } from 'react-router-dom';
import { StatusBadge, PriorityBadge, fmt } from './Badges';

/** Tableau reutilise par MyIncidents, AssignedIncidents et AdminIncidents. */
export default function IncidentTable({ incidents, showOwner = false, showAssignee = false, renderActions }) {
  if (!incidents.length) return <p className="muted">Aucun incident.</p>;
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>#</th><th>Titre</th><th>Catégorie</th><th>Priorité</th><th>Statut</th>
            {showOwner && <th>Déclaré par</th>}
            {showAssignee && <th>Technicien</th>}
            <th>Créé le</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {incidents.map((i) => (
            <tr key={i.id}>
              <td>{i.id}</td>
              <td>{i.title}</td>
              <td>{i.category}</td>
              <td><PriorityBadge value={i.priority} /></td>
              <td><StatusBadge value={i.status} /></td>
              {showOwner && <td>{i.createdBy}</td>}
              {showAssignee && <td>{i.assignedTo || <span className="muted">—</span>}</td>}
              <td>{fmt(i.createdAt)}</td>
              <td className="actions">
                <Link className="btn btn-small" to={`/incidents/${i.id}`}>Détails</Link>
                {renderActions && renderActions(i)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
