import MockAdapter from "axios-mock-adapter";
import { screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import api from "../api/axios";
import AdminBookings from "./AdminBookings";
import { adminUser, mockAuth, normalUser, renderPage } from "../test/utils";
import type { Booking } from "../types";

vi.mock("../context/AuthContext", () => ({ useAuth: vi.fn() }));

const booking: Booking = {
  id: "b1",
  roomNumber: 101,
  userId: "u1",
  status: "confirmed",
  startDate: "2030-03-10",
  endDate: "2030-03-14",
  totalPrice: 200,
};

describe("AdminBookings page", () => {
  let mock: MockAdapter;

  beforeEach(() => {
    mock = new MockAdapter(api);
    mockAuth(adminUser);
    mock.onGet("/api/bookings").reply(200, [booking]);
    mock.onGet("/api/users").reply(200, [adminUser, normalUser]);
  });

  afterEach(() => {
    mock.restore();
  });

  const openEdit = async () => {
    const row = (await screen.findByText("Room 101")).closest("tr") as HTMLElement;
    await userEvent.click(within(row).getByRole("button", { name: /edit/i }));
    return screen.findByRole("dialog");
  };

  it("pre-fills the edit form with the booking's status and dates", async () => {
    renderPage(<AdminBookings />);

    const dialog = await openEdit();

    const dateInputs = within(dialog).getAllByRole("textbox");
    const values = dateInputs.map((i) => (i as HTMLInputElement).value);
    expect(values).toContain("2030-03-10");
    expect(values).toContain("2030-03-14");
    expect(within(dialog).getAllByText("Confirmed").length).toBeGreaterThan(0);
    // room and guest are shown read-only
    expect(within(dialog).getByDisplayValue("Room 101")).toBeDisabled();
    expect(within(dialog).getByDisplayValue("Alice (alice@x.com)")).toBeDisabled();
  });

  it("saves only status and dates (the backend ignores room/user and recalculates the price)", async () => {
    mock.onPut("/api/bookings/b1").reply(200, booking);
    renderPage(<AdminBookings />);

    await openEdit();
    await userEvent.click(screen.getByRole("button", { name: /save changes/i }));

    await waitFor(() => expect(mock.history.put).toHaveLength(1));
    expect(JSON.parse(mock.history.put[0].data)).toEqual({
      status: "confirmed",
      startDate: "2030-03-10",
      endDate: "2030-03-14",
    });
  });

  it("shows the backend's conflict message when the new dates overlap another booking", async () => {
    mock.onPut("/api/bookings/b1").reply(409, { status: 409, message: "Dates overlap with existing booking" });
    renderPage(<AdminBookings />);

    await openEdit();
    await userEvent.click(screen.getByRole("button", { name: /save changes/i }));

    expect(await screen.findByText("Dates overlap with existing booking")).toBeInTheDocument();
  });
});
