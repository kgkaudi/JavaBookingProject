import { ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

interface ProtectedRouteProps {
  children: ReactNode;
}

const ProtectedRoute = ({ children }: ProtectedRouteProps) => {
  const { token, user, loading, isAdmin } = useAuth();
  const location = useLocation();

  // ---------------------------------------------------------
  // WAIT FOR AUTH TO LOAD (prevents flicker on refresh)
  // ---------------------------------------------------------
  if (loading) {
    return <div>Loading...</div>;
  }

  // ---------------------------------------------------------
  // NOT LOGGED IN → redirect to login
  // ---------------------------------------------------------
  if (!token || !user) {
    return <Navigate to="/login" replace />;
  }

  // ---------------------------------------------------------
  // ADMIN‑ONLY ROUTES (auto-detected)
  // ---------------------------------------------------------
  if (location.pathname.startsWith("/admin") && !isAdmin) {
    return <Navigate to="/unauthorized" replace />;
  }

  // ---------------------------------------------------------
  // AUTHORIZED → render page
  // ---------------------------------------------------------
  return <>{children}</>;
};

export default ProtectedRoute;
