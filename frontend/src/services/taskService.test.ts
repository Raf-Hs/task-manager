import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { Mock } from 'vitest'
import { apiClient } from './api'
import {
  changeTaskStatus,
  createTask,
  deleteTask,
  fetchStatistics,
  fetchTaskById,
  fetchTasks,
  updateTask,
} from './taskService'
import type { PagedResponse, Task, TaskStatistics } from '../types/task'

vi.mock('./api', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
  getErrorMessage: vi.fn(),
}))

// The Axios client is replaced by plain mock functions above. The cast tells
// TypeScript that the methods are mocks, so they expose the matchers of Vitest.
const mockedClient = apiClient as unknown as {
  get: Mock
  post: Mock
  put: Mock
  patch: Mock
  delete: Mock
}

const sampleTask: Task = {
  id: 1,
  title: 'Buy milk',
  description: 'Semi skimmed',
  dueDate: '2030-01-15',
  priority: 'HIGH',
  completed: false,
  createdAt: '2026-01-01T10:00:00Z',
  updatedAt: '2026-01-02T10:00:00Z',
}

const samplePage: PagedResponse<Task> = {
  content: [sampleTask],
  page: 0,
  size: 10,
  totalElements: 1,
  totalPages: 1,
  first: true,
  last: true,
}

const sampleStatistics: TaskStatistics = {
  total: 3,
  pending: 2,
  completed: 1,
  overdue: 1,
  completionPercentage: 33,
}

describe('taskService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('fetchTasks', () => {
    it('requests the first page with the default parameters when no query is given', async () => {
      mockedClient.get.mockResolvedValue({ data: samplePage })

      const result = await fetchTasks()

      expect(mockedClient.get).toHaveBeenCalledWith('/tasks', { params: {} })
      expect(result).toEqual(samplePage)
    })

    it('sends the status filter when one is given', async () => {
      mockedClient.get.mockResolvedValue({ data: samplePage })

      await fetchTasks({ status: 'PENDING' })

      expect(mockedClient.get).toHaveBeenCalledWith('/tasks', { params: { status: 'PENDING' } })
    })

    it('sends the priority filter when one is given', async () => {
      mockedClient.get.mockResolvedValue({ data: samplePage })

      await fetchTasks({ priority: 'HIGH' })

      expect(mockedClient.get).toHaveBeenCalledWith('/tasks', { params: { priority: 'HIGH' } })
    })

    it('sends the search term, trimmed', async () => {
      mockedClient.get.mockResolvedValue({ data: samplePage })

      await fetchTasks({ search: '  milk  ' })

      expect(mockedClient.get).toHaveBeenCalledWith('/tasks', { params: { search: 'milk' } })
    })

    it('sends the sort criterion and the pagination', async () => {
      mockedClient.get.mockResolvedValue({ data: samplePage })

      await fetchTasks({ sort: 'TITLE', page: 2, size: 5 })

      expect(mockedClient.get).toHaveBeenCalledWith('/tasks', {
        params: { sort: 'TITLE', page: 2, size: 5 },
      })
    })

    it('does not send the ALL values, which are the default ones', async () => {
      mockedClient.get.mockResolvedValue({ data: samplePage })

      await fetchTasks({ status: 'ALL', priority: 'ALL', search: '   ', sort: 'CREATED_AT' })

      expect(mockedClient.get).toHaveBeenCalledWith('/tasks', { params: {} })
    })
  })

  describe('fetchTaskById', () => {
    it('requests the task by id', async () => {
      mockedClient.get.mockResolvedValue({ data: sampleTask })

      const result = await fetchTaskById(1)

      expect(mockedClient.get).toHaveBeenCalledWith('/tasks/1')
      expect(result).toEqual(sampleTask)
    })
  })

  describe('fetchStatistics', () => {
    it('requests the statistics endpoint', async () => {
      mockedClient.get.mockResolvedValue({ data: sampleStatistics })

      const result = await fetchStatistics()

      expect(mockedClient.get).toHaveBeenCalledWith('/tasks/statistics')
      expect(result).toEqual(sampleStatistics)
    })
  })

  describe('createTask', () => {
    it('posts the task to the collection', async () => {
      mockedClient.post.mockResolvedValue({ data: sampleTask })

      const input = {
        title: 'Buy milk',
        description: null,
        dueDate: null,
        priority: 'HIGH' as const,
        completed: false,
      }
      const result = await createTask(input)

      expect(mockedClient.post).toHaveBeenCalledWith('/tasks', input)
      expect(result).toEqual(sampleTask)
    })
  })

  describe('updateTask', () => {
    it('puts the task on its own url', async () => {
      mockedClient.put.mockResolvedValue({ data: sampleTask })

      const input = {
        title: 'Buy milk',
        description: null,
        dueDate: null,
        priority: 'LOW' as const,
        completed: true,
      }
      await updateTask(1, input)

      expect(mockedClient.put).toHaveBeenCalledWith('/tasks/1', input)
    })
  })

  describe('changeTaskStatus', () => {
    it('patches the task without a body when no status is given', async () => {
      mockedClient.patch.mockResolvedValue({ data: sampleTask })

      await changeTaskStatus(1)

      expect(mockedClient.patch).toHaveBeenCalledWith('/tasks/1/complete', undefined)
    })

    it('patches the task with an explicit status', async () => {
      mockedClient.patch.mockResolvedValue({ data: sampleTask })

      await changeTaskStatus(1, true)

      expect(mockedClient.patch).toHaveBeenCalledWith('/tasks/1/complete', { completed: true })
    })
  })

  describe('deleteTask', () => {
    it('deletes the task by id', async () => {
      mockedClient.delete.mockResolvedValue({ data: undefined })

      await deleteTask(1)

      expect(mockedClient.delete).toHaveBeenCalledWith('/tasks/1')
    })
  })

  it('propagates the error when the request fails', async () => {
    mockedClient.get.mockRejectedValue(new Error('Network error'))

    await expect(fetchTasks()).rejects.toThrow('Network error')
  })
})
