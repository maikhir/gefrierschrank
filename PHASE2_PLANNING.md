# Phase 2 Planning: Core Functionality

## 🎯 Phase 2 Objectives

**Ziel**: Implementierung der Kernfunktionalitäten für Produktverwaltung (Items & Categories)

**Timeline**: 1-2 Wochen  
**Branch**: `feature/phase-2-core-functionality`

---

## 📋 Requirements from PRD

### Must-Have Features
- ✅ **Benutzer-Management** (Phase 1 completed)
- 🎯 **Einzelartikel-Eingabe**: Einfache Eingabemaske für individuelle Artikel
- 🎯 **Übersichtsliste**: Komplette Darstellung aller Gefrierschrank-Inhalte  
- 🎯 **Haltbarkeitsverfolgung**: Anzeige von Verfallsdaten mit 7-Tage-Warnung
- 🎯 **Kategorie-Filter**: Filterung nach Fleisch, Gemüse, Fertiggerichte, Eis
- 🎯 **Mengen-Filter**: Filterung nach Stückzahlen/Mengen
- 🔄 **CSV/Excel-Import**: (Phase 3)
- 🔄 **Foto-Upload**: (Phase 3)
- 🔄 **PWA-Funktionalität**: (Phase 4)

---

## 🏗️ Technical Implementation Plan

### Backend Tasks

