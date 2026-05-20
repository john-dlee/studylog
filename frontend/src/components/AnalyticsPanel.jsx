import { useMemo, useState } from 'react'
import {
  buildLast7DaysChart,
  buildSubjectPie,
  formatPieGradient,
} from '../utils/analyticsData'
import { formatDurationMinutes } from '../utils/datetime'

const TABS = ['Trend', 'Subjects']

export default function AnalyticsPanel({ sessions = [] }) {
  const [tab, setTab] = useState('Trend')

  const weekly = useMemo(() => buildLast7DaysChart(sessions), [sessions])
  const pie = useMemo(() => buildSubjectPie(sessions), [sessions])

  return (
    <section className="card analytics-card">
      <div className="analytics-tabs" role="tablist">
        {TABS.map((t) => (
          <button
            key={t}
            type="button"
            className={tab === t ? 'active' : ''}
            onClick={() => setTab(t)}
          >
            {t}
          </button>
        ))}
      </div>

      {tab === 'Trend' && (
        <WeeklyBarChart days={weekly.days} yTicks={weekly.yTicks} yMax={weekly.yMax} />
      )}

      {tab === 'Subjects' && (
        <SubjectsPieChart segments={pie.segments} totalMinutes={pie.totalMinutes} />
      )}
    </section>
  )
}

function WeeklyBarChart({ days, yTicks, yMax }) {
  return (
    <>
      <h3 className="analytics-subtitle">This week (Mon–Sun)</h3>
      <div className="bar-chart">
        <div className="chart-y-axis" aria-hidden>
          {yTicks.map((tick, i) => (
            <span key={`${tick}-${i}`}>{tick}m</span>
          ))}
        </div>
        <div className="chart-bars" role="img" aria-label="Study minutes per day this week, Monday through Sunday">
          {days.map((day) => {
            const heightPct = yMax > 0 ? (day.minutes / yMax) * 100 : 0
            const title = `${day.label}: ${formatDurationMinutes(day.minutes)}`
            return (
              <div key={day.key} className="chart-bar-col" title={title}>
                <div className="chart-bar-plot">
                  <div
                    className="chart-bar"
                    style={{ height: `${Math.max(heightPct, day.minutes > 0 ? 4 : 0)}%` }}
                  />
                </div>
                <span className="chart-bar-label">{day.label}</span>
              </div>
            )
          })}
        </div>
      </div>
    </>
  )
}

function SubjectsPieChart({ segments, totalMinutes }) {
  const gradient = formatPieGradient(segments)

  if (segments.length === 0) {
    return (
      <>
        <h3 className="analytics-subtitle">By subject</h3>
        <p className="analytics-empty">No study time to show yet.</p>
      </>
    )
  }

  return (
    <>
      <h3 className="analytics-subtitle">By subject</h3>
      <div className="pie-chart-layout">
        <div
          className="pie-chart"
          style={{ background: `conic-gradient(${gradient})` }}
          role="img"
          aria-label="Study time by subject"
        />
        <ul className="pie-legend">
          {segments.map((seg) => (
            <li key={seg.label}>
              <span className="pie-swatch" style={{ backgroundColor: seg.color }} aria-hidden />
              <span className="pie-legend-label">{seg.label}</span>
              <span className="pie-legend-pct">{Math.round(seg.percent)}%</span>
            </li>
          ))}
        </ul>
      </div>
      <p className="pie-total muted">
        {formatDurationMinutes(totalMinutes)} total across {segments.length}{' '}
        {segments.length === 1 ? 'category' : 'categories'}
      </p>
    </>
  )
}
