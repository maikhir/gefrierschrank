export interface User {
  id: number
  username: string
  email: string
  role: 'USER' | 'ADMIN'
  notificationsEnabled: boolean
  createdAt: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  type: string
  user: User
}

export interface AuthContextType {
  user: User | null
  token: string | null
  isAuthenticated: boolean
  login: (credentials: LoginRequest) => Promise<void>
  logout: () => void
}