/**
 * Types that describe the data exchanged with the REST API.
 *
 * They mirror the DTOs of the backend one to one, so a change on the Java
 * side is easy to reflect here.
 */

/** A task exactly as the API returns it. */
export interface Task {
  id: number
  title: string
  description: string | null
  dueDate: string | null
  completed: boolean
  createdAt: string
}

/** Payload sent when creating or updating a task. */
export interface TaskInput {
  title: string
  description: string | null
  dueDate: string | null
  completed: boolean
}

/** Filters available in the task list. */
export type TaskStatus = 'ALL' | 'PENDING' | 'COMPLETED'

/** Body returned by the API when something goes wrong. */
export interface ApiError {
  status: number
  message: string
  path: string
  timestamp: string
  fieldErrors?: Record<string, string>
}

/** A message shown to the user (success or error feedback). */
export type NotificationType = 'success' | 'error'

export interface Notification {
  id: number
  type: NotificationType
  message: string
}
