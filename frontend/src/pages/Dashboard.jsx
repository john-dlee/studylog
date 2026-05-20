import { useCallback, useEffect, useState } from 'react'
import { apiFetch } from '../api/client'
import AnalyticsPanel from '../components/AnalyticsPanel'
import DashboardHeader from '../components/DashboardHeader'
import RecentSessions from '../components/RecentSessions'
import StatsCards from '../components/StatsCards'
import StudyActivityHeatmap from '../components/StudyActivityHeatmap'
import StudySessionPanel from '../components/StudySessionPanel'
import { computeStats } from '../utils/sessionStats'

export default function Dashboard() {
  const [sessions, setSessions] = useState([])
  const [loading, setLoading] = useState(true)

  const loadSessions = useCallback(async () => {
    setLoading(true)
    try {
      const data = await apiFetch('/api/study-sessions')
      setSessions(data)
    } catch {
      setSessions([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadSessions()
  }, [loadSessions])

  const stats = computeStats(sessions)

  return (
    <div className="dashboard">
      <DashboardHeader />
      <div className="dashboard-grid">
        <div className="dashboard-left">
          <StudySessionPanel onSessionSaved={loadSessions} />
        </div>
        <div className="dashboard-right">
          <StatsCards stats={stats} />
          <StudyActivityHeatmap sessions={sessions} />
          <div className="dashboard-bottom-row">
            <AnalyticsPanel sessions={sessions} />
            <RecentSessions sessions={sessions} loading={loading} />
          </div>
        </div>
      </div>
    </div>
  )
}
