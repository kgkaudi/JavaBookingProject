# 📘 **BookingProject – Spring Boot + MongoDB + JWT Authentication**

A production‑ready backend for a hotel booking system built with **Spring Boot**, **Java 21**, **MongoDB**, **Spring Security**, and **JWT authentication**.  
Provides APIs for **rooms**, **bookings**, and **users**, consumed by the React frontend.

---

## 🚀 Features

- 🔐 **JWT Authentication** (signup, login)
- 👤 **User Management** (CRUD, roles)
- 🏨 **Room Management** (room number, type, capacity, price, availability)
- 📅 **Booking System** (create bookings, prevent double‑booking)
- 🗄️ **MongoDB Integration**
- 🛡️ **Spring Security**
- 🧰 **Lombok** for clean models
- 🌱 **Database Seeder** for initial rooms
- 🧪 **Postman Collection** included

---

## 📦 Tech Stack

| Component | Technology |
|----------|------------|
| Backend | Spring Boot |
| Language | Java 21 |
| Database | MongoDB |
| Auth | JWT |
| Build Tool | Maven |
| Models | Lombok |
| Security | Spring Security |

---

## 🛠️ Installation & Setup

### 1️⃣ Clone the repository

```bash
git clone https://github.com/yourusername/BookingProject.git
cd backend/BookingProject
```

---

### 2️⃣ Install Java 21

Verify:

```bash
java -version
```

---

### 3️⃣ Install MongoDB

Ubuntu:

```bash
sudo apt install -y mongodb
sudo systemctl start mongodb
sudo systemctl enable mongodb
```

Verify:

```bash
mongosh --version
```

---

### 4️⃣ Configure environment variables

Secrets are **not** stored in the repo. Set these before starting the app:

| Variable | Required | Description |
|----------|----------|-------------|
| `JWT_SECRET` | **yes** | Random secret, at least 32 characters (app refuses to start without it). Generate one with `openssl rand -base64 48` |
| `JWT_EXPIRATION_MS` | no | Token lifetime in ms (default `86400000` = 24h) |
| `SPRING_DATA_MONGODB_URI` | no | MongoDB connection string (default `mongodb://localhost:27017`) |
| `SEED_ENABLED` | no | `true` to create demo admin/users/rooms on startup (default `false`) |
| `SEED_ADMIN_PASSWORD` | if seeding | Password for `admin@booking.com` |
| `SEED_TEST_USER_PASSWORD` | if seeding | Password for `user1@test.com` / `user2@test.com` |

Local development example:

```bash
export JWT_SECRET="$(openssl rand -base64 48)"
export SEED_ENABLED=true SEED_ADMIN_PASSWORD='choose-a-password' SEED_TEST_USER_PASSWORD='choose-another'
mvn spring-boot:run
```

---

## ▶️ Running the Application

### Development mode

```bash
mvn spring-boot:run
```

### Production build

```bash
mvn clean install
java -jar target/BookingProject-0.0.1-SNAPSHOT.jar
```

---

## 🌱 Database Seeder

When `SEED_ENABLED=true`, the backend seeds demo data on startup (see above). Rooms:

- Room numbers (101, 102, 201…)
- Types (Single, Double, Suite)
- Capacity
- Price
- Availability

If you clear MongoDB:

```bash
mongosh
use bookingdb
db.dropDatabase()
```

Restart backend → rooms are recreated.

---

## 🧪 Postman Collection

Included in:

```
BookingProject.postman_collection.json
```

Import it into Postman to test:

- Signup  
- Login  
- Users  
- Rooms  
- Bookings  

Set the `token` variable after login.

---

## 🔐 Authentication Flow

### 1. Signup

```
POST /api/auth/signup
```

### 2. Login

```
POST /api/auth/login
```

Response:

```json
{
  "token": "your.jwt.token"
}
```

Use token:

```
Authorization: Bearer <token>
```

---

## 📁 Project Structure

```
backend/BookingProject
 ├── src/main/java/com/kostas/bookingproject
 │    ├── auth/              # JWT, login, signup
 │    ├── controllers/       # REST controllers
 │    ├── models/            # Room, Booking, User
 │    ├── repositories/      # MongoDB repositories
 │    ├── services/          # Business logic
 │    ├── security/          # JWT filters, config
 │    └── BookingProjectApplication.java
 ├── src/main/resources/
 │    └── application.properties
 └── pom.xml
```

---

## 🧱 API Endpoints

### 🔐 Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/signup` | Create new user |
| POST | `/api/auth/login` | Login and receive JWT |

---

### 👤 Users
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/users` | Admin | Get all users |
| GET | `/api/users/{id}` | User/Admin | Get user by ID |
| PUT | `/api/users/{id}` | User/Admin | Update user |

---

### 🏨 Rooms
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/rooms` | Admin | Create room |
| GET | `/api/rooms` | Public | List rooms |
| PUT | `/api/rooms/{id}` | Admin | Update room |

---

### 📅 Bookings
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/bookings` | User | Create booking |
| GET | `/api/bookings/user/{id}` | User | Get user bookings |

---

## 🧰 Build Tools

### Maven Commands

| Action | Command |
|--------|---------|
| Clean | `mvn clean` |
| Build | `mvn clean install` |
| Run | `mvn spring-boot:run` |
| Test | `mvn test` |

---

## 📝 License

This project is part of your personal development portfolio.  
Feel free to extend and customize it.