import type { TaskStatus } from '../types/task'

interface TaskFiltersProps {
  /** Currently selected filter. */
  activeFilter: TaskStatus
  /** Called when the user picks another filter. */
  onChange: (status: TaskStatus) => void
  /** Number of tasks currently displayed. */
  total: number
}

const FILTERS: Array<{ value: TaskStatus; label: string }> = [
  { value: 'ALL', label: 'All' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'COMPLETED', label: 'Completed' },
]

/** Buttons that filter the task list by status. */
export function TaskFilters({ activeFilter, onChange, total }: TaskFiltersProps) {
  return (
    <div className="filters" role="group" aria-label="Filter tasks by status">
      {FILTERS.map((filter) => (
        <button
          key={filter.value}
          type="button"
          className={`filters__button${activeFilter === filter.value ? ' filters__button--active' : ''}`}
          aria-pressed={activeFilter === filter.value}
          onClick={() => onChange(filter.value)}
        >
          {filter.label}
        </button>
      ))}
      <span className="filters__count">
        {total} {total === 1 ? 'task' : 'tasks'}
      </span>
    </div>
  )
}
