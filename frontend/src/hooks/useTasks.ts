import { useCallback, useEffect, useState } from 'react'
import * as taskService from '../services/taskService'
import { getErrorMessage } from '../services/api'
import { useDebouncedValue } from './useDebouncedValue'
import type {
  Task,
  TaskInput,
  TaskPriorityFilter,
  TaskSortOption,
  TaskStatistics,
  TaskStatus,
} from '../types/task'

/** Number of tasks shown on each page. */
const PAGE_SIZE = 10

/** Milliseconds the user has to stop typing before the search is applied. */
const SEARCH_DEBOUNCE_MS = 300

/**
 * Everything that describes which slice of the list is being shown.
 *
 * The fields live in a single object on purpose: when a filter changes, the
 * page goes back to the first one in the same update, so the list is never
 * requested twice with an inconsistent combination of values.
 */
interface TaskQuery {
  status: TaskStatus
  priority: TaskPriorityFilter
  search: string
  sort: TaskSortOption
  page: number
}

const INITIAL_QUERY: TaskQuery = {
  status: 'ALL',
  priority: 'ALL',
  search: '',
  sort: 'CREATED_AT',
  page: 0,
}

interface UseTasksResult {
  tasks: Task[]
  statistics: TaskStatistics | null
  isLoading: boolean
  isSaving: boolean
  error: string | null
  filter: TaskStatus
  priorityFilter: TaskPriorityFilter
  search: string
  sort: TaskSortOption
  page: number
  totalElements: number
  totalPages: number
  setFilter: (status: TaskStatus) => void
  setPriorityFilter: (priority: TaskPriorityFilter) => void
  setSearch: (term: string) => void
  setSort: (sort: TaskSortOption) => void
  setPage: (page: number) => void
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
  const [query, setQuery] = useState<TaskQuery>(INITIAL_QUERY)
  const [tasks, setTasks] = useState<Task[]>([])
  const [statistics, setStatistics] = useState<TaskStatistics | null>(null)
  const [totalElements, setTotalElements] = useState<number>(0)
  const [totalPages, setTotalPages] = useState<number>(0)
  const [isLoading, setIsLoading] = useState<boolean>(true)
  const [isSaving, setIsSaving] = useState<boolean>(false)
  const [error, setError] = useState<string | null>(null)

  // The text the user is typing right now, and the text that is actually
  // applied to the query once the typing stops. The box is bound to the
  // first one, otherwise every keystroke would be overwritten by the value
  // that is still waiting for the debounce.
  const [searchInput, setSearchInput] = useState<string>('')
  const debouncedSearch = useDebouncedValue(searchInput, SEARCH_DEBOUNCE_MS)

  const load = useCallback(async (current: TaskQuery) => {
    setIsLoading(true)
    setError(null)
    try {
      // The list and the figures are requested together so the panel and the
      // list always show the same moment of the database.
      const [page, figures] = await Promise.all([
        taskService.fetchTasks({
          status: current.status,
          priority: current.priority,
          search: current.search,
          sort: current.sort,
          page: current.page,
          size: PAGE_SIZE,
        }),
        taskService.fetchStatistics(),
      ])
      setTasks(page.content)
      setTotalElements(page.totalElements)
      setTotalPages(page.totalPages)
      setStatistics(figures)
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Apply the debounced search term to the query. Changing the search also
  // brings the user back to the first page.
  useEffect(() => {
    setQuery((current) =>
      current.search === debouncedSearch
        ? current
        : { ...current, search: debouncedSearch, page: 0 },
    )
  }, [debouncedSearch])

  // Reload the list whenever the query changes (and on the first render).
  useEffect(() => {
    void load(query)
  }, [query, load])

  const setFilter = useCallback((status: TaskStatus) => {
    setQuery((current) => ({ ...current, status, page: 0 }))
  }, [])

  const setPriorityFilter = useCallback((priority: TaskPriorityFilter) => {
    setQuery((current) => ({ ...current, priority, page: 0 }))
  }, [])

  const setSort = useCallback((sort: TaskSortOption) => {
    setQuery((current) => ({ ...current, sort, page: 0 }))
  }, [])

  const setSearch = useCallback((term: string) => {
    setSearchInput(term)
  }, [])

  const setPage = useCallback((page: number) => {
    setQuery((current) => ({ ...current, page: Math.max(page, 0) }))
  }, [])

  const createTask = useCallback(
    async (input: TaskInput) => {
      setIsSaving(true)
      setError(null)
      try {
        await taskService.createTask(input)
        await load(query)
        return true
      } catch (err) {
        setError(getErrorMessage(err))
        return false
      } finally {
        setIsSaving(false)
      }
    },
    [query, load],
  )

  const updateTask = useCallback(
    async (id: number, input: TaskInput) => {
      setIsSaving(true)
      setError(null)
      try {
        await taskService.updateTask(id, input)
        await load(query)
        return true
      } catch (err) {
        setError(getErrorMessage(err))
        return false
      } finally {
        setIsSaving(false)
      }
    },
    [query, load],
  )

  const toggleTask = useCallback(
    async (id: number) => {
      setIsSaving(true)
      setError(null)
      try {
        await taskService.changeTaskStatus(id)
        await load(query)
        return true
      } catch (err) {
        setError(getErrorMessage(err))
        return false
      } finally {
        setIsSaving(false)
      }
    },
    [query, load],
  )

  const removeTask = useCallback(
    async (id: number) => {
      setIsSaving(true)
      setError(null)
      try {
        await taskService.deleteTask(id)
        await load(query)
        return true
      } catch (err) {
        setError(getErrorMessage(err))
        return false
      } finally {
        setIsSaving(false)
      }
    },
    [query, load],
  )

  const reload = useCallback(async () => {
    await load(query)
  }, [query, load])

  return {
    tasks,
    statistics,
    isLoading,
    isSaving,
    error,
    filter: query.status,
    priorityFilter: query.priority,
    search: searchInput,
    sort: query.sort,
    page: query.page,
    totalElements,
    totalPages,
    setFilter,
    setPriorityFilter,
    setSearch,
    setSort,
    setPage,
    createTask,
    updateTask,
    toggleTask,
    removeTask,
    reload,
  }
}
