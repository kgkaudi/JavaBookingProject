import { Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";

import Login from "./pages/Login";
import Signup from "./pages/Signup";
import Rooms from "./pages/Rooms";
import Bookings from "./pages/Bookings";
import ProLayoutShell from "./layouts/ProLayoutShell";
import Profile from "./pages/Profile";
import RoomDetails from "./pages/RoomDetails";
import Unauthorized from "./pages/Unauthorized";
import Home from "./pages/Home";
import ForgotPassword from "./pages/ForgotPassword";
import ResetPassword from "./pages/ResetPassword";

// Admin pages
import AdminPanel from "./pages/AdminPanel";
import AdminUsers from "./pages/AdminUsers";
import AdminRooms from "./pages/AdminRooms";
import AdminBookings from "./pages/AdminBookings";

export default function App({ setDarkMode, darkMode }) {
  return (
    <AuthProvider>
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
              <ProLayoutShell
                setDarkMode={setDarkMode}
                darkMode={darkMode}
              />
            </ProtectedRoute>
          }
        >
          {/* User Routes */}
          <Route path="/" element={<Home />} />
          <Route path="/rooms" element={<Rooms />} />
          <Route path="/rooms/:id" element={<RoomDetails />} />
          <Route path="/bookings" element={<Bookings />} />
          <Route path="/profile" element={<Profile />} />

          {/* Admin Routes */}
          <Route path="/admin" element={<AdminPanel />} />
          <Route path="/admin/users" element={<AdminUsers />} />
          <Route path="/admin/rooms" element={<AdminRooms />} />
          <Route path="/admin/bookings" element={<AdminBookings />} />
        </Route>

        <Route path="/unauthorized" element={<Unauthorized />} />

      </Routes>
    </AuthProvider>
  );
}
