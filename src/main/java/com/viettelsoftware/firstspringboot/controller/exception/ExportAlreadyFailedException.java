package com.viettelsoftware.firstspringboot.controller.exception;

import com.viettelsoftware.firstspringboot.controller.exception.abc.BaseGloballyHandledException;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

@Getter
public class ExportAlreadyFailedException extends BaseGloballyHandledException {
    private final long exportId;

    public static @NonNull ExportAlreadyFailedException of(long exportId) {
        return new ExportAlreadyFailedException(exportId);
    }

    @Builder
    public ExportAlreadyFailedException(long exportId) {
        super(
                HttpStatus.BAD_REQUEST,
                String.format("Export `%d` has already failed", exportId));

        this.exportId = exportId;
    }
}
