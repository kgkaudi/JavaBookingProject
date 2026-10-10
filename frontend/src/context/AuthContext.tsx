import { createContext, useCallback, useContext, useEffect, useState } from "react";
import type { ReactNode } from "react";
import { message } from "antd";
import api, { TOKEN_KEY, USER_KEY, setUnauthorizedHandler } from "../api/axios";
import { getErrorMessage } from "../api/errors";
import { ROLE_ADMIN, ROLE_USER } from "../types";
import type { AuthResponse, User } from "../types";

export interface ProfileUpdate {
  name?: string;
  phone?: string;
}

interface AuthContextType {
  token: string | null;
  user: User | null;
  isAdmin: boolean;
  isUser: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  updateUser: (updatedData: ProfileUpdate) => Promise<void>;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

function readStoredUser(): User | null {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as User) : null;
  } catch {
    localStorage.removeItem(USER_KEY);
    return null;
  }
}

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY));
  const [user, setUser] = useState<User | null>(readStoredUser);
  // while a stored token is being verified we must not redirect to /login
  const [loading, setLoading] = useState<boolean>(() => !!localStorage.getItem(TOKEN_KEY));

  // Forget the session locally (no network call)
  const clearSession = useCallback(() => {
    setToken(null);
    setUser(null);
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }, []);

  // ---------------------------------------------------------
  // RESTORE SESSION ON REFRESH (verify the stored token)
  // ---------------------------------------------------------
  useEffect(() => {
    if (!localStorage.getItem(TOKEN_KEY)) {
      return;
    }

    let active = true;

    api
      .get<User>("/api/users/me", { skipSessionCheck: true })
      .then((res) => {
        if (!active) return;
        setUser(res.data);
        localStorage.setItem(USER_KEY, JSON.stringify(res.data));
      })
      .catch(() => {
        if (active) clearSession();
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, [clearSession]);

  // ---------------------------------------------------------
  // SESSION EXPIRED mid-use (axios interceptor tells us)
  // ---------------------------------------------------------
  useEffect(
    () =>
      setUnauthorizedHandler(() => {
        clearSession();
        message.warning("Your session has expired. Please log in again.");
      }),
    [clearSession]
  );

  // ---------------------------------------------------------
  // LOGIN
  // ---------------------------------------------------------
  const login = async (email: string, password: string) => {
    const res = await api.post<AuthResponse>("/api/auth/login", { email, password });

    // the request interceptor reads the token from storage
    localStorage.setItem(TOKEN_KEY, res.data.token);

    try {
      const me = await api.get<User>("/api/users/me", { skipSessionCheck: true });
      setToken(res.data.token);
      setUser(me.data);
      localStorage.setItem(USER_KEY, JSON.stringify(me.data));
    } catch (err) {
      localStorage.removeItem(TOKEN_KEY);
      throw err;
    }
  };

  // ---------------------------------------------------------
  // LOGOUT — tell the backend to blacklist the token, then forget it locally
  // ---------------------------------------------------------
  const logout = async () => {
    try {
      await api.post("/api/auth/logout", null, { skipSessionCheck: true });
    } catch {
      // token already expired / server unreachable: still log out locally
    } finally {
      clearSession();
    }
  };

  // ---------------------------------------------------------
  // UPDATE LOGGED-IN USER (name / phone only; the backend ignores anything else)
  // ---------------------------------------------------------
  const updateUser = async (updatedData: ProfileUpdate) => {
    if (!user) return;

    try {
      const res = await api.put<User>(`/api/users/${user.id}`, updatedData);
      setUser(res.data);
      localStorage.setItem(USER_KEY, JSON.stringify(res.data));
      message.success("Profile updated successfully");
    } catch (err) {
      message.error(getErrorMessage(err, "Failed to update profile"));
    }
  };

  // ---------------------------------------------------------
  // ROLE HELPERS
  // ---------------------------------------------------------
  const roles = user?.roles ?? [];
  const isAdmin = roles.includes(ROLE_ADMIN);
  const isUser = roles.includes(ROLE_USER);

  return (
    <AuthContext.Provider
      value={{ token, user, isAdmin, isUser, updateUser, login, logout, loading }}
    >
      {children}
    </AuthContext.Provider>
  );
};

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
};
