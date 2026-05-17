const API_BASE = import.meta.env.VITE_API_BASE_URL ?? ''

function getToken() {
  return localStorage.getItem('token')
}

export function clearAuth() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}

export async function apiFetch(path, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  }

  const token = getToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  })

  if (response.status === 204) {
    return null
  }

  const contentType = response.headers.get('content-type')
  const body = contentType?.includes('application/json')
    ? await response.json()
    : await response.text()

  if (response.status === 401) {
    if (token) {
      clearAuth()
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    }
    const message =
      typeof body === 'object' && body?.detail ? body.detail : 'Unauthorized'
    throw new Error(message)
  }

  if (!response.ok) {
    const message =
      typeof body === 'object' && body?.detail
        ? body.detail
        : typeof body === 'object' && body?.title
          ? body.title
          : 'Request failed'
    throw new Error(message)
  }

  return body
}
