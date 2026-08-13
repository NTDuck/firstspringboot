package com.viettelsoftware.firstspringboot.exception;

import com.viettelsoftware.firstspringboot.exception.abc.FirstspringbootApplicationException;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

@Getter
public class InsufficientRoleException extends FirstspringbootApplicationException {
    private final @NonNull String username;
    private final @NonNull String role;

    @Builder
    public InsufficientRoleException(@NonNull String username, @NonNull String role) {
        super(
                HttpStatus.FORBIDDEN,
                String.format("User %s denied access (must have role `%s`)", username, role));

        this.username = username;
        this.role = role;
    }
}
