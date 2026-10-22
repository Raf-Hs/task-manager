package com.taskmanager.service;

import com.taskmanager.dto.TaskFilter;
import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.dto.TaskSortOption;
import com.taskmanager.dto.TaskStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Business contract of the application.
 *
 * <p>The controller depends only on this interface, which keeps the business
 * rules testable in isolation (the implementation is mocked with Mockito) and
 * makes it possible to replace the implementation later.
 */
public interface TaskService {

    /**
     * Returns a page of tasks matching the filter.
     *
     * @param filter   status, priority and text search (all optional)
     * @param sort     criterion used to order the results
     * @param pageable pagination information
     * @return the requested page of tasks
     */
    Page<TaskResponse> findAll(TaskFilter filter, TaskSortOption sort, Pageable pageable);

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

    /** Returns the aggregated figures of the whole task list. */
    TaskStatistics getStatistics();
}
