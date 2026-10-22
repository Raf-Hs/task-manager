package com.taskmanager.mapper;

import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.entity.Task;
import org.springframework.stereotype.Component;

/**
 * Converts between the entity and the DTOs.
 *
 * <p>Keeping the conversion in a single place avoids repeating the mapping in
 * the service and makes it obvious where the API contract is built.
 */
@Component
public class TaskMapper {

    /** Builds an API response out of a persisted entity. */
    public TaskResponse toResponse(Task task) {
        if (task == null) {
            return null;
        }
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.isCompleted(),
                task.getCreatedAt());
    }

    /** Builds a brand new entity out of an incoming request. */
    public Task toEntity(TaskRequest request) {
        if (request == null) {
            return null;
        }
        return new Task(
                request.getTitle(),
                normalize(request.getDescription()),
                request.getDueDate(),
                request.isCompleted());
    }

    /** Copies the request fields into an existing entity (used by the update). */
    public void updateEntity(Task task, TaskRequest request) {
        task.setTitle(request.getTitle());
        task.setDescription(normalize(request.getDescription()));
        task.setDueDate(request.getDueDate());
        task.setCompleted(request.isCompleted());
    }

    /**
     * Turns blank optional text into {@code null} so the database stores
     * {@code NULL} instead of an empty string.
     */
    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
