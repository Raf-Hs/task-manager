import { useState } from 'react'
import { TaskForm } from '../components/TaskForm'
import { TaskList } from '../components/TaskList'
import { TaskFilters } from '../components/TaskFilters'
import { TaskSearch } from '../components/TaskSearch'
import { TaskSort } from '../components/TaskSort'
import { TaskStats } from '../components/TaskStats'
import { Pagination } from '../components/Pagination'
import { ConfirmDialog } from '../components/ConfirmDialog'
import { NotificationList } from '../components/NotificationList'
import { useTasks } from '../hooks/useTasks'
import { useNotifications } from '../hooks/useNotifications'
import type { Task, TaskInput } from '../types/task'

/**
 * Main view of the application: the form, the filters and the list of tasks.
 *
 * All the data handling lives in the `useTasks` hook, so this component only
 * coordinates the user interface.
 */
export function TasksPage() {
  const {
    tasks,
    statistics,
    isLoading,
    isSaving,
    error,
    filter,
    setFilter,
    priorityFilter,
    setPriorityFilter,
    search,
    setSearch,
    sort,
    setSort,
    page,
    setPage,
    totalElements,
    totalPages,
    createTask,
    updateTask,
    toggleTask,
    removeTask,
  } = useTasks()

  const { notifications, notify, dismiss } = useNotifications()

  const [editingTask, setEditingTask] = useState<Task | null>(null)
  const [taskToDelete, setTaskToDelete] = useState<Task | null>(null)

  // When there are no results the message changes: the list is not empty,
  // the current filters are just too restrictive.
  const isFiltered = filter !== 'ALL' || priorityFilter !== 'ALL' || search.trim() !== ''

  async function handleSubmit(input: TaskInput) {
    if (editingTask) {
      const updated = await updateTask(editingTask.id, input)
      if (updated) {
        notify('success', `Task "${input.title}" was updated.`)
        setEditingTask(null)
      }
      return
    }

    const created = await createTask(input)
    if (created) {
      notify('success', `Task "${input.title}" was created.`)
    }
  }

  async function handleToggle(task: Task) {
    const updated = await toggleTask(task.id)
    if (updated) {
      notify(
        'success',
        task.completed
          ? `Task "${task.title}" was marked as pending.`
          : `Task "${task.title}" was completed.`,
      )
    }
  }

  async function handleDelete() {
    if (!taskToDelete) {
      return
    }
    const deleted = await removeTask(taskToDelete.id)
    if (deleted) {
      notify('success', `Task "${taskToDelete.title}" was deleted.`)
    }
    setTaskToDelete(null)
  }

  return (
    <div className="container">
      <NotificationList notifications={notifications} onDismiss={dismiss} />

      <div className="layout">
        <aside className="layout__sidebar">
          <TaskForm
            key={editingTask?.id ?? 'new'}
            task={editingTask}
            isSaving={isSaving}
            onSubmit={handleSubmit}
            onCancel={() => setEditingTask(null)}
          />
        </aside>

        <main className="layout__content">
          <TaskStats statistics={statistics} />

          <div className="toolbar">
            <TaskSearch value={search} onChange={setSearch} />
            <TaskSort value={sort} onChange={setSort} />
          </div>

          <TaskFilters
            activeFilter={filter}
            onChange={setFilter}
            activePriority={priorityFilter}
            onPriorityChange={setPriorityFilter}
            total={totalElements}
          />

          {error && (
            <div className="alert alert--error" role="alert">
              {error}
            </div>
          )}

          <TaskList
            tasks={tasks}
            isLoading={isLoading}
            isBusy={isSaving}
            onToggle={handleToggle}
            onEdit={(task) => setEditingTask(task)}
            onDelete={(task) => setTaskToDelete(task)}
            emptyTitle={isFiltered ? 'No matching tasks' : 'No tasks yet'}
            emptyText={
              isFiltered
                ? 'Try another search or a different filter.'
                : 'Create your first task with the form on the left.'
            }
          />

          <Pagination
            page={page}
            totalPages={totalPages}
            totalElements={totalElements}
            onPageChange={setPage}
          />
        </main>
      </div>

      {taskToDelete && (
        <ConfirmDialog
          title="Delete task"
          message={`Are you sure you want to delete "${taskToDelete.title}"? This action cannot be undone.`}
          confirmLabel="Delete"
          onConfirm={() => void handleDelete()}
          onCancel={() => setTaskToDelete(null)}
        />
      )}
    </div>
  )
}
