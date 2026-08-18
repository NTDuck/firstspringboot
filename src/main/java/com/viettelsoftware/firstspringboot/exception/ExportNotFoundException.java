package com.viettelsoftware.firstspringboot.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

@Getter
public class ExportNotFoundException extends FirstspringbootApplicationException {
    private final long exportId;

    public static @NonNull ExportNotFoundException of(long exportId) {
        return new ExportNotFoundException(exportId);
    }

    @Builder
    public ExportNotFoundException(long exportId) {
        super(
                HttpStatus.NOT_FOUND,
                String.format("Export `%d` not found", exportId));

        this.exportId = exportId;
    }
}
