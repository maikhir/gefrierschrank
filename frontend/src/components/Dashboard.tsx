import React, { useEffect, useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import { itemsAPI } from '../services/api';
import { Item } from '../types';
import { Link } from 'react-router-dom';

const Dashboard: React.FC = () => {
  const { user, logout } = useAuth();
  const [expiringItems, setExpiringItems] = useState<Item[]>([]);
  const [expiredItems, setExpiredItems] = useState<Item[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadData = async () => {
      try {
        const [expiring, expired] = await Promise.all([
          itemsAPI.getExpiring(7),
          itemsAPI.getExpired(),
        ]);
        
        setExpiringItems(expiring);
        setExpiredItems(expired);
      } catch (error) {
        setError('Fehler beim Laden der Daten');
        console.error('Error loading dashboard data:', error);
      } finally {
        setIsLoading(false);
      }
    };

    loadData();
  }, []);

  if (isLoading) {
    return <div data-testid="loading">Laden...</div>;
  }

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>Gefrierschrank Verwaltung</h1>
        <div className="user-info">
          <span>Willkommen, {user?.username}!</span>
          <button onClick={logout}>Abmelden</button>
        </div>
      </header>

      {error && (
        <div className="error-message" role="alert">
          {error}
        </div>
      )}

      <div className="dashboard-stats">
        <div className="stat-card expiring">
          <h3>Bald ablaufend (7 Tage)</h3>
          <p className="stat-number" data-testid="expiring-count">
            {expiringItems.length}
          </p>
        </div>
        
        <div className="stat-card expired">
          <h3>Abgelaufen</h3>
          <p className="stat-number" data-testid="expired-count">
            {expiredItems.length}
          </p>
        </div>
      </div>

      <nav className="dashboard-nav">
        <Link to="/items" className="nav-link">
          Alle Artikel verwalten
        </Link>
      </nav>

      {expiringItems.length > 0 && (
        <section className="dashboard-section">
          <h2>Bald ablaufende Artikel</h2>
          <div className="item-list">
            {expiringItems.slice(0, 5).map((item) => (
              <div key={item.id} className="item-card">
                <h4>{item.name}</h4>
                <p>Ablaufdatum: {item.expiryDate}</p>
                <p>Kategorie: {item.categoryName}</p>
              </div>
            ))}
          </div>
        </section>
      )}

      {expiredItems.length > 0 && (
        <section className="dashboard-section">
          <h2>Abgelaufene Artikel</h2>
          <div className="item-list">
            {expiredItems.slice(0, 3).map((item) => (
              <div key={item.id} className="item-card expired">
                <h4>{item.name}</h4>
                <p>Abgelaufen: {item.expiryDate}</p>
                <p>Kategorie: {item.categoryName}</p>
              </div>
            ))}
          </div>
        </section>
      )}
    </div>
  );
};

export default Dashboard;