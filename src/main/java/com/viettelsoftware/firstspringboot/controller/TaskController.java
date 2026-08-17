package com.viettelsoftware.firstspringboot.controller;

import com.viettelsoftware.firstspringboot.dto.CreateTaskRequest;
import com.viettelsoftware.firstspringboot.dto.UpdateTaskRequest;
import com.viettelsoftware.firstspringboot.entity.Task;
import com.viettelsoftware.firstspringboot.exception.TaskNotFoundException;
import com.viettelsoftware.firstspringboot.service.TaskExportService;
import com.viettelsoftware.firstspringboot.service.TaskService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskExportService taskExportService;

    @PreAuthorize("hasAuthority('REALM_ROLE_GET')")
    @GetMapping
    public List<@NonNull Task> getTasks() {
        return taskService.getTasks();
    }

    @PreAuthorize("hasAuthority('REALM_ROLE_GET')")
    @GetMapping("/export")
    public Map<@NonNull String, @NonNull String> exportTasks() {
        String url = taskExportService.exportTasks();
        return Map.of("url", url);
    }

    @PreAuthorize("hasAuthority('REALM_ROLE_GET')")
    @GetMapping("/{id}")
    public @NonNull Task getTaskById(@PathVariable @NonNull long id) {
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
    public @NonNull Task updateTaskById(@PathVariable @NonNull long id, @Valid @RequestBody @NonNull UpdateTaskRequest request) {
        return taskService.updateTask(id, request.getDescription())
                .orElseThrow(() -> TaskNotFoundException.of(id));
    }

    @PreAuthorize("hasAuthority('REALM_ROLE_DELETE')")
    @DeleteMapping("/{id}")
    public Map<@NonNull String, @NonNull Boolean> deleteTaskById(@PathVariable @NonNull long id) throws TaskNotFoundException {
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
