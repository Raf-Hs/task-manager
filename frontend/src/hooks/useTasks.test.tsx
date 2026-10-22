import { beforeEach, describe, expect, it, vi } from 'vitest'
import { act, renderHook, waitFor } from '@testing-library/react'
import { useTasks } from './useTasks'
import * as taskService from '../services/taskService'
import type { Task } from '../types/task'

vi.mock('../services/taskService')

const mockedService = vi.mocked(taskService)

const sampleTask: Task = {
  id: 1,
  title: 'Buy milk',
  description: null,
  dueDate: null,
  completed: false,
  createdAt: '2026-01-01T10:00:00Z',
}

describe('useTasks', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('loads the tasks on the first render', async () => {
    mockedService.fetchTasks.mockResolvedValue([sampleTask])

    const { result } = renderHook(() => useTasks())

    await waitFor(() => expect(result.current.isLoading).toBe(false))
    expect(mockedService.fetchTasks).toHaveBeenCalledWith('ALL')
    expect(result.current.tasks).toHaveLength(1)
  })

  it('reloads the tasks when the filter changes', async () => {
    mockedService.fetchTasks.mockResolvedValue([sampleTask])

    const { result } = renderHook(() => useTasks())
    await waitFor(() => expect(result.current.isLoading).toBe(false))

    act(() => result.current.setFilter('PENDING'))

    await waitFor(() => expect(mockedService.fetchTasks).toHaveBeenCalledWith('PENDING'))
  })

  it('stores the error message when the request fails', async () => {
    mockedService.fetchTasks.mockRejectedValue(new Error('boom'))

    const { result } = renderHook(() => useTasks())

    await waitFor(() => expect(result.current.error).not.toBeNull())
    expect(result.current.tasks).toHaveLength(0)
  })

  it('creates a task and refreshes the list', async () => {
    mockedService.fetchTasks.mockResolvedValue([sampleTask])
    mockedService.createTask.mockResolvedValue(sampleTask)

    const { result } = renderHook(() => useTasks())
    await waitFor(() => expect(result.current.isLoading).toBe(false))

    const input = { title: 'Buy milk', description: null, dueDate: null, completed: false }
    let created = false
    await act(async () => {
      created = await result.current.createTask(input)
    })

    expect(created).toBe(true)
    expect(mockedService.createTask).toHaveBeenCalledWith(input)
    expect(mockedService.fetchTasks).toHaveBeenCalledTimes(2)
  })

  it('returns false and keeps the error when the creation fails', async () => {
    mockedService.fetchTasks.mockResolvedValue([])
    mockedService.createTask.mockRejectedValue(new Error('invalid title'))

    const { result } = renderHook(() => useTasks())
    await waitFor(() => expect(result.current.isLoading).toBe(false))

    const input = { title: '', description: null, dueDate: null, completed: false }
    let created = true
    await act(async () => {
      created = await result.current.createTask(input)
    })

    expect(created).toBe(false)
    expect(result.current.error).not.toBeNull()
  })

  it('toggles the status of a task', async () => {
    mockedService.fetchTasks.mockResolvedValue([sampleTask])
    mockedService.changeTaskStatus.mockResolvedValue({ ...sampleTask, completed: true })

    const { result } = renderHook(() => useTasks())
    await waitFor(() => expect(result.current.isLoading).toBe(false))

    await act(async () => {
      await result.current.toggleTask(1)
    })

    expect(mockedService.changeTaskStatus).toHaveBeenCalledWith(1)
  })

  it('deletes a task and refreshes the list', async () => {
    mockedService.fetchTasks.mockResolvedValue([sampleTask])
    mockedService.deleteTask.mockResolvedValue(undefined)

    const { result } = renderHook(() => useTasks())
    await waitFor(() => expect(result.current.isLoading).toBe(false))

    let deleted = false
    await act(async () => {
      deleted = await result.current.removeTask(1)
    })

    expect(deleted).toBe(true)
    expect(mockedService.deleteTask).toHaveBeenCalledWith(1)
  })
})
