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
@Table(name = "audit_events", indexes = {
        @Index(name = "idx_audit_service_timestamp", columnList = "service_name, timestamp"),
        @Index(name = "idx_audit_actor_timestamp", columnList = "actor_user_id, timestamp")
})
public class AuditEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @NonNull long id;

    @Builder.Default
    @Column(name = "timestamp", nullable = false)
    private @NonNull Instant timestamp = Instant.now();

    @Column(name = "service_name", nullable = false)
    private @NonNull String serviceName;

    @Column(name = "actor_user_id", nullable = false)
    private @NonNull long actorUserId;

    @Builder.Default
    @Column(name = "actor_username", nullable = false)
    private @NonNull String actorUsername;

    @Column(name = "action", nullable = false)
    private @NonNull String action;

    @Column(name = "result", nullable = false)
    private @NonNull boolean result;

    @Column(name = "exception", columnDefinition = "TEXT")
    private @Nullable String exception;
}
