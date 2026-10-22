import type { Task } from '../types/task'
import { TaskItem } from './TaskItem'

interface TaskListProps {
  tasks: Task[]
  isLoading: boolean
  isBusy: boolean
  onToggle: (task: Task) => void
  onEdit: (task: Task) => void
  onDelete: (task: Task) => void
}

/**
 * List of tasks.
 *
 * While the data is loading a spinner is shown instead of the list, so the
 * user knows the request is still in progress.
 */
export function TaskList({ tasks, isLoading, isBusy, onToggle, onEdit, onDelete }: TaskListProps) {
  if (isLoading) {
    return (
      <div className="loader" role="status" aria-live="polite">
        <span className="loader__spinner" aria-hidden="true" />
        <p>Loading tasks…</p>
      </div>
    )
  }

  if (tasks.length === 0) {
    return (
      <div className="empty-state">
        <p className="empty-state__title">No tasks yet</p>
        <p className="empty-state__text">Create your first task with the form on the left.</p>
      </div>
    )
  }

  return (
    <ul className="task-list">
      {tasks.map((task) => (
        <TaskItem
          key={task.id}
          task={task}
          isBusy={isBusy}
          onToggle={onToggle}
          onEdit={onEdit}
          onDelete={onDelete}
        />
      ))}
    </ul>
  )
}
