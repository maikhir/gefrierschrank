import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { itemApi } from '../services/api';
import { Package, AlertTriangle, Clock, Plus } from 'lucide-react';

function Dashboard() {
  const { data: stats, isLoading: statsLoading } = useQuery({
    queryKey: ['item-stats'],
    queryFn: itemApi.getStatistics,
  });

  const { data: expiredItems, isLoading: expiredLoading } = useQuery({
    queryKey: ['expired-items'],
    queryFn: itemApi.getExpired,
  });

  const { data: expiringItems, isLoading: expiringLoading } = useQuery({
    queryKey: ['expiring-items'],
    queryFn: () => itemApi.getExpiring(7),
  });

  if (statsLoading || expiredLoading || expiringLoading) {
    return (
      <div style={{ textAlign: 'center', padding: '2rem' }}>
        <div style={{ color: 'rgba(255, 255, 255, 0.8)' }}>Lade Dashboard...</div>
      </div>
    );
  }

  return (
    <div>
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '2rem'
      }}>
        <h1 style={{ color: '#fff', margin: 0 }}>Dashboard</h1>
        <Link 
          to="/items/new"
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            padding: '0.75rem 1.5rem',
            background: '#646cff',
            color: 'white',
            textDecoration: 'none',
            borderRadius: '8px',
            fontSize: '1rem'
          }}
        >
          <Plus size={20} />
          Neuer Artikel
        </Link>
      </div>

      {/* Statistics Cards */}
      <div className="stats">
        <div className="stat-item">
          <Package size={24} style={{ color: '#646cff', marginBottom: '0.5rem' }} />
          <span className="stat-value">{stats?.totalItems || 0}</span>
          <span className="stat-label">Artikel insgesamt</span>
        </div>
        
        <div className="stat-item">
          <Clock size={24} style={{ color: '#ff9500', marginBottom: '0.5rem' }} />
          <span className="stat-value">{stats?.expiringSoon || 0}</span>
          <span className="stat-label">Läuft bald ab</span>
        </div>
        
        <div className="stat-item">
          <AlertTriangle size={24} style={{ color: '#ff4444', marginBottom: '0.5rem' }} />
          <span className="stat-value">{stats?.expired || 0}</span>
          <span className="stat-label">Abgelaufen</span>
        </div>
      </div>

      <div className="dashboard">
        {/* Expired Items */}
        {expiredItems && expiredItems.length > 0 && (
          <div className="dashboard-card">
            <h3 style={{ color: '#ff4444', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <AlertTriangle size={20} />
              Abgelaufene Artikel ({expiredItems.length})
            </h3>
            <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
              {expiredItems.slice(0, 5).map((item) => (
                <div key={item.id} style={{
                  padding: '0.75rem',
                  margin: '0.5rem 0',
                  background: 'rgba(255, 68, 68, 0.1)',
                  border: '1px solid #ff4444',
                  borderRadius: '8px'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start' }}>
                    <div>
                      <strong>{item.name}</strong>
                      <div style={{ fontSize: '0.9rem', color: 'rgba(255, 255, 255, 0.7)' }}>
                        {item.categoryName} • {item.quantity} {item.unit}
                      </div>
                    </div>
                    <div style={{ 
                      background: '#ff4444', 
                      color: 'white', 
                      padding: '0.25rem 0.5rem', 
                      borderRadius: '4px', 
                      fontSize: '0.8rem' 
                    }}>
                      Abgelaufen
                    </div>
                  </div>
                </div>
              ))}
              {expiredItems.length > 5 && (
                <Link to="/items?filter=expired" style={{ 
                  color: '#646cff', 
                  textDecoration: 'none',
                  display: 'block',
                  textAlign: 'center',
                  marginTop: '1rem'
                }}>
                  Alle {expiredItems.length} anzeigen →
                </Link>
              )}
            </div>
          </div>
        )}

        {/* Expiring Soon Items */}
        {expiringItems && expiringItems.length > 0 && (
          <div className="dashboard-card">
            <h3 style={{ color: '#ff9500', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Clock size={20} />
              Läuft bald ab ({expiringItems.length})
            </h3>
            <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
              {expiringItems.slice(0, 5).map((item) => (
                <div key={item.id} style={{
                  padding: '0.75rem',
                  margin: '0.5rem 0',
                  background: 'rgba(255, 149, 0, 0.1)',
                  border: '1px solid #ff9500',
                  borderRadius: '8px'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start' }}>
                    <div>
                      <strong>{item.name}</strong>
                      <div style={{ fontSize: '0.9rem', color: 'rgba(255, 255, 255, 0.7)' }}>
                        {item.categoryName} • {item.quantity} {item.unit}
                      </div>
                      {item.expiryDate && (
                        <div style={{ fontSize: '0.8rem', color: 'rgba(255, 255, 255, 0.6)', marginTop: '0.25rem' }}>
                          Ablauf: {new Date(item.expiryDate).toLocaleDateString('de-DE')}
                        </div>
                      )}
                    </div>
                    <div style={{ 
                      background: '#ff9500', 
                      color: 'white', 
                      padding: '0.25rem 0.5rem', 
                      borderRadius: '4px', 
                      fontSize: '0.8rem' 
                    }}>
                      {item.daysUntilExpiry} Tag{item.daysUntilExpiry !== 1 ? 'e' : ''}
                    </div>
                  </div>
                </div>
              ))}
              {expiringItems.length > 5 && (
                <Link to="/items?filter=expiring" style={{ 
                  color: '#646cff', 
                  textDecoration: 'none',
                  display: 'block',
                  textAlign: 'center',
                  marginTop: '1rem'
                }}>
                  Alle {expiringItems.length} anzeigen →
                </Link>
              )}
            </div>
          </div>
        )}

        {/* Quick Actions */}
        <div className="dashboard-card">
          <h3>Schnellaktionen</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            <Link to="/items/new" style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              padding: '0.75rem',
              background: 'rgba(100, 108, 255, 0.1)',
              border: '1px solid #646cff',
              borderRadius: '8px',
              color: '#646cff',
              textDecoration: 'none',
              transition: 'all 0.2s'
            }}>
              <Plus size={18} />
              Neuen Artikel hinzufügen
            </Link>
            
            <Link to="/items" style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              padding: '0.75rem',
              background: 'rgba(255, 255, 255, 0.1)',
              border: '1px solid rgba(255, 255, 255, 0.3)',
              borderRadius: '8px',
              color: 'rgba(255, 255, 255, 0.9)',
              textDecoration: 'none',
              transition: 'all 0.2s'
            }}>
              <Package size={18} />
              Alle Artikel anzeigen
            </Link>
          </div>
        </div>
      </div>

      {/* Empty State */}
      {(!expiredItems || expiredItems.length === 0) && 
       (!expiringItems || expiringItems.length === 0) && 
       (!stats || stats.totalItems === 0) && (
        <div style={{
          textAlign: 'center',
          padding: '3rem',
          background: 'rgba(255, 255, 255, 0.1)',
          borderRadius: '12px',
          marginTop: '2rem'
        }}>
          <Package size={48} style={{ color: 'rgba(255, 255, 255, 0.5)', marginBottom: '1rem' }} />
          <h3 style={{ color: 'rgba(255, 255, 255, 0.8)', marginBottom: '1rem' }}>
            Noch keine Artikel vorhanden
          </h3>
          <p style={{ color: 'rgba(255, 255, 255, 0.6)', marginBottom: '2rem' }}>
            Fügen Sie Ihren ersten Artikel hinzu, um mit der Verwaltung zu beginnen.
          </p>
          <Link 
            to="/items/new"
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.5rem',
              padding: '0.75rem 1.5rem',
              background: '#646cff',
              color: 'white',
              textDecoration: 'none',
              borderRadius: '8px',
              fontSize: '1rem'
            }}
          >
            <Plus size={20} />
            Ersten Artikel hinzufügen
          </Link>
        </div>
      )}
    </div>
  );
}

export default Dashboard;