import axios from "axios";
import type { ApiErrorBody } from "../types";

/**
 * Turns any thrown value into a message that is safe to show to the user.
 * Understands the backend's JSON error format ({ message, fieldErrors }) and
 * never returns an object (rendering an object in a toast crashes React).
 */
export function getErrorMessage(err: unknown, fallback = "Something went wrong"): string {
  if (!axios.isAxiosError(err)) {
    return fallback;
  }

  if (!err.response) {
    return "Cannot reach the server. Check your connection and try again.";
  }

  const data: unknown = err.response.data;

  if (typeof data === "string" && data.trim()) {
    return data;
  }

  if (data && typeof data === "object") {
    const body = data as ApiErrorBody;

    const fieldMessages = body.fieldErrors ? Object.values(body.fieldErrors) : [];
    if (fieldMessages.length > 0) {
      return fieldMessages.join(". ");
    }
    if (typeof body.reason === "string" && body.reason) {
      return body.reason;
    }
    if (typeof body.message === "string" && body.message) {
      return body.message;
    }
  }

  if (err.response.status === 403) {
    return "You are not allowed to do that.";
  }

  return fallback;
}
