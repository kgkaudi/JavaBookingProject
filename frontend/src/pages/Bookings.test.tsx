import MockAdapter from "axios-mock-adapter";
import { screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import api from "../api/axios";
import Bookings from "./Bookings";
import { mockAuth, normalUser, renderPage } from "../test/utils";
import type { Booking, Room } from "../types";

vi.mock("../context/AuthContext", () => ({ useAuth: vi.fn() }));

const room: Room = { id: "r1", roomNumber: 101, type: "single", capacity: 1, price: 50, available: true };

const future = (days: number) => new Date(Date.now() + days * 86_400_000).toISOString().slice(0, 10);

const upcoming: Booking = {
  id: "b1",
  roomNumber: 101,
  userId: "u1",
  status: "confirmed",
  startDate: future(10),
  endDate: future(13),
  totalPrice: 150,
};

const cancelled: Booking = { ...upcoming, id: "b2", status: "cancelled" };

const finished: Booking = {
  ...upcoming,
  id: "b3",
  startDate: "2020-01-01",
  endDate: "2020-01-05",
};

describe("Bookings page", () => {
  let mock: MockAdapter;

  beforeEach(() => {
    mock = new MockAdapter(api);
    mockAuth(normalUser);
    mock.onGet("/api/rooms").reply(200, [room]);
  });

  afterEach(() => {
    mock.restore();
  });

  it("lets the user cancel an upcoming booking through PATCH /cancel", async () => {
    mock.onGet("/api/bookings/me").reply(200, [upcoming]);
    mock.onPatch("/api/bookings/b1/cancel").reply(200, { ...upcoming, status: "cancelled" });

    renderPage(<Bookings />);

    await userEvent.click(await screen.findByRole("button", { name: /^cancel$/i }));
    await userEvent.click(await screen.findByRole("button", { name: /yes, cancel/i }));

    await waitFor(() => expect(mock.history.patch).toHaveLength(1));
    expect(mock.history.patch[0].url).toBe("/api/bookings/b1/cancel");
  });

  it("offers no cancel button for cancelled or already finished bookings", async () => {
    mock.onGet("/api/bookings/me").reply(200, [cancelled, finished]);

    renderPage(<Bookings />);

    await screen.findAllByText("Room 101");
    expect(screen.queryByRole("button", { name: /^cancel$/i })).not.toBeInTheDocument();
  });

  it("shows the total price of each booking", async () => {
    mock.onGet("/api/bookings/me").reply(200, [upcoming]);

    renderPage(<Bookings />);

    const row = (await screen.findByText("Room 101")).closest("tr") as HTMLElement;
    expect(within(row).getByText("€150.00")).toBeInTheDocument();
  });

  it("opens the booking form with the room preselected when coming from a 'Book' button", async () => {
    mock.onGet("/api/bookings/me").reply(200, []);

    renderPage(<Bookings />, "/bookings?roomId=r1");

    expect(await screen.findByText("Create Booking")).toBeInTheDocument();
    expect(await screen.findByText(/Room 101 — single/)).toBeInTheDocument();
    // dates are still missing, so booking is not possible yet
    expect(screen.getByRole("button", { name: /^create$/i })).toBeDisabled();
  });
});
