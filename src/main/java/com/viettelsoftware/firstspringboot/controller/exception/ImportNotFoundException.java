package com.viettelsoftware.firstspringboot.controller.exception;

import com.viettelsoftware.firstspringboot.controller.exception.abc.BaseGloballyHandledException;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

@Getter
public class ImportNotFoundException extends BaseGloballyHandledException {

    private final @NonNull Long importId;

    public static ImportNotFoundException of(Long importId) {
        return new ImportNotFoundException(importId);
    }

    @Builder
    public ImportNotFoundException(Long importId) {
        super(
                HttpStatus.NOT_FOUND,
                String.format("Import `%d` not found", importId));

        this.importId = importId;
    }
}
