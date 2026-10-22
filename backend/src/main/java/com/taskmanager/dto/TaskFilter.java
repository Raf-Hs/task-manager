package com.taskmanager.dto;

/**
 * Request used to filter the task list.
 *
 * <p>Every field is optional. The service combines them into a single query
 * that the database resolves (see ADR-0010).
 */
public class TaskFilter {

    private TaskStatus status;
    private TaskPriority priority;

    public TaskStatus getStatus() {
        return status == null ? TaskStatus.ALL : status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }
}
