package com.viettelsoftware.firstspringboot.controller;

import com.viettelsoftware.firstspringboot.controller.model.CreateExportRequest;
import com.viettelsoftware.firstspringboot.controller.model.CreateImportRequest;
import com.viettelsoftware.firstspringboot.controller.model.CreateTaskRequest;
import com.viettelsoftware.firstspringboot.controller.model.UpdateTaskRequest;
import com.viettelsoftware.firstspringboot.entity.Export;
import com.viettelsoftware.firstspringboot.entity.Import;
import com.viettelsoftware.firstspringboot.entity.Task;
import com.viettelsoftware.firstspringboot.controller.exception.TaskNotFoundException;
import com.viettelsoftware.firstspringboot.service.ExportService;
import com.viettelsoftware.firstspringboot.service.ImportService;
import com.viettelsoftware.firstspringboot.service.TaskService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;
    private final ExportService exportService;
    private final ImportService importService;

    @PreAuthorize("hasAuthority('REALM_ROLE_GET')")
    @GetMapping
    public List<Task> getTasks() {
        return taskService.getTasks();
    }

    @PreAuthorize("hasAuthority('REALM_ROLE_GET')")
    @GetMapping("/export")
    public ResponseEntity<Void> exportTasks() {
        val export = exportService.create(CreateExportRequest.of(Export.Type.TASK));
        return ResponseEntity.accepted()
                .location(URI.create(String.format("/api/v1/exports/%d", export.getId())))
                .build();
    }

    @PreAuthorize("hasAuthority('REALM_ROLE_POST')")
    @PostMapping("/import")
    public ResponseEntity<Void> importTasks() {
        Import importEntity = importService.create(CreateImportRequest.of(Import.Type.TASK));
        return ResponseEntity.accepted()
                .location(URI.create(String.format("/api/v1/imports/%d", importEntity.getId())))
                .build();
    }

    @PreAuthorize("hasAuthority('REALM_ROLE_GET')")
    @GetMapping("/{id}")
    public @NonNull Task getTaskById(@PathVariable long id) {
        return taskService.getTaskById(id)
                .orElseThrow(() -> TaskNotFoundException.of(id));
    }

    @PreAuthorize("hasAuthority('REALM_ROLE_POST')")
    @PostMapping
    public @NonNull Task createTask(@Valid @RequestBody @NonNull CreateTaskRequest request) {
        Task task = Task.builder()
                .description(request.getDescription())
                .build();

        return taskService.createTask(task);
    }

    @PreAuthorize("hasAuthority('REALM_ROLE_PUT')")
    @PutMapping("/{id}")
    public @NonNull Task updateTaskById(@PathVariable long id, @Valid @RequestBody @NonNull UpdateTaskRequest request) {
        return taskService.updateTask(id, request.getDescription())
                .orElseThrow(() -> TaskNotFoundException.of(id));
    }

    @PreAuthorize("hasAuthority('REALM_ROLE_DELETE')")
    @DeleteMapping("/{id}")
    public Map<@NonNull String, @NonNull Boolean> deleteTaskById(@PathVariable long id) throws TaskNotFoundException {
        boolean exists = taskService.exists(id);
        taskService.deleteTaskById(id);

        return Map.of("deleted", exists);
    }

    @PreAuthorize("hasAuthority('REALM_ROLE_DELETE')")
    @DeleteMapping
    public Map<@NonNull String, @NonNull Long> deleteTasks() {
        long taskCount = taskService.count();
        taskService.deleteTasks();

        return Map.of("deleted", taskCount);
    }
}
