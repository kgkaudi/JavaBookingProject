import { ReactNode } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

interface ProtectedRouteProps {
  children: ReactNode;
  requireAdmin?: boolean;
}

const ProtectedRoute = ({ children, requireAdmin = false }: ProtectedRouteProps) => {
  const { token, user, loading, isAdmin } = useAuth();

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
  // ADMIN‑ONLY ROUTE
  // ---------------------------------------------------------
  if (requireAdmin && !isAdmin) {
    return <Navigate to="/unauthorized" replace />;
  }

  // ---------------------------------------------------------
  // AUTHORIZED → render page
  // ---------------------------------------------------------
  return <>{children}</>;
};

export default ProtectedRoute;
