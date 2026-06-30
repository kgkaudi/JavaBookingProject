# 📘 **BookingProject – Spring Boot + MongoDB + JWT Authentication**

A production‑ready backend for a hotel booking system built with **Spring Boot 4.1**, **Java 21**, **MongoDB**, **Spring Security**, and **JWT authentication**.  
Includes user management, room management, and booking logic.

---

## 🚀 Features

- 🔐 **JWT Authentication** (login, signup)
- 👤 **User Management** (CRUD, admin endpoints)
- 🏨 **Room Management** (create, update, list)
- 📅 **Booking System** (create bookings, check availability)
- 🗄️ **MongoDB Integration**
- 🛡️ **Spring Security 6**
- 🧰 **Lombok** for clean models
- 🧪 **Postman Collection** included

---

## 📦 Tech Stack

| Component | Technology |
|----------|------------|
| Backend | Spring Boot 4.1 |
| Language | Java 21 |
| Database | MongoDB |
| Auth | JWT (JJWT 0.11.5) |
| Build Tool | Maven |
| Models | Lombok |
| Security | Spring Security |

---

## 🛠️ Installation & Setup

### 1️⃣ Clone the repository

```bash
git clone https://github.com/yourusername/BookingProject.git
cd BookingProject
```

---

### 2️⃣ Install Java 21

Verify:

```bash
java -version
```

Should output:

```
openjdk version "21.x.x"
```

---

### 3️⃣ Install MongoDB

On Ubuntu:

```bash
sudo apt install -y mongodb
sudo systemctl start mongodb
sudo systemctl enable mongodb
```

Verify:

```bash
mongo --version
```

---

### 4️⃣ Configure application properties

Edit:

```
src/main/resources/application.properties
```

Example:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/bookingdb
jwt.secret=your-secret-key
jwt.expiration=86400000
server.port=8080
```

---

## ▶️ Running the Application

### **Development mode**

```bash
mvn spring-boot:run
```

### **Production build**

```bash
mvn clean install
java -jar target/BookingProject-0.0.1-SNAPSHOT.jar
```

---

## 🧪 Postman Collection

A full Postman collection is included in:

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

1. **Signup**

```
POST /api/auth/signup
```

2. **Login**

```
POST /api/auth/login
```

Response:

```json
{
  "token": "your.jwt.token"
}
```

3. Use token in all protected endpoints:

```
Authorization: Bearer <token>
```

---

## 📁 Project Structure

```
backend/
 ├── src/main/java/com/kostas/bookingproject
 │    ├── auth/
 │    ├── controllers/
 │    ├── models/
 │    ├── repositories/
 │    ├── services/
 │    ├── security/
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

## 🧩 Environment Variables

| Variable | Description |
|----------|-------------|
| `jwt.secret` | Secret key for JWT signing |
| `jwt.expiration` | Token expiration in ms |
| `spring.data.mongodb.uri` | MongoDB connection string |

---

## 📝 License

This project is open‑source and free to use.