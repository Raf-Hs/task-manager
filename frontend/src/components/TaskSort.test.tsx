import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { TaskSort } from './TaskSort'

describe('TaskSort', () => {
  it('shows the criterion that is applied', () => {
    render(<TaskSort value="TITLE" onChange={vi.fn()} />)

    expect(screen.getByLabelText(/sort tasks by/i)).toHaveValue('TITLE')
  })

  it('offers every criterion of the API', () => {
    render(<TaskSort value="CREATED_AT" onChange={vi.fn()} />)

    const options = screen.getAllByRole('option') as HTMLOptionElement[]
    expect(options.map((option) => option.value)).toEqual([
      'CREATED_AT',
      'UPDATED_AT',
      'DUE_DATE',
      'TITLE',
      'PRIORITY',
    ])
  })

  it('reports the chosen criterion', async () => {
    const user = userEvent.setup()
    const onChange = vi.fn()
    render(<TaskSort value="CREATED_AT" onChange={onChange} />)

    await user.selectOptions(screen.getByLabelText(/sort tasks by/i), 'PRIORITY')

    expect(onChange).toHaveBeenCalledWith('PRIORITY')
  })
})
