import type { ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { Spin } from "antd";
import { useAuth } from "../context/AuthContext";

interface ProtectedRouteProps {
  children: ReactNode;
}

const ProtectedRoute = ({ children }: ProtectedRouteProps) => {
  const { token, user, loading, isAdmin } = useAuth();
  const location = useLocation();

  // WAIT FOR AUTH TO LOAD (prevents a redirect flash on refresh)
  if (loading) {
    return (
      <div style={{ minHeight: "100vh", display: "grid", placeItems: "center" }}>
        <Spin size="large" />
      </div>
    );
  }

  // NOT LOGGED IN → login, remembering where the user wanted to go
  if (!token || !user) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  // ADMIN-ONLY ROUTES
  if (location.pathname.startsWith("/admin") && !isAdmin) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
