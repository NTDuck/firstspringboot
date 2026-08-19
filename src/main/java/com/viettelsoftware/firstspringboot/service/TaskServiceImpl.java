package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.annotation.Auditable;
import com.viettelsoftware.firstspringboot.entity.Task;
import com.viettelsoftware.firstspringboot.repository.TaskRepository;
import com.viettelsoftware.firstspringboot.service.dto.TaskWithoutIdDto;
import com.viettelsoftware.firstspringboot.service.exception.TaskNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    @Override
    @Auditable
    @Transactional(readOnly = true)
    public long count() {
        return taskRepository.count();
    }

    @Override
    @Auditable
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "tasks", key = "#taskId")
    public Task getTaskById(Long taskId) throws TaskNotFoundException {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> TaskNotFoundException.of(taskId));
    }

    @Override
    @Auditable
    @Transactional(readOnly = true)
    public List<Task> getTasks() {
        return taskRepository.findAll();
    }

    @Override
    @Auditable
    @Transactional
    @CachePut
    public Task createTask(TaskWithoutIdDto taskWithoutId) {
        val task = Task.builder()
                .description(taskWithoutId.getDescription())
                .build();

        return taskRepository.save(task);
    }

    @Override
    @Auditable
    @CacheEvict(value = "tasks", key = "#taskId")
    public void updateTask(Long taskId, TaskWithoutIdDto taskWithoutId) throws TaskNotFoundException {
        val task = taskRepository.findById(taskId)
                .orElseThrow(() -> TaskNotFoundException.of(taskId));

        task.setDescription(taskWithoutId.getDescription());
        taskRepository.save(task);
    }

    @Override
    @Auditable
    @CacheEvict(value = "tasks", key = "#taskId")
    public void deleteTaskById(Long taskId) throws TaskNotFoundException {
        val task = taskRepository.findById(taskId)
                .orElseThrow(() -> TaskNotFoundException.of(taskId));

        taskRepository.delete(task);
    }

    @Override
    @Auditable
    @CacheEvict
    public void deleteTasks() {
        taskRepository.deleteAllInBatch();
    }
}
