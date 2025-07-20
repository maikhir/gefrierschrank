console.log('Frontend startet...')

const root = document.getElementById('root')
if (root) {
  root.innerHTML = `
    <div style="font-family: Arial; max-width: 400px; margin: 2rem auto; padding: 2rem;">
      <h1 style="color: #007bff; margin-bottom: 0.5rem;">Gefrierschrank-Verwaltung</h1>
      <h2 style="color: #495057; margin-bottom: 1.5rem;">Login</h2>
      
      <div id="loginForm">
        <div style="margin-bottom: 1rem;">
          <label style="display: block; margin-bottom: 0.5rem; font-weight: bold;">Benutzername:</label>
          <input 
            type="text" 
            id="username" 
            placeholder="Benutzername eingeben"
            style="width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 4px; font-size: 16px;"
            required
          />
        </div>
        
        <div style="margin-bottom: 1rem;">
          <label style="display: block; margin-bottom: 0.5rem; font-weight: bold;">Passwort:</label>
          <input 
            type="password" 
            id="password" 
            placeholder="Passwort eingeben"
            style="width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 4px; font-size: 16px;"
            required
          />
        </div>
        
        <button 
          onclick="handleLogin()"
          style="width: 100%; padding: 0.75rem; background-color: #007bff; color: white; border: none; border-radius: 4px; font-size: 16px; cursor: pointer;"
        >
          Anmelden
        </button>
      </div>
      
      <div id="successMessage" style="display: none; margin-top: 1rem; padding: 1rem; background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; border-radius: 4px;">
        <h3>✅ Login erfolgreich!</h3>
        <p id="welcomeText"></p>
        <button onclick="handleLogout()" style="padding: 0.5rem 1rem; background-color: #6c757d; color: white; border: none; border-radius: 4px; cursor: pointer;">Zurück zum Login</button>
      </div>
      
      <div id="errorMessage" style="display: none; margin-top: 1rem; padding: 1rem; background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; border-radius: 4px;"></div>
      
      <div style="margin-top: 2rem; padding: 1rem; background-color: #f8f9fa; border: 1px solid #dee2e6; border-radius: 4px;">
        <h3 style="margin-top: 0; color: #495057;">Systeminformation:</h3>
        <p style="font-size: 0.9em; color: #6c757d; margin-bottom: 0;">Backend: http://localhost:8080</p>
        <p style="font-size: 0.9em; color: #6c757d; margin-bottom: 0;">Version: 0.1.0-SNAPSHOT</p>
      </div>
    </div>
  `
}

async function handleLogin() {
  const username = document.getElementById('username').value
  const password = document.getElementById('password').value
  const errorDiv = document.getElementById('errorMessage')
  const successDiv = document.getElementById('successMessage')
  
  // Reset messages
  errorDiv.style.display = 'none'
  successDiv.style.display = 'none'
  
  if (!username || !password) {
    showError('Bitte beide Felder ausfüllen!')
    return
  }
  
  try {
    console.log('Sende Login-Anfrage...')
    
    const response = await fetch('http://localhost:8080/api/auth/signin', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: username,
        password: password
      })
    })
    
    console.log('Response Status:', response.status)
    
    if (response.ok) {
      const data = await response.json()
      console.log('Login Response:', data)
      
      document.getElementById('loginForm').style.display = 'none'
      document.getElementById('welcomeText').innerHTML = 
        'Willkommen <strong>' + data.user.username + '</strong>!<br>' +
        'Rolle: ' + data.user.role + '<br>' +
        'E-Mail: ' + data.user.email
      successDiv.style.display = 'block'
    } else {
      const errorData = await response.json().catch(() => ({}))
      showError('Login fehlgeschlagen: ' + (errorData.message || response.statusText))
    }
  } catch (error) {
    console.error('Login Error:', error)
    showError('Verbindungsfehler: ' + error.message)
  }
}

function handleLogout() {
  document.getElementById('loginForm').style.display = 'block'
  document.getElementById('successMessage').style.display = 'none'
  document.getElementById('errorMessage').style.display = 'none'
  document.getElementById('username').value = ''
  document.getElementById('password').value = ''
}

function showError(message) {
  const errorDiv = document.getElementById('errorMessage')
  errorDiv.textContent = message
  errorDiv.style.display = 'block'
}

// Enter-Taste Support
document.addEventListener('keypress', function(e) {
  if (e.key === 'Enter') {
    handleLogin()
  }
})

window.handleLogin = handleLogin
window.handleLogout = handleLogout
