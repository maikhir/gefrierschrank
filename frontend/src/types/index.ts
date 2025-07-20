export interface User {
  id: number;
  username: string;
  email: string;
  roles: string[];
}

export interface LoginResponse {
  token: string;
  id: number;
  username: string;
  email: string;
  roles: string[];
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface Category {
  id: number;
  name: string;
  icon: string;
  defaultUnit: string;
  unitStep: number;
  minValue: number;
  maxValue: number;
}

export interface Item {
  id: number;
  name: string;
  categoryId: number;
  categoryName: string;
  quantity: number;
  unit: string;
  expiryDate: string;
  expiryType: 'MHD' | 'VD';
  description?: string;
  photoPath?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface CreateItemRequest {
  name: string;
  categoryId: number;
  quantity: number;
  unit: string;
  expiryDate: string;
  expiryType: 'MHD' | 'VD';
  description?: string;
  photoPath?: string;
}

export interface UpdateItemRequest extends CreateItemRequest {}

export interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  isLoading: boolean;
}