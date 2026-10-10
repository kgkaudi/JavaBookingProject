import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import ProtectedRoute from "./ProtectedRoute";
import { useAuth } from "../context/AuthContext";

vi.mock("../context/AuthContext", () => ({ useAuth: vi.fn() }));

type AuthValue = ReturnType<typeof useAuth>;
const user = { id: "u1", name: "Alice", email: "a@x.com", roles: ["ROLE_USER"] };

function setAuth(value: Partial<AuthValue>) {
  vi.mocked(useAuth).mockReturnValue({
    token: null,
    user: null,
    loading: false,
    isAdmin: false,
    isUser: false,
    ...value,
  } as AuthValue);
}

function renderAt(path: string) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/login" element={<div>login page</div>} />
        <Route path="/unauthorized" element={<div>unauthorized page</div>} />
        <Route
          path="/*"
          element={
            <ProtectedRoute>
              <div>secret content</div>
            </ProtectedRoute>
          }
        />
      </Routes>
    </MemoryRouter>
  );
}

describe("ProtectedRoute", () => {
  beforeEach(() => {
    vi.mocked(useAuth).mockReset();
  });

  it("redirects anonymous visitors to the login page", () => {
    setAuth({});
    renderAt("/bookings");
    expect(screen.getByText("login page")).toBeInTheDocument();
  });

  it("does not redirect while the stored session is still being verified", () => {
    setAuth({ loading: true });
    renderAt("/bookings");
    expect(screen.queryByText("login page")).not.toBeInTheDocument();
    expect(screen.queryByText("secret content")).not.toBeInTheDocument();
  });

  it("lets a logged-in user see normal pages", () => {
    setAuth({ token: "t", user, isUser: true });
    renderAt("/bookings");
    expect(screen.getByText("secret content")).toBeInTheDocument();
  });

  it("keeps normal users out of /admin pages", () => {
    setAuth({ token: "t", user, isUser: true });
    renderAt("/admin/users");
    expect(screen.getByText("unauthorized page")).toBeInTheDocument();
  });

  it("lets admins into /admin pages", () => {
    setAuth({ token: "t", user: { ...user, roles: ["ROLE_ADMIN"] }, isAdmin: true });
    renderAt("/admin/users");
    expect(screen.getByText("secret content")).toBeInTheDocument();
  });
});
