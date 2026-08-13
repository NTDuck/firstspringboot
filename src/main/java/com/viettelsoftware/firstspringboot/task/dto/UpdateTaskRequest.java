package com.viettelsoftware.firstspringboot.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Getter
@NoArgsConstructor(force = true, access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "of")
@Builder
public class UpdateTaskRequest {
    private final @NonNull String description;
}
