# Product Requirements Document (PRD) - Gefrierschrank-Verwaltungsapp

## 1. Projektübersicht

### 1.1 Projektname
Gefrierschrank-Verwaltungsapp (Arbeitstitel)

### 1.2 Projektziel
Entwicklung einer Web-Anwendung zur effizienten Verwaltung von Gefrierschrank-Inhalten mit Fokus auf Haltbarkeitsdaten, Kategorisierung und Bestandsübersicht.

### 1.3 Zielgruppe
- Primäre Zielgruppe: Privatpersonen/Haushalte, die ihren Gefrierschrank organisiert verwalten möchten
- Sekundäre Zielgruppe: Familien mit größeren Vorräten, Meal-Prep-Enthusiasten
- Personas: Organisierte Haushaltsführung, Lebensmittelverschwendung vermeiden

### 1.4 Geschäftsziele
- Effiziente Verwaltung von Gefrierschrank-Inhalten
- Reduzierung von Lebensmittelverschwendung durch Haltbarkeitsverfolgung
- Vereinfachung der Haushaltsorganisation
- Übersicht über Vorräte und Kategorien

## 2. Funktionale Anforderungen

### 2.1 Kern-Features
#### Must-Have (Essentiell)
- [ ] **Benutzer-Management**: Registrierung, Login, Rollen (Admin/User)
- [ ] **Einzelartikel-Eingabe**: Einfache Eingabemaske für individuelle Artikel
- [ ] **CSV/Excel-Import**: Bulk-Eingabe über Datei-Upload
- [ ] **Übersichtsliste**: Komplette Darstellung aller Gefrierschrank-Inhalte
- [ ] **Haltbarkeitsverfolgung**: Anzeige von Verfallsdaten mit 7-Tage-Warnung
- [ ] **Kategorie-Filter**: Filterung nach Fleisch, Gemüse, Fertiggerichte, Eis
- [ ] **Mengen-Filter**: Filterung nach Stückzahlen/Mengen
- [ ] **Foto-Upload**: Kamera-Integration mit Komprimierung (Icon-Größe)
- [ ] **PWA-Funktionalität**: Offline-Fähigkeit, installierbar
- [ ] **Theme-System**: Verschiedene Designthemes zur Auswahl

#### Should-Have (Wichtig)
- [ ] **Admin-Funktionen**: Backup, Restore, Datenbank-Reset (nur für Admins)
- [ ] **Push-Benachrichtigungen**: Verfallsdatum-Erinnerungen
- [ ] **Icon-Bibliothek**: Alternative zu Fotos mit vorgefertigten Icons
- [ ] **Sortierung**: Nach Datum, Kategorie, Name, Menge
- [ ] **Suchfunktion**: Suche nach Artikelnamen
- [ ] **Offline-Synchronisation**: Daten abgleichen bei Internetverbindung

#### Could-Have (Wünschenswert)
- [ ] **Statistiken**: Übersicht über häufigste Kategorien, etc.
- [ ] **Export-Funktionen**: Liste als PDF oder CSV
- [ ] **Barcode-Scanner**: Automatische Produkterkennung
- [ ] **Einkaufsliste**: Automatische Erstellung basierend auf niedrigen Beständen

### 2.2 Seitenstruktur
```
Dashboard/Übersicht
├── Artikel hinzufügen
│   ├── Einzelartikel
│   └── CSV/Excel-Import
├── Alle Artikel
│   ├── Filter & Sortierung
│   └── Detailansicht
├── Kategorien-Übersicht
│   ├── Fleisch
│   ├── Gemüse
│   ├── Fertiggerichte
│   └── Eis
├── Verfallsdaten-Übersicht
├── Benutzer-Profil
│   ├── Theme-Auswahl
│   └── Benachrichtigungseinstellungen
└── Verwaltung (nur Admins)
    ├── Benutzerverwaltung
    ├── Backup/Restore
    ├── Datenbank-Reset
    └── System-Einstellungen
```

