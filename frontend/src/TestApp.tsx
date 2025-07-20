import React, { useState } from 'react'

const TestApp: React.FC = () => {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('')
  const [isLoggedIn, setIsLoggedIn] = useState(false)

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    setMessage('Verbinde mit Backend...')
    
    try {
      const response = await fetch('http://localhost:8080/api/auth/signin', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username,
          password
        })
      })
      
      if (response.ok) {
        const data = await response.json()
        setMessage(`✅ Login erfolgreich! Willkommen ${data.user.username}`)
        setIsLoggedIn(true)
        console.log('Login Response:', data)
      } else {
        const errorData = await response.json()
        setMessage(`❌ Login fehlgeschlagen: ${errorData.message || response.statusText}`)
      }
    } catch (error: any) {
      setMessage(`❌ Fehler: ${error.message}`)
    }
  }

  if (isLoggedIn) {
    return (
      <div style={{ padding: '2rem', maxWidth: '600px', margin: '0 auto', fontFamily: 'Arial' }}>
        <h1>🎉 Erfolgreich eingeloggt!</h1>
        <p>{message}</p>
        <button 
          onClick={() => { setIsLoggedIn(false); setMessage(''); setUsername(''); setPassword('') }}
          style={{ 
            padding: '0.5rem 1rem', 
            backgroundColor: '#6c757d', 
            color: 'white', 
            border: 'none', 
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          Zurück zum Login
        </button>
      </div>
    )
  }

  return (
    <div style={{ padding: '2rem', maxWidth: '400px', margin: '0 auto', fontFamily: 'Arial' }}>
      <h1 style={{ color: '#007bff' }}>Gefrierschrank-Verwaltung</h1>
      <h2>Login Test</h2>
      
      <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
            Benutzername:
          </label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            style={{ 
              width: '100%', 
              padding: '0.75rem', 
              border: '1px solid #ddd', 
              borderRadius: '4px',
              fontSize: '16px'
            }}
            placeholder="admin oder user"
            required
          />
        </div>
        
        <div>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
            Passwort:
          </label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            style={{ 
              width: '100%', 
              padding: '0.75rem', 
              border: '1px solid #ddd', 
              borderRadius: '4px',
              fontSize: '16px'
            }}
            placeholder="admin123 oder user123"
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
            borderRadius: '4px',
            fontSize: '16px',
            cursor: 'pointer',
            transition: 'background-color 0.2s'
          }}
          onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#0056b3'}
          onMouseOut={(e) => e.currentTarget.style.backgroundColor = '#007bff'}
        >
          Anmelden
        </button>
      </form>

      {message && (
        <div style={{ 
          marginTop: '1rem', 
          padding: '1rem', 
          backgroundColor: message.includes('✅') ? '#d4edda' : '#f8d7da',
          color: message.includes('✅') ? '#155724' : '#721c24',
          border: `1px solid ${message.includes('✅') ? '#c3e6cb' : '#f5c6cb'}`,
          borderRadius: '4px'
        }}>
          {message}
        </div>
      )}

      <div style={{ 
        marginTop: '2rem', 
        padding: '1rem', 
        backgroundColor: '#f8f9fa', 
        borderRadius: '4px',
        border: '1px solid #dee2e6'
      }}>
        <h3 style={{ marginTop: 0, color: '#495057' }}>Test-Benutzer:</h3>
        <p><strong>Admin:</strong> <code>admin</code> / <code>admin123</code></p>
        <p><strong>User:</strong> <code>user</code> / <code>user123</code></p>
        <p style={{ fontSize: '0.9em', color: '#6c757d', marginBottom: 0 }}>
          Backend läuft auf Port 8080
        </p>
      </div>
    </div>
  )
}

export default TestApp