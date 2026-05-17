import { useEffect, useState } from 'react'
import { apiFetch } from '../api/client'

function formatDateTime(value) {
  if (!value) return '—'
  return new Date(value).toLocaleString()
}

export default function Sessions() {
  const [sessions, setSessions] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    async function loadSessions() {
      setLoading(true)
      setError('')
      try {
        const data = await apiFetch('/api/study-sessions')
        setSessions(data)
      } catch (err) {
        setError(err.message || 'Failed to load sessions')
      } finally {
        setLoading(false)
      }
    }
    loadSessions()
  }, [])

  return (
    <div>
      <h1>Study sessions</h1>
      {error && <p className="form-error">{error}</p>}
      {loading && <p>Loading sessions...</p>}
      {!loading && sessions.length === 0 && <p>No study sessions yet.</p>}
      {!loading && sessions.length > 0 && (
        <ul className="data-list sessions-list">
          {sessions.map((session) => (
            <li key={session.id}>
              <strong>{session.subjectName ?? 'Subject'}</strong> — {session.sessionType}
              <br />
              {formatDateTime(session.startAt)} → {formatDateTime(session.endedAt)}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
