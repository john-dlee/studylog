const WEEKS = 53
const ROWS = 7
const DAY_NAMES = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']
const MONTH_NAMES = [
  'Jan',
  'Feb',
  'Mar',
  'Apr',
  'May',
  'Jun',
  'Jul',
  'Aug',
  'Sep',
  'Oct',
  'Nov',
  'Dec',
]

function startOfDay(date) {
  const d = new Date(date)
  d.setHours(0, 0, 0, 0)
  return d
}

function addDays(date, days) {
  const d = new Date(date)
  d.setDate(d.getDate() + days)
  return d
}

function dateKey(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function aggregateMinutesByDay(sessions) {
  const map = new Map()
  for (const session of sessions) {
    const start = new Date(session.startAt)
    const end = new Date(session.endedAt)
    const minutes = Math.max(0, (end - start) / 60000)
    if (minutes <= 0) continue
    const key = dateKey(start)
    map.set(key, (map.get(key) || 0) + minutes)
  }
  return map
}

/** Levels 0–4: none, <30m, <2h, <3h, 3h+ */
function levelForMinutes(minutes) {
  if (minutes <= 0) return 0
  if (minutes < 30) return 1
  if (minutes < 120) return 2
  if (minutes < 180) return 3
  return 4
}

function buildMonthLabels(grid) {
  const labels = []
  let lastMonth = -1
  for (let col = 0; col < WEEKS; col++) {
    const date = grid[0][col].date
    const month = date.getMonth()
    if (month !== lastMonth) {
      labels.push({ col, label: MONTH_NAMES[month] })
      lastMonth = month
    }
  }
  return labels
}

/**
 * GitHub profile chart: columns = weeks (oldest left → newest right),
 * rows = Sun (0) … Sat (6).
 */
export function buildHeatmap(sessions) {
  const minutesByDay = aggregateMinutesByDay(sessions)
  const today = startOfDay(new Date())
  const endSunday = addDays(today, -today.getDay())
  const gridStart = addDays(endSunday, -(WEEKS - 1) * 7)

  const grid = Array.from({ length: ROWS }, () =>
    Array.from({ length: WEEKS }, () => ({
      date: new Date(),
      minutes: 0,
      level: 0,
      isFuture: false,
    })),
  )

  for (let col = 0; col < WEEKS; col++) {
    for (let row = 0; row < ROWS; row++) {
      const date = addDays(gridStart, col * 7 + row)
      const minutes = minutesByDay.get(dateKey(date)) || 0
      const isFuture = date > today
      grid[row][col] = {
        date,
        minutes,
        level: isFuture ? 0 : levelForMinutes(minutes),
        isFuture,
      }
    }
  }

  return {
    grid,
    monthLabels: buildMonthLabels(grid),
    dayNames: DAY_NAMES,
  }
}

export function formatHeatmapTooltip(date, minutes) {
  const label = date.toLocaleDateString(undefined, {
    weekday: 'long',
    month: 'long',
    day: 'numeric',
    year: 'numeric',
  })
  if (minutes <= 0) return `No study time on ${label}`
  const m = Math.round(minutes)
  if (m < 60) return `${m} minutes on ${label}`
  const h = Math.floor(m / 60)
  const rem = m % 60
  const duration = rem > 0 ? `${h} hours ${rem} minutes` : `${h} hours`
  return `${duration} on ${label}`
}
