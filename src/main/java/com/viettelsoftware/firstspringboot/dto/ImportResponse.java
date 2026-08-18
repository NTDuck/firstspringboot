package com.viettelsoftware.firstspringboot.dto;

import com.viettelsoftware.firstspringboot.entity.Import;
import lombok.*;
import org.springframework.lang.Nullable;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ImportResponse {

    private final long id;
    private final @NonNull Import.Type type;
    private final @NonNull Import.Status status;
    private final @NonNull Import.RequestedBy requestedBy;
    private final @NonNull Instant createdAt;
    private final @Nullable Long timeElapsed;
    private final @Nullable Instant completedAt;
    private final @Nullable String url;

    public static @NonNull ImportResponse from(@NonNull Import importEntity) {
        return ImportResponse.builder()
                .id(importEntity.getId())
                .type(importEntity.getType())
                .status(importEntity.getStatus())
                .requestedBy(importEntity.getRequestedBy())
                .createdAt(importEntity.getCreatedAt())
                .timeElapsed(importEntity.getTimeElapsed())
                .completedAt(importEntity.getCompletedAt())
                .url(importEntity.getUrl())
                .build();
    }
}
