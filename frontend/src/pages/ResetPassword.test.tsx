import MockAdapter from "axios-mock-adapter";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import api from "../api/axios";
import ResetPassword from "./ResetPassword";

function renderAt(url: string) {
  return render(
    <MemoryRouter initialEntries={[url]}>
      <Routes>
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/login" element={<div>login page</div>} />
        <Route path="/forgot-password" element={<div>forgot page</div>} />
      </Routes>
    </MemoryRouter>
  );
}

describe("ResetPassword page", () => {
  let mock: MockAdapter;

  beforeEach(() => {
    mock = new MockAdapter(api);
  });

  afterEach(() => {
    mock.restore();
  });

  it("explains a link without a token instead of showing a useless form", () => {
    renderAt("/reset-password");
    expect(screen.getByText("This reset link is invalid")).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /update password/i })).not.toBeInTheDocument();
  });

  it("rejects passwords shorter than 8 characters without calling the API", async () => {
    renderAt("/reset-password?token=abc");

    await userEvent.type(screen.getByPlaceholderText("At least 8 characters"), "short");
    await userEvent.type(screen.getByPlaceholderText("Repeat the password"), "short");
    await userEvent.click(screen.getByRole("button", { name: /update password/i }));

    expect(await screen.findByText("Password must be at least 8 characters")).toBeInTheDocument();
    expect(mock.history.post).toHaveLength(0);
  });

  it("rejects mismatching confirmation", async () => {
    renderAt("/reset-password?token=abc");

    await userEvent.type(screen.getByPlaceholderText("At least 8 characters"), "Passw0rd123");
    await userEvent.type(screen.getByPlaceholderText("Repeat the password"), "Different123");
    await userEvent.click(screen.getByRole("button", { name: /update password/i }));

    expect(await screen.findByText("Passwords do not match")).toBeInTheDocument();
    expect(mock.history.post).toHaveLength(0);
  });

  it("sends token + new password and goes to the login page on success", async () => {
    mock.onPost("/api/auth/reset-password").reply(200);
    renderAt("/reset-password?token=abc");

    await userEvent.type(screen.getByPlaceholderText("At least 8 characters"), "Passw0rd123");
    await userEvent.type(screen.getByPlaceholderText("Repeat the password"), "Passw0rd123");
    await userEvent.click(screen.getByRole("button", { name: /update password/i }));

    await waitFor(() => expect(screen.getByText("login page")).toBeInTheDocument());
    expect(JSON.parse(mock.history.post[0].data)).toEqual({
      token: "abc",
      newPassword: "Passw0rd123",
    });
  });

  it("shows the backend's message for an expired token", async () => {
    mock.onPost("/api/auth/reset-password").reply(400, {
      status: 400,
      message: "Invalid or expired token",
    });
    renderAt("/reset-password?token=old");

    await userEvent.type(screen.getByPlaceholderText("At least 8 characters"), "Passw0rd123");
    await userEvent.type(screen.getByPlaceholderText("Repeat the password"), "Passw0rd123");
    await userEvent.click(screen.getByRole("button", { name: /update password/i }));

    expect(await screen.findByText("Invalid or expired token")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /request a new reset link/i })).toBeInTheDocument();
  });
});
