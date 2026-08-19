package com.viettelsoftware.firstspringboot.controller.exception;

import com.viettelsoftware.firstspringboot.controller.exception.abc.BaseGloballyHandledException;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

@Getter
public class ExportNotReadyException extends BaseGloballyHandledException {
    private final long exportId;

    public static @NonNull ExportNotReadyException of(long exportId) {
        return new ExportNotReadyException(exportId);
    }

    @Builder
    public ExportNotReadyException(long exportId) {
        super(
                HttpStatus.BAD_REQUEST,
                String.format("Export `%d` is not ready for download", exportId));

        this.exportId = exportId;
    }
}
