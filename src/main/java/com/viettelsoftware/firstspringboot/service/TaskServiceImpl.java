package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.service.dto.AuthenticatedUserDto;
import com.viettelsoftware.firstspringboot.entity.AuditEvent;
import com.viettelsoftware.firstspringboot.entity.Task;
import com.viettelsoftware.firstspringboot.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class TaskServiceImpl implements TaskService {

    private TaskRepository taskRepository;

    private AuditService auditService;
    private AuthenticationService authenticationService;

    @Override
    public Long count() {
        audit("COUNT");
        return taskRepository.count();
    }

    @Override
    public Optional<Task> getTaskById(Long taskId) {
        audit("GET_TASK_BY_ID");
        return taskRepository.findById(taskId);
    }

    @Override
    public List<Task> getTasks() {
        audit("GET_TASKS");
        return taskRepository.findAll();
    }

    @Override
    public Task createTask(Task task) {
        Task created = taskRepository.save(task);
        audit("CREATE_TASK");
        return created;
    }

    @Override
    public Optional<Task> updateTask(Long taskId, String description) {
        Optional<Task> updatedTask = taskRepository.findById(taskId)
                .map(task -> taskRepository.save(task.withDescription(description)));

        audit("UPDATE_TASK");
        return updatedTask;
    }

    @Override
    public void deleteTaskById(Long taskId) {
        taskRepository.deleteById(taskId);
        audit("DELETE_TASK_BY_ID");
    }

    @Override
    public void deleteTasks() {
        taskRepository.deleteAll();
        audit("DELETE_TASKS");
    }

    private void audit(String action) {
        AuthenticatedUserDto authenticatedUser = authenticationService.getCurrentAuthenticatedUser();
        if (authenticatedUser == null) {
            return;
        }

        AuditEvent auditEvent = AuditEvent.builder()
                .serviceName(TaskService.class.getSimpleName())
                .actorUserId(authenticatedUser.getId())
                .actorUsername(authenticatedUser.getName())
                .action(action)
                .result(true)
                .exception(null)
                .build();

        auditService.audit(auditEvent);
    }
}
