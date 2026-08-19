package com.viettelsoftware.firstspringboot.controller;

import com.viettelsoftware.firstspringboot.controller.dto.CreateExportRequest;
import com.viettelsoftware.firstspringboot.controller.dto.CreateImportRequest;
import com.viettelsoftware.firstspringboot.entity.Export;
import com.viettelsoftware.firstspringboot.entity.Import;
import com.viettelsoftware.firstspringboot.entity.Task;
import com.viettelsoftware.firstspringboot.service.dto.TaskWithoutIdDto;
import com.viettelsoftware.firstspringboot.service.ExportService;
import com.viettelsoftware.firstspringboot.service.ImportService;
import com.viettelsoftware.firstspringboot.service.TaskService;
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
        val import_ = importService.create(CreateImportRequest.of(Import.Type.TASK));
        return ResponseEntity.accepted()
                .location(URI.create(String.format("/api/v1/imports/%d", import_.getId())))
                .build();
    }

    @PreAuthorize("hasAuthority('REALM_ROLE_GET')")
    @GetMapping("/{taskId}")
    public Task getTaskById(@PathVariable Long taskId) {
        return taskService.getTaskById(taskId);
    }

    @PreAuthorize("hasAuthority('REALM_ROLE_POST')")
    @PostMapping
    public Task createTask(@Valid @RequestBody TaskWithoutIdDto taskWithoutId) {
        return taskService.createTask(taskWithoutId);
    }

    @PreAuthorize("hasAuthority('REALM_ROLE_PUT')")
    @PutMapping("/{taskId}")
    public Task updateTaskById(@PathVariable Long taskId, @Valid @RequestBody TaskWithoutIdDto taskWithoutId) {
        taskService.updateTask(taskId, taskWithoutId);
        return taskService.getTaskById(taskId);
    }

    @PreAuthorize("hasAuthority('REALM_ROLE_DELETE')")
    @DeleteMapping("/{taskId}")
    public Map<String, Boolean> deleteTaskById(@PathVariable Long taskId) {
        taskService.deleteTaskById(taskId);

        // Impossible to be false - would throw TaskNotFoundException instead
        return Map.of("deleted", true);
    }

    @PreAuthorize("hasAuthority('REALM_ROLE_DELETE')")
    @DeleteMapping
    public Map<String, Long> deleteTasks() {
        val taskCount = taskService.count();
        taskService.deleteTasks();

        return Map.of("deleted", taskCount);
    }
}
