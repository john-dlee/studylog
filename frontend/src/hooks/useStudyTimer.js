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

const TICK_MS = 250

export function useStudyTimer({ onSessionSaved, subjectIdRef }) {
  const [timerMode, setTimerMode] = useState('pomodoro')
  const [phase, setPhase] = useState('work')
  const [cycle, setCycle] = useState(1)
  const [secondsLeft, setSecondsLeft] = useState(DEFAULTS.work)
  const [status, setStatus] = useState('idle')
  const [sessionType, setSessionType] = useState('POMODORO')

  const sessionStartRef = useRef(null)
  const endsAtRef = useRef(null)
  const stopwatchBaseMsRef = useRef(0)
  const stopwatchStartedAtRef = useRef(null)
  const tickRef = useRef(null)
  const handledZeroRef = useRef(false)

  const phaseDuration = useCallback(() => {
    if (timerMode === 'stopwatch') return 0
    return DEFAULTS[phase] ?? DEFAULTS.work
  }, [timerMode, phase])

  const applyPhase = useCallback(
    (nextPhase, nextCycle) => {
      handledZeroRef.current = false
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
      if (handledZeroRef.current) return
      handledZeroRef.current = true

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

  const clearTick = useCallback(() => {
    if (tickRef.current != null) {
      clearInterval(tickRef.current)
      tickRef.current = null
    }
  }, [])

  const syncStopwatchElapsed = useCallback(() => {
    const runningMs = stopwatchStartedAtRef.current
      ? Date.now() - stopwatchStartedAtRef.current
      : 0
    const totalMs = stopwatchBaseMsRef.current + runningMs
    setSecondsLeft(Math.floor(totalMs / 1000))
  }, [])

  const syncCountdownRemaining = useCallback(() => {
    if (endsAtRef.current == null) return
    const remainingMs = endsAtRef.current - Date.now()
    const next = Math.max(0, Math.ceil(remainingMs / 1000))
    setSecondsLeft(next)
    return next
  }, [])

  useEffect(() => {
    if (status !== 'running') {
      clearTick()
      return undefined
    }

    const tick = () => {
      if (timerMode === 'stopwatch') {
        syncStopwatchElapsed()
        return
      }

      const remaining = syncCountdownRemaining()
      if (remaining <= 0) {
        clearTick()
        setStatus('idle')
        const subjectId = subjectIdRef?.current ?? null
        handleTimerZero(subjectId)
      }
    }

    tick()
    tickRef.current = window.setInterval(tick, TICK_MS)

    return clearTick
  }, [
    status,
    timerMode,
    clearTick,
    syncStopwatchElapsed,
    syncCountdownRemaining,
    handleTimerZero,
    subjectIdRef,
  ])

  const stopEarly = useCallback(
    async (subjectId) => {
      clearTick()
      if (!sessionStartRef.current) {
        setStatus('idle')
        return
      }
      const planned =
        timerMode === 'pomodoro' && phase === 'work'
          ? Math.round(DEFAULTS.work / 60)
          : null
      await saveSession(subjectId, planned)
      handledZeroRef.current = true
      if (timerMode === 'pomodoro' && phase === 'work') {
        advanceAfterWorkSaved()
      } else if (timerMode === 'stopwatch') {
        stopwatchBaseMsRef.current = 0
        stopwatchStartedAtRef.current = null
        setSecondsLeft(0)
        setStatus('idle')
      } else {
        advanceAfterBreakEnded()
      }
    },
    [
      timerMode,
      phase,
      saveSession,
      advanceAfterWorkSaved,
      advanceAfterBreakEnded,
      clearTick,
    ],
  )

  const start = useCallback(() => {
    if (status === 'running') return

    const shouldTrack =
      timerMode === 'stopwatch' || (timerMode === 'pomodoro' && phase === 'work')
    if (!sessionStartRef.current && shouldTrack) {
      sessionStartRef.current = new Date()
    }

    handledZeroRef.current = false

    if (timerMode === 'stopwatch') {
      stopwatchStartedAtRef.current = Date.now()
    } else {
      endsAtRef.current = Date.now() + secondsLeft * 1000
    }

    setStatus('running')
  }, [status, timerMode, phase, secondsLeft])

  const pause = useCallback(() => {
    if (status !== 'running') return

    clearTick()

    if (timerMode === 'stopwatch') {
      if (stopwatchStartedAtRef.current) {
        stopwatchBaseMsRef.current += Date.now() - stopwatchStartedAtRef.current
        stopwatchStartedAtRef.current = null
      }
      syncStopwatchElapsed()
    } else {
      syncCountdownRemaining()
      endsAtRef.current = null
    }

    setStatus('paused')
  }, [status, timerMode, clearTick, syncStopwatchElapsed, syncCountdownRemaining])

  const resume = useCallback(() => {
    if (status !== 'paused') return

    handledZeroRef.current = false

    if (timerMode === 'stopwatch') {
      stopwatchStartedAtRef.current = Date.now()
    } else {
      endsAtRef.current = Date.now() + secondsLeft * 1000
    }

    setStatus('running')
  }, [status, timerMode, secondsLeft])

  const reset = useCallback(() => {
    clearTick()
    sessionStartRef.current = null
    endsAtRef.current = null
    stopwatchBaseMsRef.current = 0
    stopwatchStartedAtRef.current = null
    handledZeroRef.current = false
    setStatus('idle')
    if (timerMode === 'stopwatch') {
      setSecondsLeft(0)
    } else {
      setSecondsLeft(DEFAULTS[phase] ?? DEFAULTS.work)
    }
  }, [timerMode, phase, clearTick])

  const switchTimerMode = useCallback(
    (mode) => {
      clearTick()
      sessionStartRef.current = null
      endsAtRef.current = null
      stopwatchBaseMsRef.current = 0
      stopwatchStartedAtRef.current = null
      handledZeroRef.current = false
      setTimerMode(mode)
      setSessionType(mode === 'stopwatch' ? 'STOPWATCH' : 'POMODORO')
      setPhase('work')
      setCycle(1)
      setStatus('idle')
      setSecondsLeft(mode === 'stopwatch' ? 0 : DEFAULTS.work)
    },
    [clearTick],
  )

  const switchPhase = useCallback(
    (nextPhase) => {
      if (timerMode !== 'pomodoro' || status === 'running') return
      handledZeroRef.current = false
      endsAtRef.current = null
      const nextCycle = nextPhase === 'work' && phase === 'longBreak' ? 1 : cycle
      applyPhase(nextPhase, nextCycle)
    },
    [timerMode, status, applyPhase, phase, cycle],
  )

  const totalSeconds = phaseDuration() || DEFAULTS.work
  const progress =
    timerMode === 'stopwatch' || totalSeconds === 0
      ? 0
      : Math.min(100, ((totalSeconds - secondsLeft) / totalSeconds) * 100)

  return {
    timerMode,
    phase,
    phaseLabel: timerMode === 'stopwatch' ? 'Stopwatch' : PHASE_LABELS[phase],
    cycle,
    secondsLeft,
    status,
    progress,
    displayTime: formatClock(secondsLeft),
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
  const safe = Math.max(0, seconds)
  const m = Math.floor(safe / 60)
  const s = safe % 60
  return `${m}:${String(s).padStart(2, '0')}`
}
