import React from 'react'
import { useAuth } from '../contexts/AuthContext'

const DashboardPage: React.FC = () => {
  const { user, logout } = useAuth()

  return (
    <div style={{ padding: '2rem', maxWidth: '800px', margin: '0 auto' }}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1>Gefrierschrank-Verwaltung</h1>
        <div>
          <span style={{ marginRight: '1rem' }}>Willkommen, {user?.username}!</span>
          <button 
            onClick={logout}
            style={{ 
              padding: '0.5rem 1rem', 
              backgroundColor: '#dc3545', 
              color: 'white', 
              border: 'none', 
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Abmelden
          </button>
        </div>
      </header>

      <div style={{ display: 'grid', gap: '2rem' }}>
        <div style={{ padding: '1.5rem', border: '1px solid #dee2e6', borderRadius: '8px' }}>
          <h2>Dashboard - Phase 1</h2>
          <p>Benutzer-Authentifizierung erfolgreich implementiert!</p>
          
          <div style={{ marginTop: '1rem' }}>
            <h3>Benutzerinformationen:</h3>
            <ul style={{ listStyle: 'none', padding: 0 }}>
              <li><strong>ID:</strong> {user?.id}</li>
              <li><strong>Benutzername:</strong> {user?.username}</li>
              <li><strong>E-Mail:</strong> {user?.email}</li>
              <li><strong>Rolle:</strong> {user?.role}</li>
              <li><strong>Benachrichtigungen:</strong> {user?.notificationsEnabled ? 'Aktiviert' : 'Deaktiviert'}</li>
            </ul>
          </div>
        </div>

        <div style={{ padding: '1.5rem', border: '1px solid #dee2e6', borderRadius: '8px' }}>
          <h3>Phase 1: Foundation & Auth ✅</h3>
          <p>Die Grundlagen sind implementiert:</p>
          <ul>
            <li>✅ Spring Boot Backend mit JWT Authentication</li>
            <li>✅ React Frontend mit TypeScript</li>
            <li>✅ User Management System</li>
            <li>✅ Protected Routes</li>
            <li>✅ Token-basierte Authentifizierung</li>
          </ul>
        </div>

        <div style={{ padding: '1.5rem', border: '1px solid #ffc107', borderRadius: '8px', backgroundColor: '#fff9c4' }}>
          <h3>Nächste Schritte (Phase 2):</h3>
          <ul>
            <li>🔄 Artikel-Management (CRUD)</li>
            <li>🔄 Kategorie-System</li>
            <li>🔄 CSV Import</li>
            <li>🔄 Foto-Upload</li>
            <li>🔄 Verfallsdatum-Tracking</li>
          </ul>
        </div>
      </div>
    </div>
  )
}

export default DashboardPage