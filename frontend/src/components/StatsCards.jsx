import { formatDurationMs } from '../utils/datetime'

const STAT_CONFIG = [
  { key: 'todayMs', label: 'Today', icon: '🕐', color: 'stat-green' },
  { key: 'weekMs', label: 'This Week', icon: '📈', color: 'stat-blue' },
  { key: 'streak', label: 'Streak', icon: '🔥', color: 'stat-orange', format: (v) => `${v} days` },
  { key: 'totalMs', label: 'Total', icon: '🎯', color: 'stat-purple' },
]

export default function StatsCards({ stats }) {
  return (
    <div className="stats-grid">
      {STAT_CONFIG.map(({ key, label, icon, color, format }) => {
        const raw = stats[key]
        const value = format
          ? format(raw)
          : formatDurationMs(raw)
        return (
          <div key={key} className={`card stat-card ${color}`}>
            <span className="stat-icon" aria-hidden>
              {icon}
            </span>
            <div>
              <p className="stat-label">{label}</p>
              <p className="stat-value">{value}</p>
            </div>
          </div>
        )
      })}
    </div>
  )
}
