import { useCallback, useEffect, useRef, useState } from 'react'
import { apiFetch } from '../api/client'
import { useStudyTimer } from '../hooks/useStudyTimer'

export default function StudySessionPanel({ onSessionSaved }) {
  const [subjects, setSubjects] = useState([])
  const [selectedSubjectId, setSelectedSubjectId] = useState(null)
  const [newTag, setNewTag] = useState('')
  const [tagError, setTagError] = useState('')
  const [saving, setSaving] = useState(false)
  const subjectIdRef = useRef(null)
  subjectIdRef.current = selectedSubjectId

  const timer = useStudyTimer({ onSessionSaved, subjectIdRef })
  const sessionActive = timer.status === 'running' || timer.status === 'paused'

  const loadSubjects = useCallback(async () => {
    try {
      const data = await apiFetch('/api/subjects')
      setSubjects(data)
    } catch {
      setSubjects([])
    }
  }, [])

  useEffect(() => {
    loadSubjects()
  }, [loadSubjects])

  async function addTag(e) {
    e.preventDefault()
    if (sessionActive) return
    const name = newTag.trim()
    if (!name) return
    setTagError('')
    try {
      await apiFetch('/api/subjects', {
        method: 'POST',
        body: JSON.stringify({ name }),
      })
      setNewTag('')
      await loadSubjects()
    } catch (err) {
      setTagError(err.message || 'Could not add tag')
    }
  }

  function toggleTag(id) {
    if (sessionActive) return
    setSelectedSubjectId((prev) => (prev === id ? null : id))
  }

  async function handleEndSession() {
    const message =
      'End this study session and save your progress?'
    if (!window.confirm(message)) return

    setSaving(true)
    try {
      await timer.stopEarly(selectedSubjectId ?? null)
    } catch (err) {
      alert(err.message || 'Could not save session')
    } finally {
      setSaving(false)
    }
  }

  function handlePrimaryAction() {
    if (timer.status === 'idle') timer.start()
    else if (timer.status === 'running') timer.pause()
    else if (timer.status === 'paused') timer.resume()
  }

  const primaryLabel =
    timer.status === 'idle'
      ? 'Start'
      : timer.status === 'running'
        ? 'Pause'
        : 'Resume'

  const canEndSession =
    timer.timerMode === 'stopwatch' || timer.phase === 'work'
  const showEndSession = sessionActive && canEndSession

  return (
    <section className="card session-card">
      <h2 className="card-title">Study Session</h2>

      <div className="segmented">
        <button
          type="button"
          className={timer.timerMode === 'pomodoro' ? 'active' : ''}
          onClick={() => timer.switchTimerMode('pomodoro')}
          disabled={sessionActive}
        >
          Pomodoro
        </button>
        <button
          type="button"
          className={timer.timerMode === 'stopwatch' ? 'active' : ''}
          onClick={() => timer.switchTimerMode('stopwatch')}
          disabled={sessionActive}
        >
          Stopwatch
        </button>
      </div>

      {timer.timerMode === 'pomodoro' && (
        <div className="phase-row">
          <div className="phase-tabs">
            <button
              type="button"
              className={timer.phase === 'work' ? 'active' : ''}
              onClick={() => timer.switchPhase('work')}
              disabled={sessionActive}
            >
              Work
            </button>
            <button
              type="button"
              className={timer.phase === 'shortBreak' ? 'active' : ''}
              onClick={() => timer.switchPhase('shortBreak')}
              disabled={sessionActive}
            >
              Short Break
            </button>
            <button
              type="button"
              className={timer.phase === 'longBreak' ? 'active' : ''}
              onClick={() => timer.switchPhase('longBreak')}
              disabled={sessionActive}
            >
              Long Break
            </button>
          </div>
        </div>
      )}

      <div
        className="timer-ring"
        style={{ '--progress': `${timer.progress}%` }}
      >
        <div className="timer-inner">
          <span className="timer-value">{timer.displayTime}</span>
          <span className="timer-phase">{timer.phaseLabel}</span>
        </div>
      </div>

      <div className="timer-actions">
        <button
          type="button"
          className="btn-primary"
          onClick={handlePrimaryAction}
          disabled={saving}
        >
          {primaryLabel}
        </button>
        {showEndSession && (
          <button
            type="button"
            className="btn-secondary"
            onClick={handleEndSession}
            disabled={saving}
          >
            {saving ? 'Saving...' : 'End session'}
          </button>
        )}
        <button
          type="button"
          className="btn-icon"
          onClick={timer.reset}
          aria-label="Reset"
          disabled={saving || sessionActive}
          title={sessionActive ? 'End session first' : 'Reset timer'}
        >
          ↺
        </button>
        <button
          type="button"
          className="btn-icon"
          aria-label="Settings"
          disabled
          title="Coming soon"
        >
          ⚙
        </button>
      </div>

      <div className={`tags-section ${sessionActive ? 'tags-section-locked' : ''}`}>
        <div className="tags-header">
          <span>Session Tags</span>
          <span className="muted">
            {sessionActive ? 'locked during session' : 'optional — pick before Start'}
          </span>
        </div>
        <div className="tag-pills">
          {subjects.map((s) => (
            <button
              key={s.id}
              type="button"
              className={`tag-pill ${selectedSubjectId === s.id ? 'selected' : ''}`}
              onClick={() => toggleTag(s.id)}
              disabled={sessionActive}
            >
              {s.name}
            </button>
          ))}
        </div>
        <form onSubmit={addTag} className="tag-add-form">
          <input
            type="text"
            placeholder="Add new tag..."
            value={newTag}
            onChange={(e) => setNewTag(e.target.value)}
            disabled={sessionActive}
          />
          <button type="submit" disabled={sessionActive}>
            Add
          </button>
        </form>
        {tagError && <p className="form-error">{tagError}</p>}
      </div>
    </section>
  )
}
