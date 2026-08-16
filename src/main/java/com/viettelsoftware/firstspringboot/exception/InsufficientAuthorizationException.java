package com.viettelsoftware.firstspringboot.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

@Getter
public class InsufficientAuthorizationException extends FirstspringbootApplicationException {
    private final @NonNull String username;
    private final @NonNull String operation;

    @Builder
    public InsufficientAuthorizationException(@NonNull String username, @NonNull String operation) {
        super(
                HttpStatus.FORBIDDEN,
                String.format("User `%s` not authorized to `%s`", username, operation));

        this.username = username;
        this.operation = operation;
    }
}
