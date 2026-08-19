package com.viettelsoftware.firstspringboot.service.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
@AllArgsConstructor
public class TaskNotFoundException extends RuntimeException {
    private final @NonNull Long taskId;
}
