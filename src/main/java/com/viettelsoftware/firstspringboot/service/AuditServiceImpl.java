package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.AuditEvent;
import com.viettelsoftware.firstspringboot.repository.AuditEventRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Override
    public void audit(@NonNull AuditEvent event) {
        auditEventRepository.save(event);
    }

    @Override
    public @NonNull Page<@NonNull AuditEvent> getAuditEventsByDay(@NonNull LocalDate day, @NonNull Pageable pageable) {
        Instant startOfDay = day.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endOfDay = day.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return auditEventRepository.findByTimestampBetween(startOfDay, endOfDay, pageable);
    }

    @Override
    public @NonNull Page<@NonNull AuditEvent> getAuditEventsByServiceName(@NonNull String serviceName, @NonNull Pageable pageable) {
        return auditEventRepository.findByServiceName(serviceName, pageable);
    }

    @Override
    public @NonNull Page<@NonNull AuditEvent> getAuditEventsByActorUserId(@NonNull Long actorUserId, @NonNull Pageable pageable) {
        return auditEventRepository.findByActorUserId(actorUserId, pageable);
    }

    @Override
    public @NonNull Page<@NonNull AuditEvent> getAuditEventsByActorUsername(@NonNull String actorUsername, @NonNull Pageable pageable) {
        return auditEventRepository.findByActorUsername(actorUsername, pageable);
    }

    @Override
    public @NonNull Page<@NonNull AuditEvent> getAuditEventsByAction(@NonNull String action, @NonNull Pageable pageable) {
        return auditEventRepository.findByAction(action, pageable);
    }

    @Override
    public @NonNull Page<@NonNull AuditEvent> getAuditEventsByResult(@NonNull Boolean result, @NonNull Pageable pageable) {
        return auditEventRepository.findByResult(result, pageable);
    }
}
