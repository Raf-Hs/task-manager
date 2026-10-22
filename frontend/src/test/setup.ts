import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterEach } from 'vitest'

// React Testing Library keeps the DOM between tests by default. Cleaning it
// after every test avoids components leaking into the next one.
afterEach(() => {
  cleanup()
})
