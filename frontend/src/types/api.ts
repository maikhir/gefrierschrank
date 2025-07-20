export interface User {
  id: number;
  username: string;
  email: string;
  role: 'USER' | 'ADMIN';
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  id: number;
  username: string;
  email: string;
  roles: string[];
}

export interface Category {
  id: number;
  name: string;
  icon: string;
  defaultUnit: string;
  unitStep: number;
  minValue: number;
  maxValue: number;
  createdAt: string;
  updatedAt: string;
}

export interface Item {
  id: number;
  name: string;
  categoryId: number;
  categoryName: string;
  quantity: number;
  unit: string;
  expiryDate?: string;
  expiryType: 'USE_BY' | 'BEST_BEFORE';
  photoPath?: string;
  description?: string;
  userId: number;
  username: string;
  createdAt: string;
  updatedAt: string;
  expiringSoon: boolean;
  expired: boolean;
  daysUntilExpiry: number;
}

export interface CreateItemRequest {
  name: string;
  categoryId: number;
  quantity: number;
  unit: string;
  expiryDate?: string;
  expiryType: 'USE_BY' | 'BEST_BEFORE';
  photoPath?: string;
  description?: string;
}

export interface UpdateItemRequest extends CreateItemRequest {}

export interface ItemStatistics {
  totalItems: number;
  expiringSoon: number;
  expired: number;
}

export interface ApiError {
  message: string;
  status: number;
}