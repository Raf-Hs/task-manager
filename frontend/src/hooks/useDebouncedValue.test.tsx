import { describe, expect, it, vi } from 'vitest'
import { act, renderHook } from '@testing-library/react'
import { useDebouncedValue } from './useDebouncedValue'

describe('useDebouncedValue', () => {
  it('returns the initial value right away', () => {
    vi.useFakeTimers()
    const { result } = renderHook(() => useDebouncedValue('first', 300))

    expect(result.current).toBe('first')
    vi.useRealTimers()
  })

  it('does not apply a change until the delay has passed', () => {
    vi.useFakeTimers()
    const { result, rerender } = renderHook(({ value }) => useDebouncedValue(value, 300), {
      initialProps: { value: 'first' },
    })

    rerender({ value: 'second' })
    expect(result.current).toBe('first')

    act(() => {
      vi.advanceTimersByTime(300)
    })
    expect(result.current).toBe('second')
    vi.useRealTimers()
  })

  it('only applies the last change when the value changes several times', () => {
    vi.useFakeTimers()
    const { result, rerender } = renderHook(({ value }) => useDebouncedValue(value, 300), {
      initialProps: { value: 'a' },
    })

    rerender({ value: 'ab' })
    act(() => {
      vi.advanceTimersByTime(200)
    })
    rerender({ value: 'abc' })
    act(() => {
      vi.advanceTimersByTime(300)
    })

    expect(result.current).toBe('abc')
    vi.useRealTimers()
  })
})
