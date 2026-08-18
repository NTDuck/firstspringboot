package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.Task;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public interface TaskService {
    boolean exists(long taskId);

    long count();

    Optional<@NonNull Task> getTaskById(long taskId);

    List<@NonNull Task> getTasks();

    @NonNull Task createTask(@NonNull Task task);

    @NonNull Optional<@NonNull Task> updateTask(long taskId, @NonNull String description);

    void deleteTaskById(long taskId);

    void deleteTasks();
}
