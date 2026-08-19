package com.viettelsoftware.firstspringboot.controller.dto;

import com.viettelsoftware.firstspringboot.entity.Export;
import com.viettelsoftware.firstspringboot.entity.abc.ImportExport;
import lombok.*;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class ExportResponse {

    private final long id;
    private final @NonNull ImportExport.Type type;
    private final @NonNull ImportExport.Status status;
    private final @Nullable Long createdByUserId;
    private final @NonNull Instant createdAt;
    private final @Nullable Duration timeElapsed;
    private final @Nullable Instant completedAt;
    private final @Nullable String url;

    public static @NonNull ExportResponse from(@NonNull Export export) {
        return ExportResponse.builder()
                .id(export.getId())
                .type(export.getType())
                .status(export.getStatus())
                .createdByUserId(export.getCreatedByUserId())
                .createdAt(export.getCreatedAt())
                .timeElapsed(export.getTimeElapsed())
                .completedAt(export.getCompletedAt())
                .url(export.getUrl())
                .build();
    }
}
