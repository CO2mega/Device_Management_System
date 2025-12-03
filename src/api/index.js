import axios from 'axios';

// Create axios instance with default config
const api = axios.create({
  baseURL: process.env.VUE_APP_API_URL || 'http://localhost:8081/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Request interceptor - add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - handle errors
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response) {
      // Handle 401 Unauthorized
      if (error.response.status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/';
      }
      // Handle other errors
      const message = error.response.data?.message || '请求失败';
      console.error('API Error:', message);
    }
    return Promise.reject(error);
  }
);

export default api;
