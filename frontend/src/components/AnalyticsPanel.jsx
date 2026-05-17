import { useState } from 'react'

const TABS = ['Weekly', 'Trend', 'Subjects']
const DAYS = ['Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun', 'Mon']

export default function AnalyticsPanel() {
  const [tab, setTab] = useState('Weekly')

  return (
    <section className="card analytics-card">
      <div className="analytics-tabs">
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
      <h3 className="analytics-subtitle">Last 7 Days</h3>
      <div className="bar-chart-placeholder">
        <div className="chart-y-axis">
          <span>60m</span>
          <span>45m</span>
          <span>30m</span>
          <span>15m</span>
          <span>0m</span>
        </div>
        <div className="chart-bars">
          {DAYS.map((d) => (
            <div key={d} className="chart-bar-col">
              <div className="chart-bar" style={{ height: '4%' }} />
              <span>{d}</span>
            </div>
          ))}
        </div>
      </div>
      <p className="placeholder-note">Charts — {tab} view coming soon</p>
    </section>
  )
}
