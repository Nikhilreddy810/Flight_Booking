# ✈️ Flight Booking System

A secure, production-ready REST API built with **Spring Boot** for managing flight bookings with role-based access control, JWT authentication, Redis caching, and Docker support.

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.5.4 |
| Security | Spring Security, JWT Authentication, RBAC |
| Database | MySQL 8.0, Hibernate JPA |
| Migrations | Flyway |
| Caching | Redis |
| Containerization | Docker, Docker Compose |
| Testing | JUnit 5, Mockito, H2 (in-memory) |
| API Docs | Swagger UI (SpringDoc OpenAPI) |
| Build Tool | Maven |

---

## ✅ Features

- JWT-based stateless authentication
- Role-Based Access Control (ROLE_ADMIN, ROLE_USER)
- Self-registration always creates a plain `ROLE_USER`; the first admin is seeded from configuration
- Flight management (CRUD) — Admin only
- Booking and passenger management, with per-record ownership enforced on read, update and delete
- Separate `totalSeats` (capacity) and `availableSeats` (still bookable), with seats restored on cancellation
- Row-level locking on the seat count so concurrent bookings cannot overbook a flight
- Global exception handling with custom exceptions and correct HTTP status codes
- Input validation with field-level error responses
- Redis caching for flight data, evicted whenever seat counts change
- Versioned schema migrations with Flyway; Hibernate runs in `validate` mode and never writes DDL
- Dockerized with Docker Compose, with a named volume so the database survives `docker compose down`
- API documentation via Swagger UI

---

## 📁 Project Structure

```
src/main/java/com/example/flight/
├── config/
│   ├── AdminSeeder.java
│   ├── RedisConfig.java
│   ├── SecurityConfig.java
│   └── SwaggerConfig.java
├── controller/
│   ├── AuthController.java
│   ├── FlightController.java
│   ├── BookingController.java
│   └── PassengerController.java
├── dto/
│   ├── AuthResponse.java
│   ├── BookingRequest.java
│   ├── LoginRequest.java
│   ├── MessageResponse.java
│   └── RegisterRequest.java
├── service/
│   ├── AuthService.java
│   ├── FlightService.java
│   ├── BookingService.java
│   └── PassengerService.java
├── repository/
│   ├── UserRepository.java
│   ├── FlightRepository.java
│   ├── BookingRepository.java
│   └── PassengerRepository.java
├── entity/
│   ├── User.java
│   ├── Flight.java
│   ├── Booking.java
│   └── Passenger.java
├── security/
│   ├── JwtUtil.java
│   ├── JwtFilter.java
│   ├── Roles.java
│   ├── RestAccessDeniedHandler.java
│   └── RestAuthenticationEntryPoint.java
└── exception/
    ├── GlobalExceptionHandler.java
    ├── InvalidCredentialsException.java
    ├── NoSeatsAvailableException.java
    ├── ResourceNotFoundException.java
    └── UserAlreadyExistsException.java

src/main/resources/
├── application.properties
└── db/migration/
    ├── V1__baseline_schema.sql
    └── V2__add_created_by_indexes.sql
```

---

## 🔐 Configuration

No secrets live in the repository. `application.properties` reads them from the environment and the
application refuses to start if they are missing.

| Variable | Required | Purpose |
|---|---|---|
| `DB_PASSWORD` | yes | MySQL password |
| `JWT_SECRET` | yes | HS256 signing key — at least 32 characters |
| `DB_USERNAME` | no (default `root`) | MySQL user |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | no | Seeds a `ROLE_ADMIN` account on first startup |

Copy the template and fill it in:

```bash
cp .env.example .env
```

---

## ⚙️ Setup — Local

