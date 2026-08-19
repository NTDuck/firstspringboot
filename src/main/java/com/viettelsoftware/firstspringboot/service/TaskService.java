package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.Task;
import com.viettelsoftware.firstspringboot.service.dto.TaskWithoutIdDto;
import com.viettelsoftware.firstspringboot.service.exception.TaskNotFoundException;

import java.util.List;

public interface TaskService {

    long count();

    Task getTaskById(Long taskId) throws TaskNotFoundException;

    List<Task> getTasks();

    Task createTask(TaskWithoutIdDto taskWithoutId);

    void updateTask(Long taskId, TaskWithoutIdDto taskWithoutId) throws TaskNotFoundException;

    void deleteTaskById(Long taskId) throws TaskNotFoundException;

    void deleteTasks();
}
