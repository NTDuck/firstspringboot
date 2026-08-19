package com.viettelsoftware.firstspringboot.entity;

import com.viettelsoftware.firstspringboot.entity.abc.BaseEntity;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "audit_events", indexes = {
        @Index(name = "idx_audit_service_name_created_at", columnList = "service_name, created_at"),
        @Index(name = "idx_audit_actor_user_id_created_at", columnList = "actor_user_id, created_at")
})
public class AuditEvent extends BaseEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Embedded
    private Service service;

    @Embedded
    private Actor actor;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private boolean result;

    // Potential invalid state (result = false, exception != null)
    private String exception;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class Service {

        @Column(name = "service_name", nullable = false)
        private String name;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class Actor {

        @Column(name = "actor_user_id", nullable = false)
        private Long userId;

        @Column(name = "actor_username", nullable = false)
        private String username;
    }
}
