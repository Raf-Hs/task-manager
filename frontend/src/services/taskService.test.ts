import { beforeEach, describe, expect, it, vi } from 'vitest'
import { apiClient } from './api'
import {
  changeTaskStatus,
  createTask,
  deleteTask,
  fetchTaskById,
  fetchTasks,
  updateTask,
} from './taskService'
import type { Task } from '../types/task'

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

const mockedClient = vi.mocked(apiClient)

const sampleTask: Task = {
  id: 1,
  title: 'Buy milk',
  description: 'Semi skimmed',
  dueDate: '2030-01-15',
  completed: false,
  createdAt: '2026-01-01T10:00:00Z',
}

describe('taskService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('fetchTasks', () => {
    it('requests every task when no filter is applied', async () => {
      mockedClient.get.mockResolvedValue({ data: [sampleTask] })

      const result = await fetchTasks()

      expect(mockedClient.get).toHaveBeenCalledWith('/tasks', { params: {} })
      expect(result).toEqual([sampleTask])
    })

    it('sends the status filter when one is given', async () => {
      mockedClient.get.mockResolvedValue({ data: [] })

      await fetchTasks('PENDING')

      expect(mockedClient.get).toHaveBeenCalledWith('/tasks', { params: { status: 'PENDING' } })
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

  describe('createTask', () => {
    it('posts the task to the collection', async () => {
      mockedClient.post.mockResolvedValue({ data: sampleTask })

      const input = { title: 'Buy milk', description: null, dueDate: null, completed: false }
      const result = await createTask(input)

      expect(mockedClient.post).toHaveBeenCalledWith('/tasks', input)
      expect(result).toEqual(sampleTask)
    })
  })

  describe('updateTask', () => {
    it('puts the task on its own url', async () => {
      mockedClient.put.mockResolvedValue({ data: sampleTask })

      const input = { title: 'Buy milk', description: null, dueDate: null, completed: true }
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
