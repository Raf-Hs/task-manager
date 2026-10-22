package com.taskmanager.dto;

/**
 * Criteria available to sort the task list.
 *
 * <p>Each option knows how to translate itself into a Spring Data
 * {@code Sort} property, which keeps the mapping in one place instead of
 * spreading it across the controller and the service.
 */
public enum TaskSortOption {

    /** Newest tasks first. This is the default. */
    CREATED_AT("createdAt"),

    /** Most recently modified first. */
    UPDATED_AT("updatedAt"),

    /** Tasks with the closest deadline first. */
    DUE_DATE("dueDate"),

    /** Alphabetical order by title. */
    TITLE("title"),

    /** Most urgent tasks first. */
    PRIORITY("priority");

    private final String property;

    TaskSortOption(String property) {
        this.property = property;
    }

    /** Name of the entity property used to sort. */
    public String getProperty() {
        return property;
    }

    /**
     * Resolves the raw query parameter to an enum value.
     *
     * @param value raw parameter, may be {@code null} or blank
     * @return the matching option, or {@link #CREATED_AT} when the value is empty
     * @throws IllegalArgumentException when the value does not match any option
     */
    public static TaskSortOption from(String value) {
        if (value == null || value.isBlank()) {
            return CREATED_AT;
        }
        try {
            return TaskSortOption.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Unknown sort option '" + value
                            + "'. Allowed values: CREATED_AT, UPDATED_AT, DUE_DATE, TITLE, PRIORITY.");
        }
    }
}
