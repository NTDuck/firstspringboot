package com.viettelsoftware.firstspringboot.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import com.viettelsoftware.firstspringboot.validation.ValidTaskDescription;

@Getter
@Builder
@AllArgsConstructor(staticName = "of")
public class CreateTaskRequest {
    @ValidTaskDescription
    private final @NonNull String description;
}
