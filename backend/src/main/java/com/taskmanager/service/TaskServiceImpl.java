package com.taskmanager.service;

import com.taskmanager.dto.TaskFilter;
import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.dto.TaskSortOption;
import com.taskmanager.dto.TaskStatistics;
import com.taskmanager.entity.Task;
import com.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.mapper.TaskMapper;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.TaskSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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
    public Page<TaskResponse> findAll(TaskFilter filter, TaskSortOption sort, Pageable pageable) {
        // The filters and the ordering are two halves of the same query, so
        // they are composed into a single specification.
        Specification<Task> specification = TaskSpecifications.from(filter)
                .and(TaskSpecifications.orderedBy(sort));

        // The pageable is deliberately left unsorted: a sort on the pageable
        // would replace the ordering built by the specification.
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        Page<Task> page = taskRepository.findAll(specification, pageRequest);
        return page.map(taskMapper::toResponse);
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
