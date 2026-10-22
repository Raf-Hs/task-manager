package com.taskmanager.repository;

import com.taskmanager.dto.TaskFilter;
import com.taskmanager.dto.TaskPriority;
import com.taskmanager.dto.TaskStatus;
import com.taskmanager.entity.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests of the persistence layer against an in-memory database.
 *
 * <p>They confirm that the entity mapping, the derived queries and the
 * specifications work as expected.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TaskRepository - persistence")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Task givenTask(String title, TaskPriority priority, LocalDate dueDate, boolean completed) {
        return taskRepository.save(new Task(title, null, dueDate, priority, completed));
    }

    @Test
    @DisplayName("A saved task receives an id and a creation date")
    void saveAssignsIdAndCreatedAt() {
        Task task = taskRepository.save(new Task("Buy milk", "Semi skimmed", LocalDate.of(2030, 1, 15),
                TaskPriority.MEDIUM, false));

        entityManager.flush();
        entityManager.clear();

        Task found = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(found.getId()).isNotNull();
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
        assertThat(found.getTitle()).isEqualTo("Buy milk");
        assertThat(found.isCompleted()).isFalse();
        assertThat(found.getPriority()).isEqualTo(TaskPriority.MEDIUM);
    }

    @Test
    @DisplayName("The status filter only returns the matching rows")
    void filterByCompleted() {
        givenTask("Done", TaskPriority.MEDIUM, null, true);
        givenTask("Pending", TaskPriority.MEDIUM, null, false);
        entityManager.flush();

        TaskFilter completed = new TaskFilter();
        completed.setStatus(TaskStatus.COMPLETED);
        List<Task> completedResult = taskRepository.findAll(TaskSpecifications.from(completed));

        TaskFilter pending = new TaskFilter();
        pending.setStatus(TaskStatus.PENDING);
        List<Task> pendingResult = taskRepository.findAll(TaskSpecifications.from(pending));

        assertThat(completedResult).hasSize(1);
        assertThat(completedResult.get(0).getTitle()).isEqualTo("Done");
        assertThat(pendingResult).hasSize(1);
        assertThat(pendingResult.get(0).getTitle()).isEqualTo("Pending");
    }

    @Test
    @DisplayName("The priority filter only returns the matching rows")
    void filterByPriority() {
        givenTask("Urgent", TaskPriority.HIGH, null, false);
        givenTask("Whenever", TaskPriority.LOW, null, false);
        entityManager.flush();

        TaskFilter filter = new TaskFilter();
        filter.setPriority(TaskPriority.HIGH);

        List<Task> result = taskRepository.findAll(TaskSpecifications.from(filter));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Urgent");
    }

    @Test
    @DisplayName("The filters are combined with AND")
    void filtersAreCombined() {
        givenTask("Urgent pending", TaskPriority.HIGH, null, false);
        givenTask("Urgent done", TaskPriority.HIGH, null, true);
        givenTask("Low pending", TaskPriority.LOW, null, false);
        entityManager.flush();

        TaskFilter filter = new TaskFilter();
        filter.setStatus(TaskStatus.PENDING);
        filter.setPriority(TaskPriority.HIGH);

        List<Task> result = taskRepository.findAll(TaskSpecifications.from(filter));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Urgent pending");
    }

    @Test
    @DisplayName("The counters used by the statistics are correct")
    void countersAreCorrect() {
        givenTask("Done", TaskPriority.MEDIUM, null, true);
        givenTask("Pending", TaskPriority.MEDIUM, LocalDate.now().plusDays(1), false);
        givenTask("Overdue", TaskPriority.MEDIUM, LocalDate.now().minusDays(1), false);
        entityManager.flush();

        assertThat(taskRepository.count()).isEqualTo(3);
        assertThat(taskRepository.countByCompletedTrue()).isEqualTo(1);
        assertThat(taskRepository.countByCompletedFalseAndDueDateBefore(LocalDate.now())).isEqualTo(1);
    }

    @Test
    @DisplayName("delete removes the task from the database")
    void deleteRemovesTheTask() {
        Task task = taskRepository.save(new Task("Temporary", null, null, TaskPriority.MEDIUM, false));
        entityManager.flush();

        taskRepository.delete(task);
        entityManager.flush();
        entityManager.clear();

        assertThat(taskRepository.findById(task.getId())).isEmpty();
    }
}
