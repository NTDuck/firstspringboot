package com.viettelsoftware.firstspringboot.controller.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import com.viettelsoftware.firstspringboot.validation.ValidTaskDescription;

@Getter
@NoArgsConstructor(force = true, access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "of")
@Builder
public class CreateTaskRequest {
    @ValidTaskDescription
    private final @NonNull String description;
}