### 2.3 Benutzerflows
1. **Hauptflow**: Dashboard → Artikel hinzufügen → Foto aufnehmen → Speichern → Übersicht
2. **Verfallsdatum-Check**: Dashboard → Verfallsdaten-Übersicht → Artikel verwalten
3. **CSV-Import**: Artikel hinzufügen → CSV/Excel hochladen → Vorschau → Importieren
4. **Admin-Flow**: Verwaltung → Benutzer anlegen → Temporäres Passwort vergeben
5. **Einkauf-Flow**: Übersicht → Filter nach Kategorie → Artikel verwalten → Neue Artikel hinzufügen

### 2.5 Eingabe-Interfaces

#### Mengen-Eingabe (Smart Input)
- **Fleisch**: Slider/Eingabe 0,1 - 5,0 kg (Schritte: 0,1 kg)
- **Gemüse**: Eingabe 50g - 2000g (Schritte: 50g)
- **Eis/Flüssigkeiten**: Slider/Eingabe 0,1 - 5,0 L (Schritte: 0,1 L)
- **Packungen/Stück**: Eingabe 1-99 (nur ganze Zahlen)

#### Verfallsdatum-Eingabe
- **Datumspicker**: Kalender-Widget für genaue Datumsauswahl
- **Mindesthaltbarkeitsdatum (MHD)**: Separate Eingabe in ganzen oder halben Monaten
  - Eingabe-Optionen: 1, 1.5, 2, 2.5, 3, 4, 5, 6, 9, 12, 18, 24 Monate
  - Automatische Berechnung des MHD basierend auf Eingabedatum
- **Verfallsdatum-Typ**: Unterscheidung zwischen "Verbrauchen bis" und "Mindestens haltbar bis"

#### Foto-Funktionalität
- **Ein Foto pro Artikel**: Kein Multi-Upload erforderlich
- **Keine Nachbearbeitung**: Direkter Upload nach Komprimierung
- **Kamera-Integration**: Direktaufnahme oder Galerie-Upload
- **Automatische Komprimierung**: Auf Icon-Größe (z.B. 200x200px, max. 50KB)

#### Push-Benachrichtigungen
- **Tägliche Warnung**: 9:00 Uhr morgens
- **7-Tage-Vorwarnung**: Items mit Verfallsdatum in 7 Tagen
- **Kritische Warnungen**: Items die heute oder morgen ablaufen
- **Benutzer-Einstellungen**: Benachrichtigungen können pro User aktiviert/deaktiviert werden

#### Backup-System
- **Manueller Trigger**: Backup wird nur von Admins manuell ausgelöst
- **Aufbewahrung**: Letzte 10 Backups werden gespeichert
- **Vorbereitung für Automatisierung**: System-Architektur unterstützt später automatische Backups
- **Backup-Inhalt**: Komplette Datenbank inkl. Fotos
- **Restore-Funktion**: Auswahl aus verfügbaren Backup-Dateien

### 2.6 Zusätzliche Benutzerflows
4. **Verbrauch-Flow**: Artikel öffnen → Menge anpassen → Speichern (oder "Aufgebraucht" → Löschen)
5. **Warnung-Flow**: Push-Benachrichtigung → App öffnen → Verfallsdaten-Übersicht → Artikel verwalten

#### CSV/Excel-Import Format
```
Artikel | Kategorie | Menge | Verfallsdatum | Notizen
Hackfleisch | Fleisch | 500g | 2024-12-15 | Rind
Brokkoli | Gemüse | 250g | 2024-12-10 | TK
Vanilleeis | Eis | 1.5L | 2025-06-01 | Familienpackung
```

#### Mengeneinheiten-Mapping nach Kategorien
- **Fleisch**: Kilogramm (kg) - Eingabe in 0,1 kg Schritten (100g)
- **Gemüse**: Gramm (g) - Eingabe in 50g Schritten  
- **Eis**: Liter (L) - Eingabe in 0,1 L Schritten (100ml)
- **Fertiggerichte**: Gramm (g) - Eingabe in 50g Schritten
- **Flüssigkeiten**: Liter (L) - Eingabe in 0,1 L Schritten
- **Packungen**: Stück (St.) - Nur ganze Zahlen
- **Allgemein**: Stück (St.) - Nur ganze Zahlen

