import { apiClient } from './api'
import type { Task, TaskInput, TaskStatus } from '../types/task'

/**
 * All the HTTP calls of the application.
 *
 * The components never talk to Axios directly: they call these functions, so
 * the API contract lives in a single module.
 */

/** Lists the tasks, optionally filtered by status. */
export async function fetchTasks(status: TaskStatus = 'ALL'): Promise<Task[]> {
  const response = await apiClient.get<Task[]>('/tasks', {
    params: status === 'ALL' ? {} : { status },
  })
  return response.data
}

/** Retrieves a single task. */
export async function fetchTaskById(id: number): Promise<Task> {
  const response = await apiClient.get<Task>(`/tasks/${id}`)
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
