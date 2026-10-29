import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * Controle l'acces selon le role. Ex. <RoleRoute roles={['ADMIN']} />
 * Attention : amelioration d'experience utilisateur uniquement, la vraie securite est cote backend.
 */
export default function RoleRoute({ roles }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (!roles.includes(user.role)) {
    return (
      <div className="card error-card">
        <h2>403 – Accès interdit</h2>
        <p>Votre rôle ({user.role}) ne vous permet pas d'accéder à cette page.</p>
      </div>
    );
  }
  return <Outlet />;
}
