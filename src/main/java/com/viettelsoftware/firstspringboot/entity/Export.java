package com.viettelsoftware.firstspringboot.entity;

import lombok.*;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Entity
@Table(name = "exports", indexes = {
        @Index(name = "idx_exports_status_created", columnList = "status, created_at")
})
public class Export {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private @NonNull Type type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private @NonNull Status status;

    @Embedded
    private @NonNull RequestedBy requestedBy;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private @NonNull Instant createdAt = Instant.now();

    @Column(name = "time_elapsed")
    private @Nullable Long timeElapsed;

    @Column(name = "completed_at")
    private @Nullable Instant completedAt;

    @Column(name = "url", columnDefinition = "TEXT")
    private @Nullable String url;

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

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor(force = true)
    @AllArgsConstructor
    @Embeddable
    public static class RequestedBy {

        @Column(name = "requested_by_username", nullable = false)
        private @NonNull String username;

        @Column(name = "requested_by_user_id", nullable = false)
        private @NonNull Long userId;
    }
}