#### Benutzer-Management
- **Registrierung**: Nur durch Administratoren
- **Rollen**: Admin (alle Rechte), User (eigene Daten)
- **E-Mail-Bestätigung**: Nicht erforderlich
- **Erstanmeldung**: Temporäres Passwort vom Admin, muss geändert werden

## 3. Nicht-funktionale Anforderungen

### 3.1 Performance
- Ladezeit Homepage: < 3 Sekunden
- Core Web Vitals: Grüne Bewertung
- Uptime: > 99,5%

### 3.2 Sicherheit
- SSL-Verschlüsselung
- DSGVO-Konformität
- Regelmäßige Updates

### 3.3 Kompatibilität
- **Browser**: Chrome, Firefox, Safari, Edge (jeweils aktuelle 2 Versionen)
- **Geräte**: Desktop, Tablet, Mobile
- **Betriebssysteme**: Windows, macOS, iOS, Android

### 3.4 SEO-Anforderungen
- Meta-Tags auf allen Seiten
- Strukturierte Daten
- XML-Sitemap
- Robots.txt

## 4. Design-Anforderungen

### 4.1 Visueller Stil
- **Markenidentität**: [Beschreibung der Markenwahrnehmung]
- **Farbpalette**: [Primär- und Sekundärfarben]
- **Typografie**: [Schriftarten und Hierarchie]
- **Bildsprache**: [Stil der Bilder und Grafiken]

### 4.2 User Experience (UX)
- Intuitive Navigation
- Barrierefreiheit (WCAG 2.1 Level AA)
- Mobile-First Design
- Schnelle Ladezeiten

### 4.3 Wireframes/Mockups
- [Referenz zu Design-Dateien oder Beschreibung]

## 5. Technische Spezifikationen

### 5.1 Architektur
- **App-Typ**: Progressive Web App (PWA)
- **Deployment**: Desktop & Mobile über Browser, installierbar als App

### 5.2 Frontend-Technologien
- **Framework**: React oder Vue.js mit PWA-Funktionalität
- **PWA-Features**: Service Worker, Web App Manifest, Cache API
- **UI-Framework**: Modern CSS Framework (z.B. Tailwind CSS)
- **Kamera-API**: MediaDevices API für Foto-Aufnahme
- **Offline-Storage**: IndexedDB für lokale Datenspeicherung

### 5.3 Backend-Technologien
- **Server**: Node.js mit Express oder Python mit FastAPI
- **Datenbank**: PostgreSQL oder MySQL
- **Authentifizierung**: JWT-basierte Benutzerauthentifizierung
- **Datei-Upload**: Multer für Foto-Upload und CSV-Import
- **Bildverarbeitung**: Sharp.js für Foto-Komprimierung

### 5.4 Datenbank-Schema (Grundstruktur)
```sql
Users (id, username, email, role, created_at)
Items (id, name, category, quantity, expiry_date, photo_path, user_id, created_at)
Categories (id, name, icon)
Backups (id, filename, created_by, created_at)
```

### 5.5 Sicherheit & Berechtigungen
- **Rollen**: Admin (alle Rechte), User (eigene Daten)
- **Admin-Funktionen**: Backup/Restore, Datenbank-Reset, Benutzerverwaltung
- **Datenschutz**: Fotos werden komprimiert und sicher gespeichert

### 5.6 Performance-Optimierungen
- **Foto-Komprimierung**: Automatische Größenreduzierung auf Icon-Format
- **Lazy Loading**: Bilder werden bei Bedarf geladen
- **Caching**: Offline-Verfügbarkeit wichtiger Daten
- **Synchronisation**: Automatischer Abgleich bei Internetverbindung]

