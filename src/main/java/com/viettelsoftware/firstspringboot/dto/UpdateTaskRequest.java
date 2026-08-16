package com.viettelsoftware.firstspringboot.dto;

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
public class UpdateTaskRequest {
    @ValidTaskDescription
    private final @NonNull String description;
}
