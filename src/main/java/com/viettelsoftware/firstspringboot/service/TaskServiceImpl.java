package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.AuditEvent;
import com.viettelsoftware.firstspringboot.entity.Task;
import com.viettelsoftware.firstspringboot.repository.TaskRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AuditService auditService;

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
        Task created = taskRepository.save(task);
        auditService.audit(AuditEvent.builder()
                .timestamp(Instant.now())
                .serviceName("TaskService")
                .actorUserId(0L)
                .action("CREATE_TASK")
                .result(true)
                .build());
        return created;
    }

    @Override
    public @NonNull Optional<@NonNull Task> updateTask(@NonNull long taskId, @NonNull String description) {
        Optional<Task> updated = taskRepository.findById(taskId)
                .map(task -> taskRepository.save(task.withDescription(description)));

        auditService.audit(AuditEvent.builder()
                .timestamp(Instant.now())
                .serviceName("TaskService")
                .actorUserId(0L)
                .action("UPDATE_TASK")
                .result(updated.isPresent())
                .build());

        return updated;
    }

    @Override
    public void deleteTaskById(@NonNull long taskId) {
        taskRepository.deleteById(taskId);
        auditService.audit(AuditEvent.builder()
                .timestamp(Instant.now())
                .serviceName("TaskService")
                .actorUserId(0L)
                .action("DELETE_TASK")
                .result(true)
                .build());
    }

    @Override
    public void deleteTasks() {
        taskRepository.deleteAll();
        auditService.audit(AuditEvent.builder()
                .timestamp(Instant.now())
                .serviceName("TaskService")
                .actorUserId(0L)
                .action("DELETE_TASKS")
                .result(true)
                .build());
    }
}
