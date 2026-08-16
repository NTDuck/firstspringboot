package com.viettelsoftware.firstspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor(force = true, access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "of")
@Builder
public class CreateUserRequest {
    @NotBlank
    private final @NonNull String keycloakId;

    @NotBlank
    private final @NonNull String name;

    private final @NonNull String email;

    private final @NonNull String firstName;

    private final @NonNull String lastName;
}
