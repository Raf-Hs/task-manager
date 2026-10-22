package com.taskmanager.service;

import com.taskmanager.dto.TaskFilter;
import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.dto.TaskStatistics;
import com.taskmanager.entity.Task;
import com.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.mapper.TaskMapper;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.TaskSpecifications;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of the business rules.
 *
 * <p>The class is transactional: every public method runs inside a database
 * transaction, so a failed operation never leaves the storage in a half-updated
 * state.
 */
@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    /**
     * Default ordering: pending tasks first and, inside each group, the most
     * recent ones first.
     */
    private static final Sort DEFAULT_SORT = Sort.by(
            Sort.Order.asc("completed"),
            Sort.Order.desc("createdAt"),
            Sort.Order.desc("id"));

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    /**
     * Dependencies are injected through the constructor (constructor injection),
     * which makes the class easy to unit test and impossible to build with
     * missing collaborators.
     */
    public TaskServiceImpl(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> findAll(TaskFilter filter) {
        Specification<Task> specification = TaskSpecifications.from(filter);
        return taskRepository.findAll(specification, DEFAULT_SORT).stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse findById(Long id) {
        return taskMapper.toResponse(findTaskOrThrow(id));
    }

    @Override
    public TaskResponse create(TaskRequest request) {
        Task task = taskMapper.toEntity(request);
        Task saved = taskRepository.save(task);
        return taskMapper.toResponse(saved);
    }

    @Override
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = findTaskOrThrow(id);
        taskMapper.updateEntity(task, request);
        Task saved = taskRepository.save(task);
        return taskMapper.toResponse(saved);
    }

    @Override
    public TaskResponse changeCompletedStatus(Long id, Boolean completed) {
        Task task = findTaskOrThrow(id);
        boolean target = completed == null ? !task.isCompleted() : completed;
        task.setCompleted(target);
        Task saved = taskRepository.save(task);
        return taskMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        Task task = findTaskOrThrow(id);
        taskRepository.delete(task);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskStatistics getStatistics() {
        long total = taskRepository.count();
        long completed = taskRepository.countByCompletedTrue();
        long pending = total - completed;
        long overdue = taskRepository.countByCompletedFalseAndDueDateBefore(LocalDate.now());

        int completionPercentage = total == 0
                ? 0
                : (int) Math.round((completed * 100.0) / total);

        return new TaskStatistics(total, pending, completed, overdue, completionPercentage);
    }

    /** Loads a task or fails with a {@link TaskNotFoundException}. */
    private Task findTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }
}
