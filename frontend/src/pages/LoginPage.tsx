import React, { useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { LoginRequest } from '../types/auth'

const LoginPage: React.FC = () => {
  const [credentials, setCredentials] = useState<LoginRequest>({
    username: '',
    password: '',
  })
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')

  const { login } = useAuth()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoading(true)
    setError('')

    try {
      await login(credentials)
    } catch (error: any) {
      setError(error.response?.data?.message || 'Login failed. Please try again.')
    } finally {
      setIsLoading(false)
    }
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setCredentials(prev => ({
      ...prev,
      [name]: value,
    }))
  }

  return (
    <div style={{ maxWidth: '400px', margin: '2rem auto', padding: '2rem' }}>
      <h1>Gefrierschrank-Verwaltung</h1>
      <h2>Login</h2>
      
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div>
          <label htmlFor="username">Benutzername:</label>
          <input
            type="text"
            id="username"
            name="username"
            value={credentials.username}
            onChange={handleChange}
            required
            disabled={isLoading}
            style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
          />
        </div>

        <div>
          <label htmlFor="password">Passwort:</label>
          <input
            type="password"
            id="password"
            name="password"
            value={credentials.password}
            onChange={handleChange}
            required
            disabled={isLoading}
            style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
          />
        </div>

        {error && (
          <div style={{ color: 'red', padding: '0.5rem', backgroundColor: '#fee', border: '1px solid red', borderRadius: '4px' }}>
            {error}
          </div>
        )}

        <button 
          type="submit" 
          disabled={isLoading}
          style={{ 
            padding: '0.75rem', 
            backgroundColor: '#007bff', 
            color: 'white', 
            border: 'none', 
            borderRadius: '4px',
            cursor: isLoading ? 'not-allowed' : 'pointer',
            opacity: isLoading ? 0.6 : 1
          }}
        >
          {isLoading ? 'Anmelden...' : 'Anmelden'}
        </button>
      </form>

      <div style={{ marginTop: '2rem', padding: '1rem', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
        <h3>Test-Benutzer:</h3>
        <p><strong>Admin:</strong> admin / admin123</p>
        <p><em>Hinweis: Der Backend-Server muss auf Port 8080 laufen</em></p>
      </div>
    </div>
  )
}

export default LoginPage