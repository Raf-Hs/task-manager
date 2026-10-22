import { useCallback, useEffect, useState } from 'react'
import * as taskService from '../services/taskService'
import { getErrorMessage } from '../services/api'
import type {
  Task,
  TaskInput,
  TaskPriorityFilter,
  TaskStatistics,
  TaskStatus,
} from '../types/task'

interface UseTasksResult {
  tasks: Task[]
  statistics: TaskStatistics | null
  isLoading: boolean
  isSaving: boolean
  error: string | null
  filter: TaskStatus
  priorityFilter: TaskPriorityFilter
  setFilter: (status: TaskStatus) => void
  setPriorityFilter: (priority: TaskPriorityFilter) => void
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
  const [statistics, setStatistics] = useState<TaskStatistics | null>(null)
  const [isLoading, setIsLoading] = useState<boolean>(true)
  const [isSaving, setIsSaving] = useState<boolean>(false)
  const [error, setError] = useState<string | null>(null)
  const [filter, setFilter] = useState<TaskStatus>('ALL')
  const [priorityFilter, setPriorityFilter] = useState<TaskPriorityFilter>('ALL')

  const load = useCallback(async (status: TaskStatus, priority: TaskPriorityFilter) => {
    setIsLoading(true)
    setError(null)
    try {
      // The list and the figures are requested together so the panel and the
      // list always show the same moment of the database.
      const [taskList, figures] = await Promise.all([
        taskService.fetchTasks({ status, priority }),
        taskService.fetchStatistics(),
      ])
      setTasks(taskList)
      setStatistics(figures)
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Reload the list whenever a filter changes (and on the first render).
  useEffect(() => {
    void load(filter, priorityFilter)
  }, [filter, priorityFilter, load])

  const createTask = useCallback(
    async (input: TaskInput) => {
      setIsSaving(true)
      setError(null)
      try {
        await taskService.createTask(input)
        await load(filter, priorityFilter)
        return true
      } catch (err) {
        setError(getErrorMessage(err))
        return false
      } finally {
        setIsSaving(false)
      }
    },
    [filter, priorityFilter, load],
  )

  const updateTask = useCallback(
    async (id: number, input: TaskInput) => {
      setIsSaving(true)
      setError(null)
      try {
        await taskService.updateTask(id, input)
        await load(filter, priorityFilter)
        return true
      } catch (err) {
        setError(getErrorMessage(err))
        return false
      } finally {
        setIsSaving(false)
      }
    },
    [filter, priorityFilter, load],
  )

  const toggleTask = useCallback(
    async (id: number) => {
      setIsSaving(true)
      setError(null)
      try {
        await taskService.changeTaskStatus(id)
        await load(filter, priorityFilter)
        return true
      } catch (err) {
        setError(getErrorMessage(err))
        return false
      } finally {
        setIsSaving(false)
      }
    },
    [filter, priorityFilter, load],
  )

  const removeTask = useCallback(
    async (id: number) => {
      setIsSaving(true)
      setError(null)
      try {
        await taskService.deleteTask(id)
        await load(filter, priorityFilter)
        return true
      } catch (err) {
        setError(getErrorMessage(err))
        return false
      } finally {
        setIsSaving(false)
      }
    },
    [filter, priorityFilter, load],
  )

  const reload = useCallback(async () => {
    await load(filter, priorityFilter)
  }, [filter, priorityFilter, load])

  return {
    tasks,
    statistics,
    isLoading,
    isSaving,
    error,
    filter,
    priorityFilter,
    setFilter,
    setPriorityFilter,
    createTask,
    updateTask,
    toggleTask,
    removeTask,
    reload,
  }
}
