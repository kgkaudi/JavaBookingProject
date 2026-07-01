import { createContext, useContext, useEffect, useState } from "react";
import api from "../api/axios";

interface UserType {
  name: string;
  email: string;
}

interface AuthContextType {
  token: string | null;
  roles: string[];
  isAdmin: boolean;
  isUser: boolean;
  user: UserType | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [token, setToken] = useState<string | null>(null);
  const [roles, setRoles] = useState<string[]>([]);
  const [user, setUser] = useState<UserType | null>(null);

  // ---------------------------------------------------------
  // LOAD TOKEN + VALIDATE IT ON REFRESH
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

    // 🔥 Validate token by fetching the real user
    if (storedToken) {
      api
        .get("/api/users/me")
        .then((res) => {
          setUser(res.data);
          localStorage.setItem("user", JSON.stringify(res.data));
        })
        .catch(() => {
          // Token invalid → logout
          logout();
        });
    }
  }, []);

  // ---------------------------------------------------------
  // LOGIN
  // ---------------------------------------------------------
  const login = async (email: string, password: string) => {
    const res = await api.post("/api/auth/login", { email, password });

    const token = res.data.token;
    const roles = res.data.roles || [];

    // Save token
    setToken(token);
    localStorage.setItem("token", token);
    api.defaults.headers.common["Authorization"] = `Bearer ${token}`;

    // Save roles
    setRoles(roles);
    localStorage.setItem("roles", JSON.stringify(roles));

    // Fetch user info
    const me = await api.get("/api/users/me");
    // const me = await api.get("/users/me");
    setUser(me.data);
    localStorage.setItem("user", JSON.stringify(me.data));
  };

  // ---------------------------------------------------------
  // LOGOUT
  // ---------------------------------------------------------
  const logout = () => {
    setToken(null);
    setRoles([]);
    setUser(null);

    localStorage.removeItem("token");
    localStorage.removeItem("roles");
    localStorage.removeItem("user");

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
        user,
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
