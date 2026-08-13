package com.viettelsoftware.firstspringboot.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(staticName = "of")
@Builder
public class UpdateTaskRequest {
    private final @NonNull String description;
}
