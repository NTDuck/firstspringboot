package com.viettelsoftware.firstspringboot.advice;

import com.viettelsoftware.firstspringboot.exception.FirstspringbootApplicationException;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(FirstspringbootApplicationException.class)
    public @NonNull ResponseEntity<Payload> handle(@NonNull FirstspringbootApplicationException exception, @NonNull HttpServletRequest request) {
        val payload = Payload.builder()
                .status(exception.getHttpStatus().value())
                .error(exception.getError())
                .message(exception.getMessage())
                .build();

        return new ResponseEntity<>(payload, exception.getHttpStatus());
    }

    @Getter
    @Builder
    public static class Payload {
        @Builder.Default
        private final @NonNull Instant timestamp = Instant.now();

        @Builder.Default
        private final @NonNull UUID traceId = UUID.randomUUID(); // service, tts,

        private final @NonNull int status;
        private final @NonNull String error;
        private final @NonNull String message;
    }
}
