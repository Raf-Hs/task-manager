import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { TaskList } from './TaskList'
import type { Task } from '../types/task'

const tasks: Task[] = [
  {
    id: 1,
    title: 'Buy milk',
    description: null,
    dueDate: null,
    priority: 'MEDIUM',
    completed: false,
    createdAt: '2026-01-01T10:00:00Z',
    updatedAt: '2026-01-01T10:00:00Z',
  },
]

describe('TaskList', () => {
  it('shows a loading indicator while the tasks are being fetched', () => {
    render(
      <TaskList tasks={[]} isLoading isBusy={false} onToggle={vi.fn()} onEdit={vi.fn()} onDelete={vi.fn()} />,
    )

    expect(screen.getByRole('status')).toHaveTextContent(/loading tasks/i)
  })

  it('shows an empty state when there are no tasks', () => {
    render(
      <TaskList tasks={[]} isLoading={false} isBusy={false} onToggle={vi.fn()} onEdit={vi.fn()} onDelete={vi.fn()} />,
    )

    expect(screen.getByText('No tasks yet')).toBeInTheDocument()
  })

  it('renders one item per task', () => {
    render(
      <TaskList tasks={tasks} isLoading={false} isBusy={false} onToggle={vi.fn()} onEdit={vi.fn()} onDelete={vi.fn()} />,
    )

    expect(screen.getByText('Buy milk')).toBeInTheDocument()
  })
})
