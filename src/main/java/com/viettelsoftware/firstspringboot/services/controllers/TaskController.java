package com.viettelsoftware.firstspringboot.services.controllers;

import com.viettelsoftware.firstspringboot.entities.Task;
import com.viettelsoftware.firstspringboot.services.repositories.TaskRepository;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class TaskController {
    @Autowired
    private TaskRepository taskRepository;

    @GetMapping("/tasks")
    public List<@NonNull Task> getTasks() {
        return taskRepository.findAll();
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<@NonNull Task> getTaskById(@PathVariable(value = "id") @NonNull long id) throws TaskNotFoundException {
        val task = taskRepository.findById(id)
                .orElseThrow(() -> TaskNotFoundException.of(id));
        return ResponseEntity.ok(task);
    }

    @PreAuthorize("hasRole(")
    @PostMapping("/tasks")
    public @NonNull Task createTask(@Valid @RequestBody @NonNull Task task) {
        return taskRepository.save(task);
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<@NonNull Task> updateTask(@PathVariable(value = "id") @NonNull long id, @Valid @RequestBody @NonNull Task taskDetails) throws TaskNotFoundException {
        val task = taskRepository.findById(id)
                .orElseThrow(() -> TaskNotFoundException.of(id));
        task.setDescription(taskDetails.getDescription());

        val updatedTask = taskRepository.save(task);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/tasks/{id}")
    public Map<@NonNull String, @NonNull Boolean> deleteTask(@PathVariable(value = "id") @NonNull long id) throws TaskNotFoundException {
        val task = taskRepository.findById(id)
                .orElseThrow(() -> TaskNotFoundException.of(id));
        taskRepository.delete(task);

        return Map.of("deleted", Boolean.TRUE);
    }

    @DeleteMapping("/tasks")
    public void deleteTasks() {
        taskRepository.deleteAll();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class TaskNotFoundException extends Exception {
        private static final long serialVersionUID = 1L;

        public static @NonNull TaskNotFoundException of(@NonNull long taskId) {
            return new TaskNotFoundException(String.format("Task %d not found", taskId));
        }

        private TaskNotFoundException(@NonNull String message) {
            super(message);
        }
    }
}
