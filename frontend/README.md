# **Frontend – React + Vite + TypeScript**
A fast, modern frontend built with **React**, **Vite**, **TypeScript**, and **Ant Design Pro**.  
This application provides the UI for the Booking System and communicates with the backend API.

---

## **🚀 Features**
- React 18 + TypeScript  
- Vite for ultra‑fast dev & build  
- Ant Design ProTable for rich data tables  
- Axios API client  
- Routing with React Router  
- Environment‑based API configuration  
- Fully typed Room, Booking, and User models  

---

## **📦 Installation**

### **1. Navigate to the frontend folder**
```bash
cd frontend
```

### **2. Install dependencies**
```bash
npm install
```

or

```bash
yarn install
```

---

## **▶️ Running the App**

### **Development mode**
```bash
npm run dev
```

This starts Vite’s dev server at:

```
http://localhost:5173
```

---

## **🏗️ Build for Production**
```bash
npm run build
```

Output goes to:

```
frontend/dist/
```

---

## **🔧 Environment Variables**

Create a `.env` file inside `frontend/`:

```
VITE_API_URL=http://localhost:8080
```

Your Axios client uses this value automatically.

---

## **📁 Project Structure**

```
frontend/
│
├── src/
│   ├── api/
│   │   └── axios.ts        # Axios instance
│   ├── pages/
│   │   ├── Rooms.tsx       # Rooms table
│   │   ├── Bookings.tsx    # Booking page
│   │   └── Login.tsx       # Auth page
│   ├── components/
│   ├── hooks/
│   ├── types/
│   │   └── Room.ts         # Room interface
│   ├── App.tsx
│   └── main.tsx
│
├── public/
├── index.html
├── package.json
└── vite.config.ts
```

---

## **🔌 API Communication**

All API calls use the Axios instance:

```ts
import axios from "../api/axios";

const res = await axios.get("/api/rooms");
```

Backend base URL is automatically injected from:

```
VITE_API_URL
```

---

## **🎨 UI Framework**

The project uses:

- **Ant Design**
- **Ant Design Pro Components**
- **ConfigProvider locale = enUS** (English UI)

---

## **🧪 Testing (optional)**

If you add tests later:

```bash
npm run test
```

---

## **📜 Scripts Overview**

| Script | Description |
|--------|-------------|
| `npm run dev` | Start development server |
| `npm run build` | Build production bundle |
| `npm run preview` | Preview production build |
| `npm run lint` | Run ESLint |

---

## **📄 License**
This project is part of your personal development portfolio.  
Feel free to modify and extend as needed.