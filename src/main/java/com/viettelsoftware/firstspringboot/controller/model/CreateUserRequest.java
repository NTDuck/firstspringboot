package com.viettelsoftware.firstspringboot.controller.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor(force = true, access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "of")
@Builder
public class CreateUserRequest {
    @NotBlank
    private final @NonNull String keycloakId;

    @NotBlank
    @Size(min = 3, max = 255)
    @JsonAlias({"username", "name"})
    private final @NonNull String name;

    @Email
    @Size(max = 255)
    private final @NonNull String email;

    @Size(max = 255)
    private final @NonNull String firstName;

    @Size(max = 255)
    private final @NonNull String lastName;
}
