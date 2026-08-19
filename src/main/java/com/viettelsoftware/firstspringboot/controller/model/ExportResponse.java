package com.viettelsoftware.firstspringboot.controller.model;

import com.viettelsoftware.firstspringboot.entity.Export;
import lombok.*;
import org.springframework.lang.Nullable;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ExportResponse {

    private final long id;
    private final @NonNull Export.Type type;
    private final @NonNull Export.Status status;
    private final @NonNull Export.RequestedBy requestedBy;
    private final @NonNull Instant createdAt;
    private final @Nullable Long timeElapsed;
    private final @Nullable Instant completedAt;
    private final @Nullable String url;

    public static @NonNull ExportResponse from(@NonNull Export export) {
        return ExportResponse.builder()
                .id(export.getId())
                .type(export.getType())
                .status(export.getStatus())
                .requestedBy(export.getRequestedBy())
                .createdAt(export.getCreatedAt())
                .timeElapsed(export.getTimeElapsed())
                .completedAt(export.getCompletedAt())
                .url(export.getUrl())
                .build();
    }
}
