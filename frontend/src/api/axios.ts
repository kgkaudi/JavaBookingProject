import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
});

// ---------------------------------------------------------
// REQUEST INTERCEPTOR — attach token to every request
// ---------------------------------------------------------
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");

  // Skip adding Authorization for public endpoints
  const isPublic =
    config.url?.includes("/login") ||
    config.url?.includes("/signup") ||
    config.url?.includes("/auth/login") ||
    config.url?.includes("/auth/signup");

  if (token && !isPublic) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

// ---------------------------------------------------------
// RESPONSE INTERCEPTOR — auto logout on 401
// ---------------------------------------------------------
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const status = err.response?.status;

    if (status === 401) {
      // Token invalid or expired → logout user
      localStorage.removeItem("token");
      localStorage.removeItem("roles");
      localStorage.removeItem("user");

      // Redirect to login
      window.location.href = "/login";
    }

    return Promise.reject(err);
  }
);

export default api;
