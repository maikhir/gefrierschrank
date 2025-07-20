import React, { useState } from 'react'
import axios from 'axios'

const SimpleApp: React.FC = () => {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('')
  const [isLoggedIn, setIsLoggedIn] = useState(false)

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      const response = await axios.post('http://localhost:8080/api/auth/signin', {
        username,
        password
      })
      setMessage('Login erfolgreich!')
      setIsLoggedIn(true)
      console.log('Response:', response.data)
    } catch (error: any) {
      setMessage('Login fehlgeschlagen: ' + (error.response?.data?.message || error.message))
    }
  }

  if (isLoggedIn) {
    return (
      <div style={{ padding: '2rem', maxWidth: '600px', margin: '0 auto' }}>
        <h1>🎉 Erfolgreich eingeloggt!</h1>
        <p>Die Authentifizierung funktioniert korrekt.</p>
        <button onClick={() => setIsLoggedIn(false)}>Zurück zum Login</button>
      </div>
    )
  }

  return (
    <div style={{ padding: '2rem', maxWidth: '400px', margin: '0 auto' }}>
      <h1>Gefrierschrank-Verwaltung</h1>
      <h2>Login Test</h2>
      
      <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div>
          <label>Benutzername:</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            style={{ width: '100%', padding: '0.5rem' }}
            required
          />
        </div>
        
        <div>
          <label>Passwort:</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            style={{ width: '100%', padding: '0.5rem' }}
            required
          />
        </div>
        
        <button 
          type="submit"
          style={{ 
            padding: '0.75rem', 
            backgroundColor: '#007bff', 
            color: 'white', 
            border: 'none', 
            borderRadius: '4px' 
          }}
        >
          Anmelden
        </button>
      </form>

      {message && (
        <div style={{ 
          marginTop: '1rem', 
          padding: '1rem', 
          backgroundColor: message.includes('erfolgreich') ? '#d4edda' : '#f8d7da',
          color: message.includes('erfolgreich') ? '#155724' : '#721c24',
          borderRadius: '4px'
        }}>
          {message}
        </div>
      )}

      <div style={{ marginTop: '2rem', padding: '1rem', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
        <h3>Test-Benutzer:</h3>
        <p><strong>Admin:</strong> admin / admin123</p>
        <p><strong>User:</strong> user / user123</p>
        <p><em>Backend läuft auf Port 8080</em></p>
      </div>
    </div>
  )
}

export default SimpleApp