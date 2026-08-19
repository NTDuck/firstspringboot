package com.viettelsoftware.firstspringboot.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
@AllArgsConstructor(staticName = "of")
public class TaskWithoutIdDto {
    private final @NonNull String description;
}
