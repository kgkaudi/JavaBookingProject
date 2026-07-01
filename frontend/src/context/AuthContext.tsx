import { createContext, useContext, useEffect, useState } from "react";
import { message } from "antd";
import api from "../api/axios";

interface UserType {
  id: string;
  name: string;
  email: string;
  phone?: string;
  roles?: string[];
  createdAt?: string;
}

interface AuthContextType {
  token: string | null;
  user: UserType | null;
  isAdmin: boolean;
  isUser: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  updateUser: (updatedData: Partial<UserType>) => Promise<void>;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<UserType | null>(null);
  const [loading, setLoading] = useState(true);

  // ---------------------------------------------------------
  // RESTORE SESSION ON REFRESH
  // ---------------------------------------------------------
  useEffect(() => {
    const storedToken = localStorage.getItem("token");
    const storedUser = localStorage.getItem("user");

    if (!storedToken) {
      setLoading(false);
      return;
    }

    setToken(storedToken);

    if (storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch {
        localStorage.removeItem("user");
      }
    }

    api
      .get("/api/users/me")
      .then((res) => {
        setUser(res.data);
        localStorage.setItem("user", JSON.stringify(res.data));
      })
      .catch(() => {
        logout();
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);

  // ---------------------------------------------------------
  // LOGIN
  // ---------------------------------------------------------
  const login = async (email: string, password: string) => {
    const res = await api.post("/api/auth/login", { email, password });

    const token = res.data.token;
    setToken(token);
    localStorage.setItem("token", token);

    const me = await api.get("/api/users/me");
    setUser(me.data);
    localStorage.setItem("user", JSON.stringify(me.data));
  };

  // ---------------------------------------------------------
  // LOGOUT
  // ---------------------------------------------------------
  const logout = () => {
    setToken(null);
    setUser(null);

    localStorage.removeItem("token");
    localStorage.removeItem("user");
  };

  // ---------------------------------------------------------
  // UPDATE LOGGED-IN USER
  // ---------------------------------------------------------
  const updateUser = async (updatedData: Partial<UserType>) => {
    if (!user) return;

    // Handle nested structure from backend
    const userId = user.id || user.user?.id;

    try {
      const res = await api.put(`/api/users/${userId}`, updatedData);
      const updatedUser = res.data;

      setUser(updatedUser);
      localStorage.setItem("user", JSON.stringify(updatedUser));

      message.success("Profile updated successfully");
    } catch (err: any) {
      const msg = err?.response?.data?.message || "Failed to update profile";
      message.error(msg);
    }
  };

  // ---------------------------------------------------------
  // ROLE HELPERS
  // ---------------------------------------------------------
  const isAdmin = !!user?.user.roles?.includes("ROLE_ADMIN");
  const isUser = !!user?.user.roles?.includes("ROLE_USER");

  return (
    <AuthContext.Provider
      value={{
        token,
        user,
        isAdmin,
        isUser,
        updateUser,
        login,
        logout,
        loading,
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
