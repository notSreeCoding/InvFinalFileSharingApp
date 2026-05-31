import axios from "axios";

const ACCESS_TOKEN_KEY = "accessToken";
const REFRESH_TOKEN_KEY = "refreshToken";
const USER_KEY = "authUser";

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
});

api.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status !== 401 || originalRequest?._retry || originalRequest?.url?.includes("/api/auth/")) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;
    const refreshToken = getRefreshToken();
    if (!refreshToken) {
      clearSession();
      return Promise.reject(error);
    }

    try {
      const response = await axios.post(`${api.defaults.baseURL}/api/auth/refresh`, { refreshToken });
      saveSession(response.data);
      originalRequest.headers.Authorization = `Bearer ${response.data.accessToken}`;
      return api(originalRequest);
    } catch (refreshError) {
      clearSession();
      return Promise.reject(refreshError);
    }
  },
);

export function getSession() {
  const user = localStorage.getItem(USER_KEY);
  return {
    accessToken: getAccessToken(),
    refreshToken: getRefreshToken(),
    user: user ? JSON.parse(user) : null,
  };
}

export function saveSession(data) {
  localStorage.setItem(ACCESS_TOKEN_KEY, data.accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, data.refreshToken);
  localStorage.setItem(USER_KEY, JSON.stringify(data.user));
}

export function clearSession() {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

export async function register(payload) {
  const response = await api.post("/api/auth/register", payload);
  saveSession(response.data);
  return response.data;
}

export async function login(payload) {
  const response = await api.post("/api/auth/login", payload);
  saveSession(response.data);
  return response.data;
}

export async function logout() {
  const refreshToken = getRefreshToken();
  if (refreshToken) {
    await api.post("/api/auth/logout", { refreshToken }).catch(() => {});
  }
  clearSession();
}

export async function listFiles(page = 0, size = 5) {
  const response = await api.get(`/api/files?page=${page}&size=${size}`);
  return response.data;
}

export async function uploadFile(file) {
  const formData = new FormData();
  formData.append("file", file);
  const response = await api.post("/api/files", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
  return response.data;
}

export async function createShare(fileId, payload) {
  const response = await api.post(`/api/files/${fileId}/shares`, payload);
  return response.data;
}

export async function listShares(page = 0, size = 5) {
  const response = await api.get(`/api/shares?page=${page}&size=${size}`);
  return response.data;
}

export async function getPublicShare(token) {
  const response = await api.get(`/api/public/shares/${token}`);
  return response.data;
}
