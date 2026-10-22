package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access layer for {@link Task}.
 *
 * <p>Spring Data generates the implementation at runtime, so no manual SQL or
 * {@code EntityManager} usage is needed.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /** All tasks, pending ones first, newest first inside each group. */
    List<Task> findAllByOrderByCompletedAscCreatedAtDesc();

    /** Only the tasks that are already completed. */
    List<Task> findByCompletedTrueOrderByCreatedAtDesc();

    /** Only the tasks that are still pending. */
    List<Task> findByCompletedFalseOrderByCreatedAtDesc();
}
