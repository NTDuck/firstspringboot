package com.viettelsoftware.firstspringboot.exception;

import com.viettelsoftware.firstspringboot.exception.abc.FirstspringbootApplicationException;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

@Getter
public class TaskNotFoundException extends FirstspringbootApplicationException {
    private final @NonNull long taskId;

    @Builder
    public TaskNotFoundException(@NonNull long taskId) {
        super(
                HttpStatus.NOT_FOUND,
                String.format("Task %d not found", taskId));

        this.taskId = taskId;
    }
}
