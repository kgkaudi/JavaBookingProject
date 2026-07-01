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

export default function App({ setDarkMode, darkMode }) {
  return (
    <AuthProvider>
      <Routes>

        {/* Public Routes */}
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />

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
          <Route path="/" element={<Rooms />} />
          <Route path="/rooms" element={<Rooms />} />
          <Route path="/rooms/:id" element={<RoomDetails />} />
          <Route path="/bookings" element={<Bookings />} />
          <Route path="/profile" element={<Profile />} />
        </Route>

        <Route path="/unauthorized" element={<Unauthorized />} />

      </Routes>
    </AuthProvider>
  );
}
