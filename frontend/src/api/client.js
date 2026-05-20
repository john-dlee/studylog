import { loginErrorMessage, parseApiError } from './errors'

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? ''

function getToken() {
  return localStorage.getItem('token')
}

export function clearAuth() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}

export async function apiFetch(path, options = {}) {
  const { skipAuth = false, ...fetchOptions } = options

  const headers = {
    'Content-Type': 'application/json',
    ...fetchOptions.headers,
  }

  const token = skipAuth ? null : getToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...fetchOptions,
    headers,
  })

  if (response.status === 204) {
    return null
  }

  const rawText = await response.text()
  const contentType = response.headers.get('content-type') ?? ''
  let body = rawText

  if (rawText && (contentType.includes('json') || rawText.trim().startsWith('{'))) {
    try {
      body = JSON.parse(rawText)
    } catch {
      body = rawText
    }
  }

  if (response.status === 401) {
    const message = skipAuth
      ? loginErrorMessage(body)
      : parseApiError(body, 'Session expired. Please log in again.')

    if (token) {
      clearAuth()
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    }

    throw new Error(message)
  }

  if (!response.ok) {
    throw new Error(parseApiError(body, 'Request failed'))
  }

  return body
}
