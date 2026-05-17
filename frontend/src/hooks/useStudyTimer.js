import { useCallback, useEffect, useRef, useState } from 'react'
import { apiFetch } from '../api/client'
import { toLocalDateTimeString } from '../utils/datetime'

export const DEFAULTS = {
  work: 25 * 60,
  shortBreak: 5 * 60,
  longBreak: 15 * 60,
}

const PHASE_LABELS = {
  work: 'Work',
  shortBreak: 'Short Break',
  longBreak: 'Long Break',
}

export function useStudyTimer({ onSessionSaved, subjectIdRef }) {
  const [timerMode, setTimerMode] = useState('pomodoro')
  const [phase, setPhase] = useState('work')
  const [cycle, setCycle] = useState(1)
  const [secondsLeft, setSecondsLeft] = useState(DEFAULTS.work)
  const [status, setStatus] = useState('idle')
  const [sessionType, setSessionType] = useState('POMODORO')

  const sessionStartRef = useRef(null)
  const tickRef = useRef(null)
  const pendingZeroRef = useRef(false)

  const phaseDuration = useCallback(() => {
    if (timerMode === 'stopwatch') return 0
    return DEFAULTS[phase] ?? DEFAULTS.work
  }, [timerMode, phase])

  const applyPhase = useCallback(
    (nextPhase, nextCycle) => {
      setPhase(nextPhase)
      setCycle(nextCycle)
      setSecondsLeft(
        timerMode === 'stopwatch' ? 0 : (DEFAULTS[nextPhase] ?? DEFAULTS.work),
      )
    },
    [timerMode],
  )

  const saveSession = useCallback(
    async (subjectId, plannedMinutes) => {
      if (!sessionStartRef.current) return
      const startAt = sessionStartRef.current
      const endedAt = new Date()
      const body = {
        startAt: toLocalDateTimeString(startAt),
        endedAt: toLocalDateTimeString(endedAt),
        sessionType,
        plannedDurationMinutes: plannedMinutes ?? null,
      }
      if (subjectId != null) body.subjectId = subjectId

      await apiFetch('/api/study-sessions', {
        method: 'POST',
        body: JSON.stringify(body),
      })
      sessionStartRef.current = null
      onSessionSaved?.()
    },
    [sessionType, onSessionSaved],
  )

  const advanceAfterWorkSaved = useCallback(() => {
    if (cycle >= 4) {
      applyPhase('longBreak', 4)
    } else {
      applyPhase('shortBreak', cycle)
    }
    setStatus('idle')
  }, [cycle, applyPhase])

  const advanceAfterBreakEnded = useCallback(() => {
    if (phase === 'longBreak') {
      applyPhase('work', 1)
    } else if (phase === 'shortBreak') {
      applyPhase('work', cycle + 1)
    }
    setStatus('idle')
  }, [phase, cycle, applyPhase])

  const handleTimerZero = useCallback(
    async (subjectId) => {
      if (timerMode === 'pomodoro') {
        if (phase === 'work') {
          await saveSession(subjectId, Math.round(DEFAULTS.work / 60))
          advanceAfterWorkSaved()
        } else {
          advanceAfterBreakEnded()
        }
      }
    },
    [timerMode, phase, saveSession, advanceAfterWorkSaved, advanceAfterBreakEnded],
  )


  useEffect(() => {
    if (status !== 'running') return undefined

    tickRef.current = window.setInterval(() => {
      setSecondsLeft((prev) => {
        if (timerMode === 'stopwatch') return prev + 1
        if (prev <= 1) {
          clearInterval(tickRef.current)
          pendingZeroRef.current = true
          setStatus('idle')
          return 0
        }
        return prev - 1
      })
    }, 1000)

    return () => clearInterval(tickRef.current)
  }, [status, timerMode])

  useEffect(() => {
    if (!pendingZeroRef.current || status !== 'idle' || secondsLeft !== 0) return
    pendingZeroRef.current = false
    const subjectId = subjectIdRef?.current ?? null
    handleTimerZero(subjectId)
  }, [secondsLeft, status, handleTimerZero, subjectIdRef])

  const stopEarly = useCallback(
    async (subjectId) => {
      if (!sessionStartRef.current) {
        setStatus('idle')
        return
      }
      const planned =
        timerMode === 'pomodoro' && phase === 'work'
          ? Math.round(DEFAULTS.work / 60)
          : null
      await saveSession(subjectId, planned)
      if (timerMode === 'pomodoro' && phase === 'work') {
        advanceAfterWorkSaved()
      } else if (timerMode === 'stopwatch') {
        setSecondsLeft(0)
        setStatus('idle')
      } else {
        advanceAfterBreakEnded()
      }
    },
    [timerMode, phase, saveSession, advanceAfterWorkSaved, advanceAfterBreakEnded],
  )

  const start = useCallback(() => {
    if (status === 'running') return
    const shouldTrack =
      timerMode === 'stopwatch' || (timerMode === 'pomodoro' && phase === 'work')
    if (!sessionStartRef.current && shouldTrack) {
      sessionStartRef.current = new Date()
    }
    setStatus('running')
  }, [status, timerMode, phase])

  const pause = useCallback(() => setStatus('paused'), [])

  const resume = useCallback(() => setStatus('running'), [])

  const reset = useCallback(() => {
    clearInterval(tickRef.current)
    sessionStartRef.current = null
    pendingZeroRef.current = false
    setStatus('idle')
    if (timerMode === 'stopwatch') {
      setSecondsLeft(0)
    } else {
      setSecondsLeft(DEFAULTS[phase] ?? DEFAULTS.work)
    }
  }, [timerMode, phase])

  const switchTimerMode = useCallback((mode) => {
    clearInterval(tickRef.current)
    sessionStartRef.current = null
    pendingZeroRef.current = false
    setTimerMode(mode)
    setSessionType(mode === 'stopwatch' ? 'STOPWATCH' : 'POMODORO')
    setPhase('work')
    setCycle(1)
    setStatus('idle')
    setSecondsLeft(mode === 'stopwatch' ? 0 : DEFAULTS.work)
  }, [])

  const switchPhase = useCallback(
    (nextPhase) => {
      if (timerMode !== 'pomodoro' || status === 'running') return
      const nextCycle = nextPhase === 'work' && phase === 'longBreak' ? 1 : cycle
      applyPhase(nextPhase, nextCycle)
    },
    [timerMode, status, applyPhase, phase, cycle],
  )

  const totalSeconds = phaseDuration() || DEFAULTS.work
  const progress =
    timerMode === 'stopwatch' || totalSeconds === 0
      ? 0
      : ((totalSeconds - secondsLeft) / totalSeconds) * 100

  const displayTime =
    timerMode === 'stopwatch'
      ? formatClock(secondsLeft)
      : formatClock(secondsLeft)

  return {
    timerMode,
    phase,
    phaseLabel: timerMode === 'stopwatch' ? 'Stopwatch' : PHASE_LABELS[phase],
    cycle,
    secondsLeft,
    status,
    progress,
    displayTime,
    start,
    pause,
    resume,
    reset,
    stopEarly,
    switchTimerMode,
    switchPhase,
    isWorkPhase: phase === 'work' || timerMode === 'stopwatch',
  }
}

function formatClock(seconds) {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}:${String(s).padStart(2, '0')}`
}
