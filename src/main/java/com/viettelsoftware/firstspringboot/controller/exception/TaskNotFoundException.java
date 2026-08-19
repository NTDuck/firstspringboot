package com.viettelsoftware.firstspringboot.controller.exception;

import com.viettelsoftware.firstspringboot.controller.exception.abc.BaseGloballyHandledException;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

@Getter
public class TaskNotFoundException extends BaseGloballyHandledException {

    private final @NonNull Long taskId;

    public static TaskNotFoundException of(Long taskId) {
        return new TaskNotFoundException(taskId);
    }

    @Builder
    public TaskNotFoundException(Long taskId) {
        super(
                HttpStatus.NOT_FOUND,
                String.format("Task `%d` not found", taskId));

        this.taskId = taskId;
    }
}
