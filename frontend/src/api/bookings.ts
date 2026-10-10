import axios from "axios";
import api from "./axios";
import type { ApiErrorBody, Booking, BookingStatus } from "../types";

export interface Availability {
  available: boolean;
  reason?: string;
}

export interface UpdateBookingPayload {
  status?: BookingStatus;
  startDate?: string;
  endDate?: string;
}

export const getMyBookings = () => api.get<Booking[]>("/api/bookings/me").then((r) => r.data);

export const getAllBookings = () => api.get<Booking[]>("/api/bookings").then((r) => r.data);

export const createBooking = (roomId: string, startDate: string, endDate: string) =>
  api.post<Booking>("/api/bookings", null, { params: { roomId, startDate, endDate } }).then((r) => r.data);

export const cancelBooking = (id: string) => api.patch(`/api/bookings/${id}/cancel`);

export const updateBooking = (id: string, data: UpdateBookingPayload) =>
  api.put<Booking>(`/api/bookings/${id}`, data).then((r) => r.data);

export const deleteBooking = (id: string) => api.delete(`/api/bookings/${id}`);

/**
 * The backend answers 409 with { available: false, reason } when the room is taken.
 * That is a normal answer, not an error, so it is converted here.
 */
export async function checkAvailability(
  roomId: string,
  startDate: string,
  endDate: string
): Promise<Availability> {
  try {
    const res = await api.get<Availability>("/api/bookings/availability", {
      params: { roomId, startDate, endDate },
    });
    return res.data;
  } catch (err) {
    if (axios.isAxiosError(err)) {
      const body = err.response?.data as ApiErrorBody & { available?: boolean } | undefined;
      if (body && body.available === false) {
        return { available: false, reason: body.reason ?? body.message };
      }
    }
    throw err;
  }
}
