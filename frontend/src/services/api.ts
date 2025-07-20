import axios from 'axios';
import { LoginRequest, LoginResponse, Item, CreateItemRequest, UpdateItemRequest, Category } from '../types';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle auth errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const authAPI = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/auth/signin', credentials);
    return response.data;
  },
};

export const itemsAPI = {
  getAll: async (): Promise<Item[]> => {
    const response = await api.get<Item[]>('/items');
    return response.data;
  },

  getById: async (id: number): Promise<Item> => {
    const response = await api.get<Item>(`/items/${id}`);
    return response.data;
  },

  create: async (item: CreateItemRequest): Promise<Item> => {
    const response = await api.post<Item>('/items', item);
    return response.data;
  },

  update: async (id: number, item: UpdateItemRequest): Promise<Item> => {
    const response = await api.put<Item>(`/items/${id}`, item);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/items/${id}`);
  },

  search: async (query: string): Promise<Item[]> => {
    const response = await api.get<Item[]>(`/items/search?q=${encodeURIComponent(query)}`);
    return response.data;
  },

  getExpiring: async (days: number = 7): Promise<Item[]> => {
    const response = await api.get<Item[]>(`/items/expiring?days=${days}`);
    return response.data;
  },

  getExpired: async (): Promise<Item[]> => {
    const response = await api.get<Item[]>('/items/expired');
    return response.data;
  },
};

export const categoriesAPI = {
  getAll: async (): Promise<Category[]> => {
    const response = await api.get<Category[]>('/categories');
    return response.data;
  },

  getById: async (id: number): Promise<Category> => {
    const response = await api.get<Category>(`/categories/${id}`);
    return response.data;
  },
};

export default api;