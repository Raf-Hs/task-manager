package com.taskmanager.service;

import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.dto.TaskStatus;

import java.util.List;

/**
 * Business contract of the application.
 *
 * <p>The controller depends only on this interface, which keeps the business
 * rules testable in isolation (the implementation is mocked with Mockito) and
 * makes it possible to replace the implementation later.
 */
public interface TaskService {

    /** Returns every task, optionally filtered by status. */
    List<TaskResponse> findAll(TaskStatus status);

    /** Returns a single task. @throws com.taskmanager.exception.TaskNotFoundException if it does not exist. */
    TaskResponse findById(Long id);

    /** Creates a new task. */
    TaskResponse create(TaskRequest request);

    /** Updates an existing task. @throws com.taskmanager.exception.TaskNotFoundException if it does not exist. */
    TaskResponse update(Long id, TaskRequest request);

    /**
     * Marks a task as completed or pending.
     *
     * @param completed explicit target status, or {@code null} to toggle the current one
     * @throws com.taskmanager.exception.TaskNotFoundException if the task does not exist
     */
    TaskResponse changeCompletedStatus(Long id, Boolean completed);

    /** Deletes a task. @throws com.taskmanager.exception.TaskNotFoundException if it does not exist. */
    void delete(Long id);
}
