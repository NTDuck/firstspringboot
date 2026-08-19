package com.viettelsoftware.firstspringboot.service.exception;

import com.viettelsoftware.firstspringboot.exception.abc.BaseGloballyHandledException;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;

@Getter
public class TaskNotFoundException extends BaseGloballyHandledException {

    private final @NonNull Long taskId;

    public static TaskNotFoundException of(@NonNull Long taskId) {
        return new TaskNotFoundException(taskId);
    }

    @Builder
    public TaskNotFoundException(@NotNull Long taskId) {
        super(
                HttpStatus.NOT_FOUND,
                String.format("Task `%d` not found", taskId));

        this.taskId = taskId;
    }
}
