import MockAdapter from "axios-mock-adapter";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import api, { TOKEN_KEY, USER_KEY } from "../api/axios";
import { AuthProvider, useAuth } from "./AuthContext";

const alice = { id: "u1", name: "Alice", email: "alice@x.com", phone: "1", roles: ["ROLE_USER"] };

function Probe() {
  const auth = useAuth();
  return (
    <div>
      <span data-testid="user">{auth.user?.name ?? "none"}</span>
      <span data-testid="loading">{String(auth.loading)}</span>
      <span data-testid="admin">{String(auth.isAdmin)}</span>
      <button onClick={() => void auth.login("alice@x.com", "password123")}>login</button>
      <button onClick={() => void auth.logout()}>logout</button>
    </div>
  );
}

const renderProvider = () =>
  render(
    <AuthProvider>
      <Probe />
    </AuthProvider>
  );

describe("AuthProvider", () => {
  let mock: MockAdapter;

  beforeEach(() => {
    mock = new MockAdapter(api);
  });

  afterEach(() => {
    mock.restore();
  });

  it("restores a valid session on page load", async () => {
    localStorage.setItem(TOKEN_KEY, "good");
    mock.onGet("/api/users/me").reply(200, alice);

    renderProvider();

    expect(screen.getByTestId("loading")).toHaveTextContent("true");
    await waitFor(() => expect(screen.getByTestId("user")).toHaveTextContent("Alice"));
    expect(screen.getByTestId("loading")).toHaveTextContent("false");
  });

  it("drops an expired token on page load", async () => {
    localStorage.setItem(TOKEN_KEY, "expired");
    mock.onGet("/api/users/me").reply(403);

    renderProvider();

    await waitFor(() => expect(screen.getByTestId("loading")).toHaveTextContent("false"));
    expect(screen.getByTestId("user")).toHaveTextContent("none");
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
  });

  it("logs in, stores the session and exposes the flat user returned by /me", async () => {
    mock.onPost("/api/auth/login").reply(200, { token: "new-token" });
    mock.onGet("/api/users/me").reply(200, { ...alice, roles: ["ROLE_ADMIN"] });

    renderProvider();
    await waitFor(() => expect(screen.getByTestId("loading")).toHaveTextContent("false"));
    await userEvent.click(screen.getByText("login"));

    await waitFor(() => expect(screen.getByTestId("user")).toHaveTextContent("Alice"));
    expect(screen.getByTestId("admin")).toHaveTextContent("true");
    expect(localStorage.getItem(TOKEN_KEY)).toBe("new-token");
    expect(JSON.parse(localStorage.getItem(USER_KEY) ?? "{}").email).toBe("alice@x.com");
  });

  it("logout tells the backend (with the token) and then clears the session", async () => {
    localStorage.setItem(TOKEN_KEY, "good");
    mock.onGet("/api/users/me").reply(200, alice);
    mock.onPost("/api/auth/logout").reply(200, "Logged out successfully");

    renderProvider();
    await waitFor(() => expect(screen.getByTestId("user")).toHaveTextContent("Alice"));
    await userEvent.click(screen.getByText("logout"));

    await waitFor(() => expect(screen.getByTestId("user")).toHaveTextContent("none"));

    const logoutCall = mock.history.post.find((r) => r.url === "/api/auth/logout");
    expect(logoutCall).toBeDefined();
    expect(logoutCall?.headers?.Authorization).toBe("Bearer good");
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
    expect(localStorage.getItem(USER_KEY)).toBeNull();
  });

  it("logout still clears the local session when the server cannot be reached", async () => {
    localStorage.setItem(TOKEN_KEY, "good");
    mock.onGet("/api/users/me").reply(200, alice);
    mock.onPost("/api/auth/logout").networkError();

    renderProvider();
    await waitFor(() => expect(screen.getByTestId("user")).toHaveTextContent("Alice"));
    await userEvent.click(screen.getByText("logout"));

    await waitFor(() => expect(screen.getByTestId("user")).toHaveTextContent("none"));
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
  });

  it("logs the user out when a later request shows the session has expired", async () => {
    localStorage.setItem(TOKEN_KEY, "good");
    mock.onGet("/api/users/me").replyOnce(200, alice);
    renderProvider();
    await waitFor(() => expect(screen.getByTestId("user")).toHaveTextContent("Alice"));

    // the token expires: protected calls now fail and so does /me
    mock.onGet("/api/rooms").reply(403);
    mock.onGet("/api/users/me").reply(403);
    await expect(api.get("/api/rooms")).rejects.toBeTruthy();

    await waitFor(() => expect(screen.getByTestId("user")).toHaveTextContent("none"));
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
  });
});
