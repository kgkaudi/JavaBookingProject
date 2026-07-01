import api from "../api/axios";

export const checkAvailability = async (
  roomId: string,
  startDate: string,
  endDate: string
) => {
  const res = await api.get("/api/bookings/availability", {
    params: { roomId, startDate, endDate },
  });

  return res.data;
};
