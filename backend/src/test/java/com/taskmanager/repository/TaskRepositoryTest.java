package com.taskmanager.repository;

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
 * <p>They confirm that the entity mapping and the derived queries work as
 * expected.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TaskRepository - persistence")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("A saved task receives an id and a creation date")
    void saveAssignsIdAndCreatedAt() {
        Task task = taskRepository.save(new Task("Buy milk", "Semi skimmed", LocalDate.of(2030, 1, 15), false));

        entityManager.flush();
        entityManager.clear();

        Task found = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(found.getId()).isNotNull();
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getTitle()).isEqualTo("Buy milk");
        assertThat(found.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("findAllByOrderByCompletedAscCreatedAtDesc returns pending tasks first")
    void findAllSortsPendingFirst() {
        Task completed = taskRepository.save(new Task("Old done", null, null, true));
        Task pending = taskRepository.save(new Task("New pending", null, null, false));
        entityManager.flush();

        List<Task> result = taskRepository.findAllByOrderByCompletedAscCreatedAtDesc();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(pending.getId());
        assertThat(result.get(1).getId()).isEqualTo(completed.getId());
    }

    @Test
    @DisplayName("The pending and completed queries only return the matching rows")
    void filterByCompleted() {
        taskRepository.save(new Task("Done", null, null, true));
        taskRepository.save(new Task("Pending", null, null, false));
        entityManager.flush();

        assertThat(taskRepository.findByCompletedTrueOrderByCreatedAtDesc()).hasSize(1);
        assertThat(taskRepository.findByCompletedTrueOrderByCreatedAtDesc().get(0).getTitle()).isEqualTo("Done");
        assertThat(taskRepository.findByCompletedFalseOrderByCreatedAtDesc()).hasSize(1);
        assertThat(taskRepository.findByCompletedFalseOrderByCreatedAtDesc().get(0).getTitle()).isEqualTo("Pending");
    }

    @Test
    @DisplayName("delete removes the task from the database")
    void deleteRemovesTheTask() {
        Task task = taskRepository.save(new Task("Temporary", null, null, false));
        entityManager.flush();

        taskRepository.delete(task);
        entityManager.flush();
        entityManager.clear();

        assertThat(taskRepository.findById(task.getId())).isEmpty();
    }
}
