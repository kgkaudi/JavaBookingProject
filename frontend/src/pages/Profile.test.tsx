import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import Profile from "./Profile";
import { useAuth } from "../context/AuthContext";
import { mockAuth, normalUser } from "../test/utils";

vi.mock("../context/AuthContext", () => ({ useAuth: vi.fn() }));

describe("Profile page", () => {
  beforeEach(() => {
    vi.mocked(useAuth).mockReset();
  });

  it("renders the logged-in user (flat user object from /api/users/me)", () => {
    mockAuth(normalUser);
    render(
      <MemoryRouter>
        <Profile />
      </MemoryRouter>
    );

    expect(screen.getAllByText("Alice").length).toBeGreaterThan(0);
    expect(screen.getByText("alice@x.com")).toBeInTheDocument();
  });

  it("does not crash when the user disappears and comes back (hooks run on every render)", () => {
    mockAuth(normalUser);
    const { rerender } = render(
      <MemoryRouter>
        <Profile />
      </MemoryRouter>
    );

    vi.mocked(useAuth).mockReturnValue({ ...vi.mocked(useAuth)(), user: null } as ReturnType<typeof useAuth>);
    rerender(
      <MemoryRouter>
        <Profile />
      </MemoryRouter>
    );
    expect(screen.getByText("No user data available.")).toBeInTheDocument();

    mockAuth(normalUser);
    rerender(
      <MemoryRouter>
        <Profile />
      </MemoryRouter>
    );
    expect(screen.getByText("alice@x.com")).toBeInTheDocument();
  });

  it("sends only name and phone when saving", async () => {
    const updateUser = vi.fn().mockResolvedValue(undefined);
    mockAuth(normalUser, { updateUser });

    render(
      <MemoryRouter>
        <Profile />
      </MemoryRouter>
    );

    const nameInput = screen.getByPlaceholderText("Enter your name");
    await userEvent.clear(nameInput);
    await userEvent.type(nameInput, "Alice B");
    await userEvent.click(screen.getByRole("button", { name: /update profile/i }));

    await waitFor(() => expect(updateUser).toHaveBeenCalledTimes(1));
    expect(updateUser).toHaveBeenCalledWith({ name: "Alice B", phone: "6900000000" });
  });
});
