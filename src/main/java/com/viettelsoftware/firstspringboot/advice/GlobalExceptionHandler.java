package com.viettelsoftware.firstspringboot.advice;

import com.viettelsoftware.firstspringboot.exception.abc.BaseGloballyHandledException;
import com.viettelsoftware.firstspringboot.service.TraceService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final TraceService traceService;

    @ExceptionHandler(BaseGloballyHandledException.class)
    public ResponseEntity<Payload> handle(BaseGloballyHandledException exception, HttpServletRequest request) {
        val payload = Payload.builder()
                .traceId(traceService.getCurrentTraceId())
                .status(exception.getHttpStatus().value())
                .error(exception.getError())
                .message(exception.getMessage())
                .build();

        log.warn(
                "Caught by GlobalExceptionHandler: [traceId={}, status={}, error={}]",
                payload.getTraceId(),
                exception.getHttpStatus().value(),
                exception.getError());

        return new ResponseEntity<>(payload, exception.getHttpStatus());
    }

    @Getter
    @Builder
    public static class Payload {

        @Builder.Default
        private final Instant timestamp = Instant.now();
        private final String traceId;

        private final int status;
        private final String error;
        private final String message;
    }
}
