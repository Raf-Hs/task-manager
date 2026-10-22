import type { Notification } from '../types/task'

interface NotificationListProps {
  notifications: Notification[]
  /** Removes a notification once it has been shown. */
  onDismiss: (id: number) => void
}

/**
 * Feedback messages shown at the top of the page.
 *
 * They tell the user whether the last action (create, update, delete…)
 * succeeded or failed.
 */
export function NotificationList({ notifications, onDismiss }: NotificationListProps) {
  if (notifications.length === 0) {
    return null
  }

  return (
    <div className="notifications" aria-live="polite" aria-atomic="true">
      {notifications.map((notification) => (
        <div
          key={notification.id}
          className={`notification notification--${notification.type}`}
          role={notification.type === 'error' ? 'alert' : 'status'}
        >
          <span className="notification__message">{notification.message}</span>
          <button
            type="button"
            className="notification__close"
            aria-label="Dismiss notification"
            onClick={() => onDismiss(notification.id)}
          >
            ×
          </button>
        </div>
      ))}
    </div>
  )
}
