package com.viettelsoftware.firstspringboot.entity.abc;

import io.micrometer.core.lang.Nullable;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.time.Duration;
import java.time.Instant;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
public abstract class ImportExport extends AuditableEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private @NonNull Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private @NonNull Status status;

    private Instant completedAt;
    private String url;

    public enum Type {
        TASK,
        USER
    }

    public enum Status {
        PENDING,
        PROCESSING,
        SUCCESS,
        FAILED
    }

    @Transient
    public @Nullable Duration getTimeElapsed() {
        Instant createdAt = getCreatedAt();
        if (createdAt == null) return null;

        Instant rhs = completedAt != null ? completedAt : Instant.now();
        return Duration.between(createdAt, rhs);
    }
}
