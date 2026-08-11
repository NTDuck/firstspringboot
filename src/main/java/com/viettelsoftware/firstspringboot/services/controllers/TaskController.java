package com.viettelsoftware.firstspringboot.services.controllers;

import com.viettelsoftware.firstspringboot.entities.Task;
import com.viettelsoftware.firstspringboot.services.repositories.TaskRepository;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<@NonNull Task> getTaskById(@PathVariable(value = "id") @NonNull long taskId)
            throws TaskNotFoundException {
        val task = taskRepository.findById(taskId)
                .orElseThrow(() -> TaskNotFoundException.of(taskId));
        return ResponseEntity.ok().body(task);
    }

    @PostMapping("/tasks")
    public @NonNull Task createTask(@Valid @RequestBody @NonNull Task task) {
        return taskRepository.save(task);
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<@NonNull Task> updateTask(@PathVariable(value = "id") @NonNull long taskId, @Valid @RequestBody @NonNull String description)
            throws TaskNotFoundException {
        val task = taskRepository.findById(taskId)
                .orElseThrow(() -> TaskNotFoundException.of(taskId));
        task.setDescription(description);

        val updatedTask = taskRepository.save(task);
        return ResponseEntity.ok().body(updatedTask);
    }

    @DeleteMapping("/tasks/{id}")
    public Map<@NonNull String, @NonNull Boolean> deleteTask(@PathVariable(value = "id") @NonNull long taskId)
            throws TaskNotFoundException {
        val task = taskRepository.findById(taskId)
                .orElseThrow(() -> TaskNotFoundException.of(taskId));
        taskRepository.delete(task);

        return Map.of("deleted", Boolean.TRUE);
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public static class TaskNotFoundException extends Exception {
        private static final long serialVersionUID = 1L;

        public static @NonNull TaskNotFoundException of(@NonNull long taskId) {
            return new TaskNotFoundException("Task " + taskId + " not found");
        }

        private @NonNull TaskNotFoundException(@NonNull String message) {
            super(message);
        }
    }

}
