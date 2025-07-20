# Umsetzungsplan - Gefrierschrank-Verwaltungsapp

## Technischer Stack (Empfehlung)

### Frontend
- **Framework**: React 18 mit TypeScript
- **PWA**: Vite PWA Plugin für Service Worker und Manifest
- **UI Library**: Tailwind CSS + Headless UI
- **State Management**: Zustand (lightweight) oder Redux Toolkit
- **Offline Storage**: IndexedDB mit Dexie.js
- **Camera**: Browser MediaDevices API
- **Icons**: Lucide React oder Heroicons

### Backend
- **Framework**: Java Spring Boot 3.x mit Maven
- **Database**: PostgreSQL mit Spring Data JPA/Hibernate
- **Authentication**: Spring Security mit JWT
- **File Upload**: Spring Web Multipart + ImageIO für Bildkomprimierung
- **Push Notifications**: Web Push mit Spring WebFlux
- **API**: RESTful API mit Spring Web + OpenAPI/Swagger 3
- **Build Tool**: Maven
- **Java Version**: Java 17+ (LTS)

### DevOps & Deployment
- **Frontend**: Vercel oder Netlify
- **Backend**: Railway, Render oder DigitalOcean App Platform
- **Database**: Supabase PostgreSQL oder Railway PostgreSQL
- **CI/CD**: GitHub Actions

## Entwicklungsphasen

### Phase 1: Foundation & Auth (1-2 Wochen)
**Ziel**: Grundlegende Infrastruktur und Benutzerauthentifizierung

#### Backend Setup
- [ ] Spring Boot 3.x Projekt mit Maven erstellen
- [ ] PostgreSQL Datenbank Setup mit Spring Data JPA
- [ ] Spring Security mit JWT Configuration
- [ ] User Management (Admin/User Rollen) mit UserDetailsService
- [ ] RESTful API Controller Grundstruktur
- [ ] CORS Configuration und Security Filter

#### Frontend Setup
- [ ] React + TypeScript + Vite Projekt
- [ ] PWA Konfiguration (Manifest, Service Worker)
- [ ] Tailwind CSS Setup
- [ ] Router Setup (React Router)
- [ ] State Management Setup
- [ ] Authentication Context/Store

#### Database Schema
```sql
Users (id, username, email, password_hash, role, notifications_enabled, created_at)
Sessions (id, user_id, token, expires_at, created_at)
```

### Phase 2: Core Item Management (2-3 Wochen)
**Ziel**: Artikel hinzufügen, bearbeiten und anzeigen

#### Backend Features
- [ ] Items CRUD REST Controller
- [ ] Categories REST Controller
- [ ] File Upload Endpoint (Spring Multipart + ImageIO)
- [ ] CSV Import Service mit OpenCSV
- [ ] Bean Validation & Global Exception Handling

#### Frontend Features
- [ ] Dashboard/Übersicht
- [ ] Artikel hinzufügen Form
- [ ] Smart Input Komponenten (Mengen nach Kategorie)
- [ ] Kamera Integration
- [ ] CSV Import Interface
- [ ] Artikel Liste mit Grundfunktionen

#### Database Schema Extension
```sql
Categories (id, name, icon, default_unit, unit_step, min_value, max_value)
Items (id, name, category_id, quantity, unit, expiry_date, photo_path, user_id, created_at, updated_at)
```

### Phase 3: Filtering & Search (1 Woche)
**Ziel**: Erweiterte Darstellung und Suche

#### Features
- [ ] Kategorie-Filter
- [ ] Mengen-Filter
- [ ] Suchfunktion
- [ ] Sortierung (Datum, Kategorie, Name, Menge)
- [ ] Detailansicht für Artikel

### Phase 4: Expiration Tracking (1-2 Wochen)
**Ziel**: Verfallsdatum-Verfolgung und Benachrichtigungen

#### Backend Features
- [ ] Expiration Check Service mit @Scheduled
- [ ] Push Notification Service mit Spring WebFlux
- [ ] Daily Notification @Scheduled Cron Job
- [ ] Notification History JPA Repository

