package com.taskmanager.controller;

import com.taskmanager.dto.PagedResponse;
import com.taskmanager.dto.TaskFilter;
import com.taskmanager.dto.TaskPriority;
import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.dto.TaskSortOption;
import com.taskmanager.dto.TaskStatistics;
import com.taskmanager.dto.TaskStatus;
import com.taskmanager.dto.UpdateCompletedRequest;
import com.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * REST endpoints to manage tasks.
 *
 * <p>The controller is intentionally thin: it validates the input, delegates to
 * {@link TaskService} and returns the result. There is no business logic here.
 */
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "CRUD operations over the personal tasks")
public class TaskController {

    /** Maximum number of elements a single page can hold. */
    private static final int MAX_PAGE_SIZE = 100;

    /** Number of elements per page when the client does not specify it. */
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(
            summary = "List tasks",
            description = "Returns a page of tasks. The result can be filtered by status and priority, "
                    + "searched by text and sorted by the given criterion.")
    @GetMapping
    public ResponseEntity<PagedResponse<TaskResponse>> list(
            @Parameter(description = "ALL (default), PENDING or COMPLETED")
            @RequestParam(name = "status", defaultValue = "ALL", required = false) String status,
            @Parameter(description = "Filter by priority")
            @RequestParam(name = "priority", required = false) String priority,
            @Parameter(description = "Case-insensitive text search over the title and the description")
            @RequestParam(name = "search", required = false) String search,
            @Parameter(description = "CREATED_AT (default), UPDATED_AT, DUE_DATE, TITLE or PRIORITY")
            @RequestParam(name = "sort", defaultValue = "CREATED_AT", required = false) String sort,
            @Parameter(description = "Zero-based page number")
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @Parameter(description = "Elements per page (1-100)")
            @RequestParam(name = "size", defaultValue = "20", required = false) int size) {

        TaskFilter filter = new TaskFilter();
        filter.setStatus(TaskStatus.from(status));
        filter.setPriority(parsePriority(priority));
        filter.setSearch(search);

        TaskSortOption sortOption = TaskSortOption.from(sort);
        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                clampSize(size));

        return ResponseEntity.ok(PagedResponse.from(taskService.findAll(filter, sortOption, pageRequest)));
    }

    @Operation(summary = "Get task statistics", description = "Aggregated figures of the whole task list")
    @GetMapping("/statistics")
    public ResponseEntity<TaskStatistics> statistics() {
        return ResponseEntity.ok(taskService.getStatistics());
    }

    @Operation(summary = "Get a task by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The task was found"),
            @ApiResponse(responseCode = "404", description = "The task does not exist")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.findById(id));
    }

    @Operation(summary = "Create a task")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "The task was created"),
            @ApiResponse(responseCode = "400", description = "The payload is invalid")
    })
    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        TaskResponse created = taskService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Update a task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The task was updated"),
            @ApiResponse(responseCode = "400", description = "The payload is invalid"),
            @ApiResponse(responseCode = "404", description = "The task does not exist")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.update(id, request));
    }

    @Operation(
            summary = "Complete or uncomplete a task",
            description = "Toggles the status when no body is sent, otherwise sets it explicitly")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The status was updated"),
            @ApiResponse(responseCode = "404", description = "The task does not exist")
    })
    @PatchMapping("/{id}/complete")
    public ResponseEntity<TaskResponse> changeCompletedStatus(
            @PathVariable Long id,
            @RequestBody(required = false) UpdateCompletedRequest body) {
        Boolean completed = body == null ? null : body.isCompleted();
        return ResponseEntity.ok(taskService.changeCompletedStatus(id, completed));
    }

    @Operation(summary = "Delete a task")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The task was deleted"),
            @ApiResponse(responseCode = "404", description = "The task does not exist")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Parses the priority filter. An empty value means "no filter".
     */
    private TaskPriority parsePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return null;
        }
        return TaskPriority.from(priority);
    }

    /** Keeps the page size inside the allowed range. */
    private int clampSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
