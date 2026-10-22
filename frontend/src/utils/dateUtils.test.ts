import { describe, expect, it } from 'vitest'
import { formatDate, formatDateTime, isOverdue, todayAsString } from '../utils/dateUtils'

describe('dateUtils', () => {
  describe('todayAsString', () => {
    it('returns the date in the YYYY-MM-DD format', () => {
      expect(todayAsString()).toMatch(/^\d{4}-\d{2}-\d{2}$/)
    })
  })

  describe('isOverdue', () => {
    it('is true for a pending task whose deadline is in the past', () => {
      expect(isOverdue('2020-01-01', false)).toBe(true)
    })

    it('is false for a pending task whose deadline is today', () => {
      expect(isOverdue(todayAsString(), false)).toBe(false)
    })

    it('is false for a pending task whose deadline is in the future', () => {
      expect(isOverdue('2999-12-31', false)).toBe(false)
    })

    it('is false for a completed task even when the deadline is in the past', () => {
      expect(isOverdue('2020-01-01', true)).toBe(false)
    })

    it('is false when there is no deadline', () => {
      expect(isOverdue(null, false)).toBe(false)
    })
  })

  describe('formatDate', () => {
    it('formats the date as DD/MM/YYYY', () => {
      expect(formatDate('2030-01-15')).toBe('15/01/2030')
    })

    it('returns a dash when the date is missing', () => {
      expect(formatDate(null)).toBe('—')
    })
  })

  describe('formatDateTime', () => {
    it('formats the timestamp as DD/MM/YYYY HH:mm', () => {
      expect(formatDateTime('2026-01-01T10:30:00Z')).toMatch(/^\d{2}\/\d{2}\/\d{4} \d{2}:\d{2}$/)
    })

    it('returns a dash for an invalid value', () => {
      expect(formatDateTime('not-a-date')).toBe('—')
      expect(formatDateTime(null)).toBe('—')
    })
  })
})
