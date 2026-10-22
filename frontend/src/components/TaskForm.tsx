import { useState } from 'react'
import type { Task, TaskInput, TaskPriority } from '../types/task'

interface TaskFormProps {
  /** Task being edited. When it is `null` the form creates a new task. */
  task: Task | null
  /** Disables the form while a request is in flight. */
  isSaving: boolean
  /** Called with the values of the form. */
  onSubmit: (input: TaskInput) => void
  /** Called when the user cancels the edition. */
  onCancel: () => void
}

interface FormErrors {
  title?: string
  description?: string
  dueDate?: string
}

const PRIORITIES: Array<{ value: TaskPriority; label: string }> = [
  { value: 'LOW', label: 'Low' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'HIGH', label: 'High' },
]

/**
 * Form used to create and to edit tasks.
 *
 * The validation performed here mirrors the Bean Validation rules of the
 * backend, so the user gets immediate feedback without a round trip. The
 * backend validates again, because a client can always be bypassed.
 */
export function TaskForm({ task, isSaving, onSubmit, onCancel }: TaskFormProps) {
  const isEditing = task !== null
  const [title, setTitle] = useState(task?.title ?? '')
  const [description, setDescription] = useState(task?.description ?? '')
  const [dueDate, setDueDate] = useState(task?.dueDate ?? '')
  const [priority, setPriority] = useState<TaskPriority>(task?.priority ?? 'MEDIUM')
  const [completed, setCompleted] = useState(task?.completed ?? false)
  const [errors, setErrors] = useState<FormErrors>({})

  function validate(): boolean {
    const nextErrors: FormErrors = {}

    if (!title.trim()) {
      nextErrors.title = 'The title is mandatory'
    } else if (title.trim().length > 200) {
      nextErrors.title = 'The title cannot be longer than 200 characters'
    }

    if (description.length > 2000) {
      nextErrors.description = 'The description cannot be longer than 2000 characters'
    }

    setErrors(nextErrors)
    return Object.keys(nextErrors).length === 0
  }

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!validate()) {
      return
    }

    onSubmit({
      title: title.trim(),
      description: description.trim() ? description.trim() : null,
      dueDate: dueDate ? dueDate : null,
      priority,
      completed,
    })
  }

  function handleReset() {
    setTitle(task?.title ?? '')
    setDescription(task?.description ?? '')
    setDueDate(task?.dueDate ?? '')
    setPriority(task?.priority ?? 'MEDIUM')
    setCompleted(task?.completed ?? false)
    setErrors({})
  }

  return (
    <form className="task-form" onSubmit={handleSubmit} noValidate>
      <h2 className="task-form__title">{isEditing ? 'Edit task' : 'New task'}</h2>

      <div className="field">
        <label htmlFor="task-title">Title *</label>
        <input
          id="task-title"
          name="title"
          type="text"
          value={title}
          placeholder="What do you have to do?"
          onChange={(event) => setTitle(event.target.value)}
          aria-invalid={Boolean(errors.title)}
          aria-describedby={errors.title ? 'task-title-error' : undefined}
        />
        {errors.title && (
          <p className="field__error" id="task-title-error" role="alert">
            {errors.title}
          </p>
        )}
      </div>

      <div className="field">
        <label htmlFor="task-description">Description</label>
        <textarea
          id="task-description"
          name="description"
          value={description}
          rows={3}
          placeholder="Optional details"
          onChange={(event) => setDescription(event.target.value)}
        />
        {errors.description && (
          <p className="field__error" role="alert">
            {errors.description}
          </p>
        )}
      </div>

      <div className="field">
        <label htmlFor="task-due-date">Due date</label>
        <input
          id="task-due-date"
          name="dueDate"
          type="date"
          value={dueDate}
          onChange={(event) => setDueDate(event.target.value)}
        />
        {errors.dueDate && (
          <p className="field__error" role="alert">
            {errors.dueDate}
          </p>
        )}
      </div>

      <div className="field">
        <label htmlFor="task-priority">Priority</label>
        <select
          id="task-priority"
          name="priority"
          value={priority}
          onChange={(event) => setPriority(event.target.value as TaskPriority)}
        >
          {PRIORITIES.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>

      <div className="field field--checkbox">
        <input
          id="task-completed"
          name="completed"
          type="checkbox"
          checked={completed}
          onChange={(event) => setCompleted(event.target.checked)}
        />
        <label htmlFor="task-completed">Already completed</label>
      </div>

      <div className="task-form__actions">
        <button type="submit" className="btn btn--primary" disabled={isSaving}>
          {isSaving ? 'Saving…' : isEditing ? 'Save changes' : 'Create task'}
        </button>
        {isEditing && (
          <button
            type="button"
            className="btn btn--ghost"
            onClick={() => {
              handleReset()
              onCancel()
            }}
            disabled={isSaving}
          >
            Cancel
          </button>
        )}
      </div>

      <p className="task-form__hint">Fields marked with * are mandatory.</p>
    </form>
  )
}
