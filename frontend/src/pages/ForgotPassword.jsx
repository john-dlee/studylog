import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useTheme } from '../context/ThemeContext'

function toResetPath(resetLink) {
  try {
    const url = new URL(resetLink)
    return `${url.pathname}${url.search}`
  } catch {
    return resetLink
  }
}

export default function ForgotPassword() {
  const { forgotPassword } = useAuth()
  const { isDark, toggleTheme } = useTheme()
  const [email, setEmail] = useState('')
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [resetLink, setResetLink] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    setResetLink('')
    setSubmitting(true)
    try {
      const data = await forgotPassword(email)
      setMessage(data.message)
      if (data.resetLink) {
        setResetLink(data.resetLink)
      }
    } catch (err) {
      setError(err.message || 'Could not start password reset')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-shell">
      <button type="button" className="auth-theme-toggle" onClick={toggleTheme}>
        {isDark ? '☀️' : '🌙'}
      </button>
      <div className="auth-card card">
        <div className="dash-brand auth-brand">
          <span className="dash-brand-icon" aria-hidden>
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
              <path
                d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
              />
              <path
                d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"
                stroke="currentColor"
                strokeWidth="2"
              />
            </svg>
          </span>
          <span className="dash-brand-text">Studylog</span>
        </div>
        <h1>Reset password</h1>
        <p className="auth-lead">
          Enter your email. If an account exists, we will email you a reset link.
        </p>
        <form onSubmit={handleSubmit} className="auth-form">
          <label>
            Email
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoComplete="email"
            />
          </label>
          {error && <p className="form-error">{error}</p>}
          {message && <p className="auth-success">{message}</p>}
          {resetLink && (
            <p className="auth-dev-link">
              Email is not configured — use this reset link:{' '}
              <Link to={toResetPath(resetLink)}>{toResetPath(resetLink)}</Link>
            </p>
          )}
          <button type="submit" className="btn-primary" disabled={submitting}>
            {submitting ? 'Sending...' : 'Send reset link'}
          </button>
        </form>
        <p className="auth-footer">
          <Link to="/login">Back to log in</Link>
        </p>
      </div>
    </div>
  )
}
