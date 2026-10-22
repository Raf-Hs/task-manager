package com.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Payload accepted by {@code POST /api/tasks} and {@code PUT /api/tasks/{id}}.
 *
 * <p>Validation lives in the DTO so the rules are declared exactly once and are
 * applied automatically by Spring MVC through {@code @Valid}.
 */
public class TaskRequest {

    @NotBlank(message = "The title is mandatory")
    @Size(max = 200, message = "The title cannot be longer than 200 characters")
    private String title;

    @Size(max = 2000, message = "The description cannot be longer than 2000 characters")
    private String description;

    /** Optional deadline. Past dates are allowed: a task can be overdue. */
    private LocalDate dueDate;

    /** Defaults to {@code false} when the client does not send the field. */
    private boolean completed;

    public TaskRequest() {
    }

    public TaskRequest(String title, String description, LocalDate dueDate, boolean completed) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.completed = completed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
