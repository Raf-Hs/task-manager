package com.taskmanager.repository;

import com.taskmanager.dto.TaskFilter;
import com.taskmanager.dto.TaskPriority;
import com.taskmanager.dto.TaskStatus;
import com.taskmanager.entity.Task;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Composable criteria used to build the task query.
 *
 * <p>Every criterion is optional and they are combined with {@code AND}, which
 * makes the filters cumulative: "pending" + "high priority".
 *
 * <p>See ADR-0010 for why the filtering happens in the database.
 */
public final class TaskSpecifications {

    private TaskSpecifications() {
        // Utility class.
    }

    /** Builds the full specification from a filter. */
    public static Specification<Task> from(TaskFilter filter) {
        Specification<Task> specification = Specification.where(null);

        if (filter == null) {
            return specification;
        }

        specification = specification.and(statusIs(filter.getStatus()));
        specification = specification.and(priorityIs(filter.getPriority()));
        return specification;
    }

    /** Filters by completion status. */
    public static Specification<Task> statusIs(TaskStatus status) {
        if (status == null || status == TaskStatus.ALL) {
            return null;
        }
        boolean completed = status == TaskStatus.COMPLETED;
        return (root, query, builder) -> builder.equal(root.get("completed"), completed);
    }

    /** Filters by priority. */
    public static Specification<Task> priorityIs(TaskPriority priority) {
        if (priority == null) {
            return null;
        }
        return (root, query, builder) -> builder.equal(root.get("priority"), priority);
    }
}
