package com.taskmanager.repository;

import com.taskmanager.dto.TaskFilter;
import com.taskmanager.dto.TaskPriority;
import com.taskmanager.dto.TaskSortOption;
import com.taskmanager.dto.TaskStatus;
import com.taskmanager.entity.Task;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Composable criteria used to build the task query.
 *
 * <p>Every criterion is optional and they are combined with {@code AND}, which
 * makes the filters cumulative: "pending" + "high priority" + "milk".
 *
 * <p>See ADR-0010 for why the filtering and the ordering happen in the
 * database.
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

    /**
     * Applies the order of the list.
     *
     * <p>The ordering is part of the specification instead of a Spring Data
     * {@code Sort} because two of the criteria cannot be expressed as a plain
     * column name: the most urgent priority and the "completed last" rule.
     *
     * <p>Every criterion ends with the same two rules:
     *
     * <ul>
     *   <li>The completed tasks go last, so they never hide the pending
     *       work.</li>
     *   <li>The identifier breaks the ties, so two identical requests always
     *       return the same order and the pagination never repeats a task.</li>
     * </ul>
     */
    public static Specification<Task> orderedBy(TaskSortOption sort) {
        TaskSortOption criterion = sort == null ? TaskSortOption.CREATED_AT : sort;
        return (root, query, builder) -> {
            query.orderBy(orders(root, builder, criterion));
            // The ordering is not a filter: it does not narrow the result.
            return builder.conjunction();
        };
    }

    /** Builds the complete {@code ORDER BY} of the query. */
    private static List<Order> orders(Root<Task> root, CriteriaBuilder builder, TaskSortOption criterion) {
        List<Order> orders = new ArrayList<>();

        // 1. Pending tasks first, whatever the chosen criterion is.
        orders.add(builder.asc(root.get("completed")));

        // 2. The criterion chosen by the user.
        if (criterion == TaskSortOption.PRIORITY) {
            // The priority is stored as text, so it is ordered by its rank.
            orders.add(builder.asc(priorityRank(root, builder)));
        } else {
            Expression<?> column = root.get(criterion.getProperty());
            orders.add(isAscending(criterion) ? builder.asc(column) : builder.desc(column));
        }

        // 3. Stable tie breaker.
        orders.add(builder.desc(root.get("id")));
        return orders;
    }

    /**
     * Titles are read alphabetically and deadlines as "the closest one
     * first", so both are ascending. The rest are descending: the newest or
     * the most recently modified first.
     */
    private static boolean isAscending(TaskSortOption criterion) {
        return criterion == TaskSortOption.TITLE || criterion == TaskSortOption.DUE_DATE;
    }

    /**
     * Turns the priority into its rank, so the tasks can be sorted from the
     * most urgent one to the least urgent one.
     *
     * <p>The priority is stored as text ({@code LOW}, {@code MEDIUM},
     * {@code HIGH}), so an alphabetical order would put {@code MEDIUM} before
     * {@code HIGH}, which is not what anybody expects from "sort by priority".
     */
    private static Expression<Integer> priorityRank(Root<Task> root, CriteriaBuilder builder) {
        return builder.<Integer>selectCase()
                .when(builder.equal(root.get("priority"), TaskPriority.HIGH), 0)
                .when(builder.equal(root.get("priority"), TaskPriority.MEDIUM), 1)
                .otherwise(2);
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
