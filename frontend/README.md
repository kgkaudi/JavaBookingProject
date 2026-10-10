# **Frontend – React + Vite + TypeScript**
A fast, modern frontend built with **React**, **Vite**, **TypeScript**, and **Ant Design Pro**.  
This application provides the UI for the Booking System and communicates with the backend API.

---

## **🚀 Features**
- React 19 + TypeScript  
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

Copy `.env.example` to `.env` inside `frontend/`:

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
│   │   ├── axios.ts        # Axios instance: token header + expired-session handling
│   │   ├── errors.ts       # getErrorMessage(): turns any error into a safe message
│   │   ├── auth.ts         # login / signup / logout / password reset calls
│   │   ├── rooms.ts        # rooms API
│   │   ├── bookings.ts     # bookings API (incl. cancel + availability)
│   │   └── users.ts        # users API (profile, promote / demote)
│   ├── context/
│   │   └── AuthContext.tsx # session state (login, logout, restore on refresh)
│   ├── components/         # ProtectedRoute, ErrorBoundary, ...
│   ├── layouts/            # ProLayout shell
│   ├── pages/              # one file per screen (lazy loaded)
│   ├── test/               # test setup + helpers
│   ├── types.ts            # User, Room, Booking, ...
│   ├── antd-compat.ts      # React 19 patch for antd v5 (toasts do not render without it)
│   ├── App.tsx             # routes
│   ├── Root.tsx            # providers (theme, locale, router)
│   └── main.tsx
│
├── public/
├── .env.example
├── index.html
├── package.json
└── vite.config.ts
```

---

## **🔌 API Communication**

Use the typed helpers in `src/api/`; they share one Axios instance that adds the JWT automatically:

```ts
import { getRooms } from "../api/rooms";

const rooms = await getRooms();
```

### Errors

The backend answers every error with JSON (`{ status, message, fieldErrors? }`).
**Never put `err.response.data` into a toast or the UI** – it is an object. Use the helper:

```ts
import { getErrorMessage } from "../api/errors";

try { await createBooking(...) } catch (err) { message.error(getErrorMessage(err, "Failed to book")) }
```

### Sessions

- On login the token is stored in `localStorage`; on page load it is verified with `GET /api/users/me`.
- Logout calls `POST /api/auth/logout` so the backend invalidates the token, then clears local state.
- If a request shows the token has expired (the backend answers 401 or 403), the user is logged out
  and sent to the login page with a notice.

### Roles and permissions (enforced by the backend)

- Profile updates (`PUT /api/users/{id}`) only change `name`, `email` and `phone`.
- Role changes use `PUT /api/users/{id}/promote` and `/demote` (admin only).
- Booking edits (admin) only change `status`, `startDate` and `endDate`; the price is recalculated.
- Passwords must be 8–72 characters.

> In development the password-reset link is printed in the **backend** console (there is no email service yet).

---

## **🎨 UI Framework**

The project uses:

- **Ant Design 5** + **Ant Design Pro Components**
- **ConfigProvider locale = enUS** (English UI)
- Dark mode (remembered between visits)

---

## **🧪 Testing**

```bash
npm test            # run once
npm run test:watch  # watch mode
```

Tests use **Vitest**, **Testing Library** and **axios-mock-adapter**. They cover the session logic,
route protection, error handling and the contract with the backend (what each page sends).

---

## **📜 Scripts Overview**

| Script | Description |
|--------|-------------|
| `npm run dev` | Start development server |
| `npm run build` | Type-check and build the production bundle |
| `npm run preview` | Preview production build |
| `npm run lint` | Run ESLint |
| `npm run typecheck` | Run the TypeScript compiler only |
| `npm test` | Run the unit and component tests |

---

## **📄 License**
This project is part of your personal development portfolio.  
Feel free to modify and extend as needed.