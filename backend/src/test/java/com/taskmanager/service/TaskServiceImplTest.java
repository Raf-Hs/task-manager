package com.taskmanager.service;

import com.taskmanager.dto.TaskFilter;
import com.taskmanager.dto.TaskPriority;
import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.dto.TaskStatus;
import com.taskmanager.dto.TaskStatistics;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

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
        existingTask = new Task("Buy milk", "Semi skimmed", LocalDate.of(2030, 1, 15),
                TaskPriority.MEDIUM, false);
        existingTask.setId(1L);
        existingTask.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));
        existingTask.setUpdatedAt(Instant.parse("2026-01-01T10:00:00Z"));
    }

    // ------------------------------------------------------------------
    // Listing
    // ------------------------------------------------------------------

    @Test
    @DisplayName("findAll returns every task when no filter is applied")
    void findAllReturnsEveryTask() {
        when(taskRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(existingTask));

        List<TaskResponse> result = taskService.findAll(new TaskFilter());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("Buy milk");
        assertThat(result.get(0).getDueDate()).isEqualTo(LocalDate.of(2030, 1, 15));
        assertThat(result.get(0).getPriority()).isEqualTo(TaskPriority.MEDIUM);
        verify(taskRepository, times(1)).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    @DisplayName("findAll returns an empty list when there are no tasks")
    void findAllReturnsEmptyList() {
        when(taskRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of());

        assertThat(taskService.findAll(new TaskFilter())).isEmpty();
    }

    @Test
    @DisplayName("findAll keeps the pending tasks before the completed ones")
    void findAllKeepsPendingFirst() {
        when(taskRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(existingTask));

        taskService.findAll(new TaskFilter());

        ArgumentCaptor<Sort> captor = ArgumentCaptor.forClass(Sort.class);
        verify(taskRepository).findAll(any(Specification.class), captor.capture());

        Sort.Order byCompleted = captor.getValue().getOrderFor("completed");
        assertThat(byCompleted).isNotNull();
        assertThat(byCompleted.isAscending()).isTrue();
    }

    @Test
    @DisplayName("findAll applies the status filter")
    void findAllAppliesStatusFilter() {
        when(taskRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of());

        TaskFilter filter = new TaskFilter();
        filter.setStatus(TaskStatus.PENDING);

        taskService.findAll(filter);

        verify(taskRepository).findAll(any(Specification.class), any(Sort.class));
    }

    // ------------------------------------------------------------------
    // Reading a single task
    // ------------------------------------------------------------------

    @Test
    @DisplayName("findById returns the requested task")
    void findByIdReturnsTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));

        TaskResponse result = taskService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.isCompleted()).isFalse();
        assertThat(result.getPriority()).isEqualTo(TaskPriority.MEDIUM);
    }

    @Test
    @DisplayName("findById fails when the task does not exist")
    void findByIdFailsWhenMissing() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ------------------------------------------------------------------
    // Creating
    // ------------------------------------------------------------------

    @Test
    @DisplayName("create persists the task and returns it")
    void createPersistsTask() {
        TaskRequest request = new TaskRequest("Write the report", "Final version",
                LocalDate.of(2030, 2, 1), TaskPriority.HIGH, false);
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
        assertThat(captor.getValue().getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("create stores an empty description as null")
    void createNormalizesBlankDescription() {
        TaskRequest request = new TaskRequest("Only a title", "   ", null, null, false);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskService.create(request);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getDescription()).isNull();
    }

    @Test
    @DisplayName("create applies the medium priority when none is given")
    void createAppliesDefaultPriority() {
        TaskRequest request = new TaskRequest("No priority", null, null, null, false);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse result = taskService.create(request);

        assertThat(result.getPriority()).isEqualTo(TaskPriority.MEDIUM);
    }

    // ------------------------------------------------------------------
    // Updating
    // ------------------------------------------------------------------

    @Test
    @DisplayName("update modifies the existing task")
    void updateModifiesTask() {
        TaskRequest request = new TaskRequest("Updated title", "Updated description", null,
                TaskPriority.LOW, true);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse result = taskService.update(1L, request);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Updated title");
        assertThat(captor.getValue().getDescription()).isEqualTo("Updated description");
        assertThat(captor.getValue().isCompleted()).isTrue();
        assertThat(captor.getValue().getPriority()).isEqualTo(TaskPriority.LOW);
        assertThat(result.getTitle()).isEqualTo("Updated title");
    }

    @Test
    @DisplayName("update fails when the task does not exist")
    void updateFailsWhenMissing() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());
        TaskRequest request = new TaskRequest("Title", null, null, null, false);

        assertThatThrownBy(() -> taskService.update(99L, request))
                .isInstanceOf(TaskNotFoundException.class);
        verify(taskRepository, never()).save(any(Task.class));
    }

    // ------------------------------------------------------------------
    // Changing the completion status
    // ------------------------------------------------------------------

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

    // ------------------------------------------------------------------
    // Deleting
    // ------------------------------------------------------------------

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

    // ------------------------------------------------------------------
    // Statistics
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getStatistics aggregates the figures of the list")
    void getStatisticsAggregatesFigures() {
        when(taskRepository.count()).thenReturn(10L);
        when(taskRepository.countByCompletedTrue()).thenReturn(4L);
        when(taskRepository.countByCompletedFalseAndDueDateBefore(any(LocalDate.class))).thenReturn(2L);

        TaskStatistics statistics = taskService.getStatistics();

        assertThat(statistics.getTotal()).isEqualTo(10);
        assertThat(statistics.getCompleted()).isEqualTo(4);
        assertThat(statistics.getPending()).isEqualTo(6);
        assertThat(statistics.getOverdue()).isEqualTo(2);
        assertThat(statistics.getCompletionPercentage()).isEqualTo(40);
    }

    @Test
    @DisplayName("getStatistics returns zeroed figures when the list is empty")
    void getStatisticsHandlesEmptyList() {
        when(taskRepository.count()).thenReturn(0L);
        when(taskRepository.countByCompletedTrue()).thenReturn(0L);
        when(taskRepository.countByCompletedFalseAndDueDateBefore(any(LocalDate.class))).thenReturn(0L);

        TaskStatistics statistics = taskService.getStatistics();

        assertThat(statistics.getTotal()).isZero();
        assertThat(statistics.getPending()).isZero();
        assertThat(statistics.getCompleted()).isZero();
        assertThat(statistics.getCompletionPercentage()).isZero();
    }

    // ------------------------------------------------------------------
    // Filter helpers
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TaskFilter defaults to the ALL status")
    void taskFilterDefaultsToAll() {
        TaskFilter filter = new TaskFilter();

        assertThat(filter.getStatus()).isEqualTo(TaskStatus.ALL);
        assertThat(filter.getPriority()).isNull();
    }
}
