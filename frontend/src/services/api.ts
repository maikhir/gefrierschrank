import { 
  Category, 
  Item, 
  CreateItemRequest, 
  UpdateItemRequest, 
  ItemStatistics 
} from '../types/api';
import { authService } from './auth';

const API_BASE_URL = 'http://localhost:8082/api';

class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = 'ApiError';
  }
}

async function apiRequest<T>(
  endpoint: string, 
  options: RequestInit = {}
): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;
  const headers = {
    'Content-Type': 'application/json',
    ...authService.getAuthHeaders(),
    ...options.headers,
  };

  const response = await fetch(url, {
    ...options,
    headers,
  });

  if (!response.ok) {
    let errorMessage = `HTTP ${response.status}`;
    try {
      const errorData = await response.json();
      errorMessage = errorData.message || errorMessage;
    } catch {
      errorMessage = response.statusText || errorMessage;
    }
    throw new ApiError(response.status, errorMessage);
  }

  if (response.status === 204) {
    return {} as T; // No content
  }

  return response.json();
}

// Category API
export const categoryApi = {
  getAll: (): Promise<Category[]> => 
    apiRequest('/categories'),

  getById: (id: number): Promise<Category> => 
    apiRequest(`/categories/${id}`),

  create: (category: Omit<Category, 'id' | 'createdAt' | 'updatedAt'>): Promise<Category> => 
    apiRequest('/categories', {
      method: 'POST',
      body: JSON.stringify(category),
    }),

  update: (id: number, category: Omit<Category, 'id' | 'createdAt' | 'updatedAt'>): Promise<Category> => 
    apiRequest(`/categories/${id}`, {
      method: 'PUT',
      body: JSON.stringify(category),
    }),

  delete: (id: number): Promise<void> => 
    apiRequest(`/categories/${id}`, {
      method: 'DELETE',
    }),

  exists: (id: number): Promise<boolean> => 
    apiRequest(`/categories/${id}/exists`),
};

// Item API
export const itemApi = {
  getAll: (): Promise<Item[]> => 
    apiRequest('/items'),

  getById: (id: number): Promise<Item> => 
    apiRequest(`/items/${id}`),

  create: (item: CreateItemRequest): Promise<Item> => 
    apiRequest('/items', {
      method: 'POST',
      body: JSON.stringify(item),
    }),

  update: (id: number, item: UpdateItemRequest): Promise<Item> => 
    apiRequest(`/items/${id}`, {
      method: 'PUT',
      body: JSON.stringify(item),
    }),

  delete: (id: number): Promise<void> => 
    apiRequest(`/items/${id}`, {
      method: 'DELETE',
    }),

  search: (query: string): Promise<Item[]> => 
    apiRequest(`/items/search?q=${encodeURIComponent(query)}`),

  getByCategory: (categoryId: number): Promise<Item[]> => 
    apiRequest(`/items/category/${categoryId}`),

  getExpiring: (days: number = 7): Promise<Item[]> => 
    apiRequest(`/items/expiring?days=${days}`),

  getExpired: (): Promise<Item[]> => 
    apiRequest('/items/expired'),

  getFiltered: (params: {
    categoryId?: number;
    search?: string;
    expiringSoon?: boolean;
    expiryDays?: number;
    sortBy?: string;
    page?: number;
    size?: number;
  }): Promise<{
    content: Item[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
  }> => {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        searchParams.append(key, String(value));
      }
    });
    return apiRequest(`/items/filter?${searchParams.toString()}`);
  },

  getStatistics: (): Promise<ItemStatistics> => 
    apiRequest('/items/stats'),

  getExpiringSoonCount: (days: number = 7): Promise<number> => 
    apiRequest(`/items/stats/expiring?days=${days}`),
};

export { ApiError };