import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
});

// ---------------------------------------------------------
// REQUEST INTERCEPTOR — attach token to every request
// ---------------------------------------------------------
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");

  const isPublic =
    config.url?.includes("/login") ||
    config.url?.includes("/signup") ||
    config.url?.includes("/auth/login") ||
    config.url?.includes("/auth/signup");

  if (token && !isPublic) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

// ---------------------------------------------------------
// RESPONSE INTERCEPTOR — avoid logout loop on /me
// ---------------------------------------------------------
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const status = err.response?.status;
    const url = err.config?.url;

    const isMeEndpoint =
      url?.includes("/users/me") || url?.includes("/api/users/me");

    if (status === 401 && !isMeEndpoint) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      window.location.href = "/login";
    }

    return Promise.reject(err);
  }
);

export default api;
