package com.taskmanager.dto;

/**
 * Optional body for {@code PATCH /api/tasks/{id}/complete}.
 *
 * <p>When the body is sent the status is set explicitly; when it is omitted the
 * service toggles the current value.
 */
public class UpdateCompletedRequest {

    private boolean completed;

    public UpdateCompletedRequest() {
    }

    public UpdateCompletedRequest(boolean completed) {
        this.completed = completed;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
