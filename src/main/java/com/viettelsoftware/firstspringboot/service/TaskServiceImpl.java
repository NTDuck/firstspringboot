package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.Task;
import com.viettelsoftware.firstspringboot.repository.TaskRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Override
    public @NonNull boolean exists(@NonNull long taskId) {
        return taskRepository.existsById(taskId);
    }

    @Override
    public @NonNull long count() {
        return taskRepository.count();
    }

    @Override
    public Optional<@NonNull Task> getTaskById(@NonNull long taskId) {
        return taskRepository.findById(taskId);
    }

    @Override
    public List<@NonNull Task> getTasks() {
        return taskRepository.findAll();
    }

    @Override
    public @NonNull Task createTask(@NonNull Task task) {
        return taskRepository.save(task);
    }

    @Override
    public @NonNull Optional<@NonNull Task> updateTask(@NonNull long taskId, @NonNull String description) {
        return taskRepository.findById(taskId)
                .map(task -> task.withDescription(description));
    }

    @Override
    public void deleteTaskById(@NonNull long taskId) {
        taskRepository.deleteById(taskId);
    }

    @Override
    public void deleteTasks() {
        taskRepository.deleteAll();
    }
}
