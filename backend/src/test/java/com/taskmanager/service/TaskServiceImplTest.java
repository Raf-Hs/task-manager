package com.taskmanager.service;

import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.dto.TaskStatus;
import com.taskmanager.entity.Task;
import com.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.mapper.TaskMapper;
import com.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests of {@link TaskServiceImpl}.
 *
 * <p>The repository is mocked with Mockito, so these tests run in milliseconds
 * and do not need a database. They verify the business rules in isolation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService - business rules")
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    // The mapper is a pure stateless function, so the real implementation is
    // used here: mocking it would only duplicate its behaviour in the test.
    private final TaskMapper taskMapper = new TaskMapper();

    private TaskServiceImpl taskService;

    private Task existingTask;

    @BeforeEach
    void setUp() {
        taskService = new TaskServiceImpl(taskRepository, taskMapper);
        existingTask = new Task("Buy milk", "Semi skimmed", LocalDate.of(2030, 1, 15), false);
        existingTask.setId(1L);
        existingTask.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));
    }

    @Test
    @DisplayName("findAll returns every task when no filter is applied")
    void findAllReturnsEveryTask() {
        when(taskRepository.findAllByOrderByCompletedAscCreatedAtDesc()).thenReturn(List.of(existingTask));

        List<TaskResponse> result = taskService.findAll(TaskStatus.ALL);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("Buy milk");
        assertThat(result.get(0).getDueDate()).isEqualTo(LocalDate.of(2030, 1, 15));
        verify(taskRepository, times(1)).findAllByOrderByCompletedAscCreatedAtDesc();
    }

    @Test
    @DisplayName("findAll filters the pending tasks")
    void findAllFiltersPendingTasks() {
        when(taskRepository.findByCompletedFalseOrderByCreatedAtDesc()).thenReturn(List.of(existingTask));

        List<TaskResponse> result = taskService.findAll(TaskStatus.PENDING);

        assertThat(result).hasSize(1);
        verify(taskRepository, times(1)).findByCompletedFalseOrderByCreatedAtDesc();
        verify(taskRepository, never()).findAllByOrderByCompletedAscCreatedAtDesc();
    }

    @Test
    @DisplayName("findAll filters the completed tasks")
    void findAllFiltersCompletedTasks() {
        Task completed = new Task("Already done", null, null, true);
        completed.setId(2L);
        when(taskRepository.findByCompletedTrueOrderByCreatedAtDesc()).thenReturn(List.of(completed));

        List<TaskResponse> result = taskService.findAll(TaskStatus.COMPLETED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isCompleted()).isTrue();
        verify(taskRepository, times(1)).findByCompletedTrueOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("findAll falls back to every task when no status is given")
    void findAllWithoutStatus() {
        when(taskRepository.findAllByOrderByCompletedAscCreatedAtDesc()).thenReturn(List.of(existingTask));

        List<TaskResponse> result = taskService.findAll(null);

        assertThat(result).hasSize(1);
        verify(taskRepository, times(1)).findAllByOrderByCompletedAscCreatedAtDesc();
    }

    @Test
    @DisplayName("findById returns the requested task")
    void findByIdReturnsTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));

        TaskResponse result = taskService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("findById fails when the task does not exist")
    void findByIdFailsWhenMissing() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("create persists the task and returns it")
    void createPersistsTask() {
        TaskRequest request = new TaskRequest("Write the report", "Final version",
                LocalDate.of(2030, 2, 1), false);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            saved.setId(10L);
            saved.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));
            return saved;
        });

        TaskResponse result = taskService.create(request);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Write the report");
        assertThat(captor.getValue().getDescription()).isEqualTo("Final version");
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("create stores an empty description as null")
    void createNormalizesBlankDescription() {
        TaskRequest request = new TaskRequest("Only a title", "   ", null, false);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskService.create(request);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getDescription()).isNull();
    }

    @Test
    @DisplayName("update modifies the existing task")
    void updateModifiesTask() {
        TaskRequest request = new TaskRequest("Updated title", "Updated description", null, true);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse result = taskService.update(1L, request);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Updated title");
        assertThat(captor.getValue().getDescription()).isEqualTo("Updated description");
        assertThat(captor.getValue().isCompleted()).isTrue();
        assertThat(result.getTitle()).isEqualTo("Updated title");
    }

    @Test
    @DisplayName("update fails when the task does not exist")
    void updateFailsWhenMissing() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());
        TaskRequest request = new TaskRequest("Title", null, null, false);

        assertThatThrownBy(() -> taskService.update(99L, request))
                .isInstanceOf(TaskNotFoundException.class);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("changeCompletedStatus toggles the status when no target is given")
    void changeCompletedStatusToggles() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskService.changeCompletedStatus(1L, null);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().isCompleted()).isTrue();
    }

    @Test
    @DisplayName("changeCompletedStatus sets the explicit target status")
    void changeCompletedStatusSetsExplicitValue() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskService.changeCompletedStatus(1L, false);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().isCompleted()).isFalse();
    }

    @Test
    @DisplayName("changeCompletedStatus fails when the task does not exist")
    void changeCompletedStatusFailsWhenMissing() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.changeCompletedStatus(99L, null))
                .isInstanceOf(TaskNotFoundException.class);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("delete removes the task")
    void deleteRemovesTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));

        taskService.delete(1L);

        verify(taskRepository, times(1)).delete(existingTask);
    }

    @Test
    @DisplayName("delete fails when the task does not exist")
    void deleteFailsWhenMissing() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delete(99L))
                .isInstanceOf(TaskNotFoundException.class);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    @DisplayName("delete never touches an id that was not found")
    void deleteDoesNotDeleteUnknownId() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delete(42L)).isInstanceOf(TaskNotFoundException.class);
        verify(taskRepository, never()).delete(any(Task.class));
    }
}
