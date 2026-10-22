package com.taskmanager.dto;

/**
 * Priority of a task.
 *
 * <p>It is stored as a string (not as a number) so that the database stays
 * readable and the value is stable even if the order changes in the future.
 */
public enum TaskPriority {

    /** Can wait. */
    LOW,

    /** The default value. */
    MEDIUM,

    /** Needs attention now. */
    HIGH;

    /**
     * Resolves the raw query parameter or payload value to an enum value.
     *
     * @param value raw value, may be {@code null} or blank
     * @return the matching priority, or {@link #MEDIUM} when the value is empty
     * @throws IllegalArgumentException when the value does not match any priority
     */
    public static TaskPriority from(String value) {
        if (value == null || value.isBlank()) {
            return MEDIUM;
        }
        try {
            return TaskPriority.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Unknown priority '" + value + "'. Allowed values: LOW, MEDIUM, HIGH.");
        }
    }
}