### 5.7 Hosting & Deployment
- **Hosting**: VPS oder Cloud-Server für Backend (z.B. DigitalOcean, AWS)
- **Frontend-Hosting**: CDN für PWA-Assets (z.B. Netlify, Vercel)
- **Datenbank-Hosting**: Separate Datenbank-Instanz oder Cloud-DB
- **SSL**: HTTPS zwingend erforderlich für PWA-Funktionalität

### 5.9 Datenbank-Schema (Erweitert)
```sql
Users (
    id, username, email, password_hash, role, 
    notifications_enabled, created_at, last_login
)

Items (
    id, name, category, quantity, unit, 
    expiry_date, mhd_months, expiry_type,
    photo_path, notes, user_id, 
    created_at, updated_at
)

Categories (
    id, name, icon, default_unit, 
    unit_step, min_value, max_value
)

Backups (
    id, filename, file_size, created_by, 
    created_at, backup_type
)

Notifications (
    id, user_id, item_id, notification_type,
    sent_at, is_read
)
```

### 5.10 Automatische Einheiten-Erkennung
- System erkennt Kategorie und stellt entsprechende Eingabe-Komponente bereit
- Import-Validierung prüft Einheiten-Kompatibilität
- Smart-Defaults basierend auf Kategorie-Mapping

## 6. Design & User Experience

### 6.1 Design-Prinzipien
- **Modern & Clean**: Zeitgemäßes, minimalistisches Design
- **Mobile-First**: Optimiert für Smartphone-Nutzung
- **Intuitive Navigation**: Einfache Bedienung beim Einkaufen/Kochen
- **Theme-System**: Personalisierbare Farb- und Design-Themes

### 6.2 Theme-Optionen
- **Standard**: Neutrales, professionelles Design
- **Dark Mode**: Dunkles Theme für bessere Lesbarkeit
- **Colorful**: Farbenfrohe Variante mit kategoriespezifischen Farben
- **Minimal**: Reduziertes Design für Fokus auf Funktionalität

### 6.3 Responsive Design
- **Mobile** (320px - 768px): Hauptfokus, optimierte Touch-Bedienung
- **Tablet** (768px - 1024px): Erweiterte Listenansicht
- **Desktop** (1024px+): Vollständige Funktionalität mit Seitenleiste

### 6.4 Usability-Prinzipien
- **Schneller Zugriff**: Wichtigste Funktionen in max. 2 Klicks erreichbar
- **Klare Kategorien**: Visuelle Unterscheidung durch Farben/Icons
- **Verfallsdatum-Warnungen**: Deutliche visuelle Hervorhebung
- **Offline-Hinweise**: Klare Anzeige des Verbindungsstatus

## 7. Projektplanung

### 7.1 Meilensteine
| Phase | Beschreibung | Geschätzte Dauer | Deadline |
|-------|-------------|------------------|----------|
| Konzept | Wireframes, Design | 2 Wochen | [Datum] |
| Entwicklung | Frontend/Backend | 4-6 Wochen | [Datum] |
| Testing | QA, Bugfixes | 1 Woche | [Datum] |
| Launch | Deployment, Go-Live | 1 Woche | [Datum] |

### 7.2 Ressourcen
- **Team**: [Entwickler, Designer, Content-Manager]
- **Budget**: [Grober Rahmen]
- **Tools**: [Entwicklungstools, Projektmanagement]

### 7.3 Risiken
- [Risiko 1 + Mitigation-Strategie]
- [Risiko 2 + Mitigation-Strategie]

## 8. Erfolgs-Metriken

### 8.1 KPIs (Key Performance Indicators)
- Unique Visitors pro Monat
- Conversion Rate
- Bounce Rate
- Durchschnittliche Sitzungsdauer

### 8.2 Analyse-Tools
- Google Analytics
- Search Console
- Heatmap-Tools (z.B. Hotjar)

## 9. Wartung & Updates

### 9.1 Regelmäßige Aufgaben
- Content