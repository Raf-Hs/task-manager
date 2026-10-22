import type { Task } from '../types/task'
import { formatDate, formatDateTime, isOverdue } from '../utils/dateUtils'

interface TaskItemProps {
  task: Task
  /** Disables the buttons while another request is in flight. */
  isBusy: boolean
  /** Marks the task as completed or pending. */
  onToggle: (task: Task) => void
  /** Starts the edition of the task. */
  onEdit: (task: Task) => void
  /** Asks for the deletion of the task. */
  onDelete: (task: Task) => void
}

/**
 * A single task rendered as a card.
 *
 * Completed tasks are shown with a strikethrough and overdue ones with a red
 * badge, so the state of the list can be read at a glance.
 */
export function TaskItem({ task, isBusy, onToggle, onEdit, onDelete }: TaskItemProps) {
  const overdue = isOverdue(task.dueDate, task.completed)

  return (
    <li className={`task-card${task.completed ? ' task-card--completed' : ''}`}>
      <div className="task-card__main">
        <div className="task-card__header">
          <h3 className="task-card__title">{task.title}</h3>
          {task.completed && <span className="badge badge--success">Completed</span>}
          {overdue && <span className="badge badge--danger">Overdue</span>}
        </div>

        {task.description && <p className="task-card__description">{task.description}</p>}

        <dl className="task-card__meta">
          <div>
            <dt>Due date</dt>
            <dd className={overdue ? 'task-card__due--overdue' : undefined}>{formatDate(task.dueDate)}</dd>
          </div>
          <div>
            <dt>Created</dt>
            <dd>{formatDateTime(task.createdAt)}</dd>
          </div>
        </dl>
      </div>

      <div className="task-card__actions">
        <button
          type="button"
          className="btn btn--small"
          onClick={() => onToggle(task)}
          disabled={isBusy}
        >
          {task.completed ? 'Mark as pending' : 'Complete'}
        </button>
        <button
          type="button"
          className="btn btn--small btn--ghost"
          onClick={() => onEdit(task)}
          disabled={isBusy}
        >
          Edit
        </button>
        <button
          type="button"
          className="btn btn--small btn--danger"
          onClick={() => onDelete(task)}
          disabled={isBusy}
        >
          Delete
        </button>
      </div>
    </li>
  )
}
