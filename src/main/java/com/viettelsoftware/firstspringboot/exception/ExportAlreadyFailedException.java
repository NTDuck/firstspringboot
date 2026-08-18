package com.viettelsoftware.firstspringboot.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

@Getter
public class ExportAlreadyFailedException extends FirstspringbootApplicationException {
    private final @NonNull long exportId;

    public static @NonNull ExportAlreadyFailedException of(@NonNull long exportId) {
        return new ExportAlreadyFailedException(exportId);
    }

    @Builder
    public ExportAlreadyFailedException(@NonNull long exportId) {
        super(
                HttpStatus.BAD_REQUEST,
                String.format("Export `%d` has already failed", exportId));

        this.exportId = exportId;
    }
}
