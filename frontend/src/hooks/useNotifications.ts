import { useCallback, useRef, useState } from 'react'
import type { Notification, NotificationType } from '../types/task'

interface UseNotificationsResult {
  notifications: Notification[]
  /** Shows a message that disappears after a few seconds. */
  notify: (type: NotificationType, message: string) => void
  /** Removes a single message. */
  dismiss: (id: number) => void
}

/**
 * Small hook that keeps the feedback messages of the application.
 *
 * Each message is removed automatically after 4 seconds, and can also be
 * closed manually.
 */
export function useNotifications(autoDismissMs = 4000): UseNotificationsResult {
  const [notifications, setNotifications] = useState<Notification[]>([])
  const nextId = useRef(1)

  const dismiss = useCallback((id: number) => {
    setNotifications((current) => current.filter((notification) => notification.id !== id))
  }, [])

  const notify = useCallback(
    (type: NotificationType, message: string) => {
      const id = nextId.current++
      setNotifications((current) => [...current, { id, type, message }])
      window.setTimeout(() => dismiss(id), autoDismissMs)
    },
    [autoDismissMs, dismiss],
  )

  return { notifications, notify, dismiss }
}
