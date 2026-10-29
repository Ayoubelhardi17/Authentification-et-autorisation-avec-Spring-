import { createContext, useContext, useState } from 'react';
import * as authService from '../services/authService';

const AuthContext = createContext(null);

function readUser() {
  try {
    const token = localStorage.getItem('token');
    const user = JSON.parse(localStorage.getItem('user') || 'null');
    return token && user ? user : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readUser);

  const login = async (email, password) => {
    await authService.login(email, password);
    setUser(readUser());
  };

  const register = async (fullName, email, password) => {
    await authService.register(fullName, email, password);
    setUser(readUser());
  };

  const logout = async () => {
    await authService.logout();
    setUser(null);
  };

  /** A appeler apres une modification du profil pour rafraichir le nom dans la Navbar. */
  const refreshUser = () => setUser(readUser());

  return (
    <AuthContext.Provider value={{ user, login, register, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
