interface ConfirmDialogProps {
  /** Title of the dialog. */
  title: string
  /** Body of the dialog. */
  message: string
  /** Label of the confirm button. */
  confirmLabel?: string
  /** Called when the user confirms. */
  onConfirm: () => void
  /** Called when the user cancels. */
  onCancel: () => void
}

/**
 * Modal dialog used to confirm a destructive action such as deleting a task.
 */
export function ConfirmDialog({
  title,
  message,
  confirmLabel = 'Delete',
  onConfirm,
  onCancel,
}: ConfirmDialogProps) {
  return (
    <div className="modal-backdrop" role="presentation" onClick={onCancel}>
      <div
        className="modal"
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="confirm-dialog-title"
        onClick={(event) => event.stopPropagation()}
      >
        <h2 className="modal__title" id="confirm-dialog-title">
          {title}
        </h2>
        <p className="modal__message">{message}</p>
        <div className="modal__actions">
          <button type="button" className="btn btn--ghost" onClick={onCancel}>
            Cancel
          </button>
          <button type="button" className="btn btn--danger" onClick={onConfirm} autoFocus>
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  )
}
