package com.viettelsoftware.firstspringboot.config.exception;

import com.viettelsoftware.firstspringboot.exception.abc.FirstspringbootApplicationException;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

@Getter
public class InsufficientAuthorizationException extends FirstspringbootApplicationException {
    private final @NonNull String username;
    private final @NonNull String authorizationExpression;

    @Builder
    public InsufficientAuthorizationException(@NonNull String username, @NonNull String authorizationExpression) {
        super(
                HttpStatus.FORBIDDEN,
                String.format("User %s lacks authorization `%s`", username, authorizationExpression));

        this.username = username;
        this.authorizationExpression = authorizationExpression;
    }
}
