# ✈️ Flight Booking Microservices System

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot 3](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen.svg?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg?style=for-the-badge&logo=postgresql)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-Cache-red.svg?style=for-the-badge&logo=redis)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg?style=for-the-badge&logo=docker)](https://www.docker.com/)
[![CI/CD](https://img.shields.io/badge/GitHub_Actions-CI-blue?style=for-the-badge&logo=githubactions)](https://github.com/Nikhilreddy810/Flight_Booking/actions)
[![License](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)](LICENSE)

A high-performance, distributed microservices system for flight reservation management engineered with **Java 21**, **Spring Boot 3**, **PostgreSQL**, **Redis**, and **Flyway**. 

Designed with enterprise-grade patterns, this system features **row-level pessimistic write locking** (`PESSIMISTIC_WRITE`) to eliminate race conditions and double-booking during high-concurrency flash sales, paired with **stateless JWT authentication**, **in-memory Redis caching**, and **automatic transactional rollback compensation**.

---

## 🏛️ System Architecture Topology

Each microservice follows the **Database-per-Service** pattern, maintaining absolute data isolation while communicating via lightweight synchronous REST APIs using Spring's modern `RestClient`.

```text
                        ┌─────────────────────────┐
                        │   Flight Auth Service   │ :8081
                        │ • JWT Generation (RBAC) │
                        │ • BCrypt Password Hash  │
                        └────────────┬────────────┘
                                     │
                                Bearer JWT
                                     │
            ┌────────────────────────┴────────────────────────┐
            ▼                                                 ▼
 ┌──────────────────────┐   REST (Reserve / Release)   ┌──────────────────────┐
 │   Booking Service    │─────────────────────────────►│    Flight Service    │
 │        :8083         │                              │        :8082         │
 │ • Booking Lifecycle  │                              │ • Flight Catalog     │
 │ • Passenger Records  │                              │ • Pessimistic Locks  │
 │ • Auto-Compensations │                              │ • Redis Cache Layer  │
 └──────────┬───────────┘                              └──────────┬───────────┘
            │                                                     │
            ▼                                                     ▼
 ┌──────────────────────┐                              ┌──────────────────────┐
 │ PostgreSQL (BookingDB)│                              │ PostgreSQL (FlightDB)│
 └──────────────────────┘                              └──────────┬───────────┘
                                                                  │
                                                                  ▼
                                                       ┌──────────────────────┐
                                                       │   Redis Cache Engine │
                                                       └──────────────────────┘
```

---

## 🧩 Microservices Matrix

| Microservice | Port | Database | Primary Responsibilities | Core Tech Stack |
| :--- | :---: | :---: | :--- | :--- |
| **`flight-auth-service`** | `8081` | `flight_auth_db` | User registration, login, BCrypt password hashing, stateless JWT issuance, and RBAC (`ROLE_USER`, `ROLE_ADMIN`). Seeded admin startup. | Java 21, Spring Boot 3, Spring Security, JWT, PostgreSQL, Flyway |
| **`flight-flight-service`** | `8082` | `flight_flight_db` | Flight catalog management, real-time availability search, row-level pessimistic write locking, atomic seat reservation/release, and Redis query caching. | Java 21, Spring Boot 3, Spring Data JPA, PostgreSQL, Redis, Flyway |
| **`flight-booking-service`** | `8083` | `flight_booking_db` | Passenger profile management, flight booking creation, ownership validation, inter-service HTTP integration via `RestClient`, and transactional seat rollbacks. | Java 21, Spring Boot 3, Spring Data JPA, `RestClient`, PostgreSQL, Flyway |

---

## 🔒 Concurrency & Resiliency Design

> [!IMPORTANT]
> **Zero Overbooking Guarantee:** In high-concurrency reservation scenarios (e.g., flash sales), standard read-modify-write cycles cause race conditions where multiple users grab the last available seat simultaneously. This system prevents overbooking through strict concurrency controls:

1. **Row-Level Pessimistic Locking (`PESSIMISTIC_WRITE`)**:
   - When a reservation request reaches `flight-flight-service`, the repository executes an explicit SQL row lock (`SELECT ... FOR UPDATE` via `@Lock(LockModeType.PESSIMISTIC_WRITE)`).
   - Concurrent transactions attempting to reserve seats on the same flight are queued at the database level until the active transaction commits or rolls back.

2. **Automated Seat Rollback & Compensation**:
   - If `flight-booking-service` successfully reserves a seat via REST but encounters a database failure while saving the booking entity, a targeted rollback handler automatically invokes `/api/flights/{id}/release-seat` to release the seat.

3. **In-Memory Caching Strategy**:
   - Flight search queries are cached in **Redis** (`@Cacheable("flights")`). Any mutation to flight records or seat availability triggers immediate cache eviction (`@CacheEvict`), ensuring zero dirty reads for search operations without burdening PostgreSQL under heavy read loads.

---

## 🔄 Inter-Service Communication Flow

```text
 Client                 Booking Service                      Flight Service                  Database
   │                          │                                    │                            │
   │─── Create Booking ──────►│                                    │                            │
   │                          │─── POST /api/flights/{id}/reserve─►│                            │
   │                          │                                    │─── SELECT FOR UPDATE ─────►│ (Lock Row)
   │                          │                                    │◄── Lock Acquired ──────────│
   │                          │                                    │─── Decrement Seats ───────►│ (Update & Commit)
   │                          │◄── 200 OK (Seat Reserved) ─────────│                            │
   │                          │                                    │                            │
   │                          │─── Save Booking Entity ───────────►│ (BookingDB)                │
   │◄── 201 Created ──────────│                                    │                            │
```

---

## 📑 Interactive API Documentation (Swagger UI)

Each service includes built-in OpenAPI 3.0 documentation. When running services locally, navigate to the following Swagger endpoints:

* 🔐 **Auth Service:** [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
* ✈️ **Flight Service:** [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)
* 🎟️ **Booking Service:** [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)

---

## 🛠️ API Reference Summary

### 1. Authentication Service (`:8081`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/auth/register` | Public | Register new user credentials |
| `POST` | `/api/auth/login` | Public | Authenticate user and receive JWT bearer token |

### 2. Flight Service (`:8082`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :---: | :--- |
| `GET` | `/api/flights` | Public | Get all flights (Redis cached) |
| `POST` | `/api/flights` | Admin | Create a new flight listing |
| `GET` | `/api/flights/{id}` | Public | Fetch single flight details |
| `PUT` | `/api/flights/{id}` | Admin | Update flight schedule or total capacity |
| `DELETE`| `/api/flights/{id}` | Admin | Remove flight from catalog |
| `POST` | `/api/flights/{id}/reserve-seat` | Internal/Service | Reserve 1 seat (Pessimistic write lock) |
| `POST` | `/api/flights/{id}/release-seat` | Internal/Service | Release 1 seat (Pessimistic write lock) |

### 3. Booking Service (`:8083`)

| Method | Endpoint | Headers Required | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/passengers` | `X-Username` | Create passenger profile |
| `GET` | `/api/passengers` | `X-Username`, `X-Role` | List passenger profiles (User filtered / Admin all) |
| `POST` | `/api/bookings` | `X-Username` | Create flight booking & trigger seat allocation |
| `GET` | `/api/bookings` | `X-Username`, `X-Role` | Get active bookings |
| `DELETE`| `/api/bookings/{id}` | `X-Username`, `X-Role` | Cancel booking & trigger seat release |

---

## 🚀 Getting Started

### Prerequisites

* **Java 21 JDK** or higher
* **PostgreSQL 15+** running on `localhost:5432`
* **Redis Server** running on `localhost:6379`
* **Gradle Wrapper** (included in repository)

---

### Step 1: Clone Repository

```bash
git clone https://github.com/Nikhilreddy810/Flight_Booking.git
cd Flight_Booking
```

---

### Step 2: Database Initialization

Create 3 PostgreSQL databases (or update `application.properties` in each microservice accordingly):

```sql
CREATE DATABASE flight_auth_db;
CREATE DATABASE flight_flight_db;
CREATE DATABASE flight_booking_db;
```

> [!NOTE]
> Database schemas will be created automatically upon service boot via **Flyway Migrations**.

---

### Step 3: Run Microservices

Launch each service in separate terminal windows:

#### **Linux / macOS:**

```bash
# Terminal 1 - Auth Service
cd flight-auth-service && ./gradlew bootRun

# Terminal 2 - Flight Service
cd flight-flight-service && ./gradlew bootRun

# Terminal 3 - Booking Service
cd flight-booking-service && ./gradlew bootRun
```

#### **Windows (PowerShell / CMD):**

```powershell
# Terminal 1 - Auth Service
cd flight-auth-service; .\gradlew.bat bootRun

# Terminal 2 - Flight Service
cd flight-flight-service; .\gradlew.bat bootRun

# Terminal 3 - Booking Service
cd flight-booking-service; .\gradlew.bat bootRun
```

---

## 🐳 Docker Support

Each microservice contains a production-ready containerized setup using standard Dockerfiles:

```bash
# Example: Build and run Flight Auth Service container
cd flight-auth-service
docker build -t flight-auth-service:latest .
docker run -p 8081:8081 flight-auth-service:latest
```

---

## 🔄 CI/CD Pipeline

Automated build verification runs on every push to the `main` branch via **GitHub Actions** (`.github/workflows/ci.yml`):

```yaml
Git Push ──► GitHub Actions ──► Set up Java 21 ──► Build Auth Service ──► Build Flight Service ──► Build Booking Service
```

---

## 🗺️ Engineering Roadmap

- [ ] **Apache Kafka Event Streaming:** Transition inter-service seat allocation to async saga orchestrations.
- [ ] **Spring Cloud API Gateway:** Centralized routing, rate limiting, and unified JWT validation filter.
- [ ] **Distributed Tracing:** Integrate OpenTelemetry & Zipkin/Jaeger for cross-service transaction observability.
- [ ] **Docker Compose & Helm Charts:** One-command orchestration for PostgreSQL, Redis, and all microservices.

---

## 👨‍💻 Author

**Nikhil Reddy Levaku**
* 🌐 **GitHub:** [@Nikhilreddy810](https://github.com/Nikhilreddy810)
* 💼 **LinkedIn:** [Nikhil Reddy Levaku](https://www.linkedin.com/in/nikhilreddylevaku/)
* 📧 **Email:** [nikhilreddy810@gmail.com](mailto:nikhilreddy810@gmail.com)

---

<p center="align">
  <i>Built with ❤️ using Java 21 & Spring Boot 3 Microservices</i>
</p>