export function computeStats(sessions) {
  const now = new Date()
  const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const startOfWeek = new Date(startOfToday)
  startOfWeek.setDate(startOfWeek.getDate() - ((startOfWeek.getDay() + 6) % 7))

  let todayMs = 0
  let weekMs = 0
  let totalMs = 0
  const studyDays = new Set()

  for (const s of sessions) {
    const start = new Date(s.startAt)
    const end = new Date(s.endedAt)
    const ms = Math.max(0, end - start)
    totalMs += ms
    if (start >= startOfToday) todayMs += ms
    if (start >= startOfWeek) weekMs += ms
    studyDays.add(start.toDateString())
  }

  const streak = computeStreak(studyDays)

  return {
    todayMs,
    weekMs,
    totalMs,
    streak,
  }
}

function computeStreak(studyDays) {
  if (studyDays.size === 0) return 0
  let streak = 0
  const cursor = new Date()
  cursor.setHours(0, 0, 0, 0)
  while (studyDays.has(cursor.toDateString())) {
    streak += 1
    cursor.setDate(cursor.getDate() - 1)
  }
  return streak
}
