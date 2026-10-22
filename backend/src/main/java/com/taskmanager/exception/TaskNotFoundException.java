package com.taskmanager.exception;

/**
 * Thrown when a task is requested by an id that does not exist.
 *
 * <p>The global handler translates it into an HTTP 404 response.
 */
public class TaskNotFoundException extends RuntimeException {

    private final Long taskId;

    public TaskNotFoundException(Long taskId) {
        super("Task with id " + taskId + " was not found");
        this.taskId = taskId;
    }

    public Long getTaskId() {
        return taskId;
    }
}
