package com.viettelsoftware.firstspringboot.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

@Getter
public class TaskNotFoundException extends FirstspringbootApplicationException {
    private final long taskId;

    public static @NonNull TaskNotFoundException of(long taskId) {
        return new TaskNotFoundException(taskId);
    }

    @Builder
    public TaskNotFoundException(long taskId) {
        super(
                HttpStatus.NOT_FOUND,
                String.format("Task `%d` not found", taskId));

        this.taskId = taskId;
    }
}
