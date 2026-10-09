# ✈️ Flight Booking Microservices System

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot 3](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen.svg?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg?style=for-the-badge&logo=postgresql)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-Cache-red.svg?style=for-the-badge&logo=redis)](https://redis.io/)
[![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-4.2-231F20.svg?style=for-the-badge&logo=apachekafka)](https://kafka.apache.org/)
[![Nginx](https://img.shields.io/badge/Nginx-Reverse_Proxy-009639.svg?style=for-the-badge&logo=nginx)](https://nginx.org/)
[![Docker Compose](https://img.shields.io/badge/Docker_Compose-Multi--Container-2496ED.svg?style=for-the-badge&logo=docker)](https://www.docker.com/)
[![CI/CD](https://img.shields.io/badge/GitHub_Actions-CI-blue?style=for-the-badge&logo=githubactions)](https://github.com/Nikhilreddy810/Flight_Booking/actions)
[![License](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)](LICENSE)

A high-concurrency, distributed flight booking platform engineered with **Java 21**, **Spring Boot 3**, **PostgreSQL**, **Redis**, **Apache Kafka**, and **Nginx Reverse Proxy**. 

Originally engineered as a monolithic backend, the system has undergone architectural refactoring into **independently deployable microservices** backed by the **Database-per-Service** pattern, asynchronous event-driven messaging, row-level pessimistic write locking (`PESSIMISTIC_WRITE`) for zero overbooking, multi-stage Docker containerization, Docker Compose orchestration, and path-based ingress routing via an Nginx reverse proxy.

---

## 🏛️ System Architecture Topology

Incoming client traffic arrives at a unified **Nginx Reverse Proxy** (port `80`), which performs path-based routing and forwards requests with standard proxy headers to individual microservices. Internal services communicate synchronously via REST (`RestClient` forwarding JWTs) and asynchronously via **Apache Kafka** event streaming.

```text
                                  CLIENT / BROWSER
                                         │
                                   HTTP :80 (Host)
                                         ▼
                      ┌──────────────────────────────────────┐
                      │         NGINX REVERSE PROXY          │
                      │  • Path Routing (/api/auth, flights, │
                      │    bookings, passengers, notify)     │
                      │  • Host / Real-IP / Forwarded-Proto  │
                      └──────────────────┬───────────────────┘
                                         │
     ┌───────────────────┬───────────────┴───────────────┬───────────────────┐
     │ /api/auth/*       │ /api/flights/*                │ /api/bookings/*   │ /api/notifications/*
     ▼                   ▼                               ▼                   ▼
┌──────────────┐   ┌──────────────┐             ┌──────────────┐   ┌──────────────────────┐
│ Auth Service │   │Flight Service│◄───REST─────│Booking Service│  │ Notification Service │
│    :8081     │   │    :8082     │(Reserve/Rel)│    :8083     │  │        :8084         │
└──────┬───────┘   └──────┬───────┘             └──────┬───────┘   └──────────▲───────────┘
       │                  │                            │                      │
       │                  ├───────────┐                │ (Publish Event)      │ (Consume Event)
       ▼                  ▼           ▼                ▼                      │
┌──────────────┐   ┌──────────────┐ ┌─────┐    ┌──────────────┐   ┌───────────┴──────────┐
│PostgreSQL DB │   │PostgreSQL DB │ │Redis│    │PostgreSQL DB │   │  APACHE KAFKA BROKER │
│ flight_auth  │   │ flight_flight│ │ :6379    │flight_booking│   │ topic:booking-created│
│  (Flyway)    │   │  (Flyway)    │ └─────┘    │  (Flyway)    │   └──────────────────────┘
└──────────────┘   └──────────────┘            └──────────────┘
```

---

## 🧩 Microservices Matrix

| Service | Container Port | Database | Primary Responsibilities | Tech Stack & Patterns |
| :--- | :---: | :---: | :--- | :--- |
| **`flight-auth-service`** | `8081` | `Flight_Auth_DB` (PostgreSQL) | User registration, login, BCrypt password hashing, stateless JWT issuance, RBAC (`ROLE_USER`, `ROLE_ADMIN`), seeded admin on boot. | Java 21, Spring Boot 3, Spring Security, JWT, PostgreSQL, Flyway |
| **`flight-flight-service`** | `8082` | `Flight_Flight_DB` (PostgreSQL) | Flight catalog & scheduling, real-time seat queries, row-level pessimistic write locking (`PESSIMISTIC_WRITE`), atomic reserve/release endpoints, Redis query caching (`@Cacheable` / `@CacheEvict`). | Java 21, Spring Boot 3, Spring Data JPA, PostgreSQL, Redis, Flyway |
| **`flight-booking-service`** | `8083` | `Flight_Booking_DB` (PostgreSQL) | Passenger profiles, booking lifecycle, ownership validation, synchronous REST calls via `RestClient` to Flight Service, transactional compensation, Kafka event publishing (`KafkaTemplate`). | Java 21, Spring Boot 3, Spring Data JPA, `RestClient`, Kafka Producer, PostgreSQL, Flyway |
| **`flight-notification-service`** | `8084` | *Stateless Consumer* | Listens to `booking-created` Kafka topic via `@KafkaListener`, consumes serialized booking payloads, and processes asynchronous confirmation notifications. | Java 21, Spring Boot 3, Spring Kafka Consumer, JSON Deserializer |
| **`flight-nginx`** | `80` | *None (Gateway)* | Single entry point reverse proxy; terminates host traffic on port 80 and routes to upstream services based on URL prefix. | Nginx 1.31 Alpine, Custom `default.conf` |

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

4. **Asynchronous Decoupling via Apache Kafka**:
   - Email/SMS confirmation workloads are removed from the synchronous booking request-response lifecycle. `flight-booking-service` publishes a `BookingCreatedEvent` to Kafka; `flight-notification-service` consumes it asynchronously. A slow or degraded notification process never blocks booking completion.

---

## 🌐 Nginx Reverse Proxy Configuration & Ingress Architecture

The project includes an Nginx reverse proxy service (`flight-nginx`) orchestrated inside Docker Compose on port `80`.

### Proxy Architecture & Routing Rules

Nginx routes requests to backend services based on URI prefix while preserving client request headers:

```nginx
# Sample from nginx/default.conf
server {
    listen 80;
    server_name localhost;

    # Auth Service Routing
    location = /api/auth {
        proxy_pass http://auth-service:8081/api/auth;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /api/auth/ {
        proxy_pass http://auth-service:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Flight Service Routing
    location /api/flights {
        proxy_pass http://flight-service:8082;
        ...
    }

    # Booking & Passenger Routing (handled by Booking Service)
    location /api/bookings {
        proxy_pass http://booking-service:8083;
        ...
    }

    location /api/passengers {
        proxy_pass http://booking-service:8083;
        ...
    }

    # Notification Service Routing
    location /api/notifications {
        proxy_pass http://notification-service:8084;
        ...
    }
}
```

### Observed Testing Behavior & Status

During integration testing of the Nginx layer:
* ✅ **Flight Service via Nginx (`GET /api/flights` & `GET /api/flights/`):** Returns `HTTP 200 OK` with JSON flight catalog, matching direct backend response on `:8082`.
* ⚠️ **Booking & Passenger Endpoints (`GET /api/bookings`, `GET /api/passengers`):** Returns `HTTP 403 Forbidden` through Nginx when invoked without required JWT authentication headers (`X-Username` / Bearer token), confirming Spring Security filters are actively intercepting requests.
* ⚠️ **Auth Service Endpoints (`GET /api/auth/`, `GET /api/auth/login`):** Observed `HTTP 500` / `HTTP 404` depending on path trailing slash formatting and missing POST payload validation; under investigation to refine URL rewriting and CORS.
* ⚠️ **Path Redirects (HTTP 301):** Requests without trailing slashes on certain nested paths trigger internal redirects (`HTTP 301 Moved Permanently`) if exact and prefix location blocks mismatch between Nginx and Spring Boot context paths.

---

## 🛠️ API Reference Summary

### 1. Authentication Service (`:8081` / `/api/auth`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/auth/register` | Public | Register new user credentials |
| `POST` | `/api/auth/login` | Public | Authenticate user and receive JWT bearer token |

### 2. Flight Service (`:8082` / `/api/flights`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :---: | :--- |
| `GET` | `/api/flights` | Public | Get all flights (Redis cached) |
| `POST` | `/api/flights` | Admin | Create a new flight listing |
| `GET` | `/api/flights/{id}` | Public | Fetch single flight details |
| `PUT` | `/api/flights/{id}` | Admin | Update flight schedule or total capacity |
| `DELETE`| `/api/flights/{id}` | Admin | Remove flight from catalog |
| `POST` | `/api/flights/{id}/reserve-seat` | Internal/Service | Reserve 1 seat (Pessimistic write lock) |
| `POST` | `/api/flights/{id}/release-seat` | Internal/Service | Release 1 seat (Pessimistic write lock) |

### 3. Booking Service (`:8083` / `/api/bookings` & `/api/passengers`)

| Method | Endpoint | Headers Required | Description |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/passengers` | `X-Username` | Create passenger profile |
| `GET` | `/api/passengers` | `X-Username`, `X-Role` | List passenger profiles (User filtered / Admin all) |
| `POST` | `/api/bookings` | `X-Username` | Create flight booking & trigger seat allocation + Kafka event |
| `GET` | `/api/bookings` | `X-Username`, `X-Role` | Get active bookings |
| `DELETE`| `/api/bookings/{id}` | `X-Username`, `X-Role` | Cancel booking & trigger seat release |

### 4. Notification Service (`:8084` / Internal Kafka Consumer)

| Channel | Topic | Payload | Action |
| :--- | :--- | :--- | :--- |
| **Kafka Topic** | `booking-created` | JSON (`BookingCreatedEvent`) | Consumes booking details asynchronously and triggers confirmation |

---

## 🐳 Docker & Multi-Container Orchestration

### Multi-Stage Docker Builds
Each microservice includes an optimized multi-stage `Dockerfile`:
* **Stage 1 (Build):** Compiles code with Gradle on `eclipse-temurin:21-jdk`.
* **Stage 2 (Runtime):** Copies only `app.jar` into a slim `eclipse-temurin:21-jre` runtime container to minimize image size and attack surface.

### Docker Compose Architecture (`docker-compose.yml`)

The complete platform orchestrates **10 containers** in a unified bridge network (`flight_booking_default`):
1. `flight-nginx`: Reverse proxy on port `80`.
2. `flight-auth-service`: Port `8081`.
3. `flight-auth-db`: PostgreSQL 16 on host port `5433` -> internal `5432`.
4. `flight-flight-service`: Port `8082`.
5. `flight-flight-db`: PostgreSQL 16 on host port `5434` -> internal `5432`.
6. `flight-redis`: Redis 7 on port `6379`.
7. `flight-booking-service`: Port `8083`.
8. `flight-booking-db`: PostgreSQL 16 on host port `5435` -> internal `5432`.
9. `flight-notification-service`: Port `8084`.
10. `flight-kafka`: Apache Kafka 4.2 in KRaft mode (broker/controller) on port `9092` (internal `9094`).

---

## 🚀 Getting Started

### Prerequisites

* **Docker & Docker Compose** installed and running
* **Java 21 JDK** (optional, for running locally outside containers)
* **Git**

---

### Running via Docker Compose (Recommended)

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Nikhilreddy810/Flight_Booking.git
   cd Flight_Booking
   ```

2. **Configure environment variables:**
   Create a `.env` file in the root directory:
   ```env
   DB_USERNAME=postgres
   DB_PASSWORD=your_secure_password
   JWT_SECRET=your_base64_jwt_secret_key_minimum_256_bits
   ADMIN_USERNAME=admin
   ADMIN_PASSWORD=admin_secure_password
   ```

3. **Start all services:**
   ```bash
   docker compose up --build -d
   ```

4. **Verify container health:**
   ```bash
   docker ps
   ```

5. **Test ingress routing through Nginx:**
   ```bash
   curl -i http://localhost/api/flights
   ```

6. **View logs:**
   ```bash
   # All services
   docker compose logs -f

   # Specific service
   docker logs -f flight-nginx
   docker logs -f flight-booking-service
   ```

7. **Stop containers:**
   ```bash
   docker compose down
   ```

---

## 🔄 CI/CD Pipeline

Multi-service build verification executes on every push to the `main` branch via **GitHub Actions** (`.github/workflows/ci.yml`):

```yaml
Git Push ──► GitHub Actions Runner ──► Set up Java 21 ──► Build Auth ──► Build Flight ──► Build Booking
```

---

## 🗺️ Engineering Roadmap & Current Status

- [x] **Monolith to Microservices Refactoring:** 4 independent domain services (Auth, Flight, Booking, Notification).
- [x] **Database Isolation:** Database-per-service pattern with individual PostgreSQL instances and Flyway migrations.
- [x] **Concurrency Controls:** Row-level `PESSIMISTIC_WRITE` locks preventing double-booking during flash sales.
- [x] **Distributed Caching:** Redis integration for flight availability search with `@CacheEvict`.
- [x] **Event-Driven Messaging:** Apache Kafka 4.2 KRaft containerized broker and `booking-created` consumer pipeline.
- [x] **Multi-Stage Docker Builds:** Production-grade `Dockerfile` for each microservice.
- [x] **Full-Stack Orchestration:** 10-container `docker-compose.yml` defining services, databases, Redis, Kafka, and Nginx.
- [x] **CI/CD Automation:** GitHub Actions CI workflow building all services on push.
- [ ] **Nginx Reverse Proxy Route Hardening:** Resolve trailing slash redirect anomalies (HTTP 301) and standardize upstream proxy pass paths for Auth Service.
- [ ] **End-to-End API Gateway Integration:** Transition Nginx routing or implement Spring Cloud Gateway with centralized rate limiting and unified JWT parsing filter.
- [ ] **Distributed Tracing & Metrics:** Integrate OpenTelemetry, Prometheus, and Grafana for distributed transaction visibility across HTTP and Kafka hops.
- [ ] **AWS Cloud Deployment:** Terraform infrastructure-as-code for ECS / EC2 deployment.

---

## 👨‍💻 Author

**Nikhil Reddy Levaku**
* 🌐 **Portfolio:** [nikhilreddy810.github.io/portfolio](https://nikhilreddy810.github.io/portfolio/)
* 💼 **LinkedIn:** [Nikhil Reddy Levaku](https://www.linkedin.com/in/nikhilreddylevaku/)
* 💻 **GitHub:** [@Nikhilreddy810](https://github.com/Nikhilreddy810)
* 📧 **Email:** [levakunikhilreddy8@gmail.com](mailto:levakunikhilreddy8@gmail.com)

---

<p align="center">
  <i>Engineered with Java 21, Spring Boot 3, Apache Kafka, Docker, and Nginx</i>
</p>