import { useCallback, useEffect, useState } from 'react'
import * as taskService from '../services/taskService'
import { getErrorMessage } from '../services/api'
import type { Task, TaskInput, TaskStatus } from '../types/task'

interface UseTasksResult {
  tasks: Task[]
  isLoading: boolean
  isSaving: boolean
  error: string | null
  filter: TaskStatus
  setFilter: (status: TaskStatus) => void
  createTask: (input: TaskInput) => Promise<boolean>
  updateTask: (id: number, input: TaskInput) => Promise<boolean>
  toggleTask: (id: number) => Promise<boolean>
  removeTask: (id: number) => Promise<boolean>
  reload: () => Promise<void>
}

/**
 * Custom hook that owns the state of the task list.
 *
 * It isolates the components from Axios, the loading flags and the error
 * handling, so the view components only have to render data and dispatch
 * actions.
 */
export function useTasks(): UseTasksResult {
  const [tasks, setTasks] = useState<Task[]>([])
  const [isLoading, setIsLoading] = useState<boolean>(true)
  const [isSaving, setIsSaving] = useState<boolean>(false)
  const [error, setError] = useState<string | null>(null)
  const [filter, setFilter] = useState<TaskStatus>('ALL')

  const load = useCallback(async (status: TaskStatus) => {
    setIsLoading(true)
    setError(null)
    try {
      const data = await taskService.fetchTasks(status)
      setTasks(data)
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Reload the list whenever the filter changes (and on the first render).
  useEffect(() => {
    void load(filter)
  }, [filter, load])

  const createTask = useCallback(async (input: TaskInput) => {
    setIsSaving(true)
    setError(null)
    try {
      await taskService.createTask(input)
      await load(filter)
      return true
    } catch (err) {
      setError(getErrorMessage(err))
      return false
    } finally {
      setIsSaving(false)
    }
  }, [filter, load])

  const updateTask = useCallback(async (id: number, input: TaskInput) => {
    setIsSaving(true)
    setError(null)
    try {
      await taskService.updateTask(id, input)
      await load(filter)
      return true
    } catch (err) {
      setError(getErrorMessage(err))
      return false
    } finally {
      setIsSaving(false)
    }
  }, [filter, load])

  const toggleTask = useCallback(async (id: number) => {
    setIsSaving(true)
    setError(null)
    try {
      await taskService.changeTaskStatus(id)
      await load(filter)
      return true
    } catch (err) {
      setError(getErrorMessage(err))
      return false
    } finally {
      setIsSaving(false)
    }
  }, [filter, load])

  const removeTask = useCallback(async (id: number) => {
    setIsSaving(true)
    setError(null)
    try {
      await taskService.deleteTask(id)
      await load(filter)
      return true
    } catch (err) {
      setError(getErrorMessage(err))
      return false
    } finally {
      setIsSaving(false)
    }
  }, [filter, load])

  const reload = useCallback(async () => {
    await load(filter)
  }, [filter, load])

  return {
    tasks,
    isLoading,
    isSaving,
    error,
    filter,
    setFilter,
    createTask,
    updateTask,
    toggleTask,
    removeTask,
    reload,
  }
}
