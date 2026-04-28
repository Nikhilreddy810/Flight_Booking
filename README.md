# ✈️ Flight Booking System

A secure, production-ready REST API built with **Spring Boot** for managing flight bookings with role-based access control, JWT authentication, Redis caching, and Docker support.

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.5.4 |
| Security | Spring Security, JWT Authentication, RBAC |
| Database | MySQL 8.0, Hibernate JPA |
| Caching | Redis |
| Containerization | Docker, Docker Compose |
| Testing | JUnit 5, Mockito |
| API Docs | Swagger UI (SpringDoc OpenAPI) |
| Build Tool | Maven |

---

## ✅ Features

- JWT-based stateless authentication
- Role-Based Access Control (ROLE_ADMIN, ROLE_USER)
- Flight management (CRUD) — Admin only
- Booking and passenger management
- Seat availability check with auto-restoration on cancellation
- Global exception handling with custom exceptions
- Input validation
- Redis caching for flight data
- Dockerized with Docker Compose
- API documentation via Swagger UI

---

## 📁 Project Structure

```
src/main/java/com/example/flight/
├── config/
│   ├── RedisConfig.java
│   └── SwaggerConfig.java
├── controller/
│   ├── AuthController.java
│   ├── FlightController.java
│   ├── BookingController.java
│   └── PassengerController.java
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
│   └── SecurityConfig.java
└── exception/
    ├── GlobalExceptionHandler.java
    └── ResourceNotFoundException.java
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

**3. Configure application.properties**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/flight_booking
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
jwt.secret=yourSecretKey
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

**4. Run Redis**
```bash
docker run -d -p 6379:6379 --name redis redis
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

**1. Build the JAR**
```bash
mvn clean package -DskipTests
```

**2. Start all services**
```bash
docker-compose up --build
```

This starts:
- MySQL on port 3307
- Redis on port 6379
- Spring Boot app on port 8080

---

## 📌 API Endpoints

### Auth
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | /api/auth/register | Public | Register user |
| POST | /api/auth/login | Public | Login and get JWT token |

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
| GET | /api/bookings | ADMIN/USER | Get bookings |
| POST | /api/bookings | USER | Create booking |
| DELETE | /api/bookings/{id} | ADMIN/USER | Cancel booking |

### Passengers
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | /api/passengers | ADMIN/USER | Get passengers |
| POST | /api/passengers | USER | Add passenger |
| DELETE | /api/passengers/{id} | ADMIN/USER | Delete passenger |

---

## 📬 Sample Requests

### Register
```json
POST /api/auth/register
{
  "username": "nikhil",
  "password": "pass123",
  "role": "ROLE_USER"
}
```

### Login
```json
POST /api/auth/login
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

6 unit tests covering BookingService and PassengerService using JUnit 5 and Mockito.

---

## 👤 Author

**Nikhil Reddy Levaku**
- GitHub: [github.com/Nikhilreddy810](https://github.com/Nikhilreddy810)
- LinkedIn: [linkedin.com/in/nikhilreddylevaku](https://linkedin.com/in/nikhilreddylevaku)
- Email: levakunikhilreddy8@gmail.com
