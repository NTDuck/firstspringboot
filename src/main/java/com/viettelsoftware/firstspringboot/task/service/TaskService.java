package com.viettelsoftware.firstspringboot.task.service;

import com.viettelsoftware.firstspringboot.task.entity.Task;
import com.viettelsoftware.firstspringboot.task.repository.TaskRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    public @NonNull boolean exists(@NonNull long taskId) {
        return taskRepository.existsById(taskId);
    }

    public @NonNull long count() {
        return taskRepository.count();
    }

    public Optional<@NonNull Task> getTaskById(@NonNull long taskId) {
        return taskRepository.findById(taskId);
    }

    public List<@NonNull Task> getTasks() {
        return taskRepository.findAll();
    }

    public @NonNull Task createTask(@NonNull Task task) {
        return taskRepository.save(task);
    }

    public @NonNull Optional<@NonNull Task> updateTask(@NonNull long taskId, @NonNull String description) {
        return taskRepository.findById(taskId)
                .map(task -> task.withDescription(description));
    }

    public void deleteTaskById(@NonNull long taskId) {
        taskRepository.deleteById(taskId);
    }

    public void deleteTasks() {
        taskRepository.deleteAll();
    }

    @Autowired
    private TaskRepository taskRepository;
}