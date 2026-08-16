package com.viettelsoftware.firstspringboot.exception;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

@Getter
public abstract class FirstspringbootApplicationException extends RuntimeException {
    private final @NonNull HttpStatus httpStatus;
    private final @NonNull String error;

    protected FirstspringbootApplicationException(@NonNull HttpStatus httpStatus, @NonNull String message) {
        super(message);

        this.httpStatus = httpStatus;
        // Transforms into SCREAMING_SNAKE_CASE
        this.error = httpStatus
                .getReasonPhrase()
                .toUpperCase()
                .replace(" ", "_");
    }

    protected FirstspringbootApplicationException(@NonNull HttpStatus httpStatus, @NonNull String error, @NonNull String message) {
        super(message);

        this.httpStatus = httpStatus;
        this.error = error;
    }
}
