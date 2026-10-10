import api from "./axios";
import type { AuthResponse, User } from "../types";

export interface SignupPayload {
  name: string;
  email: string;
  password: string;
  phone: string;
}

export const login = (email: string, password: string) =>
  api.post<AuthResponse>("/api/auth/login", { email, password }).then((r) => r.data);

export const signup = (data: SignupPayload) =>
  api.post<AuthResponse>("/api/auth/signup", data).then((r) => r.data);

export const logout = () => api.post("/api/auth/logout");

export const getMe = () => api.get<User>("/api/users/me").then((r) => r.data);

export const requestPasswordReset = (email: string) =>
  api.post("/api/auth/request-reset", { email });

export const resetPassword = (token: string, newPassword: string) =>
  api.post("/api/auth/reset-password", { token, newPassword });
