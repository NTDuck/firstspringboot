package com.viettelsoftware.firstspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor(force = true, access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "of")
@Builder
public class UpdateTaskRequest {
    @NotBlank
    @Size(max = 1024)
    private final @NonNull String description;
}
