package com.viettelsoftware.firstspringboot.controller;

import com.viettelsoftware.firstspringboot.entity.AuditEvent;
import com.viettelsoftware.firstspringboot.service.AuditService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @GetMapping
    @PreAuthorize("hasAuthority('REALM_ROLE_AUDIT_READ') and hasAuthority('REALM_ROLE_GET')")
    public @NonNull Page<@NonNull AuditEvent> getAuditEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day,
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) Long actorUserId,
            @RequestParam(required = false) String actorUsername,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Boolean result,
            Pageable pageable) {

        if (day != null) {
            return auditService.getAuditEventsByDay(day, pageable);
        }
        if (serviceName != null) {
            return auditService.getAuditEventsByServiceName(serviceName, pageable);
        }
        if (actorUserId != null) {
            return auditService.getAuditEventsByActorUserId(actorUserId, pageable);
        }
        if (actorUsername != null) {
            return auditService.getAuditEventsByActorUsername(actorUsername, pageable);
        }
        if (action != null) {
            return auditService.getAuditEventsByAction(action, pageable);
        }
        if (result != null) {
            return auditService.getAuditEventsByResult(result, pageable);
        }
        return auditService.getAuditEventsByDay(LocalDate.now(), pageable);
    }
}
