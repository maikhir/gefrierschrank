# Secrets - Gefrierschrank Verwaltung

## Test-Benutzer (Development)

### Administrator
- **Benutzername**: `admin`
- **Passwort**: `admin123`
- **Rolle**: ADMIN
- **E-Mail**: admin@gefrierschrank.app

### Standard-Benutzer
- **Benutzername**: `user`
- **Passwort**: `user123`
- **Rolle**: USER
- **E-Mail**: user@gefrierschrank.app

## Backend-Konfiguration

### H2-Database
- **JDBC URL**: `jdbc:h2:file:./data/gefrierschrank_db`
- **Username**: `sa`
- **Password**: *(leer)*
- **Console**: http://localhost:8080/h2-console

### JWT Configuration
- **Secret**: `mySecretKey12345678901234567890123456789012345678901234567890`
- **Expiration**: 86400000ms (24 Stunden)

### Ports
- **Backend**: 8080
- **Frontend**: 5173
- **H2 Console**: 8080/h2-console

## ⚠️ Wichtige Sicherheitshinweise

- Diese Credentials sind nur für Development/Testing
- In Production müssen alle Passwörter geändert werden
- JWT Secret muss in Production über Umgebungsvariablen gesetzt werden
- H2 Console sollte in Production deaktiviert werden