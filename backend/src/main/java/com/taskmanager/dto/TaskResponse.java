package com.taskmanager.dto;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Representation of a task returned by the API.
 *
 * <p>It is intentionally read-only: it mirrors exactly the fields described in
 * the functional requirements (id, title, description, dueDate, completed,
 * createdAt).
 */
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private boolean completed;
    private Instant createdAt;

    public TaskResponse() {
    }

    public TaskResponse(Long id, String title, String description, LocalDate dueDate,
                        boolean completed, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.completed = completed;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
