package com.viettelsoftware.firstspringboot.controller.exception;

import com.viettelsoftware.firstspringboot.controller.exception.abc.BaseGloballyHandledException;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

@Getter
public class ExportNotFoundException extends BaseGloballyHandledException {
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
