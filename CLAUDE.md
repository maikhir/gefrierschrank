# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Gefrierschrank-Verwaltungsapp** (Freezer Management App) - a Progressive Web App (PWA) for managing frozen food inventory with focus on expiration tracking and household organization.

## Key Features & Architecture

### Core Functionality
- **User Management**: Admin/User roles with registration by administrators only
- **Item Management**: Individual item entry and CSV/Excel bulk import
- **Expiration Tracking**: 7-day warnings, push notifications at 9:00 AM daily
- **Category System**: Fleisch (Meat), Gemüse (Vegetables), Fertiggerichte (Ready meals), Eis (Ice cream)
- **Photo Integration**: Single photo per item with automatic compression to 200x200px/50KB
- **Offline Capabilities**: PWA with IndexedDB for local storage and sync

### Technical Stack (Planned)
- **Frontend**: React with TypeScript and PWA functionality
- **Backend**: Java Spring Boot 3.x with Maven
- **Database**: PostgreSQL with Spring Data JPA/Hibernate
- **Authentication**: Spring Security with JWT
- **File Handling**: Spring Web Multipart, ImageIO for image compression
- **PWA Features**: Service Worker, Web App Manifest, Cache API
- **Build Tools**: Maven (backend), Vite (frontend)

### Database Schema Structure
```sql
Users (id, username, email, role, notifications_enabled, created_at)
Items (id, name, category, quantity, unit, expiry_date, mhd_months, photo_path, user_id)
Categories (id, name, icon, default_unit, unit_step, min_value, max_value)
Backups (id, filename, created_by, created_at)
Notifications (id, user_id, item_id, notification_type, sent_at, is_read)
```

### Smart Input System
- **Meat**: 0.1-5.0 kg (0.1 kg steps)
- **Vegetables**: 50g-2000g (50g steps)
- **Ice/Liquids**: 0.1-5.0 L (0.1 L steps)
- **Packages**: 1-99 pieces (whole numbers only)

### Admin Features
- Manual backup system (last 10 backups retained)
- Database reset functionality
- User management with temporary passwords
- Backup/Restore operations

### Mobile-First Design
- Primary focus on mobile devices (320px-768px)
- Theme system: Standard, Dark Mode, Colorful, Minimal
- Touch-optimized interface for kitchen/shopping use
- Offline-first approach with sync capabilities

## Development Notes

This repository currently contains only the PRD (Product Requirements Document). The actual application implementation has not yet begun. When developing:

1. Follow the PWA architecture described in the PRD
2. Implement mobile-first responsive design
3. Ensure DSGVO compliance for German users
4. Focus on offline-first functionality
5. Implement the role-based permission system (Admin/User)
6. Use the specified unit systems for different food categories