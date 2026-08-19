package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.AuditEvent;
import com.viettelsoftware.firstspringboot.service.dto.AuditEventsQueryFilterDto;
import com.viettelsoftware.firstspringboot.service.exception.CurrentAuthenticatedUserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

public interface AuditService {

    void auditSuccess(String serviceName, String action) throws CurrentAuthenticatedUserNotFoundException;
    void auditFailure(String serviceName, String action, @Nullable Exception exception) throws CurrentAuthenticatedUserNotFoundException;

    Page<AuditEvent> getAuditEvents(AuditEventsQueryFilterDto filter, Pageable pageable);
}
