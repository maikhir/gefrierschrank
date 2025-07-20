import React, { createContext, useContext, useState, useEffect } from 'react'
import { User, LoginRequest, AuthContextType } from '../types/auth'
import { authApi } from '../services/api'

const AuthContext = createContext<AuthContextType | null>(null)

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null)
  const [token, setToken] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const isAuthenticated = !!token && !!user

  useEffect(() => {
    const initAuth = async () => {
      const savedToken = localStorage.getItem('authToken')
      const savedUser = localStorage.getItem('user')
      
      if (savedToken && savedUser) {
        try {
          setToken(savedToken)
          setUser(JSON.parse(savedUser))
          // Verify token is still valid
          await authApi.getCurrentUser()
        } catch (error) {
          // Token is invalid, clear stored data
          localStorage.removeItem('authToken')
          localStorage.removeItem('user')
          setToken(null)
          setUser(null)
        }
      }
      setIsLoading(false)
    }

    initAuth()
  }, [])

  const login = async (credentials: LoginRequest) => {
    try {
      const response = await authApi.login(credentials)
      const { token: newToken, user: newUser } = response
      
      setToken(newToken)
      setUser(newUser)
      localStorage.setItem('authToken', newToken)
      localStorage.setItem('user', JSON.stringify(newUser))
    } catch (error) {
      throw error
    }
  }

  const logout = () => {
    setToken(null)
    setUser(null)
    localStorage.removeItem('authToken')
    localStorage.removeItem('user')
  }

  if (isLoading) {
    return <div>Loading...</div>
  }

  const value: AuthContextType = {
    user,
    token,
    isAuthenticated,
    login,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}