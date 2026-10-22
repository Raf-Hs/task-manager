package com.taskmanager.service;

import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.dto.TaskStatus;
import com.taskmanager.entity.Task;
import com.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.mapper.TaskMapper;
import com.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<TaskResponse> findAll(TaskStatus status) {
        TaskStatus effectiveStatus = status == null ? TaskStatus.ALL : status;
        List<Task> tasks = switch (effectiveStatus) {
            case PENDING -> taskRepository.findByCompletedFalseOrderByCreatedAtDesc();
            case COMPLETED -> taskRepository.findByCompletedTrueOrderByCreatedAtDesc();
            case ALL -> taskRepository.findAllByOrderByCompletedAscCreatedAtDesc();
        };
        return tasks.stream().map(taskMapper::toResponse).toList();
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

    /** Loads a task or fails with a {@link TaskNotFoundException}. */
    private Task findTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }
}
