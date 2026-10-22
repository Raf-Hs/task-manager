import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { TaskSearch } from './TaskSearch'

describe('TaskSearch', () => {
  it('shows the text that is being typed', () => {
    render(<TaskSearch value="milk" onChange={vi.fn()} />)

    expect(screen.getByLabelText(/search tasks/i)).toHaveValue('milk')
  })

  it('reports every change to the user', async () => {
    const user = userEvent.setup()
    const onChange = vi.fn()
    render(<TaskSearch value="" onChange={onChange} />)

    await user.type(screen.getByLabelText(/search tasks/i), 'm')

    expect(onChange).toHaveBeenCalledWith('m')
  })

  it('does not show the clear button when the search is empty', () => {
    render(<TaskSearch value="" onChange={vi.fn()} />)

    expect(screen.queryByRole('button', { name: /clear the search/i })).not.toBeInTheDocument()
  })

  it('empties the search when the clear button is pressed', async () => {
    const user = userEvent.setup()
    const onChange = vi.fn()
    render(<TaskSearch value="milk" onChange={onChange} />)

    await user.click(screen.getByRole('button', { name: /clear the search/i }))

    expect(onChange).toHaveBeenCalledWith('')
  })
})
