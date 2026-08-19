package com.viettelsoftware.firstspringboot.controller.dto;

import com.viettelsoftware.firstspringboot.entity.Import;
import com.viettelsoftware.firstspringboot.entity.abc.ImportExport;
import lombok.*;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class ImportResponse {

    private final long id;
    private final @NonNull ImportExport.Type type;
    private final @NonNull ImportExport.Status status;
    private final @Nullable Long createdByUserId;
    private final @NonNull Instant createdAt;
    private final @Nullable Duration timeElapsed;
    private final @Nullable Instant completedAt;
    private final @Nullable String url;

    public static @NonNull ImportResponse from(@NonNull Import importEntity) {
        return ImportResponse.builder()
                .id(importEntity.getId())
                .type(importEntity.getType())
                .status(importEntity.getStatus())
                .createdByUserId(importEntity.getCreatedByUserId())
                .createdAt(importEntity.getCreatedAt())
                .timeElapsed(importEntity.getTimeElapsed())
                .completedAt(importEntity.getCompletedAt())
                .url(importEntity.getUrl())
                .build();
    }
}
