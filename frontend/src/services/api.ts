import axios from 'axios'

// Temporary inline types to fix import issue
interface LoginRequest {
  username: string
  password: string
}

interface LoginResponse {
  token: string
  type: string
  user: any
}

const API_BASE_URL = 'http://localhost:8080/api'

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor to add auth token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('authToken')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export const authApi = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/auth/signin', credentials)
    return response.data
  },

  getCurrentUser: async () => {
    const response = await api.get('/auth/me')
    return response.data
  },

  refreshToken: async () => {
    const response = await api.post('/auth/refresh')
    return response.data
  },
}