#### Frontend Features
- [ ] Verfallsdaten-Übersicht
- [ ] 7-Tage-Warnung UI
- [ ] Push Notification Setup
- [ ] Benachrichtigungseinstellungen

#### Database Schema Extension
```sql
Notifications (id, user_id, item_id, type, message, sent_at, read_at)
```

### Phase 5: PWA & Offline (1 Woche)
**Ziel**: Vollständige PWA-Funktionalität

#### Features
- [ ] Service Worker für Offline-Caching
- [ ] IndexedDB für lokale Datenspeicherung
- [ ] Offline/Online Synchronisation
- [ ] PWA Installation Prompts
- [ ] Background Sync für Uploads

### Phase 6: Themes & UX Polish (1 Woche)
**Ziel**: Theme-System und UI-Verbesserungen

#### Features
- [ ] Theme System (Standard, Dark, Colorful, Minimal)
- [ ] Theme Context/Provider
- [ ] Responsive Design Optimierungen
- [ ] Loading States & Skeleton Screens
- [ ] Error Boundaries

### Phase 7: Admin Features (1 Woche)
**Ziel**: Admin-Panel und Backup-System

#### Features
- [ ] Admin Dashboard
- [ ] Benutzerverwaltung
- [ ] Backup/Restore System
- [ ] Datenbank-Reset
- [ ] System-Einstellungen

#### Database Schema Extension
```sql
Backups (id, filename, file_size, created_by, created_at, backup_type)
```

### Phase 8: Testing & Optimization (1 Woche)
**Ziel**: Qualitätssicherung und Performance

#### Tasks
- [ ] Backend Unit Tests (JUnit 5 + Mockito)
- [ ] Integration Tests (Spring Boot Test)
- [ ] Frontend Tests (Jest + React Testing Library)
- [ ] E2E Tests (Playwright)
- [ ] Performance Optimierung
- [ ] Accessibility Testing
- [ ] Security Audit

### Phase 9: Deployment & Launch (1 Woche)
**Ziel**: Produktionsdeployment

#### Tasks
- [ ] Production Build Optimierung
- [ ] Environment Variables Setup
- [ ] SSL Zertifikate
- [ ] Monitoring Setup
- [ ] Error Tracking (Sentry)
- [ ] Analytics Setup
- [ ] Documentation

## Zeitschätzung

**Gesamt: 9-12 Wochen**

- Phase 1 (Foundation): 1-2 Wochen
- Phase 2 (Core Features): 2-3 Wochen
- Phase 3 (Filtering): 1 Woche
- Phase 4 (Expiration): 1-2 Wochen
- Phase 5 (PWA): 1 Woche
- Phase 6 (Themes): 1 Woche
- Phase 7 (Admin): 1 Woche
- Phase 8 (Testing): 1 Woche
- Phase 9 (Deployment): 1 Woche

## Prioritäten nach MoSCoW

### Must-Have (Phases 1-4)
- Benutzer-Management
- Artikel-Management (CRUD)
- CSV Import
- Foto-Upload
- Verfallsdatum-Tracking
- Basic PWA

### Should-Have (Phases 5-7)
- Vollständige PWA-Funktionalität
- Theme System
- Admin Features
- Push Notifications

### Could-Have (Zukünftige Releases)
- Statistiken
- Export-Funktionen
- Barcode Scanner
- Einkaufsliste

## Risiken & Mitigation

1. **PWA Browser Kompatibilität**: Frühe Tests auf verschiedenen Geräten
2. **Foto-Upload Performance**: Komprimierung und Progressive Loading
3. **Offline Sync Komplexität**: Schrittweise Implementierung mit Fallbacks
4. **Push Notification Setup**: Browser-spezifische Tests

## Nächste Schritte

1. **Projekt initialisieren**: Repository Setup mit beiden Teilen (Frontend/Backend)
2. **Development Environment**: Docker Compose für lokale Entwicklung
3. **Phase 1 starten**: Backend Foundation und Authentication