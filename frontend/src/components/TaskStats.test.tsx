import { describe, expect, it } from 'vitest'
import { render, screen, within } from '@testing-library/react'
import { TaskStats } from './TaskStats'
import type { TaskStatistics } from '../types/task'

const statistics: TaskStatistics = {
  total: 10,
  pending: 6,
  completed: 4,
  overdue: 2,
  completionPercentage: 40,
}

describe('TaskStats', () => {
  it('renders nothing while the figures are not available yet', () => {
    const { container } = render(<TaskStats statistics={null} />)

    expect(container).toBeEmptyDOMElement()
  })

  it('shows the four figures of the list', () => {
    render(<TaskStats statistics={statistics} />)

    const panel = screen.getByRole('region', { name: /task statistics/i })
    expect(within(panel).getByText('10')).toBeInTheDocument()
    expect(within(panel).getByText('Total')).toBeInTheDocument()
    expect(within(panel).getByText('Pending')).toBeInTheDocument()
    expect(within(panel).getByText('Completed')).toBeInTheDocument()
    expect(within(panel).getByText('Overdue')).toBeInTheDocument()
    expect(within(panel).getByText('Completion')).toBeInTheDocument()
  })

  it('shows the completion percentage and its progress bar', () => {
    render(<TaskStats statistics={statistics} />)

    expect(screen.getByText('40%')).toBeInTheDocument()
    const bar = screen.getByRole('progressbar')
    expect(bar).toHaveAttribute('aria-valuenow', '40')
  })
})
