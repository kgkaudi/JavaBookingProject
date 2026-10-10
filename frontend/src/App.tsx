import { lazy, Suspense } from "react";
import { Routes, Route } from "react-router-dom";
import { Spin } from "antd";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";

// Pages (and the heavy ProLayout shell) are loaded on demand so the first screen downloads less JavaScript
const ProLayoutShell = lazy(() => import("./layouts/ProLayoutShell"));
const Login = lazy(() => import("./pages/Login"));
const Signup = lazy(() => import("./pages/Signup"));
const ForgotPassword = lazy(() => import("./pages/ForgotPassword"));
const ResetPassword = lazy(() => import("./pages/ResetPassword"));
const Unauthorized = lazy(() => import("./pages/Unauthorized"));
const NotFound = lazy(() => import("./pages/NotFound"));

const Home = lazy(() => import("./pages/Home"));
const Rooms = lazy(() => import("./pages/Rooms"));
const RoomDetails = lazy(() => import("./pages/RoomDetails"));
const Bookings = lazy(() => import("./pages/Bookings"));
const Profile = lazy(() => import("./pages/Profile"));

const AdminPanel = lazy(() => import("./pages/AdminPanel"));
const AdminUsers = lazy(() => import("./pages/AdminUsers"));
const AdminRooms = lazy(() => import("./pages/AdminRooms"));
const AdminBookings = lazy(() => import("./pages/AdminBookings"));

interface AppProps {
  setDarkMode: (value: boolean) => void;
  darkMode: boolean;
}

export default function App({ setDarkMode, darkMode }: AppProps) {
  return (
    <AuthProvider>
      <Suspense
        fallback={
          <div style={{ minHeight: "60vh", display: "grid", placeItems: "center" }}>
            <Spin size="large" />
          </div>
        }
      >
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />

          {/* Forgot Password Flow */}
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/reset-password" element={<ResetPassword />} />

          {/* Protected Layout + Nested Protected Pages */}
          <Route
            element={
              <ProtectedRoute>
                <ProLayoutShell setDarkMode={setDarkMode} darkMode={darkMode} />
              </ProtectedRoute>
            }
          >
            {/* User Routes */}
            <Route path="/" element={<Home />} />
            <Route path="/rooms" element={<Rooms />} />
            <Route path="/rooms/:id" element={<RoomDetails />} />
            <Route path="/bookings" element={<Bookings />} />
            <Route path="/profile" element={<Profile />} />

            {/* Admin Routes (ProtectedRoute also checks the role) */}
            <Route path="/admin" element={<AdminPanel />} />
            <Route path="/admin/users" element={<AdminUsers />} />
            <Route path="/admin/rooms" element={<AdminRooms />} />
            <Route path="/admin/bookings" element={<AdminBookings />} />

            {/* Unknown URL inside the app */}
            <Route path="*" element={<NotFound />} />
          </Route>

          <Route path="/unauthorized" element={<Unauthorized />} />
        </Routes>
      </Suspense>
    </AuthProvider>
  );
}
