import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { TaskForm } from './TaskForm'
import type { Task } from '../types/task'

const existingTask: Task = {
  id: 7,
  title: 'Buy milk',
  description: 'Semi skimmed',
  dueDate: '2030-01-15',
  completed: false,
  createdAt: '2026-01-01T10:00:00Z',
}

function renderForm(props: Partial<React.ComponentProps<typeof TaskForm>> = {}) {
  const onSubmit = vi.fn()
  const onCancel = vi.fn()
  render(
    <TaskForm task={null} isSaving={false} onSubmit={onSubmit} onCancel={onCancel} {...props} />,
  )
  return { onSubmit, onCancel }
}

describe('TaskForm', () => {
  it('shows the title of the form for a new task', () => {
    renderForm()
    expect(screen.getByRole('heading', { name: 'New task' })).toBeInTheDocument()
  })

  it('shows the title of the form when editing', () => {
    renderForm({ task: existingTask })
    expect(screen.getByRole('heading', { name: 'Edit task' })).toBeInTheDocument()
  })

  it('fills the fields with the values of the edited task', () => {
    renderForm({ task: existingTask })

    expect(screen.getByLabelText(/title/i)).toHaveValue('Buy milk')
    expect(screen.getByLabelText(/description/i)).toHaveValue('Semi skimmed')
    expect(screen.getByLabelText(/due date/i)).toHaveValue('2030-01-15')
  })

  it('rejects a blank title without calling onSubmit', async () => {
    const user = userEvent.setup()
    const { onSubmit } = renderForm()

    await user.click(screen.getByRole('button', { name: /create task/i }))

    expect(await screen.findByText('The title is mandatory')).toBeInTheDocument()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('rejects a title longer than 200 characters', async () => {
    const user = userEvent.setup()
    const { onSubmit } = renderForm()

    const input = screen.getByLabelText(/title/i)
    await user.clear(input)
    await user.type(input, 'a'.repeat(201))
    await user.click(screen.getByRole('button', { name: /create task/i }))

    expect(
      await screen.findByText('The title cannot be longer than 200 characters'),
    ).toBeInTheDocument()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('submits the values of the form', async () => {
    const user = userEvent.setup()
    const { onSubmit } = renderForm()

    await user.type(screen.getByLabelText(/title/i), 'Write the report')
    await user.type(screen.getByLabelText(/description/i), 'Final version')
    await user.type(screen.getByLabelText(/due date/i), '2030-02-01')
    await user.click(screen.getByRole('button', { name: /create task/i }))

    expect(onSubmit).toHaveBeenCalledWith({
      title: 'Write the report',
      description: 'Final version',
      dueDate: '2030-02-01',
      completed: false,
    })
  })

  it('sends null when the optional fields are empty', async () => {
    const user = userEvent.setup()
    const { onSubmit } = renderForm()

    await user.type(screen.getByLabelText(/title/i), 'Only a title')
    await user.click(screen.getByRole('button', { name: /create task/i }))

    expect(onSubmit).toHaveBeenCalledWith({
      title: 'Only a title',
      description: null,
      dueDate: null,
      completed: false,
    })
  })

  it('shows the cancel button only while editing', async () => {
    const user = userEvent.setup()
    const { onCancel } = renderForm({ task: existingTask })

    await user.click(screen.getByRole('button', { name: /cancel/i }))

    expect(onCancel).toHaveBeenCalled()
  })

  it('disables the buttons while the form is saving', () => {
    renderForm({ isSaving: true })
    expect(screen.getByRole('button', { name: /saving/i })).toBeDisabled()
  })
})
