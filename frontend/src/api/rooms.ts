import api from "./axios";
import type { Room, RoomInput } from "../types";

export const getRooms = () => api.get<Room[]>("/api/rooms").then((r) => r.data);

export const getRoom = (id: string) => api.get<Room>(`/api/rooms/${id}`).then((r) => r.data);

export const createRoom = (room: RoomInput) =>
  api.post<Room>("/api/rooms", room).then((r) => r.data);

export const updateRoom = (id: string, room: RoomInput) =>
  api.put<Room>(`/api/rooms/${id}`, room).then((r) => r.data);

export const deleteRoom = (id: string) => api.delete(`/api/rooms/${id}`);
