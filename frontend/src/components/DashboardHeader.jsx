import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useTheme } from '../context/ThemeContext'

export default function DashboardHeader() {
  const { user, logout } = useAuth()
  const { toggleTheme, isDark } = useTheme()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)

  const initial = (user?.username?.[0] ?? user?.email?.[0] ?? '?').toUpperCase()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <header className="dash-header">
      <div className="dash-brand">
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

      <div className="dash-header-actions">
        <button
          type="button"
          className="icon-btn"
          onClick={toggleTheme}
          aria-label={isDark ? 'Switch to light mode' : 'Switch to dark mode'}
        >
          {isDark ? (
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="4" />
              <path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.41-1.41M17.66 6.34l1.41-1.41" />
            </svg>
          ) : (
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
            </svg>
          )}
        </button>

        <div className="profile-menu-wrap">
          <button
            type="button"
            className="profile-avatar"
            onClick={() => setMenuOpen((o) => !o)}
            aria-label="Account menu"
          >
            {initial}
          </button>
          {menuOpen && (
            <div className="profile-dropdown">
              <p className="profile-name">{user?.username}</p>
              <p className="profile-email">{user?.email}</p>
              <button type="button" onClick={handleLogout}>
                Log out
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  )
}
