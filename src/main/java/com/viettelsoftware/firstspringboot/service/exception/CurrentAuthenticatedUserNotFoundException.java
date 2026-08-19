package com.viettelsoftware.firstspringboot.service.exception;

import com.viettelsoftware.firstspringboot.exception.abc.BaseGloballyHandledException;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CurrentAuthenticatedUserNotFoundException extends BaseGloballyHandledException {

    public static CurrentAuthenticatedUserNotFoundException of() {
        return new CurrentAuthenticatedUserNotFoundException();
    }

    @Builder
    public CurrentAuthenticatedUserNotFoundException() {
        super(HttpStatus.UNAUTHORIZED, "Current authenticated user not found");
    }
}
