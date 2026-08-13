package com.viettelsoftware.firstspringboot.task.controller;

import com.viettelsoftware.firstspringboot.task.dto.CreateTaskRequest;
import com.viettelsoftware.firstspringboot.task.dto.UpdateTaskRequest;
import com.viettelsoftware.firstspringboot.task.entity.Task;
import com.viettelsoftware.firstspringboot.task.exception.TaskNotFoundException;
import com.viettelsoftware.firstspringboot.task.repository.TaskRepository;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {
    @PreAuthorize("hasRole('REALM_ROLE_GET')")
    @GetMapping
    public List<@NonNull Task> getTasks() {
        return taskRepository.findAll();
    }

    @PreAuthorize("hasRole('REALM_ROLE_GET')")
    @GetMapping("/{id}")
    public @NonNull Task getTaskById(@PathVariable @NonNull long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> TaskNotFoundException.of(id));
    }

    @PreAuthorize("hasRole('REALM_ROLE_POST')")
    @PostMapping
    public @NonNull Task createTask(@Valid @RequestBody @NonNull CreateTaskRequest request) {
        val task = request.toTask();
        return taskRepository.save(task);
    }

    @PreAuthorize("hasRole('REALM_ROLE_PUT')")
    @PutMapping("/{id}")
    public @NonNull Task updateTask(@PathVariable @NonNull long id, @Valid @RequestBody @NonNull UpdateTaskRequest request) {
        val task = taskRepository.findById(id)
                .orElseThrow(() -> TaskNotFoundException.of(id));
        task.setDescription(request.getDescription());

        return taskRepository.save(task);
    }

    @PreAuthorize("hasRole('REALM_ROLE_DELETE')")
    @DeleteMapping("/{id}")
    public Map<@NonNull String, @NonNull Boolean> deleteTask(@PathVariable @NonNull long id) throws TaskNotFoundException {
        val task = taskRepository.findById(id)
                .orElseThrow(() -> TaskNotFoundException.of(id));
        taskRepository.delete(task);

        return Map.of("deleted", true);
    }

    @PreAuthorize("hasRole('REALM_ROLE_DELETE')")
    @DeleteMapping
    public Map<@NonNull String, @NonNull Long> deleteTasks() {
        val taskCount = taskRepository.count();
        taskRepository.deleteAll();

        return Map.of("deleted", taskCount);
    }

    @Autowired
    private TaskRepository taskRepository;
}
