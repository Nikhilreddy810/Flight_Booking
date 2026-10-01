Flight Booking System
A distributed, microservices-based Flight Booking System engineered with Java 21, Spring Boot 3, and PostgreSQL, designed to eliminate race conditions and overbooking under high concurrency.

⚡ Architecture Topology


                       ┌─────────────────────────┐
                       │   Flight Auth Service   │ :8081
                       │ • JWT Generation (RBAC) │
                       └────────────┬────────────┘
                                    │
                               Bearer JWT
                                    │
           ┌────────────────────────┴────────────────────────┐
           ▼                                                 ▼
┌──────────────────────┐   REST (Reserve Seat)    ┌──────────────────────┐
│   Booking Service    │─────────────────────────►│    Flight Service    │
│        :8083         │                          │        :8082         │
│ • Booking Management │                          │ • Search & Catalog   │
│ • Auto-Rollbacks     │                          │ • Pessimistic Locks  │
└──────────┬───────────┘                          │ • Redis Cache Layer  │
           │                                      └──────────┬───────────┘
           ▼                                                 ▼
┌──────────────────────┐                          ┌──────────────────────┐
│   PostgreSQL (DB)    │                          │   PostgreSQL (DB)    │
│   Flyway Migrations  │                          │   Flyway Migrations  │
└──────────────────────┘                          └──────────────────────┘
🧩 Microservices Breakdown
Service	Port	Key Responsibilities	Tech Stack
Auth Service	8081	User registration, login, BCrypt password hashing, stateless JWT issuance, and RBAC (ROLE_USER, ROLE_ADMIN).	Java 21, Spring Boot, Spring Security, PostgreSQL, Flyway
Flight Service	8082	Flight catalog, scheduling, Redis search caching, and row-level pessimistic write locking for seat allocation.	Java 21, Spring Boot, PostgreSQL, Redis, Flyway
Booking Service	8083	Passenger records, booking lifecycle, ownership validation, and atomic inter-service REST communication.	Java 21, Spring Boot, Spring Data JPA, PostgreSQL, Flyway
🔒 Concurrency & State Safety
To prevent double-booking during flash-sale bursts:

Pessimistic Locking (PESSIMISTIC_WRITE): Ensures only one transaction locks a specific seat row during reservation.
Transactional Rollbacks (@Transactional): If booking persistence or payment fails, the state automatically reverts so the seat remains available.
Redis Caching: Hot flight availability queries are served in-memory with automatic TTL expiration to protect database throughput.
🛠️ Technology Stack
Core & Framework: Java 21, Spring Boot 3.x, Spring Security, Spring Data JPA
Databases & Cache: PostgreSQL (Independent Database per service), Redis, Flyway Migrations
Inter-Service Communication: REST APIs with JWT Bearer Authentication
DevOps & Build: Docker, Gradle Wrapper, GitHub Actions CI Pipeline
API Documentation: OpenAPI 3.0 / Swagger UI
📑 Interactive API Docs (Swagger UI)
When running locally, inspect and test endpoints via Swagger:

Auth Service: http://localhost:8081/swagger-ui/index.html
Flight Service: http://localhost:8082/swagger-ui/index.html
Booking Service: http://localhost:8083/swagger-ui/index.html
🚀 Quickstart Guide
1. Clone
bash


git clone https://github.com/Nikhilreddy810/Flight_Booking.git
cd Flight_Booking
2. Run Services (Separate Terminals)
bash


# Terminal 1 - Auth
cd flight-auth-service && ./gradlew bootRun
# Terminal 2 - Flight
cd flight-flight-service && ./gradlew bootRun
# Terminal 3 - Booking
cd flight-booking-service && ./gradlew bootRun
(On Windows, use .\gradlew.bat bootRun)

🔄 CI/CD Pipeline
Automated multi-service verification with GitHub Actions on every push:

Git Push ──► GitHub Actions Runner ──► [Build Auth] ──► [Build Flight] ──► [Build Booking]
🗺️ Roadmap
 Kafka event streaming for asynchronous booking events
 Centralized API Gateway & Eureka Service Discovery
 Single-command docker-compose.yml
 OpenTelemetry & Distributed Tracing
👨‍💻 Author
Nikhil Reddy Levaku
Portfolio
 • LinkedIn
 • GitHub