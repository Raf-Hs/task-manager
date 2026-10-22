package com.taskmanager.dto;

/**
 * Allowed values for the {@code status} query parameter used to filter tasks.
 *
 * <ul>
 *   <li>{@link #ALL} - every task (default).</li>
 *   <li>{@link #PENDING} - tasks that are not completed yet.</li>
 *   <li>{@link #COMPLETED} - tasks already completed.</li>
 * </ul>
 */
public enum TaskStatus {

    ALL,
    PENDING,
    COMPLETED;

    /**
     * Resolves the raw query parameter to an enum value.
     *
     * @param value raw parameter, may be {@code null} or blank
     * @return the matching status, or {@link #ALL} when the value is empty
     * @throws IllegalArgumentException when the value does not match any status
     */
    public static TaskStatus from(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }
        try {
            return TaskStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Unknown status '" + value + "'. Allowed values: ALL, PENDING, COMPLETED.");
        }
    }
}
