import { AxiosError, type InternalAxiosRequestConfig } from "axios";
import { describe, expect, it } from "vitest";
import { getErrorMessage } from "./errors";

function httpError(status: number, data: unknown): AxiosError {
  const config = { headers: {} } as InternalAxiosRequestConfig;
  return new AxiosError("Request failed", "ERR_BAD_REQUEST", config, null, {
    status,
    statusText: "",
    headers: {},
    config,
    data,
  });
}

describe("getErrorMessage", () => {
  it("uses the message of the backend error body", () => {
    const err = httpError(409, { status: 409, error: "Conflict", message: "Email already in use" });
    expect(getErrorMessage(err)).toBe("Email already in use");
  });

  it("prefers field errors over the generic 'Validation failed' message", () => {
    const err = httpError(400, {
      message: "Validation failed",
      fieldErrors: { email: "Email must be a valid address", password: "Password is required" },
    });
    expect(getErrorMessage(err)).toBe("Email must be a valid address. Password is required");
  });

  it("uses the 'reason' returned by the availability endpoint", () => {
    const err = httpError(409, { available: false, reason: "Dates overlap with existing booking" });
    expect(getErrorMessage(err)).toBe("Dates overlap with existing booking");
  });

  it("still supports plain-text error bodies", () => {
    expect(getErrorMessage(httpError(400, "Invalid credentials"))).toBe("Invalid credentials");
  });

  it("never returns an object (rendering one in a toast would crash React)", () => {
    const result = getErrorMessage(httpError(500, { unexpected: { nested: true } }), "Fallback");
    expect(typeof result).toBe("string");
    expect(result).toBe("Fallback");
  });

  it("explains a network failure", () => {
    const err = new AxiosError("Network Error", "ERR_NETWORK");
    expect(getErrorMessage(err)).toMatch(/cannot reach the server/i);
  });

  it("has a friendly default for 403 without a body", () => {
    expect(getErrorMessage(httpError(403, ""))).toBe("You are not allowed to do that.");
  });

  it("falls back for non-axios errors", () => {
    expect(getErrorMessage(new Error("boom"), "Fallback")).toBe("Fallback");
    expect(getErrorMessage("whatever", "Fallback")).toBe("Fallback");
  });
});
