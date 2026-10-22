import { TasksPage } from './pages/TasksPage'

/**
 * Root component. It renders the header of the application and the main view.
 */
export default function App() {
  return (
    <div className="app">
      <header className="app__header">
        <div className="container app__header-inner">
          <h1 className="app__title">
            <span className="app__logo" aria-hidden="true">
              ✓
            </span>
            Task Manager
          </h1>
          <p className="app__subtitle">Organise your personal tasks in one place</p>
        </div>
      </header>

      <TasksPage />

      <footer className="app__footer">
        <div className="container">
          <p>
            Task Manager · Java 21 + Spring Boot 3 + PostgreSQL + React + TypeScript
          </p>
        </div>
      </footer>
    </div>
  )
}
