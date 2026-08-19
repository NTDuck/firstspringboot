package com.viettelsoftware.firstspringboot.exception.abc;

import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseGloballyHandledException extends RuntimeException {

    private final @NonNull HttpStatus httpStatus;
    private final @NonNull String error;

    protected BaseGloballyHandledException(@NotNull HttpStatus httpStatus, String message) {
        super(message);

        this.httpStatus = httpStatus;
        this.error = getAndFormatErrorFromHttpStatus(httpStatus);
    }

    // Format as SCREAMING_SNAKE_CASE
    private String getAndFormatErrorFromHttpStatus(HttpStatus httpStatus) {
        return httpStatus
                .getReasonPhrase()
                .toUpperCase()
                .replace(" ", "_");
    }

    protected BaseGloballyHandledException(@NotNull HttpStatus httpStatus, @NotNull String error, String message) {
        super(message);

        this.httpStatus = httpStatus;
        this.error = error;
    }
}
