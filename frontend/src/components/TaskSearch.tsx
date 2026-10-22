interface TaskSearchProps {
  /** Text currently typed by the user. */
  value: string
  /** Called every time the user types. */
  onChange: (term: string) => void
}

/**
 * Box used to search tasks by title or description.
 *
 * The component is controlled: it does not wait, it only reports what the
 * user types. The delay before the request is sent lives in the hook, so the
 * same behaviour is applied everywhere the search is used.
 */
export function TaskSearch({ value, onChange }: TaskSearchProps) {
  return (
    <div className="task-search">
      <label htmlFor="task-search" className="visually-hidden">
        Search tasks
      </label>
      <input
        id="task-search"
        name="search"
        type="search"
        value={value}
        placeholder="Search by title or description…"
        onChange={(event) => onChange(event.target.value)}
      />
      {value !== '' && (
        <button
          type="button"
          className="task-search__clear"
          onClick={() => onChange('')}
          aria-label="Clear the search"
        >
          ×
        </button>
      )}
    </div>
  )
}
