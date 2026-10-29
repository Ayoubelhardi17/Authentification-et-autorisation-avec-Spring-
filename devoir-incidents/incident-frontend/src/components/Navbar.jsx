import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const LINKS = {
  EMPLOYEE: [
    ['/incidents/my', 'Mes incidents'],
    ['/incidents/new', 'Nouvel incident'],
  ],
  TECHNICIAN: [['/incidents/assigned', 'Incidents affectés']],
  ADMIN: [
    ['/admin/incidents', 'Incidents'],
    ['/admin/users', 'Utilisateurs'],
  ],
};

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <span className="brand">🛡️ Incidents IT</span>
      <div className="links">
        <NavLink to="/" end>Dashboard</NavLink>
        {(LINKS[user.role] || []).map(([to, label]) => (
          <NavLink key={to} to={to}>{label}</NavLink>
        ))}
        <NavLink to="/profile">Profil</NavLink>
      </div>
      <div className="user">
        <span>{user.fullName} <span className={`badge role-${user.role}`}>{user.role}</span></span>
        <button className="btn btn-light" onClick={handleLogout}>Déconnexion</button>
      </div>
    </nav>
  );
}
