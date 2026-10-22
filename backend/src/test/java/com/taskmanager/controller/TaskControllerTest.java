package com.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.dto.TaskFilter;
import com.taskmanager.dto.TaskPriority;
import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.dto.TaskSortOption;
import com.taskmanager.dto.TaskStatistics;
import com.taskmanager.dto.UpdateCompletedRequest;
import com.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests of the REST layer.
 *
 * <p>The controller is loaded as in a real application but the service is
 * mocked, so the tests focus on HTTP behaviour: status codes, JSON contract
 * and validation.
 */
@WebMvcTest(TaskController.class)
@ActiveProfiles("test")
@DisplayName("TaskController - REST endpoints")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    private TaskResponse sampleResponse() {
        return new TaskResponse(1L, "Buy milk", "Semi skimmed",
                LocalDate.of(2030, 1, 15), TaskPriority.HIGH, false,
                Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-02T10:00:00Z"));
    }

    private Page<TaskResponse> samplePage() {
        return new PageImpl<>(List.of(sampleResponse()), PageRequest.of(0, 20), 1);
    }

    // ------------------------------------------------------------------
    // GET /api/tasks
    // ------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/tasks returns a page of tasks")
    void listTasks() throws Exception {
        when(taskService.findAll(any(TaskFilter.class), any(TaskSortOption.class), any(Pageable.class)))
                .thenReturn(samplePage());

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Buy milk"))
                .andExpect(jsonPath("$.content[0].completed").value(false))
                .andExpect(jsonPath("$.content[0].priority").value("HIGH"))
                .andExpect(jsonPath("$.content[0].updatedAt").value("2026-01-02T10:00:00Z"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @DisplayName("GET /api/tasks?status=PENDING filters the pending tasks")
    void listTasksFilteredByStatus() throws Exception {
        when(taskService.findAll(any(TaskFilter.class), any(TaskSortOption.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/api/tasks").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(0)));

        ArgumentCaptor<TaskFilter> captor = ArgumentCaptor.forClass(TaskFilter.class);
        verify(taskService).findAll(captor.capture(), any(TaskSortOption.class), any(Pageable.class));
        assertThat(captor.getValue().getStatus().name()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("GET /api/tasks?priority=HIGH filters by priority")
    void listTasksFilteredByPriority() throws Exception {
        when(taskService.findAll(any(TaskFilter.class), any(TaskSortOption.class), any(Pageable.class)))
                .thenReturn(samplePage());

        mockMvc.perform(get("/api/tasks").param("priority", "HIGH"))
                .andExpect(status().isOk());

        ArgumentCaptor<TaskFilter> captor = ArgumentCaptor.forClass(TaskFilter.class);
        verify(taskService).findAll(captor.capture(), any(TaskSortOption.class), any(Pageable.class));
        assertThat(captor.getValue().getPriority()).isEqualTo(TaskPriority.HIGH);
    }

    @Test
    @DisplayName("GET /api/tasks?search=milk searches by text")
    void listTasksFilteredBySearch() throws Exception {
        when(taskService.findAll(any(TaskFilter.class), any(TaskSortOption.class), any(Pageable.class)))
                .thenReturn(samplePage());

        mockMvc.perform(get("/api/tasks").param("search", "milk"))
                .andExpect(status().isOk());

        ArgumentCaptor<TaskFilter> captor = ArgumentCaptor.forClass(TaskFilter.class);
        verify(taskService).findAll(captor.capture(), any(TaskSortOption.class), any(Pageable.class));
        assertThat(captor.getValue().getSearch()).isEqualTo("milk");
    }

    @Test
    @DisplayName("GET /api/tasks?sort=TITLE sorts by title")
    void listTasksSorted() throws Exception {
        when(taskService.findAll(any(TaskFilter.class), any(TaskSortOption.class), any(Pageable.class)))
                .thenReturn(samplePage());

        mockMvc.perform(get("/api/tasks").param("sort", "TITLE"))
                .andExpect(status().isOk());

        verify(taskService).findAll(any(TaskFilter.class), eq(TaskSortOption.TITLE), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/tasks?page=2&size=5 uses the requested pagination")
    void listTasksPaginated() throws Exception {
        when(taskService.findAll(any(TaskFilter.class), any(TaskSortOption.class), any(Pageable.class)))
                .thenReturn(samplePage());

        mockMvc.perform(get("/api/tasks").param("page", "2").param("size", "5"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(taskService).findAll(any(TaskFilter.class), any(TaskSortOption.class), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    @DisplayName("GET /api/tasks?size=500 clamps the page size to 100")
    void listTasksClampsPageSize() throws Exception {
        when(taskService.findAll(any(TaskFilter.class), any(TaskSortOption.class), any(Pageable.class)))
                .thenReturn(samplePage());

        mockMvc.perform(get("/api/tasks").param("size", "500"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(taskService).findAll(any(TaskFilter.class), any(TaskSortOption.class), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
    }

    @Test
    @DisplayName("GET /api/tasks?page=-1 falls back to the first page")
    void listTasksClampsNegativePage() throws Exception {
        when(taskService.findAll(any(TaskFilter.class), any(TaskSortOption.class), any(Pageable.class)))
                .thenReturn(samplePage());

        mockMvc.perform(get("/api/tasks").param("page", "-1"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(taskService).findAll(any(TaskFilter.class), any(TaskSortOption.class), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isZero();
    }

    @Test
    @DisplayName("GET /api/tasks?status=UNKNOWN returns 400")
    void listTasksWithInvalidStatus() throws Exception {
        mockMvc.perform(get("/api/tasks").param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /api/tasks?priority=URGENT returns 400")
    void listTasksWithInvalidPriority() throws Exception {
        mockMvc.perform(get("/api/tasks").param("priority", "URGENT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /api/tasks?sort=random returns 400")
    void listTasksWithInvalidSort() throws Exception {
        mockMvc.perform(get("/api/tasks").param("sort", "random"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ------------------------------------------------------------------
    // GET /api/tasks/statistics
    // ------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/tasks/statistics returns the aggregated figures")
    void statistics() throws Exception {
        when(taskService.getStatistics()).thenReturn(new TaskStatistics(10, 6, 4, 2, 40));

        mockMvc.perform(get("/api/tasks/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10))
                .andExpect(jsonPath("$.pending").value(6))
                .andExpect(jsonPath("$.completed").value(4))
                .andExpect(jsonPath("$.overdue").value(2))
                .andExpect(jsonPath("$.completionPercentage").value(40));
    }

    // ------------------------------------------------------------------
    // GET /api/tasks/{id}
    // ------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/tasks/{id} returns the task")
    void getTaskById() throws Exception {
        when(taskService.findById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Buy milk"))
                .andExpect(jsonPath("$.dueDate").value("2030-01-15"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.createdAt").value("2026-01-01T10:00:00Z"))
                .andExpect(jsonPath("$.updatedAt").value("2026-01-02T10:00:00Z"));
    }

    @Test
    @DisplayName("GET /api/tasks/{id} returns 404 when the task does not exist")
    void getTaskByIdNotFound() throws Exception {
        when(taskService.findById(99L)).thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("99")))
                .andExpect(jsonPath("$.path").value("/api/tasks/99"));
    }

    @Test
    @DisplayName("GET /api/tasks/{id} returns 400 for a non numeric id")
    void getTaskByInvalidId() throws Exception {
        mockMvc.perform(get("/api/tasks/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ------------------------------------------------------------------
    // POST /api/tasks
    // ------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/tasks creates a task and returns 201")
    void createTask() throws Exception {
        TaskRequest request = new TaskRequest("Write the report", "Final version",
                LocalDate.of(2030, 2, 1), TaskPriority.HIGH, false);
        when(taskService.create(any(TaskRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/tasks/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Buy milk"));
    }

    @Test
    @DisplayName("POST /api/tasks rejects a blank title with 400")
    void createTaskWithBlankTitle() throws Exception {
        TaskRequest request = new TaskRequest("   ", null, null, null, false);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.title").exists());

        verify(taskService, never()).create(any(TaskRequest.class));
    }

    @Test
    @DisplayName("POST /api/tasks rejects a title longer than 200 characters")
    void createTaskWithTooLongTitle() throws Exception {
        TaskRequest request = new TaskRequest("a".repeat(201), null, null, null, false);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    @Test
    @DisplayName("POST /api/tasks rejects an unknown priority with 400")
    void createTaskWithInvalidPriority() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Valid\",\"priority\":\"URGENT\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/tasks rejects a malformed body with 400")
    void createTaskWithMalformedBody() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not-json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ------------------------------------------------------------------
    // PUT /api/tasks/{id}
    // ------------------------------------------------------------------

    @Test
    @DisplayName("PUT /api/tasks/{id} updates the task")
    void updateTask() throws Exception {
        TaskRequest request = new TaskRequest("Updated title", null, null, TaskPriority.LOW, true);
        when(taskService.update(eq(1L), any(TaskRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /api/tasks/{id} returns 404 when the task does not exist")
    void updateTaskNotFound() throws Exception {
        TaskRequest request = new TaskRequest("Updated title", null, null, null, false);
        when(taskService.update(eq(99L), any(TaskRequest.class)))
                .thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(put("/api/tasks/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/tasks/{id} validates the payload")
    void updateTaskValidatesPayload() throws Exception {
        TaskRequest request = new TaskRequest("", null, null, null, false);

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    // ------------------------------------------------------------------
    // PATCH /api/tasks/{id}/complete
    // ------------------------------------------------------------------

    @Test
    @DisplayName("PATCH /api/tasks/{id}/complete toggles the status")
    void completeTaskWithoutBody() throws Exception {
        when(taskService.changeCompletedStatus(1L, null)).thenReturn(sampleResponse());

        mockMvc.perform(patch("/api/tasks/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(taskService).changeCompletedStatus(1L, null);
    }

    @Test
    @DisplayName("PATCH /api/tasks/{id}/complete sets an explicit status")
    void completeTaskWithExplicitStatus() throws Exception {
        when(taskService.changeCompletedStatus(eq(1L), eq(Boolean.TRUE))).thenReturn(sampleResponse());

        mockMvc.perform(patch("/api/tasks/1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateCompletedRequest(true))))
                .andExpect(status().isOk());

        verify(taskService).changeCompletedStatus(1L, Boolean.TRUE);
    }

    @Test
    @DisplayName("PATCH /api/tasks/{id}/complete returns 404 when the task does not exist")
    void completeTaskNotFound() throws Exception {
        when(taskService.changeCompletedStatus(eq(99L), any()))
                .thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(patch("/api/tasks/99/complete"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // DELETE /api/tasks/{id}
    // ------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /api/tasks/{id} removes the task and returns 204")
    void deleteTask() throws Exception {
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService).delete(1L);
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} returns 404 when the task does not exist")
    void deleteTaskNotFound() throws Exception {
        doThrow(new TaskNotFoundException(99L)).when(taskService).delete(anyLong());

        mockMvc.perform(delete("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
