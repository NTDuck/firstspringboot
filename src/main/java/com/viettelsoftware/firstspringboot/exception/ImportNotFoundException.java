package com.viettelsoftware.firstspringboot.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

@Getter
public class ImportNotFoundException extends FirstspringbootApplicationException {
    private final long importId;

    public static @NonNull ImportNotFoundException of(long importId) {
        return new ImportNotFoundException(importId);
    }

    @Builder
    public ImportNotFoundException(long importId) {
        super(
                HttpStatus.NOT_FOUND,
                String.format("Import `%d` not found", importId));

        this.importId = importId;
    }
}
