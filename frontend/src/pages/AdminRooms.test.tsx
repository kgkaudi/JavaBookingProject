import MockAdapter from "axios-mock-adapter";
import { screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import api from "../api/axios";
import AdminRooms from "./AdminRooms";
import { adminUser, mockAuth, renderPage } from "../test/utils";
import type { Room } from "../types";

vi.mock("../context/AuthContext", () => ({ useAuth: vi.fn() }));

const room101: Room = { id: "r1", roomNumber: 101, type: "single", capacity: 1, price: 50, available: true };

describe("AdminRooms page", () => {
  let mock: MockAdapter;

  beforeEach(() => {
    mock = new MockAdapter(api);
    mockAuth(adminUser);
    mock.onGet("/api/rooms").reply(200, [room101]);
  });

  afterEach(() => {
    mock.restore();
  });

  it("toggles availability with a full PUT (the backend has no /availability endpoint)", async () => {
    mock.onPut("/api/rooms/r1").reply(200, { ...room101, available: false });
    renderPage(<AdminRooms />);

    const row = (await screen.findByText("101")).closest("tr") as HTMLElement;
    await userEvent.click(within(row).getByRole("button", { name: /mark unavailable/i }));

    await waitFor(() => expect(mock.history.put).toHaveLength(1));
    expect(JSON.parse(mock.history.put[0].data)).toEqual({
      roomNumber: 101,
      type: "single",
      capacity: 1,
      price: 50,
      available: false,
    });
    expect(mock.history.patch).toHaveLength(0);
  });

  it("pre-fills the edit form with the existing room's values", async () => {
    renderPage(<AdminRooms />);

    const row = (await screen.findByText("101")).closest("tr") as HTMLElement;
    await userEvent.click(within(row).getByRole("button", { name: /edit/i }));

    const dialog = await screen.findByRole("dialog");
    const spinbuttons = within(dialog).getAllByRole("spinbutton");
    expect(spinbuttons[0]).toHaveValue("101"); // room number
    expect(spinbuttons[1]).toHaveValue("1"); // capacity
    expect(spinbuttons[2]).toHaveValue("50.00"); // price
    expect(within(dialog).getByRole("switch")).toBeChecked();
  });

  it("starts the 'Add Room' form empty but bookable by default", async () => {
    renderPage(<AdminRooms />);

    await screen.findByText("101");
    await userEvent.click(screen.getByRole("button", { name: /add room/i }));

    const dialog = await screen.findByRole("dialog");
    expect(within(dialog).getByRole("switch")).toBeChecked();
    expect(within(dialog).getAllByRole("spinbutton")[0]).toHaveValue("");
  });

  it("opens 'Add Room' empty even right after editing a room (no leftover values)", async () => {
    renderPage(<AdminRooms />);

    const row = (await screen.findByText("101")).closest("tr") as HTMLElement;
    await userEvent.click(within(row).getByRole("button", { name: /edit/i }));
    await screen.findByRole("dialog");
    await userEvent.click(screen.getByRole("button", { name: /cancel/i }));

    await userEvent.click(screen.getByRole("button", { name: /add room/i }));

    await waitFor(() => {
      const dialog = screen.getByRole("dialog");
      expect(within(dialog).getAllByRole("spinbutton")[0]).toHaveValue("");
    });
  });

  it("shows the backend's message when the room number already exists", async () => {
    mock.onPut("/api/rooms/r1").reply(409, { status: 409, message: "Room number already exists" });
    renderPage(<AdminRooms />);

    const row = (await screen.findByText("101")).closest("tr") as HTMLElement;
    await userEvent.click(within(row).getByRole("button", { name: /edit/i }));
    await userEvent.click(await screen.findByRole("button", { name: /save changes/i }));

    expect(await screen.findByText("Room number already exists")).toBeInTheDocument();
  });
});
