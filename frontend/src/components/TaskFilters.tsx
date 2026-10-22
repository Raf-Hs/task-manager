import type { TaskPriorityFilter, TaskStatus } from '../types/task'

interface TaskFiltersProps {
  /** Currently selected status filter. */
  activeFilter: TaskStatus
  /** Called when the user picks another status filter. */
  onChange: (status: TaskStatus) => void
  /** Currently selected priority filter. */
  activePriority: TaskPriorityFilter
  /** Called when the user picks another priority filter. */
  onPriorityChange: (priority: TaskPriorityFilter) => void
  /** Number of tasks currently displayed. */
  total: number
}

const FILTERS: Array<{ value: TaskStatus; label: string }> = [
  { value: 'ALL', label: 'All' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'COMPLETED', label: 'Completed' },
]

const PRIORITY_FILTERS: Array<{ value: TaskPriorityFilter; label: string }> = [
  { value: 'ALL', label: 'Any priority' },
  { value: 'HIGH', label: 'High' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'LOW', label: 'Low' },
]

/** Buttons that filter the task list by status and by priority. */
export function TaskFilters({
  activeFilter,
  onChange,
  activePriority,
  onPriorityChange,
  total,
}: TaskFiltersProps) {
  return (
    <div className="filters">
      <div className="filters__group" role="group" aria-label="Filter tasks by status">
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
      </div>

      <label className="filters__select">
        <span className="visually-hidden">Filter tasks by priority</span>
        <select
          value={activePriority}
          onChange={(event) => onPriorityChange(event.target.value as TaskPriorityFilter)}
        >
          {PRIORITY_FILTERS.map((filter) => (
            <option key={filter.value} value={filter.value}>
              {filter.label}
            </option>
          ))}
        </select>
      </label>

      <span className="filters__count">
        {total} {total === 1 ? 'task' : 'tasks'}
      </span>
    </div>
  )
}
