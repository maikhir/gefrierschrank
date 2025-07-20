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

### Technical Stack (Implemented)
- **Frontend**: React with TypeScript and PWA functionality (Vite build system)
- **Backend**: Spring Boot 3.2.0 with Java 21 and Maven
- **Database**: H2 Database (file-based, with H2 Console enabled)
- **Authentication**: Spring Security with JWT (using JJWT library)
- **Additional Libraries**: OpenCSV for CSV processing, SpringDoc OpenAPI for API documentation
- **Testing**: Spring Boot Test, Spring Security Test, Testcontainers

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

## Architecture Overview

### Backend Structure
- **Package Structure**: `com.gefrierschrank.app` with standard Spring Boot layout
  - `controller/`: REST API endpoints (Auth, Category, Item)
  - `entity/`: JPA entities (User, Item, Category, ExpiryType)
  - `repository/`: Spring Data JPA repositories
  - `service/`: Business logic layer
  - `dto/`: Data Transfer Objects
  - `security/`: JWT authentication and security configuration
  - `config/`: Security and application configuration

### Database Configuration
- **H2 Database**: File-based storage at `./data/gefrierschrank_db`
- **H2 Console**: Accessible at `/h2-console` (development only)
- **JPA Settings**: Auto DDL update enabled, SQL logging available

### Security & Authentication
- **JWT Authentication**: Custom filter chain with JWT token validation
- **CORS Configuration**: Configured for local development (ports 3000, 5173)
- **JWT Secret**: Configured in application.yml (should be externalized for production)

## Development Commands

### Backend (Spring Boot)
```bash
# Build and run backend
cd backend
mvn clean install
mvn spring-boot:run

# Run tests
mvn test

# Build without tests
mvn clean package -DskipTests
```

### Frontend (React/Vite)
```bash
# Development server (assuming standard Vite setup)
cd frontend
npm install
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

### Database Access
- **H2 Console**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:file:./data/gefrierschrank_db`
- **Username**: `sa`
- **Password**: (empty)

### API Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html (when running)
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## Development Notes

### Key Implementation Details
1. **Database**: Currently using H2 for development (file-based persistence)
2. **Authentication**: JWT-based with Spring Security filter chain
3. **API Design**: RESTful endpoints with proper HTTP methods and status codes
4. **Entity Relationships**: JPA entities with proper mappings between User, Item, Category
5. **CORS**: Configured for local frontend development

### Important Files
- **Backend Entry Point**: `backend/src/main/java/com/gefrierschrank/app/GefrierschrankApplication.java`
- **Security Config**: `backend/src/main/java/com/gefrierschrank/app/config/SecurityConfig.java`
- **Database Config**: `backend/src/main/resources/application.yml`
- **Frontend Entry**: `frontend/index.html` (React app structure incomplete)