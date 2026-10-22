import axios from 'axios'
import type { AxiosError } from 'axios'
import type { ApiError } from '../types/task'

/**
 * Base URL of the REST API.
 *
 * It defaults to a relative `/api` path: during development Vite proxies those
 * calls to the Spring Boot backend (see `vite.config.ts`). Set the environment
 * variable `VITE_API_BASE_URL` to point to a different host, for example
 * `http://localhost:8080/api`.
 */
const baseURL = import.meta.env.VITE_API_BASE_URL ?? '/api'

/**
 * Pre-configured Axios client. Every HTTP call of the application goes
 * through it, so the base URL and the headers are defined in a single place.
 */
export const apiClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
})

/**
 * Extracts a human readable message from an Axios error.
 *
 * The backend always answers with the `ApiError` shape, so the message can be
 * taken from the body. If the server is unreachable, a friendlier text is
 * returned instead of a raw network error.
 */
export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiError>
    const response = axiosError.response

    if (response?.data) {
      const body = response.data
      if (body.fieldErrors && Object.keys(body.fieldErrors).length > 0) {
        const details = Object.entries(body.fieldErrors)
          .map(([field, message]) => `${field}: ${message}`)
          .join(', ')
        return details
      }
      if (body.message) {
        return body.message
      }
    }

    if (!response) {
      return 'Could not reach the server. Is the backend running on port 8080?'
    }

    return `Request failed with status ${response.status}`
  }

  if (error instanceof Error) {
    return error.message
  }

  return 'Unexpected error'
}
