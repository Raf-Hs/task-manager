interface PaginationProps {
  /** Zero-based number of the page being shown. */
  page: number
  /** Total number of pages. */
  totalPages: number
  /** Total number of tasks that match the current filters. */
  totalElements: number
  /** Called with the number of the page to show. */
  onPageChange: (page: number) => void
}

/**
 * Controls that move between the pages of the task list.
 *
 * They are hidden when everything fits in a single page, so the interface
 * stays clean for the usual case of a short list.
 */
export function Pagination({
  page,
  totalPages,
  totalElements,
  onPageChange,
}: PaginationProps) {
  if (totalPages <= 1) {
    return null
  }

  const isFirst = page === 0
  const isLast = page >= totalPages - 1

  return (
    <nav className="pagination" aria-label="Pagination of the task list">
      <button
        type="button"
        className="btn btn--small btn--ghost"
        onClick={() => onPageChange(page - 1)}
        disabled={isFirst}
      >
        Previous
      </button>

      <span className="pagination__info">
        Page {page + 1} of {totalPages} · {totalElements}{' '}
        {totalElements === 1 ? 'task' : 'tasks'}
      </span>

      <button
        type="button"
        className="btn btn--small btn--ghost"
        onClick={() => onPageChange(page + 1)}
        disabled={isLast}
      >
        Next
      </button>
    </nav>
  )
}
