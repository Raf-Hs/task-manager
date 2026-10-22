package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * Data access layer for {@link Task}.
 *
 * <p>Spring Data generates the implementation at runtime, so no manual SQL or
 * {@code EntityManager} usage is needed.
 *
 * <p>It extends {@link JpaSpecificationExecutor} so the service can compose
 * dynamic criteria (search text, filter by status, filter by priority) into a
 * single query that the database resolves.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    /** Counts the tasks that are already completed. */
    long countByCompletedTrue();

    /** Counts the pending tasks whose deadline has already passed. */
    long countByCompletedFalseAndDueDateBefore(LocalDate date);
}
