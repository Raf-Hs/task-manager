import { apiClient } from './api'
import type {
  PagedResponse,
  Task,
  TaskInput,
  TaskPriorityFilter,
  TaskSortOption,
  TaskStatistics,
  TaskStatus,
} from '../types/task'

/**
 * All the HTTP calls of the application.
 *
 * The components never talk to Axios directly: they call these functions, so
 * the API contract lives in a single module.
 */

/** Query accepted by the list endpoint. Every field is optional. */
export interface TaskListQuery {
  status?: TaskStatus
  priority?: TaskPriorityFilter
  search?: string
  sort?: TaskSortOption
  page?: number
  size?: number
}

/** Lists a page of tasks, optionally filtered, searched and sorted. */
export async function fetchTasks(query: TaskListQuery = {}): Promise<PagedResponse<Task>> {
  const params: Record<string, string | number> = {}

  if (query.status && query.status !== 'ALL') {
    params.status = query.status
  }
  if (query.priority && query.priority !== 'ALL') {
    params.priority = query.priority
  }
  if (query.search && query.search.trim() !== '') {
    params.search = query.search.trim()
  }
  if (query.sort && query.sort !== 'CREATED_AT') {
    params.sort = query.sort
  }
  if (query.page !== undefined) {
    params.page = query.page
  }
  if (query.size !== undefined) {
    params.size = query.size
  }

  const response = await apiClient.get<PagedResponse<Task>>('/tasks', { params })
  return response.data
}

/** Retrieves a single task. */
export async function fetchTaskById(id: number): Promise<Task> {
  const response = await apiClient.get<Task>(`/tasks/${id}`)
  return response.data
}

/** Retrieves the aggregated figures of the task list. */
export async function fetchStatistics(): Promise<TaskStatistics> {
  const response = await apiClient.get<TaskStatistics>('/tasks/statistics')
  return response.data
}

/** Creates a new task. */
export async function createTask(input: TaskInput): Promise<Task> {
  const response = await apiClient.post<Task>('/tasks', input)
  return response.data
}

/** Updates an existing task. */
export async function updateTask(id: number, input: TaskInput): Promise<Task> {
  const response = await apiClient.put<Task>(`/tasks/${id}`, input)
  return response.data
}

/**
 * Marks a task as completed or pending.
 *
 * When `completed` is not given the API toggles the current status.
 */
export async function changeTaskStatus(id: number, completed?: boolean): Promise<Task> {
  const response = await apiClient.patch<Task>(
    `/tasks/${id}/complete`,
    completed === undefined ? undefined : { completed },
  )
  return response.data
}

/** Deletes a task. */
export async function deleteTask(id: number): Promise<void> {
  await apiClient.delete(`/tasks/${id}`)
}
