import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AxiosError } from 'axios'
import { TasksPage } from './TasksPage'
import * as taskService from '../services/taskService'
import type { Task } from '../types/task'

vi.mock('../services/taskService')

const mockedService = vi.mocked(taskService)

const pendingTask: Task = {
  id: 1,
  title: 'Buy milk',
  description: 'Semi skimmed',
  dueDate: '2999-01-15',
  completed: false,
  createdAt: '2026-01-01T10:00:00Z',
}

const completedTask: Task = {
  id: 2,
  title: 'Finished task',
  description: null,
  dueDate: null,
  completed: true,
  createdAt: '2026-01-02T10:00:00Z',
}

/** Builds a real AxiosError so `getErrorMessage` can read the response body. */
function apiError(message: string, status = 400): AxiosError {
  return new AxiosError('Request failed', 'ERR_BAD_REQUEST', undefined, undefined, {
    status,
    statusText: 'Bad Request',
    data: { status, message, path: '/api/tasks', timestamp: '2026-01-01T10:00:00Z' },
    headers: {},
    config: { headers: {} } as never,
  })
}

/** Returns the card of a task, so the queries are not ambiguous. */
function cardOf(title: string): HTMLElement {
  const element = screen.getByText(title).closest('li')
  if (!element) {
    throw new Error(`No task card found for "${title}"`)
  }
  return element
}

describe('TasksPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockedService.fetchTasks.mockResolvedValue([pendingTask, completedTask])
    mockedService.createTask.mockResolvedValue(pendingTask)
    mockedService.updateTask.mockResolvedValue(pendingTask)
    mockedService.changeTaskStatus.mockResolvedValue(completedTask)
    mockedService.deleteTask.mockResolvedValue(undefined)
  })

  it('shows the loading indicator while the tasks are being fetched', async () => {
    render(<TasksPage />)

    expect(screen.getByRole('status')).toHaveTextContent(/loading tasks/i)
    await waitFor(() => expect(screen.queryByRole('status')).not.toBeInTheDocument())
  })

  it('renders the tasks returned by the API', async () => {
    render(<TasksPage />)

    expect(await screen.findByText('Buy milk')).toBeInTheDocument()
    expect(screen.getByText('Finished task')).toBeInTheDocument()
    expect(screen.getByText('2 tasks')).toBeInTheDocument()
  })

  it('creates a task and shows a success message', async () => {
    const user = userEvent.setup()
    render(<TasksPage />)
    await screen.findByText('Buy milk')

    await user.type(screen.getByLabelText(/title/i), 'New task from the page')
    await user.click(screen.getByRole('button', { name: /create task/i }))

    expect(mockedService.createTask).toHaveBeenCalledWith({
      title: 'New task from the page',
      description: null,
      dueDate: null,
      completed: false,
    })
    expect(await screen.findByText(/was created/i)).toBeInTheDocument()
  })

  it('shows the error returned by the API when the creation fails', async () => {
    const user = userEvent.setup()
    mockedService.createTask.mockRejectedValue(apiError('The title is mandatory'))

    render(<TasksPage />)
    await screen.findByText('Buy milk')

    await user.type(screen.getByLabelText(/title/i), 'Broken')
    await user.click(screen.getByRole('button', { name: /create task/i }))

    expect(await screen.findByText(/The title is mandatory/i)).toBeInTheDocument()
  })

  it('changes the status of a task', async () => {
    const user = userEvent.setup()
    render(<TasksPage />)
    await screen.findByText('Buy milk')

    await user.click(within(cardOf('Buy milk')).getByRole('button', { name: /^complete$/i }))

    expect(mockedService.changeTaskStatus).toHaveBeenCalledWith(1)
  })

  it('asks for confirmation before deleting a task', async () => {
    const user = userEvent.setup()
    render(<TasksPage />)
    await screen.findByText('Buy milk')

    await user.click(within(cardOf('Buy milk')).getByRole('button', { name: /^delete$/i }))

    const dialog = await screen.findByRole('alertdialog')
    expect(within(dialog).getByText(/cannot be undone/i)).toBeInTheDocument()
    expect(mockedService.deleteTask).not.toHaveBeenCalled()
  })

  it('deletes the task once the confirmation is accepted', async () => {
    const user = userEvent.setup()
    render(<TasksPage />)
    await screen.findByText('Buy milk')

    await user.click(within(cardOf('Buy milk')).getByRole('button', { name: /^delete$/i }))
    const dialog = await screen.findByRole('alertdialog')
    await user.click(within(dialog).getByRole('button', { name: /^delete$/i }))

    await waitFor(() => expect(mockedService.deleteTask).toHaveBeenCalledWith(1))
    expect(await screen.findByText(/was deleted/i)).toBeInTheDocument()
  })

  it('does not delete the task when the confirmation is cancelled', async () => {
    const user = userEvent.setup()
    render(<TasksPage />)
    await screen.findByText('Buy milk')

    await user.click(within(cardOf('Buy milk')).getByRole('button', { name: /^delete$/i }))
    const dialog = await screen.findByRole('alertdialog')
    await user.click(within(dialog).getByRole('button', { name: /cancel/i }))

    await waitFor(() => expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument())
    expect(mockedService.deleteTask).not.toHaveBeenCalled()
  })

  it('requests the tasks again when the filter changes', async () => {
    const user = userEvent.setup()
    render(<TasksPage />)
    await screen.findByText('Buy milk')

    const filters = screen.getByRole('group', { name: /filter tasks by status/i })
    await user.click(within(filters).getByRole('button', { name: /^pending$/i }))

    await waitFor(() => expect(mockedService.fetchTasks).toHaveBeenCalledWith('PENDING'))
  })

  it('loads the task into the form when it is edited', async () => {
    const user = userEvent.setup()
    render(<TasksPage />)
    await screen.findByText('Buy milk')

    await user.click(within(cardOf('Buy milk')).getByRole('button', { name: /^edit$/i }))

    expect(await screen.findByRole('heading', { name: /edit task/i })).toBeInTheDocument()
    expect(screen.getByLabelText(/title/i)).toHaveValue('Buy milk')
  })

  it('saves the changes of an edited task', async () => {
    const user = userEvent.setup()
    render(<TasksPage />)
    await screen.findByText('Buy milk')

    await user.click(within(cardOf('Buy milk')).getByRole('button', { name: /^edit$/i }))
    const titleInput = await screen.findByLabelText(/title/i)
    await user.clear(titleInput)
    await user.type(titleInput, 'Updated title')
    await user.click(screen.getByRole('button', { name: /save changes/i }))

    expect(mockedService.updateTask).toHaveBeenCalledWith(1, {
      title: 'Updated title',
      description: 'Semi skimmed',
      dueDate: '2999-01-15',
      completed: false,
    })
  })
})
