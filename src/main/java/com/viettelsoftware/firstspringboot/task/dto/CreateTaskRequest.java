package com.viettelsoftware.firstspringboot.task.dto;

import com.viettelsoftware.firstspringboot.task.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(staticName = "of")
@Builder
public class CreateTaskRequest {
    private final @NonNull String description;

    public @NonNull Task toTask() {
        return Task.builder()
                .description(description)
                .build();
    }
}