#### 1. Database Schema Extension
```sql
-- Categories Table
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    icon VARCHAR(100),
    default_unit VARCHAR(20) NOT NULL,
    unit_step DECIMAL(5,2) DEFAULT 1,
    min_value DECIMAL(8,2) DEFAULT 0,
    max_value DECIMAL(8,2) DEFAULT 1000,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Items Table
CREATE TABLE items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    category_id BIGINT NOT NULL,
    quantity DECIMAL(8,2) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    expiry_date DATE,
    expiry_type ENUM('USE_BY', 'BEST_BEFORE') DEFAULT 'BEST_BEFORE',
    photo_path VARCHAR(500),
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

#### 2. JPA Entities
- [ ] **Category Entity**: JPA entity with validation
- [ ] **Item Entity**: JPA entity with relationships
- [ ] **ExpiryType Enum**: USE_BY vs BEST_BEFORE

#### 3. Repository Layer
- [ ] **CategoryRepository**: Spring Data JPA repository
- [ ] **ItemRepository**: With custom queries for filtering
- [ ] **Query Methods**: Find by category, expiry date, user

#### 4. Service Layer
- [ ] **CategoryService**: CRUD operations, default categories initialization
- [ ] **ItemService**: CRUD operations, expiry calculation, business logic
- [ ] **ExpiryService**: Warning calculations, expiry tracking

#### 5. REST Controllers
- [ ] **CategoryController**: GET /api/categories
- [ ] **ItemController**: Full CRUD REST API
  - GET /api/items (with query parameters for filtering)
  - POST /api/items
  - PUT /api/items/{id}
  - DELETE /api/items/{id}
  - GET /api/items/expiring (items expiring within 7 days)

#### 6. DTOs & Validation
- [ ] **CategoryDto**: Category data transfer objects
- [ ] **ItemDto/CreateItemRequest/UpdateItemRequest**: Item DTOs with validation
- [ ] **Validation Annotations**: @Valid, @NotNull, @NotBlank, custom validators

### Frontend Tasks

#### 1. Dashboard Enhancement
- [ ] **Dashboard Page**: Extend existing dashboard with item overview
- [ ] **Quick Stats**: Item count, expiring items, categories overview
- [ ] **Recent Items**: Recently added items display

#### 2. Item Management UI
- [ ] **Add Item Form**: Comprehensive form with smart inputs
  - Category selection dropdown
  - Smart quantity input based on category
  - Expiry date picker with MHD calculation
  - Form validation and error handling
- [ ] **Item List Component**: Paginated list with basic filtering
- [ ] **Item Card**: Individual item display component
- [ ] **Edit/Delete Actions**: Inline editing and deletion

#### 3. Smart Input Components
- [ ] **Quantity Input**: Category-aware input with unit selection
  - Fleisch: 0.1-5.0 kg (0.1 kg steps)
  - Gemüse: 50g-2000g (50g steps)  
  - Flüssigkeiten: 0.1-5.0 L (0.1 L steps)
  - Stück: 1-99 (whole numbers)
- [ ] **Expiry Date Input**: Date picker with MHD presets
- [ ] **Category Selector**: Dropdown with icons (future)

#### 4. Basic Filtering
- [ ] **Category Filter**: Dropdown to filter by category
- [ ] **Expiry Filter**: Show expiring items (7 days)
- [ ] **Search Input**: Basic text search by item name
- [ ] **Sort Options**: By expiry date, name, category, quantity

#### 5. State Management
- [ ] **Items Context**: React context for items state
- [ ] **API Integration**: Items CRUD operations
- [ ] **Error Handling**: User-friendly error messages
- [ ] **Loading States**: Loading indicators

---

## 📊 Success Criteria

### Functional Requirements
- [ ] Users can add items with name, category, quantity, expiry date
- [ ] Items are displayed in a clear, organized list
- [ ] Users can edit and delete their items
- [ ] Basic filtering by category works
- [ ] Expiry date warnings are visible (7-day threshold)
- [ ] Smart quantity inputs work correctly per category
- [ ] All CRUD operations are secure and validated

### Technical Requirements
- [ ] RESTful API follows OpenAPI standards
- [ ] Database schema supports all required fields
- [ ] Frontend-backend integration works seamlessly
- [ ] Input validation on both frontend and backend
- [ ] Error handling provides clear feedback
- [ ] Code follows clean architecture principles

### Performance Requirements
- [ ] Item list loads quickly (< 2 seconds for 100+ items)
- [ ] Adding/editing items feels responsive
- [ ] Database queries are optimized
- [ ] Frontend state management is efficient

---

## 🚀 Development Phases

### Phase 2.1: Backend Foundation (2-3 days)
1. Database schema design and migration
2. JPA entities with relationships
3. Repository layer with basic queries
4. Service layer with business logic
5. Basic REST controllers

### Phase 2.2: Frontend Core (2-3 days)
1. Dashboard extension
2. Add item form with smart inputs
3. Item list display
4. Basic CRUD operations
5. Error handling and validation

### Phase 2.3: Filtering & Polish (1-2 days)
1. Category and expiry filtering
2. Basic search functionality
3. Sort options
4. UI polish and responsive design
5. Testing and bug fixes

---

## 🧪 Testing Strategy

### Backend Testing
- [ ] Unit tests for services
- [ ] Integration tests for controllers
- [ ] Repository tests with @DataJpaTest
- [ ] Validation tests for DTOs

### Frontend Testing
- [ ] Component unit tests
- [ ] Integration tests for CRUD flows
- [ ] Form validation testing
- [ ] API integration testing

### Manual Testing
- [ ] Complete user workflows
- [ ] Cross-browser compatibility
- [ ] Mobile responsiveness
- [ ] Error scenarios

---

## 📝 Technical Notes

### Category Defaults
```java
// Default categories to be initialized
MEAT("Fleisch", "kg", 0.1, 0.1, 5.0)
VEGETABLES("Gemüse", "g", 50, 50, 2000) 
READY_MEALS("Fertiggerichte", "Stück", 1, 1, 50)
ICE_CREAM("Eis", "L", 0.1, 0.1, 5.0)
```

### API Endpoints Design
```
GET    /api/categories
GET    /api/items?category=1&expiring=true&search=chicken
POST   /api/items
GET    /api/items/{id}
PUT    /api/items/{id}  
DELETE /api/items/{id}
GET    /api/items/expiring?days=7
```

### Frontend Routes
```
/dashboard          - Main overview
/items             - All items list
/items/add         - Add new item
/items/{id}        - Item detail/edit
/items/expiring    - Expiring items
```

---

**Ready to start implementation!** 🎯