export const StatusBadge = ({ value }) => <span className={`badge st-${value}`}>{value}</span>;
export const PriorityBadge = ({ value }) => <span className={`badge pr-${value}`}>{value}</span>;
export const fmt = (iso) => (iso ? new Date(iso).toLocaleString('fr-FR') : '—');
