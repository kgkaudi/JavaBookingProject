import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  // Skip adding Authorization for public endpoints
  if (token && !config.url?.includes("/login") && !config.url?.includes("/signup")) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});


export default api;
