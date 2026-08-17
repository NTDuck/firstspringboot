package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.AuditEvent;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

public interface AuditService {

    void audit(@NonNull AuditEvent event);

    @NonNull Page<@NonNull AuditEvent> search(
            @Nullable LocalDate day,
            @Nullable String serviceName,
            @Nullable Long actorUserId,
            @Nullable String actorUsername,
            @Nullable String action,
            @Nullable Boolean result,
            @NonNull Pageable pageable);

    @NonNull Page<@NonNull AuditEvent> getAuditEventsByDay(@NonNull LocalDate day, @NonNull Pageable pageable);

    @NonNull Page<@NonNull AuditEvent> getAuditEventsByServiceName(@NonNull String serviceName, @NonNull Pageable pageable);

    @NonNull Page<@NonNull AuditEvent> getAuditEventsByActorUserId(@NonNull Long actorUserId, @NonNull Pageable pageable);

    @NonNull Page<@NonNull AuditEvent> getAuditEventsByActorUsername(@NonNull String actorUsername, @NonNull Pageable pageable);

    @NonNull Page<@NonNull AuditEvent> getAuditEventsByAction(@NonNull String action, @NonNull Pageable pageable);

    @NonNull Page<@NonNull AuditEvent> getAuditEventsByResult(@NonNull Boolean result, @NonNull Pageable pageable);
}
