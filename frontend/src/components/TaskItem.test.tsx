import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { TaskItem } from './TaskItem'
import type { Task } from '../types/task'

function buildTask(overrides: Partial<Task> = {}): Task {
  return {
    id: 1,
    title: 'Buy milk',
    description: 'Semi skimmed',
    dueDate: '2999-01-15',
    priority: 'MEDIUM',
    completed: false,
    createdAt: '2026-01-01T10:00:00Z',
    updatedAt: '2026-01-01T10:00:00Z',
    ...overrides,
  }
}

describe('TaskItem', () => {
  it('renders the title and the description of the task', () => {
    render(
      <TaskItem task={buildTask()} isBusy={false} onToggle={vi.fn()} onEdit={vi.fn()} onDelete={vi.fn()} />,
    )

    expect(screen.getByText('Buy milk')).toBeInTheDocument()
    expect(screen.getByText('Semi skimmed')).toBeInTheDocument()
  })

  it('shows the completed badge for a finished task', () => {
    render(
      <TaskItem
        task={buildTask({ completed: true })}
        isBusy={false}
        onToggle={vi.fn()}
        onEdit={vi.fn()}
        onDelete={vi.fn()}
      />,
    )

    expect(screen.getByText('Completed')).toBeInTheDocument()
    expect(screen.queryByText('Overdue')).not.toBeInTheDocument()
  })

  it('shows the overdue badge for a pending task with a past deadline', () => {
    render(
      <TaskItem
        task={buildTask({ dueDate: '2020-01-01' })}
        isBusy={false}
        onToggle={vi.fn()}
        onEdit={vi.fn()}
        onDelete={vi.fn()}
      />,
    )

    expect(screen.getByText('Overdue')).toBeInTheDocument()
  })

  it('shows the priority of the task', () => {
    render(
      <TaskItem
        task={buildTask({ priority: 'HIGH' })}
        isBusy={false}
        onToggle={vi.fn()}
        onEdit={vi.fn()}
        onDelete={vi.fn()}
      />,
    )

    expect(screen.getByText('High priority')).toBeInTheDocument()
  })

  it('calls onToggle when the complete button is pressed', async () => {
    const user = userEvent.setup()
    const onToggle = vi.fn()
    render(
      <TaskItem task={buildTask()} isBusy={false} onToggle={onToggle} onEdit={vi.fn()} onDelete={vi.fn()} />,
    )

    await user.click(screen.getByRole('button', { name: /complete/i }))

    expect(onToggle).toHaveBeenCalledTimes(1)
  })

  it('offers to mark a completed task as pending', () => {
    render(
      <TaskItem
        task={buildTask({ completed: true })}
        isBusy={false}
        onToggle={vi.fn()}
        onEdit={vi.fn()}
        onDelete={vi.fn()}
      />,
    )

    expect(screen.getByRole('button', { name: /mark as pending/i })).toBeInTheDocument()
  })

  it('calls onEdit and onDelete with the task', async () => {
    const user = userEvent.setup()
    const onEdit = vi.fn()
    const onDelete = vi.fn()
    const task = buildTask()
    render(
      <TaskItem task={task} isBusy={false} onToggle={vi.fn()} onEdit={onEdit} onDelete={onDelete} />,
    )

    await user.click(screen.getByRole('button', { name: /^edit$/i }))
    await user.click(screen.getByRole('button', { name: /delete/i }))

    expect(onEdit).toHaveBeenCalledWith(task)
    expect(onDelete).toHaveBeenCalledWith(task)
  })

  it('disables the action buttons while the task is busy', () => {
    render(
      <TaskItem task={buildTask()} isBusy onToggle={vi.fn()} onEdit={vi.fn()} onDelete={vi.fn()} />,
    )

    expect(screen.getByRole('button', { name: /complete/i })).toBeDisabled()
    expect(screen.getByRole('button', { name: /^edit$/i })).toBeDisabled()
    expect(screen.getByRole('button', { name: /delete/i })).toBeDisabled()
  })
})
