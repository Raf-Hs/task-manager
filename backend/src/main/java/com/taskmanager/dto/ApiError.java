package com.taskmanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error body returned by the {@code @RestControllerAdvice}.
 *
 * <p>Every error has the same shape so the frontend can handle them uniformly:
 * HTTP status, a human readable message, the requested path and - when the
 * failure comes from Bean Validation - a map with the offending fields.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApiError {

    private int status;
    private String message;
    private String path;
    private Instant timestamp;
    private Map<String, String> fieldErrors;

    public ApiError() {
    }

    public ApiError(int status, String message, String path) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.timestamp = Instant.now();
    }

    public ApiError(int status, String message, String path, Map<String, String> fieldErrors) {
        this(status, message, path);
        this.fieldErrors = fieldErrors;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}
