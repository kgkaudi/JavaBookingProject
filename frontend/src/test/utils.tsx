import { render } from "@testing-library/react";
import { StrictMode } from "react";
import type { ReactElement } from "react";
import { App as AntApp } from "antd";
import { MemoryRouter } from "react-router-dom";
import { vi } from "vitest";
import { useAuth } from "../context/AuthContext";
import type { User } from "../types";

type AuthValue = ReturnType<typeof useAuth>;

export const adminUser: User = {
  id: "admin1",
  name: "Admin",
  email: "admin@booking.com",
  phone: "1",
  roles: ["ROLE_ADMIN"],
};

export const normalUser: User = {
  id: "u1",
  name: "Alice",
  email: "alice@x.com",
  phone: "6900000000",
  roles: ["ROLE_USER"],
};

/** Pretend `user` is logged in (requires vi.mock("../context/AuthContext") in the test file). */
export function mockAuth(user: User, overrides: Partial<AuthValue> = {}) {
  vi.mocked(useAuth).mockReturnValue({
    token: "t",
    user,
    loading: false,
    isAdmin: user.roles.includes("ROLE_ADMIN"),
    isUser: user.roles.includes("ROLE_USER"),
    login: vi.fn(),
    logout: vi.fn(),
    updateUser: vi.fn(),
    ...overrides,
  } as AuthValue);
}

/** Renders like the real app does: inside <StrictMode> (it double-mounts components in development). */
export function renderPage(ui: ReactElement, route = "/") {
  return render(
    <StrictMode>
      <AntApp>
        <MemoryRouter initialEntries={[route]}>{ui}</MemoryRouter>
      </AntApp>
    </StrictMode>
  );
}
