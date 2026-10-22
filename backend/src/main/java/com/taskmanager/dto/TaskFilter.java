package com.taskmanager.dto;

/**
 * Request used to filter, search and sort the task list.
 *
 * <p>Every field is optional. The service combines them into a single query
 * that the database resolves (see ADR-0010).
 */
public class TaskFilter {

    private TaskStatus status;
    private TaskPriority priority;
    private String search;

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

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
