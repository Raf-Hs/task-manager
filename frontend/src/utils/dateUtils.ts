/**
 * Helpers to work with dates on the client side.
 *
 * The API uses the ISO-8601 format (`YYYY-MM-DD` for dates), which JavaScript
 * parses as UTC midnight. These helpers keep the comparisons consistent with
 * the "today" of the user running the browser.
 */

/** Returns today's date as `YYYY-MM-DD`. */
export function todayAsString(): string {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

/**
 * Tells whether a task is overdue: it is still pending and its deadline is
 * before today.
 */
export function isOverdue(dueDate: string | null, completed: boolean): boolean {
  if (completed || !dueDate) {
    return false
  }
  return dueDate < todayAsString()
}

/** Formats a date as `DD/MM/YYYY`. */
export function formatDate(date: string | null): string {
  if (!date) {
    return '—'
  }
  const [year, month, day] = date.split('-')
  return `${day}/${month}/${year}`
}

/** Formats an ISO timestamp as `DD/MM/YYYY HH:mm`. */
export function formatDateTime(timestamp: string | null): string {
  if (!timestamp) {
    return '—'
  }
  const date = new Date(timestamp)
  if (Number.isNaN(date.getTime())) {
    return '—'
  }
  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const year = date.getFullYear()
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${day}/${month}/${year} ${hours}:${minutes}`
}
