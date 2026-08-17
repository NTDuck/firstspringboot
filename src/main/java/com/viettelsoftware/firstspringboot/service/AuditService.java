package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.dto.AuditQuery;
import com.viettelsoftware.firstspringboot.entity.AuditEvent;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditService {
    void audit(@NonNull AuditEvent event);

    @NonNull Page<@NonNull AuditEvent> search(@NonNull AuditQuery query, @NonNull Pageable pageable);
}
