import axios, { type AxiosError } from "axios";

declare module "axios" {
  interface AxiosRequestConfig {
    /** internal: do not run the expired-session check for this request */
    skipSessionCheck?: boolean;
  }
}

export const TOKEN_KEY = "token";
export const USER_KEY = "user";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? "http://localhost:8080",
});

// Endpoints that must NOT receive the Authorization header
const PUBLIC_PATHS = [
  "/api/auth/login",
  "/api/auth/signup",
  "/api/auth/request-reset",
  "/api/auth/reset-password",
];

const isPublic = (url?: string) => !!url && PUBLIC_PATHS.some((p) => url.startsWith(p));

// ---------------------------------------------------------
// Session-expiry notification (registered by AuthProvider)
// ---------------------------------------------------------
let unauthorizedHandler: (() => void) | null = null;

export function setUnauthorizedHandler(handler: () => void) {
  unauthorizedHandler = handler;
  return () => {
    if (unauthorizedHandler === handler) {
      unauthorizedHandler = null;
    }
  };
}

// ---------------------------------------------------------
// REQUEST INTERCEPTOR — attach token to non-public requests
// ---------------------------------------------------------
api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);

  if (token && !isPublic(config.url)) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

// ---------------------------------------------------------
// RESPONSE INTERCEPTOR — detect an expired / invalid session
//
// The backend answers 401 *or* 403 for a bad token, and 403 is also used for
// "logged in but not allowed". So on 403 we ask /api/users/me whether the
// token is still valid before deciding to log the user out.
// ---------------------------------------------------------
let probe: Promise<boolean> | null = null;

function sessionIsValid(): Promise<boolean> {
  if (!probe) {
    probe = api
      .get("/api/users/me", { skipSessionCheck: true })
      .then(() => true)
      .catch(() => false)
      .finally(() => {
        probe = null;
      });
  }
  return probe;
}

api.interceptors.response.use(
  (res) => res,
  async (error: AxiosError) => {
    const status = error.response?.status;
    const config = error.config;
    const hadToken = !!localStorage.getItem(TOKEN_KEY);

    if (hadToken && config && !config.skipSessionCheck && !isPublic(config.url)) {
      if (status === 401) {
        unauthorizedHandler?.();
      } else if (status === 403 && !(await sessionIsValid())) {
        unauthorizedHandler?.();
      }
    }

    return Promise.reject(error);
  }
);

export default api;
