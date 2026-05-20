import { Fragment, useMemo } from 'react'
import { buildHeatmap, formatHeatmapTooltip } from '../utils/heatmapData'

const DAY_LABELS = ['', 'Mon', '', 'Wed', '', 'Fri', '']

export default function StudyActivityHeatmap({ sessions = [] }) {
  const { grid, monthLabels } = useMemo(() => buildHeatmap(sessions), [sessions])

  const monthByCol = useMemo(() => {
    const map = new Map(monthLabels.map(({ col, label }) => [col, label]))
    return map
  }, [monthLabels])

  const weekCount = grid[0]?.length ?? 0

  return (
    <section className="card heatmap-card">
      <h2 className="card-title contrib-title">Study activity in the last year</h2>

      <div className="contrib-scroll">
        <div
          className="contrib-grid"
          style={{ '--week-count': weekCount }}
          role="grid"
          aria-label="Study activity heatmap"
        >
          <div className="contrib-corner" role="presentation" />
          {Array.from({ length: weekCount }, (_, col) => (
            <div key={`m-${col}`} className="contrib-month" role="columnheader">
              {monthByCol.get(col) ?? ''}
            </div>
          ))}

          {grid.map((weekRow, row) => (
            <Fragment key={row}>
              <div className="contrib-day-label" role="rowheader">
                {DAY_LABELS[row]}
              </div>
              {weekRow.map((cell) => {
                const levelClass = cell.isFuture
                  ? 'contrib-level-future'
                  : `contrib-level-${cell.level}`
                return (
                  <div
                    key={cellKey(cell.date)}
                    role="gridcell"
                    className={`contrib-cell ${levelClass}`}
                    title={formatHeatmapTooltip(cell.date, cell.minutes)}
                    tabIndex={0}
                  />
                )
              })}
            </Fragment>
          ))}
        </div>
      </div>

      <div className="contrib-footer">
        <div className="contrib-legend" aria-hidden>
          <span>Less</span>
          {[0, 1, 2, 3, 4].map((level) => (
            <span key={level} className={`contrib-cell contrib-level-${level}`} />
          ))}
          <span>More</span>
        </div>
      </div>
    </section>
  )
}

function cellKey(date) {
  return `${date.getFullYear()}-${date.getMonth()}-${date.getDate()}`
}