### Prerequisites
- Java 21
- Maven
- MySQL 8.0

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/Nikhilreddy810/Flight_Booking.git
cd Flight_Booking
```

**2. Create MySQL database**
```sql
CREATE DATABASE flight_booking;
```

**3. Run Redis**
```bash
docker run -d -p 6379:6379 --name redis redis
```

**4. Export the configuration**
```bash
export DB_PASSWORD='your_password'
export JWT_SECRET='a-long-random-string-of-at-least-32-characters'
export ADMIN_USERNAME='admin'
export ADMIN_PASSWORD='your_admin_password'
```

**5. Run the application**
```bash
mvn spring-boot:run
```

---

## 🐳 Setup — Docker Compose

### Prerequisites
- Docker Desktop

### Steps

**1. Create your .env**
```bash
cp .env.example .env
```

**2. Build the JAR**
```bash
mvn clean package -DskipTests
```

**3. Start all services**
```bash
docker-compose up --build
```

This starts:
- MySQL on port 3307, backed by the `mysql_data` named volume
- Redis on port 6379
- Spring Boot app on port 8080

Flyway builds the schema on first startup. The volume means `docker compose down` keeps your data;
use `docker compose down -v` when you deliberately want to wipe it and start clean.

---

## 👑 Getting an admin account

`POST /auth/register` is public, so it can only ever create `ROLE_USER` accounts — a request cannot
choose its own role. Set `ADMIN_USERNAME` and `ADMIN_PASSWORD` and the application creates that
admin on startup if it does not already exist.

---

## 📌 API Endpoints

### Auth
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | /auth/register | Public | Register a ROLE_USER account |
| POST | /auth/login | Public | Login and get JWT token |

### Flights
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | /api/flights | Public | Get all flights |
| GET | /api/flights/{id} | Public | Get flight by ID |
| POST | /api/flights | ADMIN | Add flight |
| PUT | /api/flights/{id} | ADMIN | Update flight |
| DELETE | /api/flights/{id} | ADMIN | Delete flight |

### Bookings
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | /api/bookings | ADMIN/USER | Admins see all bookings, users see their own |
| POST | /api/bookings | ADMIN/USER | Create booking |
| DELETE | /api/bookings/{id} | Owner or ADMIN | Cancel booking and release the seat |

### Passengers
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | /api/passengers | ADMIN/USER | Admins see all passengers, users see their own |
| POST | /api/passengers | ADMIN/USER | Add passenger |
| PUT | /api/passengers/{id} | Owner or ADMIN | Update passenger |
| DELETE | /api/passengers/{id} | Owner or ADMIN | Delete passenger |

---

## 📬 Sample Requests

### Register
```json
POST /auth/register
{
  "username": "nikhil",
  "password": "pass123"
}
```
Response:
```json
{
  "message": "User registered successfully"
}
```

### Login
```json
POST /auth/login
{
  "username": "nikhil",
  "password": "pass123"
}
```
Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Add Flight (Admin)
```json
POST /api/flights
Authorization: Bearer <token>
{
  "flightNumber": "AI101",
  "airline": "Air India",
  "source": "Hyderabad",
  "destination": "Delhi",
  "totalSeats": 100,
  "price": 4500.0
}
```
`availableSeats` is derived from `totalSeats` and is not accepted from the client.

### Error responses

Every failure returns the same JSON shape:
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "flightId": "Flight ID is required"
  }
}
```

| Status | When |
|---|---|
| 400 | Validation failure, malformed body, or no seats available |
| 401 | Missing, invalid, or expired token; bad login credentials |
| 403 | Authenticated but not allowed (wrong role, or another user's record) |
| 404 | Flight, passenger, or booking not found |
| 409 | Username already taken, or record still referenced by a booking |

---

## 📖 API Documentation

Swagger UI available at:
```
http://localhost:8080/swagger-ui/index.html
```

---

## 🧪 Running Tests

```bash
mvn test
```

30 tests covering AuthService, FlightService, BookingService and PassengerService with JUnit 5 and
Mockito, plus a Spring context test. The suite runs against in-memory H2, so no MySQL, Redis or
environment configuration is needed to build the project.

---

## 🗄️ Database migrations

The schema is owned by **Flyway**, not by Hibernate. `spring.jpa.hibernate.ddl-auto=validate`
means Hibernate only *checks* that the entities match the schema Flyway produced and fails fast
on drift — it never issues DDL of its own.

Migrations live in `src/main/resources/db/migration` and run automatically at startup:

| Version | Script | Purpose |
|---|---|---|
| V1 | `V1__baseline_schema.sql` | The four tables, keys and foreign keys |
| V2 | `V2__add_created_by_indexes.sql` | Indexes behind `findByCreatedBy` |

Flyway records what it has applied in a `flyway_schema_history` table, so every migration runs
exactly once no matter how often the app restarts.

**To change the schema**, add a new numbered file — never edit one that has already run, since
Flyway checksums applied scripts and will refuse to start if one changed:

```
src/main/resources/db/migration/V3__your_change.sql
```

### Existing databases

`spring.flyway.baseline-on-migrate=true` stamps a database that predates Flyway at V1 rather than
trying to recreate tables it already has, then applies V2 onward. Nothing to do by hand.

One exception: if your database predates the `available_seats` column, run this **once** before
the first Flyway startup. The old code decremented `total_seats` on every booking, so in such a
database `total_seats` holds the *remaining* seats rather than the aircraft capacity:

```sql
ALTER TABLE flight ADD COLUMN available_seats INT NOT NULL DEFAULT 0;

UPDATE flight f
SET f.available_seats = f.total_seats,
    f.total_seats = f.total_seats +
        (SELECT COUNT(*) FROM booking b WHERE b.flight_id = f.id);
```

⚠️ That statement is **not idempotent** — running it twice inflates `total_seats`. It is
deliberately not a migration file for that reason; databases created from V1 onward already have
the column and must not run it.

### Tests

Flyway is disabled for tests (`spring.flyway.enabled=false`) because these scripts are
MySQL-specific. The test suite builds its schema directly from the entities on in-memory H2.

---

## 👤 Author

**Nikhil Reddy Levaku**
- GitHub: [github.com/Nikhilreddy810](https://github.com/Nikhilreddy810)
- LinkedIn: [linkedin.com/in/nikhilreddylevaku](https://linkedin.com/in/nikhilreddylevaku)
- Email: levakunikhilreddy8@gmail.com
