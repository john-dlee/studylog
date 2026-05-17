export default function RecentSessions({ sessions, loading }) {
  return (
    <section className="card recent-card">
      <h2 className="card-title">
        <span className="title-icon">📅</span> Recent Sessions
      </h2>
      {loading && <p className="muted">Loading...</p>}
      {!loading && sessions.length === 0 && (
        <p className="empty-message">
          No study sessions yet. Start a timer to record your first session!
        </p>
      )}
      {!loading && sessions.length > 0 && (
        <ul className="recent-list">
          {sessions.slice(0, 8).map((s) => (
            <li key={s.id}>
              <div className="recent-row-top">
                <strong>{s.subjectName ?? 'No tag'}</strong>
                <span className="badge">{s.sessionType}</span>
              </div>
              <span className="muted">
                {formatRange(s.startAt, s.endedAt)}
              </span>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

function formatRange(start, end) {
  const s = new Date(start)
  const e = new Date(end)
  const opts = { month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit' }
  const mins = Math.round((e - s) / 60000)
  return `${s.toLocaleString(undefined, opts)} · ${mins}m`
}
