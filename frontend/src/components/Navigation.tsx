import { Link, useLocation } from 'react-router-dom';
import { authService } from '../services/auth';
import { LogOut, Home, Package, Plus, Settings } from 'lucide-react';

function Navigation() {
  const location = useLocation();
  const user = authService.getUser();
  const isAdmin = authService.isAdmin();

  const handleLogout = () => {
    authService.logout();
    window.location.reload();
  };

  const navItems = [
    { path: '/', label: 'Dashboard', icon: Home },
    { path: '/items', label: 'Artikel', icon: Package },
    { path: '/items/new', label: 'Hinzufügen', icon: Plus },
  ];

  if (isAdmin) {
    navItems.push({ path: '/categories', label: 'Kategorien', icon: Settings });
  }

  return (
    <nav style={{
      background: 'rgba(255, 255, 255, 0.1)',
      padding: '1rem',
      borderBottom: '1px solid rgba(255, 255, 255, 0.2)',
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center'
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '2rem' }}>
        <h2 style={{ margin: 0, color: '#fff' }}>🧊 Gefrierschrank</h2>
        <div style={{ display: 'flex', gap: '1rem' }}>
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.path;
            return (
              <Link
                key={item.path}
                to={item.path}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  padding: '0.5rem 1rem',
                  borderRadius: '8px',
                  textDecoration: 'none',
                  color: isActive ? '#646cff' : 'rgba(255, 255, 255, 0.9)',
                  background: isActive ? 'rgba(100, 108, 255, 0.1)' : 'transparent',
                  border: isActive ? '1px solid #646cff' : '1px solid transparent',
                  transition: 'all 0.2s'
                }}
              >
                <Icon size={18} />
                {item.label}
              </Link>
            );
          })}
        </div>
      </div>
      
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <span style={{ color: 'rgba(255, 255, 255, 0.8)' }}>
          Hallo, {user?.username}
        </span>
        <button
          onClick={handleLogout}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            padding: '0.5rem 1rem',
            borderRadius: '8px',
            background: 'transparent',
            color: 'rgba(255, 255, 255, 0.9)',
            border: '1px solid rgba(255, 255, 255, 0.3)',
            cursor: 'pointer'
          }}
        >
          <LogOut size={18} />
          Abmelden
        </button>
      </div>
    </nav>
  );
}

export default Navigation;