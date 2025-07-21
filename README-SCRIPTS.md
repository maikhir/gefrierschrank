# 🚀 Gefrierschrank Management - Development Scripts

Diese Skripte vereinfachen das Starten, Stoppen und Verwalten der Gefrierschrank Management App während der Entwicklung.

## 📋 Verfügbare Skripte

### 1. `start.sh` - Application starten
Startet Backend und/oder Frontend automatisch.

```bash
# Beide Services starten (Standard)
./start.sh

# Nur Backend starten
./start.sh --backend-only

# Nur Frontend starten  
./start.sh --frontend-only

# Hilfe anzeigen
./start.sh --help
```

**Features:**
- ✅ Automatische Port-Prüfung (8080, 5173)
- ✅ Backend Build & Start mit Maven
- ✅ Frontend Dependencies Installation
- ✅ Startup-Validation mit Health-Checks
- ✅ PID-Tracking für sauberes Shutdown
- ✅ Comprehensive Logging

### 2. `stop.sh` - Application stoppen
Stoppt laufende Services sauber.

```bash
# Beide Services stoppen (Standard)
./stop.sh

# Nur Backend stoppen
./stop.sh --backend-only

# Nur Frontend stoppen
./stop.sh --frontend-only

# Force Kill (wenn normale Stops nicht funktionieren)
./stop.sh --force

# Hilfe anzeigen
./stop.sh --help
```

**Features:**
- ✅ Graceful Shutdown mit PID-Tracking
- ✅ Port-basierter Fallback-Stop
- ✅ Force-Kill Option für hängende Prozesse
- ✅ Automatic Cleanup von PID-Files

### 3. `dev.sh` - Development Helper
Umfassendes Management-Tool für Entwicklung.

```bash
# Status anzeigen
./dev.sh status

# Services starten/stoppen/neustarten
./dev.sh start [--backend-only|--frontend-only]
./dev.sh stop [--backend-only|--frontend-only] 
./dev.sh restart

# Logs anzeigen
./dev.sh logs backend     # Backend Logs (letzte 50 Zeilen)
./dev.sh logs frontend    # Frontend Logs (letzte 50 Zeilen)

# Tests ausführen
./dev.sh test all         # Alle Tests
./dev.sh test backend     # Nur Backend Tests
./dev.sh test frontend    # Nur Frontend Tests

# Build Services
./dev.sh build all        # Beide Services builden
./dev.sh build backend    # Nur Backend
./dev.sh build frontend   # Nur Frontend

# Cleanup
./dev.sh clean            # Build-Artifacts und Logs löschen

# Datenbank zurücksetzen
./dev.sh reset-data       # H2 Database löschen (⚠️ alle Daten weg!)

# Hilfe
./dev.sh help
```

## 🔧 Systemanforderungen

### Erforderliche Software:
- **Java 21+** für Backend
- **Maven 3.6+** für Backend Build
- **Node.js 18+** für Frontend
- **npm 9+** für Frontend Dependencies

### Automatische Prüfung:
Die Skripte prüfen automatisch alle Anforderungen beim Start.

## 📊 Service URLs nach Start

| Service | URL | Beschreibung |
|---------|-----|-------------|
| **Frontend** | http://localhost:5173 | React Development Server |
| **Backend API** | http://localhost:8080 | Spring Boot REST API |
| **H2 Console** | http://localhost:8080/h2-console | Database Management |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | API Documentation |

### H2 Database Zugangsdaten:
- **JDBC URL:** `jdbc:h2:file:./data/gefrierschrank_db`
- **Username:** `sa`
- **Password:** (leer)

## 📜 Logging

### Log-Dateien:
- `backend.log` - Spring Boot Application Logs
- `frontend.log` - Vite Development Server Logs

### Log-Monitoring:
```bash
# Logs in Echtzeit verfolgen
tail -f backend.log
tail -f frontend.log

# Oder mit dem dev.sh Script
./dev.sh logs backend
./dev.sh logs frontend
```

## 🚨 Troubleshooting

### Problem: Port bereits belegt
```bash
# Prüfen welcher Prozess den Port belegt
lsof -i :8080    # Backend Port
lsof -i :5173    # Frontend Port

# Force Stop verwenden
./stop.sh --force
```

### Problem: Backend startet nicht
```bash
# Backend Logs prüfen
./dev.sh logs backend

# Manual Backend Build
cd backend
mvn clean compile
```

### Problem: Frontend Dependencies fehlen
```bash
# Dependencies neu installieren
cd frontend
rm -rf node_modules package-lock.json
npm install
```

### Problem: Database Fehler
```bash
# Database zurücksetzen
./dev.sh reset-data

# Oder manuell
rm -f backend/data/gefrierschrank_db.*
```

## 🔄 Typical Development Workflow

```bash
# 1. Application starten
./start.sh

# 2. Status prüfen
./dev.sh status

# 3. Development... (Code changes)

# 4. Tests ausführen
./dev.sh test all

# 5. Bei Problemen: Logs prüfen
./dev.sh logs backend
./dev.sh logs frontend

# 6. Application stoppen
./stop.sh

# 7. Cleanup (optional)
./dev.sh clean
```

## ⚡ Quick Commands

```bash
# Full Development Cycle
./start.sh && ./dev.sh test all && ./stop.sh

# Reset Everything
./stop.sh --force && ./dev.sh clean && ./dev.sh reset-data

# Check Everything is Working
./dev.sh status && ./dev.sh logs backend | tail -10

# Restart with fresh build
./stop.sh && ./dev.sh build all && ./start.sh
```

## 📁 File Structure nach Script-Ausführung

```
gs-mgmt/
├── start.sh              # Start Script
├── stop.sh               # Stop Script  
├── dev.sh                # Development Helper
├── backend.log           # Backend Logs
├── frontend.log          # Frontend Logs
├── backend.pid           # Backend PID (während running)
├── frontend.pid          # Frontend PID (während running)
├── .app_status           # App Status Info
└── uploads/              # File Upload Directory
    ├── images/           # Uploaded Images
    └── csv/              # Uploaded CSV Files
```

## 🎯 Pro Tips

1. **Immer mit `./dev.sh status` starten** um den aktuellen Zustand zu prüfen
2. **Bei Änderungen am Backend:** Restart nicht nötig (Spring Boot DevTools)
3. **Bei Frontend Änderungen:** Hot Reload automatisch aktiv
4. **Vor Commits:** `./dev.sh test all` ausführen
5. **Bei persistenten Problemen:** `./dev.sh clean` und neu starten
6. **Production Build testen:** `./dev.sh build all`

## 🔐 Sicherheitshinweise

- Scripts validieren automatisch System-Requirements
- PID-Files werden sauber verwaltet und aufgeräumt
- Force-Kill nur als letztes Mittel verwenden
- Database Reset löscht ALLE Daten unwiderruflich
- Upload-Directory wird automatisch erstellt aber nicht gesichert