import type { TaskSortOption } from '../types/task'

interface TaskSortProps {
  /** Criterion currently applied. */
  value: TaskSortOption
  /** Called when the user picks another criterion. */
  onChange: (sort: TaskSortOption) => void
}

const OPTIONS: Array<{ value: TaskSortOption; label: string }> = [
  { value: 'CREATED_AT', label: 'Newest first' },
  { value: 'UPDATED_AT', label: 'Recently modified' },
  { value: 'DUE_DATE', label: 'Closest deadline' },
  { value: 'TITLE', label: 'Title (A-Z)' },
  { value: 'PRIORITY', label: 'Priority' },
]

/** Selector that chooses how the task list is ordered. */
export function TaskSort({ value, onChange }: TaskSortProps) {
  return (
    <label className="task-sort">
      <span className="visually-hidden">Sort tasks by</span>
      <select
        value={value}
        onChange={(event) => onChange(event.target.value as TaskSortOption)}
      >
        {OPTIONS.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </label>
  )
}
