package com.taskmanager.controller;

import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.dto.TaskStatus;
import com.taskmanager.dto.UpdateCompletedRequest;
import com.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import java.util.List;

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

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "List tasks", description = "Returns every task, optionally filtered by status")
    @GetMapping
    public ResponseEntity<List<TaskResponse>> list(
            @Parameter(description = "ALL (default), PENDING or COMPLETED")
            @RequestParam(name = "status", defaultValue = "ALL", required = false) String status) {
        TaskStatus taskStatus = TaskStatus.from(status);
        return ResponseEntity.ok(taskService.findAll(taskStatus));
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
}
