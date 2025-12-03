# Device Management System - Backend

Spring Boot backend for the Device Management System.

## Tech Stack

- **Framework**: Spring Boot 3.2
- **Security**: Spring Security + JWT
- **Database**: H2 (development) / MySQL (production)
- **ORM**: Spring Data JPA
- **Migration**: Flyway
- **Cache**: Redis (optional)

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+

### Running the Application

```bash
# Navigate to backend directory
cd backend

# Build the project
mvn clean package -DskipTests

# Run the application
mvn spring-boot:run
```

The server will start at `http://localhost:8081`

### API Endpoints

#### Authentication
- `POST /api/auth/login` - Login with staff ID and password
- `POST /api/auth/register` - Register new user

#### Devices
- `GET /api/devices` - List all devices (paginated)
- `GET /api/devices/{id}` - Get device by ID
- `POST /api/devices` - Create new device
- `PUT /api/devices/{id}` - Update device
- `DELETE /api/devices/{id}` - Delete device (Admin only)

#### Users
- `GET /api/users` - List all users (paginated)
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create new user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user (Admin only)

#### Loan Records
- `GET /api/loans` - List all loan records (paginated)
- `GET /api/loans/{id}` - Get loan record by ID
- `POST /api/loans` - Apply for device loan
- `PUT /api/loans/{id}/approve` - Approve loan (Admin only)
- `PUT /api/loans/{id}/reject` - Reject loan (Admin only)
- `POST /api/loans/{id}/return` - Return device

#### Dashboard
- `GET /api/dashboard/statistics` - Get dashboard statistics

### Default Credentials

| Role  | Staff ID   | Password  |
|-------|------------|-----------|
| Admin | ADMIN001   | admin123  |
| User  | E001       | admin123  |

### Database

Development uses H2 in-memory database. Access H2 Console at:
`http://localhost:8081/h2-console`

- JDBC URL: `jdbc:h2:mem:dmsdb`
- Username: `sa`
- Password: (empty)

### Configuration

Edit `src/main/resources/application.properties` for:
- Database connection
- JWT secret and expiration
- Redis configuration
- CORS settings

### Production Configuration

For production, configure MySQL:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/dms
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

## Architecture

```
com.dms
├── config/           # Configuration classes
├── controller/       # REST controllers
├── dto/              # Data Transfer Objects
├── entity/           # JPA entities
├── repository/       # Spring Data repositories
├── security/         # JWT and authentication
└── service/          # Business logic
```

## Security Features

- JWT-based authentication
- Role-based authorization (ADMIN, USER)
- Password encryption with BCrypt
- CORS configuration
- Pessimistic locking for concurrent operations
