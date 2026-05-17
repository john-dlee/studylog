const MONTHS = ['May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec', 'Jan', 'Feb', 'Mar', 'Apr', 'May']
const LEVELS = ['level-0', 'level-1', 'level-2', 'level-3']

export default function StudyActivityHeatmap() {
  const cells = Array.from({ length: 7 * 13 }, (_, i) => LEVELS[i % 4])

  return (
    <section className="card heatmap-card">
      <h2 className="card-title">
        <span className="title-icon">✨</span> Study Activity
      </h2>
      <div className="heatmap-wrap">
        <div className="heatmap-months">
          {MONTHS.map((m) => (
            <span key={m}>{m}</span>
          ))}
        </div>
        <div className="heatmap-grid">
          <div className="heatmap-days">
            <span>Mon</span>
            <span>Wed</span>
            <span>Fri</span>
          </div>
          <div className="heatmap-cells">
            {cells.map((level, i) => (
              <span key={i} className={`heatmap-cell ${level}`} />
            ))}
          </div>
        </div>
        <div className="heatmap-legend">
          <span>Less</span>
          {LEVELS.map((l) => (
            <span key={l} className={`heatmap-cell ${l}`} />
          ))}
          <span>More</span>
        </div>
      </div>
      <p className="placeholder-note">Activity heatmap — coming soon with real data</p>
    </section>
  )
}
