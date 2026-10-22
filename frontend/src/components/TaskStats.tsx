import type { TaskStatistics } from '../types/task'

interface TaskStatsProps {
  /** Aggregated figures. `null` while the first request is in flight. */
  statistics: TaskStatistics | null
}

/**
 * Panel that summarises the state of the task list.
 *
 * The figures come from the aggregated endpoint of the backend, so they are
 * computed by the database and not by counting the rows that happen to be on
 * the screen.
 */
export function TaskStats({ statistics }: TaskStatsProps) {
  if (!statistics) {
    return null
  }

  const figures: Array<{ label: string; value: number; modifier?: string }> = [
    { label: 'Total', value: statistics.total },
    { label: 'Pending', value: statistics.pending },
    { label: 'Completed', value: statistics.completed },
    { label: 'Overdue', value: statistics.overdue, modifier: 'task-stats__value--danger' },
  ]

  return (
    <section className="task-stats" aria-label="Task statistics">
      {figures.map((figure) => (
        <div className="task-stats__item" key={figure.label}>
          <span className={`task-stats__value${figure.modifier ? ` ${figure.modifier}` : ''}`}>
            {figure.value}
          </span>
          <span className="task-stats__label">{figure.label}</span>
        </div>
      ))}

      <div className="task-stats__item task-stats__item--progress">
        <span className="task-stats__value">{statistics.completionPercentage}%</span>
        <span className="task-stats__label">Completion</span>
        <div
          className="task-stats__bar"
          role="progressbar"
          aria-valuenow={statistics.completionPercentage}
          aria-valuemin={0}
          aria-valuemax={100}
          aria-label="Percentage of completed tasks"
        >
          <span
            className="task-stats__bar-fill"
            style={{ width: `${statistics.completionPercentage}%` }}
          />
        </div>
      </div>
    </section>
  )
}
