import { useEffect, useState } from 'react'

/**
 * Returns a value that only changes after it has stopped changing for a while.
 *
 * It is used by the search box: the user types freely and the request is
 * sent once they stop, instead of one request per keystroke.
 *
 * @param value     the value to delay
 * @param delayMs   milliseconds the value must stay still before it is applied
 */
export function useDebouncedValue<T>(value: T, delayMs = 300): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value)

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedValue(value), delayMs)
    // The timer is cancelled when the value changes again, so only the last
    // keystroke ends up being applied.
    return () => clearTimeout(timer)
  }, [value, delayMs])

  return debouncedValue
}
