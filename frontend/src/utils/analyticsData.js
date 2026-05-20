function sessionMinutes(session) {
  const start = new Date(session.startAt)
  const end = new Date(session.endedAt)
  return Math.max(0, (end - start) / 60000)
}

function startOfDay(date) {
  const d = new Date(date)
  d.setHours(0, 0, 0, 0)
  return d
}

function dayKey(date) {
  return startOfDay(date).getTime()
}

const Y_STEPS = [15, 30, 45, 60, 90, 120, 180, 240, 360, 480]

function niceYMax(maxMinutes) {
  if (maxMinutes <= 0) return 60
  for (const step of Y_STEPS) {
    if (maxMinutes <= step) return step
  }
  return Math.ceil(maxMinutes / 60) * 60
}

/** Current calendar week Mon → Sun. */
export function buildLast7DaysChart(sessions) {
  const today = startOfDay(new Date())
  const monday = new Date(today)
  const daysSinceMonday = (today.getDay() + 6) % 7
  monday.setDate(today.getDate() - daysSinceMonday)

  const days = []
  for (let i = 0; i < 7; i += 1) {
    const date = new Date(monday)
    date.setDate(monday.getDate() + i)
    days.push({
      date,
      key: dayKey(date),
      label: date.toLocaleDateString(undefined, { weekday: 'short' }),
      minutes: 0,
    })
  }

  const dayByKey = new Map(days.map((d) => [d.key, d]))

  for (const session of sessions) {
    const start = new Date(session.startAt)
    const bucket = dayByKey.get(dayKey(start))
    if (bucket) bucket.minutes += sessionMinutes(session)
  }

  const peak = Math.max(...days.map((d) => d.minutes), 0)
  const yMax = niceYMax(peak)

  return {
    days,
    yMax,
    yTicks: [yMax, Math.round((yMax * 3) / 4), Math.round(yMax / 2), Math.round(yMax / 4), 0],
  }
}

const PIE_COLORS = [
  '#2563eb',
  '#7c3aed',
  '#db2777',
  '#ea580c',
  '#16a34a',
  '#0891b2',
  '#ca8a04',
  '#64748b',
]

const NO_TAG_LABEL = 'No tag'

/** Study time share by subject name; sessions without a subject → "No tag". */
export function buildSubjectPie(sessions) {
  const totals = new Map()

  for (const session of sessions) {
    const label = session.subjectName?.trim() ? session.subjectName : NO_TAG_LABEL
    totals.set(label, (totals.get(label) || 0) + sessionMinutes(session))
  }

  const entries = [...totals.entries()].sort((a, b) => b[1] - a[1])
  const grandTotal = entries.reduce((sum, [, minutes]) => sum + minutes, 0)

  if (grandTotal <= 0) {
    return { segments: [], totalMinutes: 0 }
  }

  let cursor = 0
  const segments = entries.map(([label, minutes], index) => {
    const percent = (minutes / grandTotal) * 100
    const startPercent = cursor
    cursor += percent
    return {
      label,
      minutes,
      percent,
      startPercent,
      endPercent: cursor,
      color: PIE_COLORS[index % PIE_COLORS.length],
    }
  })

  return { segments, totalMinutes: grandTotal }
}

export function formatPieGradient(segments) {
  if (segments.length === 0) return 'var(--border)'
  if (segments.length === 1) {
    return `${segments[0].color} 0% 100%`
  }
  return segments
    .map((s) => `${s.color} ${s.startPercent}% ${s.endPercent}%`)
    .join(', ')
}
