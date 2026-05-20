import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { apiFetch, clearAuth } from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [token, setToken] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')
    if (storedToken && storedUser) {
      setToken(storedToken)
      setUser(JSON.parse(storedUser))
    }
    setLoading(false)
  }, [])

  const login = useCallback(async (email, password) => {
    const data = await apiFetch('/api/users/login', {
      method: 'POST',
      skipAuth: true,
      body: JSON.stringify({ email, password }),
    })
    const nextUser = { id: data.id, username: data.username, email: data.email }
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(nextUser))
    setToken(data.token)
    setUser(nextUser)
    return nextUser
  }, [])

  const register = useCallback(async (username, email, password) => {
    await apiFetch('/api/users/register', {
      method: 'POST',
      skipAuth: true,
      body: JSON.stringify({ username, email, password }),
    })
  }, [])

  const forgotPassword = useCallback(async (email) => {
    return apiFetch('/api/users/forgot-password', {
      method: 'POST',
      skipAuth: true,
      body: JSON.stringify({ email }),
    })
  }, [])

  const resetPassword = useCallback(async (token, newPassword) => {
    return apiFetch('/api/users/reset-password', {
      method: 'POST',
      skipAuth: true,
      body: JSON.stringify({ token, newPassword }),
    })
  }, [])

  const logout = useCallback(() => {
    clearAuth()
    setToken(null)
    setUser(null)
  }, [])

  const value = useMemo(
    () => ({
      user,
      token,
      loading,
      isAuthenticated: Boolean(token),
      login,
      register,
      forgotPassword,
      resetPassword,
      logout,
    }),
    [user, token, loading, login, register, forgotPassword, resetPassword, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}
