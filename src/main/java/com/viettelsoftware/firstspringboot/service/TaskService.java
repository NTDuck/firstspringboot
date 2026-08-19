package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.Task;
import com.viettelsoftware.firstspringboot.service.exception.TaskNotFoundException;
import com.viettelsoftware.firstspringboot.service.dto.TaskWithoutIdDto;

import java.util.List;
import java.util.Optional;

public interface TaskService {

    Long count();

    Optional<Task> getTaskById(Long taskId);

    List<Task> getTasks();

    Task createTask(TaskWithoutIdDto taskWithoutId);

    void updateTask(Long taskId, TaskWithoutIdDto taskWithoutId) throws TaskNotFoundException;

    void deleteTaskById(Long taskId) throws TaskNotFoundException;

    void deleteTasks();
}
