import { apiClient } from './api'
import type {
  Task,
  TaskInput,
  TaskPriorityFilter,
  TaskStatistics,
  TaskStatus,
} from '../types/task'

/**
 * All the HTTP calls of the application.
 *
 * The components never talk to Axios directly: they call these functions, so
 * the API contract lives in a single module.
 */

/** Filters accepted by the list endpoint. */
export interface TaskListFilters {
  status?: TaskStatus
  priority?: TaskPriorityFilter
}

/** Lists the tasks, optionally filtered by status and priority. */
export async function fetchTasks(filters: TaskListFilters = {}): Promise<Task[]> {
  const params: Record<string, string> = {}

  if (filters.status && filters.status !== 'ALL') {
    params.status = filters.status
  }
  if (filters.priority && filters.priority !== 'ALL') {
    params.priority = filters.priority
  }

  const response = await apiClient.get<Task[]>('/tasks', { params })
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
