import { createContext, useContext, useEffect, useState } from "react";
import api from "../api/axios";

interface AuthContextType {
  token: string | null;
  roles: string[];
  isAdmin: boolean;
  isUser: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [token, setToken] = useState<string | null>(null);
  const [roles, setRoles] = useState<string[]>([]);

  // ---------------------------------------------------------
  // LOAD TOKEN + ROLES FROM LOCALSTORAGE ON REFRESH
  // ---------------------------------------------------------
  useEffect(() => {
    const storedToken = localStorage.getItem("token");
    const storedRoles = localStorage.getItem("roles");

    if (storedToken) {
      setToken(storedToken);
      api.defaults.headers.common["Authorization"] = `Bearer ${storedToken}`;
    }

    if (storedRoles) {
      setRoles(JSON.parse(storedRoles));
    }
  }, []);

  // ---------------------------------------------------------
  // LOGIN
  // ---------------------------------------------------------
  const login = async (email: string, password: string) => {
    const res = await api.post("/api/auth/login", { email, password });

    const token = res.data.token;
    const roles = res.data.roles || [];

    // Save to state
    setToken(token);
    setRoles(roles);

    // Save to localStorage
    localStorage.setItem("token", token);
    localStorage.setItem("roles", JSON.stringify(roles));

    // Set axios header
    api.defaults.headers.common["Authorization"] = `Bearer ${token}`;
  };

  // ---------------------------------------------------------
  // LOGOUT
  // ---------------------------------------------------------
  const logout = () => {
    setToken(null);
    setRoles([]);

    localStorage.removeItem("token");
    localStorage.removeItem("roles");

    delete api.defaults.headers.common["Authorization"];
  };

  // ---------------------------------------------------------
  // ROLE HELPERS
  // ---------------------------------------------------------
  const isAdmin = roles.includes("ADMIN");
  const isUser = roles.includes("USER");

  return (
    <AuthContext.Provider
      value={{
        token,
        roles,
        isAdmin,
        isUser,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
};
