import api from "./axios";

export const login = async (email: string, password: string) => {
  const res = await api.post("/api/auth/login", {
    email: email,
    password: password
  });
  return res.data;
};

export const signup = async (data: any) => {
  const res = await api.post("/api/auth/signup", data);
  return res.data;
};

export const logout = async () => {
  await api.post("/api/auth/logout");
};
