package com.taskmanager.dto;

/**
 * Aggregated figures about the task list.
 *
 * <p>The counts are computed with a single grouped query instead of four
 * separate ones, so the cost does not grow with the number of figures.
 */
public class TaskStatistics {

    private long total;
    private long pending;
    private long completed;
    private long overdue;
    private int completionPercentage;

    public TaskStatistics() {
    }

    public TaskStatistics(long total, long pending, long completed, long overdue, int completionPercentage) {
        this.total = total;
        this.pending = pending;
        this.completed = completed;
        this.overdue = overdue;
        this.completionPercentage = completionPercentage;
    }

    /** Total number of tasks. */
    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    /** Tasks that are not completed yet. */
    public long getPending() {
        return pending;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }

    /** Tasks already completed. */
    public long getCompleted() {
        return completed;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }

    /** Pending tasks whose deadline is already past. */
    public long getOverdue() {
        return overdue;
    }

    public void setOverdue(long overdue) {
        this.overdue = overdue;
    }

    /** Percentage of completed tasks, rounded to the nearest integer. */
    public int getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(int completionPercentage) {
        this.completionPercentage = completionPercentage;
    }
}
