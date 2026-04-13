# ✈️ Flight Booking System
A RESTful backend API for managing flight bookings built with Java and Spring Boot.
Supports JWT-based authentication, role-based access control, seat management, and passenger booking workflows.

## 🛠️ Tech Stack
- Java 21, Spring Boot 3.5.4
- Spring Security + JWT Authentication
- MySQL 8, Spring Data JPA / Hibernate
- Maven, Swagger UI
- JUnit 5 + Mockito

## 🚀 Getting Started
1. Clone the repository
2. Create MySQL database: `CREATE DATABASE flight_booking;`
3. Set environment variable: `DB_PASSWORD=your_mysql_password`
4. Run: `mvn spring-boot:run`
5. Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## 🔐 API Endpoints
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | /auth/register | Public | Register new user |
| POST | /auth/login | Public | Get JWT token |
| GET | /api/flights | Public | View all flights |
| POST/PUT/DELETE | /api/flights | 🔒 ADMIN | Manage flights |
| GET/POST/DELETE | /api/bookings | 🔒 JWT | Own bookings (USER) / All (ADMIN) |
| GET/POST/PUT/DELETE | /api/passengers | 🔒 JWT | Own passengers (USER) / All (ADMIN) |

## 🛡️ Role-Based Access Control (RBAC)
| Role | Access |
|------|--------|
| ROLE_ADMIN | Full access — all flights, bookings, passengers |
| ROLE_USER | View flights, manage own bookings and passengers only |

**Register with role:**
```json
{ "username": "admin", "password": "1234", "role": "ROLE_ADMIN" }
```

## 🔑 Authentication
Login at `/auth/login` → copy token → add to protected requests:
`Authorization: Bearer <your_token>`

## ✅ Key Highlights
- JWT token based authentication and authorization
- Role-based access control (ADMIN vs USER)
- Users can only access their own bookings and passengers
- Automatic seat restoration on booking cancellation
- Global exception handling with meaningful error messages
- Input validation on all request bodies

## 🧪 Testing
- 6 JUnit tests written for BookingService
- Covers booking creation, cancellation, seat management and exception handling
- Run tests: `mvn test`

## 📊 Project Stats
- 14 REST API endpoints
- 4 database tables
- 6 JUnit tests — all passing
- JWT secured APIs
- RBAC implemented
