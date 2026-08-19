package com.viettelsoftware.firstspringboot.controller;

import com.viettelsoftware.firstspringboot.entity.AuditEvent;
import com.viettelsoftware.firstspringboot.service.AuditService;
import com.viettelsoftware.firstspringboot.service.dto.AuditEventsQueryFilterDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditService auditService;

    @PreAuthorize("hasAuthority('REALM_ROLE_AUDIT_READ') and hasAuthority('REALM_ROLE_GET')")
    @GetMapping
    public Page<AuditEvent> getAuditEvents(@ModelAttribute AuditEventsQueryFilterDto filter, Pageable pageable) {
        return auditService.getAuditEvents(filter, pageable);
    }
}
