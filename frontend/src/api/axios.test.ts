import MockAdapter from "axios-mock-adapter";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { Mock } from "vitest";
import api, { TOKEN_KEY, setUnauthorizedHandler } from "./axios";

describe("api client", () => {
  let mock: MockAdapter;
  let onUnauthorized: Mock<() => void>;
  let unregister: () => void;

  beforeEach(() => {
    mock = new MockAdapter(api);
    onUnauthorized = vi.fn<() => void>();
    unregister = setUnauthorizedHandler(onUnauthorized);
  });

  afterEach(() => {
    unregister();
    mock.restore();
  });

  const meCalls = () => mock.history.get.filter((r) => r.url === "/api/users/me").length;

  // ---------------- Authorization header ----------------

  it("sends the bearer token on private endpoints", async () => {
    localStorage.setItem(TOKEN_KEY, "abc");
    mock.onGet("/api/rooms").reply((config) => [200, String(config.headers?.Authorization)]);

    const res = await api.get("/api/rooms");

    expect(res.data).toBe("Bearer abc");
  });

  it("does not send the token to public auth endpoints", async () => {
    localStorage.setItem(TOKEN_KEY, "abc");
    mock.onPost("/api/auth/login").reply((config) => [200, String(config.headers?.Authorization)]);

    const res = await api.post("/api/auth/login", {});

    expect(res.data).toBe("undefined");
  });

  // ---------------- expired / invalid session ----------------

  it("reports a 401 as an expired session", async () => {
    localStorage.setItem(TOKEN_KEY, "abc");
    mock.onGet("/api/rooms").reply(401);

    await expect(api.get("/api/rooms")).rejects.toBeTruthy();

    expect(onUnauthorized).toHaveBeenCalledTimes(1);
  });

  it("treats a 403 as an expired session when /me also fails", async () => {
    localStorage.setItem(TOKEN_KEY, "expired");
    mock.onGet("/api/rooms").reply(403);
    mock.onGet("/api/users/me").reply(403);

    await expect(api.get("/api/rooms")).rejects.toBeTruthy();

    expect(onUnauthorized).toHaveBeenCalledTimes(1);
  });

  it("keeps the session on a 403 when the token is still valid (just not allowed)", async () => {
    localStorage.setItem(TOKEN_KEY, "valid");
    mock.onGet("/api/users").reply(403);
    mock.onGet("/api/users/me").reply(200, { id: "u1" });

    await expect(api.get("/api/users")).rejects.toBeTruthy();

    expect(onUnauthorized).not.toHaveBeenCalled();
  });

  it("checks /me only once when several requests fail with 403 at the same time", async () => {
    localStorage.setItem(TOKEN_KEY, "expired");
    mock.onGet(/\/api\/(rooms|bookings)/).reply(403);
    mock.onGet("/api/users/me").reply(403);

    await Promise.allSettled([api.get("/api/rooms"), api.get("/api/bookings/me")]);

    expect(meCalls()).toBe(1);
  });

  it("does not log out when there is no token", async () => {
    mock.onGet("/api/rooms").reply(401);

    await expect(api.get("/api/rooms")).rejects.toBeTruthy();

    expect(onUnauthorized).not.toHaveBeenCalled();
  });

  it("does not treat a failed login as an expired session", async () => {
    localStorage.setItem(TOKEN_KEY, "stale-token");
    mock.onPost("/api/auth/login").reply(401);

    await expect(api.post("/api/auth/login", {})).rejects.toBeTruthy();

    expect(onUnauthorized).not.toHaveBeenCalled();
  });
});
