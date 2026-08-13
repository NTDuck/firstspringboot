package com.viettelsoftware.firstspringboot.advice;

import com.viettelsoftware.firstspringboot.exception.abc.FirstspringbootApplicationException;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {
//    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(FirstspringbootApplicationException.class)
    public @NonNull ResponseEntity<Payload> handle(@NonNull FirstspringbootApplicationException exception, @NonNull HttpServletRequest request) {
        val payload = Payload.builder()
                .status(exception.getHttpStatus())
                .error(exception.getError())
                .message(exception.getMessage())
                .build();

        return new ResponseEntity<>(payload, payload.getStatus());
    }

    @Getter
    @Builder
    public static class Payload {
        @Builder.Default
        private final @NonNull Instant timestamp = Instant.now();

        @Builder.Default
        private final @NonNull UUID traceId = UUID.randomUUID();

        private final @NonNull HttpStatus status;
        private final @NonNull String error;
        private final @NonNull String message;

//        // https://oneuptime.com/blog/post/2026-01-27-global-exception-handler-spring-boot/
//        private final @Nullable Object details;
    }
}

// where role used, how to check

//    custom security config
//    global exception handler
