package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.dto.CurrentUser;
import com.viettelsoftware.firstspringboot.entity.AuditEvent;
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

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuthService authService;

    @Override
    public boolean exists(long taskId) {
        audit("EXISTS");
        return taskRepository.existsById(taskId);
    }

    @Override
    public long count() {
        audit("COUNT");
        return taskRepository.count();
    }

    @Override
    public Optional<@NonNull Task> getTaskById(long taskId) {
        audit("GET_TASK_BY_ID");
        return taskRepository.findById(taskId);
    }

    @Override
    public List<@NonNull Task> getTasks() {
        audit("GET_TASKS");
        return taskRepository.findAll();
    }

    @Override
    public @NonNull Task createTask(@NonNull Task task) {
        Task created = taskRepository.save(task);
        audit("CREATE_TASK");
        return created;
    }

    @Override
    public @NonNull Optional<@NonNull Task> updateTask(long taskId, @NonNull String description) {
        Optional<Task> updatedTask = taskRepository.findById(taskId)
                .map(task -> taskRepository.save(task.withDescription(description)));

        audit("UPDATE_TASK");
        return updatedTask;
    }

    @Override
    public void deleteTaskById(long taskId) {
        taskRepository.deleteById(taskId);
        audit("DELETE_TASK_BY_ID");
    }

    @Override
    public void deleteTasks() {
        taskRepository.deleteAll();
        audit("DELETE_TASKS");
    }

    private void audit(@NonNull String action) {
        CurrentUser currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        AuditEvent auditEvent = AuditEvent.builder()
                .serviceName(TaskService.class.getSimpleName())
                .actorUserId(currentUser.getId())
                .actorUsername(currentUser.getName())
                .action(action)
                .result(true)
                .exception(null)
                .build();

        auditService.audit(auditEvent);
    }
}
