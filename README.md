# ✈️ Flight Booking System

A RESTful backend API for managing flight bookings built with Java and Spring Boot.
Supports JWT-based authentication, seat management, and passenger booking workflows.

## 🛠️ Tech Stack
- Java 21, Spring Boot 3.5.4
- Spring Security + JWT Authentication
- MySQL 8, Spring Data JPA / Hibernate
- Maven, Swagger UI

## 🚀 Getting Started

1. Clone the repository
2. Create MySQL database: `CREATE DATABASE flight_booking;`
3. Set environment variable: `DB_PASSWORD=your_mysql_password`
4. Run: `mvn spring-boot:run`
5. Swagger UI: `http://localhost:8080/swagger-ui.html`

## 🔐 API Endpoints

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | /auth/register | Public | Register new user |
| POST | /auth/login | Public | Get JWT token |
| GET/POST | /api/flights | Public | View and add flights |
| GET/POST/DELETE | /api/bookings | 🔒 JWT | Manage bookings |
| GET/POST/PUT/DELETE | /api/passengers | 🔒 JWT | Manage passengers |

## 🔑 Authentication
Login at `/auth/login` → copy token → add to protected requests:
`Authorization: Bearer <your_token>`

## ✅ Key Highlights
- JWT token based authentication and authorization
- Automatic seat restoration on booking cancellation
- Global exception handling with meaningful error messages
- Input validation on all request bodies

## API Documentation
Run the application and visit:
http://localhost:8080/swagger-ui.html
  
