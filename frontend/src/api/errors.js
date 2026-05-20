const LOGIN_ERROR = 'Incorrect email or password'

function normalizeBody(body) {
  if (typeof body === 'string') {
    const trimmed = body.trim()
    if (trimmed.startsWith('{') || trimmed.startsWith('[')) {
      try {
        return JSON.parse(trimmed)
      } catch {
        return trimmed
      }
    }
    return trimmed
  }
  return body
}

export function parseApiError(body, fallback = 'Request failed') {
  const data = normalizeBody(body)

  if (typeof data !== 'object' || data === null) {
    if (typeof data === 'string' && data.trim()) {
      return data
    }
    return fallback
  }

  const detail = data.detail
  if (typeof detail === 'string' && detail.trim()) {
    return detail
  }

  if (detail != null && typeof detail !== 'object') {
    return String(detail)
  }

  if (data.errors && typeof data.errors === 'object') {
    const first = Object.values(data.errors).find((v) => typeof v === 'string' && v.trim())
    if (first) return first
  }

  if (typeof data.message === 'string' && data.message.trim()) {
    return data.message
  }

  if (typeof data.title === 'string' && data.title.trim()) {
    const title = data.title
    if (title !== 'Unauthorized' && title !== 'Conflict' && title !== 'Invalid Request Body') {
      return title
    }
  }

  return fallback
}

export function loginErrorMessage(body) {
  return parseApiError(body, LOGIN_ERROR)
}
