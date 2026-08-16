package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.Task;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public interface TaskService {
    @NonNull boolean exists(@NonNull long taskId);

    @NonNull long count();

    Optional<@NonNull Task> getTaskById(@NonNull long taskId);

    List<@NonNull Task> getTasks();

    @NonNull Task createTask(@NonNull Task task);

    @NonNull Optional<@NonNull Task> updateTask(@NonNull long taskId, @NonNull String description);

    void deleteTaskById(@NonNull long taskId);

    void deleteTasks();
}
