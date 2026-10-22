/**
 * Types that describe the data exchanged with the REST API.
 *
 * They mirror the DTOs of the backend one to one, so a change on the Java
 * side is easy to reflect here.
 */

/** Urgency of a task, as defined by the backend `TaskPriority` enum. */
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH'

/** Priority filter of the list. `ALL` means "no filter". */
export type TaskPriorityFilter = 'ALL' | TaskPriority

/** Criteria available to sort the list, as defined by `TaskSortOption`. */
export type TaskSortOption = 'CREATED_AT' | 'UPDATED_AT' | 'DUE_DATE' | 'TITLE' | 'PRIORITY'

/** A task exactly as the API returns it. */
export interface Task {
  id: number
  title: string
  description: string | null
  dueDate: string | null
  priority: TaskPriority
  completed: boolean
  createdAt: string
  updatedAt: string
}

/** Payload sent when creating or updating a task. */
export interface TaskInput {
  title: string
  description: string | null
  dueDate: string | null
  priority: TaskPriority
  completed: boolean
}

/** Filters available in the task list. */
export type TaskStatus = 'ALL' | 'PENDING' | 'COMPLETED'

/**
 * Page of results returned by the list endpoint.
 *
 * It mirrors the `PagedResponse` DTO of the backend, which is used instead of
 * Spring's `Page` so the JSON contract stays stable.
 */
export interface PagedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

/** Aggregated figures returned by `GET /api/tasks/statistics`. */
export interface TaskStatistics {
  total: number
  pending: number
  completed: number
  overdue: number
  completionPercentage: number
}

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
