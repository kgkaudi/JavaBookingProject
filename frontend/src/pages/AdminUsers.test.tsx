import MockAdapter from "axios-mock-adapter";
import { screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import api from "../api/axios";
import AdminUsers from "./AdminUsers";
import { adminUser, mockAuth, normalUser, renderPage } from "../test/utils";

vi.mock("../context/AuthContext", () => ({ useAuth: vi.fn() }));

describe("AdminUsers page (contract with the secured backend)", () => {
  let mock: MockAdapter;

  beforeEach(() => {
    mock = new MockAdapter(api);
    mockAuth(adminUser);
    mock.onGet("/api/users").reply(200, [adminUser, normalUser]);
  });

  afterEach(() => {
    mock.restore();
  });

  const rowOf = async (email: string) => {
    const cell = await screen.findByText(email);
    return cell.closest("tr") as HTMLElement;
  };

  it("does not let an admin delete their own account", async () => {
    renderPage(<AdminUsers />);

    const ownRow = await rowOf(adminUser.email);
    expect(within(ownRow).getByRole("button", { name: /delete/i })).toBeDisabled();

    const otherRow = await rowOf(normalUser.email);
    expect(within(otherRow).getByRole("button", { name: /delete/i })).toBeEnabled();
  });

  it("creates a user with an initial password and a roles array", async () => {
    mock.onPost("/api/users").reply(200, { ...normalUser, id: "u2" });
    const user = userEvent.setup({ delay: null });
    renderPage(<AdminUsers />);
    await rowOf(normalUser.email);

    await user.click(screen.getByRole("button", { name: /add user/i }));

    await user.type(screen.getByPlaceholderText("Enter full name"), "Bob");
    await user.type(screen.getByPlaceholderText("Enter email"), "bob@x.com");
    await user.type(screen.getByPlaceholderText("Enter phone number"), "123");
    await user.type(screen.getByPlaceholderText("At least 8 characters"), "Passw0rd123");
    await user.click(screen.getByRole("button", { name: /create user/i }));

    await waitFor(() => expect(mock.history.post).toHaveLength(1));
    expect(JSON.parse(mock.history.post[0].data)).toEqual({
      name: "Bob",
      email: "bob@x.com",
      phone: "123",
      password: "Passw0rd123",
      roles: ["ROLE_USER"],
    });
  });

  it("refuses to create a user with a short password", async () => {
    renderPage(<AdminUsers />);
    await rowOf(normalUser.email);

    await userEvent.click(screen.getByRole("button", { name: /add user/i }));
    await userEvent.type(screen.getByPlaceholderText("Enter full name"), "Bob");
    await userEvent.type(screen.getByPlaceholderText("Enter email"), "bob@x.com");
    await userEvent.type(screen.getByPlaceholderText("At least 8 characters"), "short");
    await userEvent.click(screen.getByRole("button", { name: /create user/i }));

    expect(await screen.findByText("Password must be at least 8 characters")).toBeInTheDocument();
    expect(mock.history.post).toHaveLength(0);
  });

  it("pre-fills the edit form with the existing user's values", async () => {
    renderPage(<AdminUsers />);

    const row = await rowOf(normalUser.email);
    await userEvent.click(within(row).getByRole("button", { name: /edit/i }));

    expect(await screen.findByPlaceholderText("Enter full name")).toHaveValue("Alice");
    expect(screen.getByPlaceholderText("Enter email")).toHaveValue("alice@x.com");
    expect(screen.getByPlaceholderText("Enter phone number")).toHaveValue("6900000000");
    // the Select shows the current role as its selected item
    expect(screen.getAllByText("User").length).toBeGreaterThan(0);
  });

  it("shows another user's values when editing a second user after closing the first", async () => {
    renderPage(<AdminUsers />);

    const aliceRow = await rowOf(normalUser.email);
    await userEvent.click(within(aliceRow).getByRole("button", { name: /edit/i }));
    expect(await screen.findByPlaceholderText("Enter full name")).toHaveValue("Alice");
    await userEvent.click(screen.getByRole("button", { name: /cancel/i }));

    const adminRow = await rowOf(adminUser.email);
    await userEvent.click(within(adminRow).getByRole("button", { name: /edit/i }));
    await waitFor(() => expect(screen.getByPlaceholderText("Enter full name")).toHaveValue("Admin"));
  });

  it("opens 'Add User' empty even right after editing someone (no leftover values)", async () => {
    renderPage(<AdminUsers />);

    const row = await rowOf(normalUser.email);
    await userEvent.click(within(row).getByRole("button", { name: /edit/i }));
    expect(await screen.findByPlaceholderText("Enter full name")).toHaveValue("Alice");
    await userEvent.click(screen.getByRole("button", { name: /cancel/i }));

    await userEvent.click(screen.getByRole("button", { name: /add user/i }));

    await waitFor(() => expect(screen.getByPlaceholderText("Enter full name")).toHaveValue(""));
    expect(screen.getByPlaceholderText("Enter email")).toHaveValue("");
    expect(screen.getByPlaceholderText("Enter phone number")).toHaveValue("");
  });

  it("updates profile fields with PUT and never sends roles or a password", async () => {
    mock.onPut(`/api/users/${normalUser.id}`).reply(200, normalUser);
    renderPage(<AdminUsers />);

    const row = await rowOf(normalUser.email);
    await userEvent.click(within(row).getByRole("button", { name: /edit/i }));

    const nameInput = await screen.findByPlaceholderText("Enter full name");
    await userEvent.clear(nameInput);
    await userEvent.type(nameInput, "Alice B");
    await userEvent.click(screen.getByRole("button", { name: /save changes/i }));

    await waitFor(() => expect(mock.history.put).toHaveLength(1));
    const body = JSON.parse(mock.history.put[0].data);
    expect(body).toEqual({ name: "Alice B", email: "alice@x.com", phone: "6900000000" });
    expect(body).not.toHaveProperty("roles");
    expect(body).not.toHaveProperty("role");
    // role unchanged → no promote/demote call
    expect(mock.history.put.some((r) => /promote|demote/.test(r.url ?? ""))).toBe(false);
  });
});
