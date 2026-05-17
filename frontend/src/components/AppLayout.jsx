import { Link, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function AppLayout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <div className="app-layout">
      <header className="app-header">
        <Link to="/" className="brand">
          Studylog
        </Link>
        <nav className="app-nav">
          <Link to="/">Dashboard</Link>
          <Link to="/subjects">Subjects</Link>
          <Link to="/sessions">Sessions</Link>
        </nav>
        <div className="user-bar">
          <span>{user?.username}</span>
          <button type="button" onClick={handleLogout}>
            Log out
          </button>
        </div>
      </header>
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  )
}
