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
 * makes the filters cumulative: "pending" + "high priority" + "milk".
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
        specification = specification.and(matchesText(filter.getSearch()));
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

    /**
     * Case-insensitive search over the title and the description.
     *
     * <p>The pattern is wrapped in wildcards, and the special characters of
     * SQL LIKE are escaped so a search such as {@code %} does not match
     * everything.
     */
    public static Specification<Task> matchesText(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + escapeLike(search.trim().toLowerCase()) + "%";
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.like(builder.lower(root.get("title")), pattern, '\\'));
            predicates.add(builder.like(builder.lower(root.get("description")), pattern, '\\'));
            return builder.or(predicates.toArray(new Predicate[0]));
        };
    }

    /** Escapes the characters that have a special meaning in a LIKE pattern. */
    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
