import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Pagination } from './Pagination'

describe('Pagination', () => {
  it('is hidden when everything fits in a single page', () => {
    const { container } = render(
      <Pagination page={0} totalPages={1} totalElements={3} onPageChange={vi.fn()} />,
    )

    expect(container).toBeEmptyDOMElement()
  })

  it('shows the current page and the totals', () => {
    render(<Pagination page={1} totalPages={5} totalElements={42} onPageChange={vi.fn()} />)

    expect(screen.getByText(/page 2 of 5/i)).toBeInTheDocument()
    expect(screen.getByText(/42 tasks/i)).toBeInTheDocument()
  })

  it('uses the singular form when there is only one task', () => {
    render(<Pagination page={0} totalPages={2} totalElements={1} onPageChange={vi.fn()} />)

    expect(screen.getByText(/1 task$/i)).toBeInTheDocument()
  })

  it('disables the previous button on the first page', () => {
    render(<Pagination page={0} totalPages={5} totalElements={42} onPageChange={vi.fn()} />)

    expect(screen.getByRole('button', { name: /previous/i })).toBeDisabled()
    expect(screen.getByRole('button', { name: /next/i })).toBeEnabled()
  })

  it('disables the next button on the last page', () => {
    render(<Pagination page={4} totalPages={5} totalElements={42} onPageChange={vi.fn()} />)

    expect(screen.getByRole('button', { name: /next/i })).toBeDisabled()
    expect(screen.getByRole('button', { name: /previous/i })).toBeEnabled()
  })

  it('asks for the previous and the next page', async () => {
    const user = userEvent.setup()
    const onPageChange = vi.fn()
    render(<Pagination page={1} totalPages={5} totalElements={42} onPageChange={onPageChange} />)

    await user.click(screen.getByRole('button', { name: /previous/i }))
    expect(onPageChange).toHaveBeenCalledWith(0)

    await user.click(screen.getByRole('button', { name: /next/i }))
    expect(onPageChange).toHaveBeenCalledWith(2)
  })
})
