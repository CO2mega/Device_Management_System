# 智维设备管理系统 (Device Management System)

A full-stack device management system with Vue.js frontend and Spring Boot backend.

## Project Structure

```
Device_Management_System/
├── src/                    # Vue.js frontend source
│   ├── api/               # API service layer (Axios)
│   ├── components/        # Vue components
│   ├── router/            # Vue Router configuration
│   └── main.js            # Application entry
├── backend/               # Spring Boot backend
│   ├── src/main/java/     # Java source code
│   └── src/main/resources/ # Configuration & migrations
└── public/                # Static assets
```

## Features

### Frontend (Vue.js 2 + Element UI)
- 🎨 Modern "tech" light theme with cyan→violet gradient
- 📊 Dashboard with ECharts statistics
- 📋 Device management (CRUD)
- 👥 User management (CRUD)
- 📝 Loan application and approval workflow
- 📅 Return records with timeline view

### Backend (Spring Boot 3.2)
- 🔐 JWT authentication with Spring Security
- 👮 Role-based access control (Admin/User)
- 💾 H2 database (dev) / MySQL (production)
- 🔄 Flyway database migrations
- 🔒 Pessimistic locking for concurrent operations
- 📦 Redis support (optional)

## Quick Start

### Frontend

```bash
# Install dependencies
npm install

# Development server
npm run serve

# Production build
npm run build

# Lint
npm run lint
```

### Backend

```bash
# Navigate to backend
cd backend

# Build
mvn clean package -DskipTests

# Run
mvn spring-boot:run
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/login | User login |
| POST | /api/auth/register | User registration |
| GET | /api/devices | List devices |
| POST | /api/devices | Create device |
| PUT | /api/devices/{id} | Update device |
| DELETE | /api/devices/{id} | Delete device (Admin) |
| GET | /api/users | List users |
| POST | /api/loans | Apply for loan |
| PUT | /api/loans/{id}/approve | Approve loan (Admin) |
| PUT | /api/loans/{id}/reject | Reject loan (Admin) |
| POST | /api/loans/{id}/return | Return device |

## Default Credentials

| Role | Staff ID | Password |
|------|----------|----------|
| Admin | ADMIN001 | admin123 |
| User | E001 | admin123 |

## Configuration

Frontend API URL can be configured via environment variable:
```
VUE_APP_API_URL=http://localhost:8081/api
```

Backend configuration in `backend/src/main/resources/application.properties`

## License

WTFPL
