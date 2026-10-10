export const ROLE_ADMIN = "ROLE_ADMIN";
export const ROLE_USER = "ROLE_USER";

export interface User {
  id: string;
  name: string;
  email: string;
  phone?: string;
  roles: string[];
}

export interface Room {
  id: string;
  roomNumber: number;
  type: string;
  capacity: number;
  price: number;
  available: boolean;
}

export type RoomInput = Omit<Room, "id">;

export type BookingStatus = "pending" | "confirmed" | "cancelled";

export interface Booking {
  id: string;
  roomNumber: number;
  userId: string;
  status: BookingStatus;
  startDate: string;
  endDate: string;
  totalPrice: number;
}

export interface AuthResponse {
  token: string;
}

/** Error body returned by the backend's GlobalExceptionHandler. */
export interface ApiErrorBody {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  fieldErrors?: Record<string, string>;
  /** returned by GET /api/bookings/availability when a room is not free */
  reason?: string;
}
