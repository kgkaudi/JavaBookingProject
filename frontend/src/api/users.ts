import api from "./axios";
import type { User } from "../types";

export interface CreateUserPayload {
  name: string;
  email: string;
  password: string;
  phone?: string;
  roles: string[];
}

export interface UpdateUserPayload {
  name?: string;
  email?: string;
  phone?: string;
}

export const getUsers = () => api.get<User[]>("/api/users").then((r) => r.data);

export const createUser = (data: CreateUserPayload) =>
  api.post<User>("/api/users", data).then((r) => r.data);

/** Profile fields only — roles are changed with promoteUser / demoteUser. */
export const updateUser = (id: string, data: UpdateUserPayload) =>
  api.put<User>(`/api/users/${id}`, data).then((r) => r.data);

export const promoteUser = (id: string) =>
  api.put<User>(`/api/users/${id}/promote`).then((r) => r.data);

export const demoteUser = (id: string) =>
  api.put<User>(`/api/users/${id}/demote`).then((r) => r.data);

export const deleteUser = (id: string) => api.delete(`/api/users/${id}`);
