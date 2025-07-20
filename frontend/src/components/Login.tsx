import { useState } from 'react';
import { authService } from '../services/auth';

function Login() {
  const [credentials, setCredentials] = useState({
    username: '',
    password: ''
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string>('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!credentials.username || !credentials.password) {
      setError('Bitte füllen Sie alle Felder aus.');
      return;
    }

    setIsLoading(true);
    setError('');

    try {
      await authService.login(credentials);
      window.location.reload(); // Reload to update authentication state
    } catch (error) {
      setError('Anmeldung fehlgeschlagen. Bitte überprüfen Sie Ihre Anmeldedaten.');
      console.error('Login error:', error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      minHeight: '100vh',
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    }}>
      <form onSubmit={handleSubmit} className="form" style={{ maxWidth: '400px', width: '100%' }}>
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <h1 style={{ color: '#fff', margin: 0, fontSize: '2rem' }}>
            🧊 Gefrierschrank
          </h1>
          <p style={{ color: 'rgba(255, 255, 255, 0.8)', marginTop: '0.5rem' }}>
            Verwalten Sie Ihre Tiefkühlprodukte
          </p>
        </div>

        {error && (
          <div style={{
            background: 'rgba(255, 68, 68, 0.1)',
            border: '1px solid #ff4444',
            borderRadius: '8px',
            padding: '0.75rem',
            marginBottom: '1rem',
            color: '#ff4444',
            textAlign: 'center'
          }}>
            {error}
          </div>
        )}

        <div className="form-group">
          <label htmlFor="username">Benutzername</label>
          <input
            id="username"
            type="text"
            value={credentials.username}
            onChange={(e) => setCredentials({ ...credentials, username: e.target.value })}
            disabled={isLoading}
            placeholder="Benutzername eingeben"
          />
        </div>

        <div className="form-group">
          <label htmlFor="password">Passwort</label>
          <input
            id="password"
            type="password"
            value={credentials.password}
            onChange={(e) => setCredentials({ ...credentials, password: e.target.value })}
            disabled={isLoading}
            placeholder="Passwort eingeben"
          />
        </div>

        <button 
          type="submit" 
          disabled={isLoading}
          style={{
            width: '100%',
            padding: '0.75rem',
            fontSize: '1rem',
            background: isLoading ? '#666' : '#646cff',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            cursor: isLoading ? 'not-allowed' : 'pointer',
            marginTop: '1rem'
          }}
        >
          {isLoading ? 'Anmeldung läuft...' : 'Anmelden'}
        </button>

        <div style={{
          marginTop: '2rem',
          textAlign: 'center',
          color: 'rgba(255, 255, 255, 0.7)',
          fontSize: '0.9rem'
        }}>
          <p style={{ margin: '0.5rem 0' }}>Testbenutzer:</p>
          <p style={{ margin: '0.25rem 0' }}>Admin: admin / admin123</p>
          <p style={{ margin: '0.25rem 0' }}>Benutzer: user / user123</p>
        </div>
      </form>
    </div>
  );
}

export default Login;